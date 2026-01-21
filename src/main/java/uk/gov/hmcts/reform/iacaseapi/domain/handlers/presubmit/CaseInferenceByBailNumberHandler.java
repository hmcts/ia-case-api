package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREVIOUS_BAIL_APPLICATION_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREVIOUS_APPLICATION_DONE_VIA_ARIA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.PREVIOUS_APPLICATION_DONE_VIA_CCD;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.HAS_PREVIOUS_BAIL_APPLICATION;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class CaseInferenceByBailNumberHandler implements PreSubmitCallbackHandler<BailCase> {

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && (callback.getEvent() == Event.START_APPLICATION
               || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
               || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT)
               && callback.getPageId().equals(HAS_PREVIOUS_BAIL_APPLICATION.value());
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);

        String bailReferenceNumber = bailCase.read(PREVIOUS_BAIL_APPLICATION_NUMBER, String.class).orElse("");
        String hasPreviousBailApplication = bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class).orElse("");

        if (hasPreviousBailApplication.equals("Yes")) {
            if (bailReferenceNumber.matches("[0-9]{16}")) {
                bailCase.write(PREVIOUS_APPLICATION_DONE_VIA_CCD, YesOrNo.YES);
                bailCase.write(PREVIOUS_APPLICATION_DONE_VIA_ARIA, YesOrNo.NO);
            } else if (bailReferenceNumber.matches("[a-zA-Z]{2}\\/[0-9]{5}")) {
                bailCase.write(PREVIOUS_APPLICATION_DONE_VIA_CCD, YesOrNo.NO);
                bailCase.write(PREVIOUS_APPLICATION_DONE_VIA_ARIA, YesOrNo.YES);
            } else {
                response.addError("Invalid bail number provided. The bail number must be either 16 digits long "
                                  + "(e.g. 1111222233334444) or 8 characters long (e.g. HW/12345)");
            }
        } else {
            bailCase.write(PREVIOUS_APPLICATION_DONE_VIA_CCD, YesOrNo.NO);
            bailCase.write(PREVIOUS_APPLICATION_DONE_VIA_ARIA, YesOrNo.NO);
        }

        return response;
    }

}
