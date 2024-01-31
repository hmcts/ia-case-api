package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_BEFORE_ADJOURN_WITHOUT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STATE_BEFORE_ADJOURN_WITHOUT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HMC_REQUEST_SUCCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.adjournedBeforeHearingDay;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.adjournedOnHearingDay;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.relistCaseImmediately;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AutoRequestHearingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordAdjournmentDetailsStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final AutoRequestHearingService autoRequestHearingService;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == RECORD_ADJOURNMENT_DETAILS;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback,
        PreSubmitCallbackResponse<AsylumCase> callbackResponse
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        boolean relistImmediately = relistCaseImmediately(callback.getCaseDetails().getCaseData(), true);

        State state = trySetCaseToAdjourned(callback, relistImmediately);

        return new PreSubmitCallbackResponse<>(makeAutoHearingRequest(callback, relistImmediately), state);
    }

    private State trySetCaseToAdjourned(Callback<AsylumCase> callback, boolean relistImmediately) {
        State state = callback.getCaseDetails().getState();
        if (relistImmediately) {

            return state;
        } else {
            AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
            String currentHearingDate = asylumCase.read(LIST_CASE_HEARING_DATE, String.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingDate is missing"));
            asylumCase.write(STATE_BEFORE_ADJOURN_WITHOUT_DATE, callback.getCaseDetails().getState().toString());
            asylumCase.write(DATE_BEFORE_ADJOURN_WITHOUT_DATE, currentHearingDate);

            return State.ADJOURNED;
        }

    }

    private AsylumCase makeAutoHearingRequest(Callback<AsylumCase> callback, boolean relistCaseImmediately) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean canAutoCreate = relistCaseImmediately && adjournedOnHearingDay(asylumCase);

        if (autoRequestHearingService.shouldAutoRequestHearing(asylumCase, canAutoCreate)) {
            // Auto create hearing
            asylumCase = autoRequestHearingService
                .makeAutoHearingRequest(callback, MANUAL_CREATE_HEARING_REQUIRED);
        } else if (relistCaseImmediately && adjournedBeforeHearingDay(asylumCase)) {
            // Auto update hearing
            asylumCase = autoRequestHearingService
                .makeAutoHearingRequest(callback, UPDATE_HMC_REQUEST_SUCCESS);
        } else if (!relistCaseImmediately && adjournedBeforeHearingDay(asylumCase)) {
            // Auto cancel hearing
            asylumCase = autoRequestHearingService
                .makeAutoHearingRequest(callback, MANUAL_CANCEL_HEARINGS_REQUIRED);
        }

        return asylumCase;
    }
}
