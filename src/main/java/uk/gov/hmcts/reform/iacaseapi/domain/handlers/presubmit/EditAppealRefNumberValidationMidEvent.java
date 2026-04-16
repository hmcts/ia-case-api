package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberValidator;

@Component
public class EditAppealRefNumberValidationMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String ARIA_APPEAL_REFERENCE_PAGE_ID = "appealReferenceNumber";

    private final AppealReferenceNumberValidator appealReferenceNumberValidator;

    public EditAppealRefNumberValidationMidEvent(AppealReferenceNumberValidator appealReferenceNumberValidator) {
        this.appealReferenceNumberValidator = appealReferenceNumberValidator;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && callback.getEvent() == Event.EDIT_APPEAL
            && callback.getPageId().equals(ARIA_APPEAL_REFERENCE_PAGE_ID);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        String appealReferenceNumber = asylumCase
                .read(APPEAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("appealReferenceNumber is missing"));

        String ccdRefNumber = asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)
            .orElseThrow(() -> new IllegalStateException("ccdReferenceNumber is missing"));

        List<String> validationErrors = appealReferenceNumberValidator.validate(appealReferenceNumber, ccdRefNumber);
        validationErrors.forEach(response::addError);

        return response;
    }

}
