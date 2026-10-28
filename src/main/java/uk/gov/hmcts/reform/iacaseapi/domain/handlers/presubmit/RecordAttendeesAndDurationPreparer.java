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

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
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
}
