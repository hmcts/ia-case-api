package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
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

        HearingAdjournmentDay hearingAdjournmentDay = asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class)
            .orElseThrow(() -> new IllegalStateException("Hearing adjournment day is not present"));

        boolean relistCaseImmediately = asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)
            .map(relist -> YES == relist)
            .orElseThrow(() -> new IllegalStateException("Response to relist case immediately is not present"));

        if (!relistCaseImmediately) {
            String currentHearingDate = asylumCase.read(LIST_CASE_HEARING_DATE, String.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingDate is missing."));
            asylumCase.write(STATE_BEFORE_ADJOURN_WITHOUT_DATE, currentState.toString());
            asylumCase.write(DATE_BEFORE_ADJOURN_WITHOUT_DATE, currentHearingDate);
        }
        if (hearingAdjournmentDay == BEFORE_HEARING_DATE) {
            callbackResponse.setData(iaHearingsApiService.aboutToSubmit(callback));
        }

        return new PreSubmitCallbackResponse<>(asylumCase, relistCaseImmediately ? currentState : State.ADJOURNED);
    }
}
