package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Component
@Slf4j
public class HearingsUpdateHearingRequestSubmit implements PreSubmitCallbackHandler<AsylumCase> {

    private final String hearingsApiEndpoint;
    private final String aboutToSubmitPath;

    AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;

    public HearingsUpdateHearingRequestSubmit(
            AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator,
            @Value("${hearingsApi.endpoint}") String hearingsApiEndpoint,
            @Value("${hearingsApi.aboutToSubmitPath}") String aboutToSubmitPath
    ) {
        this.asylumCaseCallbackApiDelegator = asylumCaseCallbackApiDelegator;
        this.hearingsApiEndpoint = hearingsApiEndpoint;
        this.aboutToSubmitPath = aboutToSubmitPath;
    }

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
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
        AsylumCase asylumCase = updateHearing(callback);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private AsylumCase updateHearing(Callback<AsylumCase> callback) {
        return asylumCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + aboutToSubmitPath);
    }

}
