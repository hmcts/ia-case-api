package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TRIBUNAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

@Component
public class AddStatutoryTimeframe24WeeksPreStartHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final LocalDate STF24W_LIVE_DATE = LocalDate.of(2026, 5, 1);

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

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (!caseReceivedAfterLive(asylumCase)) {
            String errorMessage = "This event cannot be run on a case created before " + STF24W_LIVE_DATE.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            response.addError(errorMessage);
        }

        return response;
    }

    private boolean caseReceivedAfterLive(AsylumCase asylumCase) {
        Optional<String> tribunalReceivedDate = asylumCase.read(TRIBUNAL_RECEIVED_DATE);
        if (tribunalReceivedDate.isPresent() && LocalDate.parse(tribunalReceivedDate.get()).isBefore(STF24W_LIVE_DATE)) {
            return false;
        }

        Optional<String> appealSubmissionDate = asylumCase.read(APPEAL_SUBMISSION_DATE);
        if (appealSubmissionDate.isPresent() && LocalDate.parse(appealSubmissionDate.get()).isBefore(STF24W_LIVE_DATE)) {
            return false;
        }

        return true;
    }
}
