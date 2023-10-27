package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AdjournmentDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@Component
public class RecordAdjournmentDetailsPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final IaHearingsApiService iaHearingsApiService;

    public RecordAdjournmentDetailsPreparer(IaHearingsApiService iaHearingsApiService) {
        this.iaHearingsApiService = iaHearingsApiService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == ABOUT_TO_START
               && callback.getEvent() == RECORD_ADJOURNMENT_DETAILS;
    }


    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        clearAdjournmentDetails(callback);

        AsylumCase asylumCase = iaHearingsApiService.aboutToStart(callback);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void clearAdjournmentDetails(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Arrays.asList(
            ADJOURNMENT_DETAILS_HEARING,
            HEARING_ADJOURNMENT_WHEN,
            RELIST_CASE_IMMEDIATELY,
            NEXT_HEARING_LOCATION,
            NEXT_HEARING_DURATION,
            HEARING_ADJOURNMENT_DECISION_PARTY,
            HEARING_ADJOURNMENT_DECISION_PARTY_NAME,
            HEARING_ADJOURNMENT_REQUESTING_PARTY,
            ANY_ADDITIONAL_ADJOURNMENT_INFO,
            ADDITIONAL_ADJOURNMENT_INFO,
            NEXT_HEARING_DATE,
            NEXT_HEARING_DATE_FIXED,
            NEXT_HEARING_DATE_RANGE_EARLIEST,
            NEXT_HEARING_DATE_RANGE_LATEST,
            SHOULD_RESERVE_OR_EXCLUDE_JUDGE,
            RESERVE_OR_EXCLUDE_JUDGE,
            NEXT_HEARING_FORMAT).forEach(asylumCase::clear);
    }

}