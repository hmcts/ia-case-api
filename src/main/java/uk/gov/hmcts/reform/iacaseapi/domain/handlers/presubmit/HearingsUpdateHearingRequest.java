package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@Component
@Slf4j
public class HearingsUpdateHearingRequest implements PreSubmitCallbackHandler<AsylumCase> {

    public static final String NO_HEARINGS_ERROR_MESSAGE =
        "You've made an invalid request. You must request a substantive hearing before you can update a hearing.";
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

        return (callbackStage == ABOUT_TO_START
                || callbackStage == MID_EVENT)
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

            if (hasNoHearings(asylumCase)) {
                PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                response.addError(NO_HEARINGS_ERROR_MESSAGE);
                return response;
            }
        } else {
            asylumCase = getHearingDetails(callback);
        }

        asylumCase.clear(MANUAL_UPDATE_HEARING_REQUIRED);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean hasNoHearings(AsylumCase asylumCase) {
        Optional<DynamicList> hearings = asylumCase.read(CHANGE_HEARINGS, DynamicList.class);

        if (hearings.isEmpty()) {
            return true;
        } else {
            return hearings.get().getListItems().isEmpty();
        }
    }

    private AsylumCase getHearings(Callback<AsylumCase> callback) {
        return iaHearingsApiService.aboutToStart(callback);
    }

    private AsylumCase getHearingDetails(Callback<AsylumCase> callback) {
        return iaHearingsApiService.midEvent(callback);
    }
}
