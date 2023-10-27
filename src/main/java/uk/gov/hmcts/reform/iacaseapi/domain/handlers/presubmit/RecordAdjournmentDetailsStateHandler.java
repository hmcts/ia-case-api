package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;

@Slf4j
@Component
public class RecordAdjournmentDetailsStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private IaHearingsApiService iaHearingsApiService;

    public RecordAdjournmentDetailsStateHandler(IaHearingsApiService iaHearingsApiService) {
        this.iaHearingsApiService = iaHearingsApiService;
    }

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

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final State currentState = callback.getCaseDetails().getState();

        boolean  adjournedOnHearingDay = asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class)
                .map(adjournmentDay -> ON_HEARING_DATE == adjournmentDay)
                .orElseThrow(() -> new IllegalStateException("Hearing adjournment day is not present"));
        boolean relistCaseImmediately = asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)
                .map(relist -> YES == relist)
                .orElseThrow(() -> new IllegalStateException("Response to relist case immediately is not present"));
        YesOrNo updateRequestSuccess = YES;

        if (!adjournedOnHearingDay && relistCaseImmediately) {
            try {
                asylumCase = iaHearingsApiService.aboutToSubmit(callback);
            } catch (AsylumCaseServiceResponseException ex) {
                log.error("Error updating HMC hearing details. " + ex);
                updateRequestSuccess = NO;
            }
        }

        asylumCase.write(UPDATE_HMC_REQUEST_SUCCESS, updateRequestSuccess);

        if (adjournedOnHearingDay || !relistCaseImmediately) {
            return new PreSubmitCallbackResponse<>(asylumCase, State.ADJOURNED);
        } else {
            return new PreSubmitCallbackResponse<>(asylumCase, currentState);
        }
    }
}
