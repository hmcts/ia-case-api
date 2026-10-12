package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.handle24wValidity;

@Component
public class AddStatutoryTimeframe24WeeksPreStartHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final LocalDate stf24wLiveDate;

    public AddStatutoryTimeframe24WeeksPreStartHandler(@Value("${app.statutory-timeframe.live-date}") String stf24wLiveDate) {
        this.stf24wLiveDate = LocalDate.parse(stf24wLiveDate);
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_START) && callback.getEvent().equals(ADD_STATUTORY_TIMEFRAME_24_WEEKS);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        return handle24wValidity(callback, stf24wLiveDate);
    }
}
