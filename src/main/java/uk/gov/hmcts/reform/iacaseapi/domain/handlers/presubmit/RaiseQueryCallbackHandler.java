package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.QM_LATEST_QUERY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel.ADMIN_OFFICER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel.LEGAL_REPRESENTATIVE;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.LatestQuery;

@Slf4j
@Component
public class RaiseQueryCallbackHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    public RaiseQueryCallbackHandler(UserDetails userDetails, UserDetailsHelper userDetailsHelper) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage,
                             Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.QUERY_MANAGEMENT_RAISE_QUERY;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        AsylumCaseFieldDefinition targetCollection =
                determineQueryCollection();

        if (targetCollection == null) {
            throw new IllegalStateException(
                    "Unable to determine query collection for this asylum case"
            );
        }

        CaseQueriesCollection queries =
                asylumCase.read(targetCollection, CaseQueriesCollection.class)
                        .orElse(null);

        if (queries == null
                || queries.getCaseMessages() == null
                || queries.getCaseMessages().isEmpty()) {

            log.debug("No case messages found — nothing to update.");
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        Optional<IdValue<CaseMessage>> latestMessageOpt =
                queries.getCaseMessages().stream()
                        .filter(m -> m.getValue() != null)
                        .max(Comparator.comparing(m ->
                                Optional.ofNullable(m.getValue().getCreatedOn())
                                        .orElse(OffsetDateTime.MIN)
                        ));

        if (latestMessageOpt.isEmpty()) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        IdValue<CaseMessage> latestMessage = latestMessageOpt.get();

        String wrapperId = latestMessage.getId(); // CCD collection ID
        CaseMessage caseMessage = latestMessage.getValue();

        String queryId = caseMessage.getId(); // Query Management ID

        YesOrNo isHearingRelated =
                Optional.ofNullable(caseMessage.getIsHearingRelated())
                        .orElse(YesOrNo.NO);

        log.info("Latest query detected. WrapperId={}, QueryId={}, HearingRelated={}",
                wrapperId, queryId, isHearingRelated);

        LatestQuery latestQuery = LatestQuery.builder()
                .queryId(queryId)
                .isHearingRelated(isHearingRelated)
                .build();

        IdValue<LatestQuery> wrappedLatestQuery =
                new IdValue<>(wrapperId, latestQuery);

        List<IdValue<LatestQuery>> existingLatestQueries =
                asylumCase.read(QM_LATEST_QUERY)
                        .map(this::castLatestQueryList)
                        .orElse(new ArrayList<>());

        existingLatestQueries.removeIf(q -> q.getId().equals(wrapperId));

        existingLatestQueries.add(wrappedLatestQuery);

        asylumCase.write(QM_LATEST_QUERY, existingLatestQueries);

        log.info("User details role is " + userDetails.getRoles() + "User details helper " + userDetailsHelper.getLoggedInUserRoleLabel(userDetails));

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    @SuppressWarnings("unchecked")
    private List<IdValue<LatestQuery>> castLatestQueryList(Object raw) {
        if (raw instanceof List<?> list) {
            List<IdValue<LatestQuery>> typedList = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof IdValue<?> idValue
                        && idValue.getValue() instanceof LatestQuery latestQuery) {

                    typedList.add(
                            new IdValue<>(idValue.getId(), latestQuery)
                    );
                }
            }
            return typedList;
        }
        return new ArrayList<>();
    }

    private AsylumCaseFieldDefinition determineQueryCollection() {

        UserRoleLabel currentUser = userDetailsHelper.getLoggedInUserRoleLabel(userDetails);
        if (currentUser.equals(LEGAL_REPRESENTATIVE)) {
            return AsylumCaseFieldDefinition.QM_LEGAL_REPRESENTATIVE_QUERIES;
        } else if (currentUser.equals(ADMIN_OFFICER)) {
            return AsylumCaseFieldDefinition.QM_ADMIN_QUERIES;
        }

        return null;
    }
}