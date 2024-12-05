package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DeletionDateProvider;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DELETION_DATE;

@Slf4j
@Service
public class AppealSetDraftDeletionDateHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final DeletionDateProvider deletionDateProvider;

    public AppealSetDraftDeletionDateHandler(DeletionDateProvider deletionDateProvider) {
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

        LocalDate deletionDate = deletionDateProvider.getDeletionDate();

        asylumCase.write(DELETION_DATE, deletionDate.toString());

        log.info(
            "Setting deletionDate when starting appeal, caseId {}, deletionDate {}",
            callback.getCaseDetails().getId(),
            deletionDate
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
