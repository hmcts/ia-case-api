package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION;

import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@Component
@Slf4j
public class HearingsUpdateHearingRequest implements PreSubmitCallbackHandler<AsylumCase> {

    private IaHearingsApiService iaHearingsApiService;

    public HearingsUpdateHearingRequest(
            IaHearingsApiService iaHearingsApiService
    ) {
        this.iaHearingsApiService = iaHearingsApiService;
    }

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                || callbackStage == PreSubmitCallbackStage.MID_EVENT)
                && Objects.equals(Event.UPDATE_HEARING_REQUEST, callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase;

        if (callback.getCaseDetails().getCaseData().read(CHANGE_HEARINGS).isEmpty()) {
            asylumCase = getHearings(callback);
        } else {
            asylumCase = getHearingDetails(callback);
            setHearingLocationDetails(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private AsylumCase getHearings(Callback<AsylumCase> callback) {
        return iaHearingsApiService.aboutToStart(callback);
    }

    private AsylumCase getHearingDetails(Callback<AsylumCase> callback) {
        return iaHearingsApiService.midEvent(callback);
    }

    private static void setHearingLocationDetails(AsylumCase asylumCase) {
        Optional<String> hearingLocation = asylumCase.read(CHANGE_HEARING_LOCATION);
        if (hearingLocation.isPresent()) {
            String hearingCenterValue = HearingCentre.getValueByEpimsId(hearingLocation.get());
            asylumCase.write(CHANGE_HEARING_LOCATION, hearingCenterValue);
        }
    }
}
