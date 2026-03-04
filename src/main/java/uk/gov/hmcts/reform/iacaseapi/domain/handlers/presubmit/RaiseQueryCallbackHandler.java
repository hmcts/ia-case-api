package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.QM_LATEST_QUERY;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement.LatestQuery;

@Slf4j
@Component
public class RaiseQueryCallbackHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final UserDetails userDetails;

    public RaiseQueryCallbackHandler(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
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

        AsylumCaseFieldDefinition targetCollection = getQueryCollectionField(asylumCase);
        if (targetCollection == null) {
            throw new IllegalStateException("Unable to determine query collection for this asylum case");
        }

        CaseQueriesCollection queriesList = asylumCase
                .read(targetCollection, CaseQueriesCollection.class)
                .orElse(CaseQueriesCollection.builder().caseMessages(List.of()).build());

        if (queriesList.getCaseMessages() == null || queriesList.getCaseMessages().isEmpty()) {
            log.info("No case messages found, QM_LATEST_QUERY not set.");
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        IdValue<CaseMessage> latestCaseMessage =
                queriesList.getCaseMessages().stream()
                        .filter(m -> m.getValue() != null)
                        .max(Comparator.comparing(
                                m -> Optional.ofNullable(m.getValue().getCreatedOn())
                                        .orElse(OffsetDateTime.MIN)
                        ))
                        .orElse(null);

        if (latestCaseMessage == null) {
            log.info("No valid case messages found.");
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        String latestQueryId = latestCaseMessage.getId();

        YesOrNo isHearingRelated = Optional.ofNullable(
                latestCaseMessage.getValue().getIsHearingRelated()
        ).orElse(YesOrNo.NO);

        LatestQuery latestQuery = LatestQuery.builder()
                .queryId(latestQueryId)
                .isHearingRelated(isHearingRelated)
                .build();

        IdValue<LatestQuery> wrappedLatestQuery =
                new IdValue<>(latestQueryId, latestQuery);

        List<IdValue<LatestQuery>> existingLatestQueries = new ArrayList<>();
        asylumCase.read(QM_LATEST_QUERY).ifPresent(rawList -> {
            if (rawList instanceof List<?> list) {
                for (Object obj : list) {
                    if (obj instanceof IdValue<?> idValue && idValue.getValue() instanceof LatestQuery existingLatestQuery) {
                        existingLatestQueries.add(new IdValue<>(idValue.getId(), existingLatestQuery));
                    }
                }
            }
        });

        existingLatestQueries.removeIf(q -> q.getId().equals(latestQueryId));

        existingLatestQueries.add(wrappedLatestQuery);
        log.info("Hearing flag raw value: {} body: {}",
                latestCaseMessage.getValue().getIsHearingRelated(), latestCaseMessage.getValue().getBody());

        asylumCase.write(QM_LATEST_QUERY, existingLatestQueries);

        log.info("QM_LATEST_QUERY updated with queryId={} hearingRelated={}",
                latestQueryId, isHearingRelated);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private AsylumCaseFieldDefinition getQueryCollectionField(AsylumCase asylumCase) {
        if (HandlerUtils.isLegalRepJourney(asylumCase)) {
            return AsylumCaseFieldDefinition.QM_LEGAL_REPRESENTATIVE_QUERIES;
        } else if (HandlerUtils.isAipJourney(asylumCase)) {
            return AsylumCaseFieldDefinition.QM_AIP_QUERIES;
        } else if (HandlerUtils.isInternalCase(asylumCase)) {
            return AsylumCaseFieldDefinition.QM_ADMIN_QUERIES;
        }
        return null;
    }
}