package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

import static java.util.Objects.requireNonNull;

@Service
@RequiredArgsConstructor
public class UpdateInterpreterBookingStatusHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final IaHearingsApiService iaHearingsApiService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPDATE_INTERPRETER_BOOKING_STATUS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse =
                new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
        try {
            AsylumCase asylumCase = iaHearingsApiService.aboutToSubmit(callback);
            asylumCasePreSubmitCallbackResponse.setData(asylumCase);
        } catch (Exception ex) {
            String errorMessage = String.format("Hearing cannot be auto updated for Case %s",
                    callback.getCaseDetails().getId()
            );
            asylumCasePreSubmitCallbackResponse.addError(errorMessage);
        }
        return asylumCasePreSubmitCallbackResponse;
    }
}
