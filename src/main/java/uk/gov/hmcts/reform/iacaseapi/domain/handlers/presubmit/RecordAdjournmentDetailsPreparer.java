package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

@Component
@RequiredArgsConstructor
public class RecordAdjournmentDetailsPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    public static final String NO_HEARINGS_ERROR_MESSAGE =
        "You've made an invalid request. You must request a substantive hearing before you can adjourn a hearing.";
    private final IaHearingsApiService iaHearingsApiService;
    private final LocationBasedFeatureToggler locationBasedFeatureToggler;

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

        HandlerUtils.checkAndUpdateAutoHearingRequestEnabled(locationBasedFeatureToggler, asylumCase);

        if (hasNoHearings(asylumCase)) {
            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError(NO_HEARINGS_ERROR_MESSAGE);
            return response;
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean hasNoHearings(AsylumCase asylumCase) {
        Optional<DynamicList> hearings = asylumCase.read(ADJOURNMENT_DETAILS_HEARING, DynamicList.class);

        if (hearings.isEmpty()) {
            return true;
        } else {
            return hearings.get().getListItems().isEmpty();
        }
    }

    private void clearAdjournmentDetails(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Arrays.asList(
            ADJOURNMENT_DETAILS_HEARING,
            HEARING_ADJOURNMENT_WHEN,
            RELIST_CASE_IMMEDIATELY,
            NEXT_HEARING_VENUE,
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
