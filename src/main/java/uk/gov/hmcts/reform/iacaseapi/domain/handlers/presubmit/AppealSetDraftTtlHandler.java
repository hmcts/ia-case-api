package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TtlCcdObject;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DeletionDateProvider;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class AppealSetDraftTtlHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private static final String TTL_SUSPENDED_NO = "No";

    private final DeletionDateProvider deletionDateProvider;

    public AppealSetDraftTtlHandler(DeletionDateProvider deletionDateProvider) {
        this.deletionDateProvider = deletionDateProvider;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String ttlString = deletionDateProvider.getTtl().toString();

        TtlCcdObject ttlCcdObject = TtlCcdObject.builder()
                .suspended(TTL_SUSPENDED_NO)
                .overrideTTL(ttlString)
                .systemTTL(ttlString)
                .build();

        asylumCase.write(AsylumCaseFieldDefinition.TTL, ttlCcdObject);

        log.info(
            "Setting deletionDate when starting appeal, caseId {}, ttlDetails {}",
            callback.getCaseDetails().getId(),
                ttlCcdObject
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.START_APPEAL;
    }
}
