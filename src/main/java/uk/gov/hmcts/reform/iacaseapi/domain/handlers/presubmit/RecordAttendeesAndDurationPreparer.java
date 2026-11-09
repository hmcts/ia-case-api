package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

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

import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

@Component
@RequiredArgsConstructor
public class RecordAttendeesAndDurationPreparer implements PreSubmitCallbackHandler<AsylumCase> {

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
               && callback.getEvent() == RECORD_ATTENDEES_AND_DURATION;
    }


    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        clearAttendeesAndDurationDetails(callback);

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
        Optional<DynamicList> hearings = asylumCase.read(ATTENDEES_AND_DURATION_HEARING, DynamicList.class);

        return hearings.map(dynamicList -> dynamicList.getListItems().isEmpty()).orElse(true);
    }

    private void clearAttendeesAndDurationDetails(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Arrays.asList(
            ATTENDEES_AND_DURATION_HEARING,
            ATTENDEES_AND_DURATION_HEARING_ID,
            ATTENDEES_AND_DURATION_HEARING_DATE,
            ATTENDEES_AND_DURATION_PAPER_HEARING,
            ATTENDEES_AND_DURATION_HEARING_METHODS,
            ATTENDEES_AND_DURATION_HEARING_STAGE,
            ATTENDEES_AND_DURATION_HEARING_RESULT,
            ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON,
            ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON,
            ATTENDEES_AND_DURATION_HEARING_JUDGE,
            ATTENDEES_AND_DURATION_DATE_OF_HEARING,
            ATTENDEES_AND_DURATION_HEARING_START_TIME,
            ATTENDEES_AND_DURATION_HEARING_FINISH_TIME,
            ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED,
            ATTENDEES_AND_DURATION_PAUSE_START,
            ATTENDEES_AND_DURATION_PAUSE_END
        ).forEach(asylumCase::clear);
    }
}
