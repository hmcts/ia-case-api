package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
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

import java.time.OffsetDateTime;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.QM_LATEST_QUERY;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.determineQueryCollection;

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
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage);
        requireNonNull(callback);

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

        AsylumCaseFieldDefinition queryCollection = determineQueryCollection(userDetailsHelper, userDetails);

        List<IdValue<CaseMessage>> queries =
            asylumCase.read(queryCollection, CaseQueriesCollection.class)
                .map(CaseQueriesCollection::getCaseMessages)
                .orElse(Collections.emptyList());

        if (queries.isEmpty()) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        Optional<CaseMessage> latestMessage =
            queries.stream()
                .map(IdValue::getValue)
                .filter(Objects::nonNull)
                .max(Comparator.comparing(m ->
                    Optional.ofNullable(m.getCreatedOn()).orElse(OffsetDateTime.MIN)
                ));

        if (latestMessage.isEmpty()) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        CaseMessage message = latestMessage.get();

        LatestQuery latestQuery = LatestQuery.builder()
            .queryId(message.getId())
            .isHearingRelated(Optional.ofNullable(message.getIsHearingRelated()).orElse(YesOrNo.NO))
            .build();

        asylumCase.write(QM_LATEST_QUERY, latestQuery);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}