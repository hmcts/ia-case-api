package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TRIBUNAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAppealOutOfCountry;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAppellantInDetention;

@Component
public class AddStatutoryTimeframe24WeeksPreStartHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final LocalDate stf24wLiveDate;
    private static final Set<State> supportedStates = Set.of(
        State.PENDING_PAYMENT,
        State.APPEAL_SUBMITTED,
        State.AWAITING_RESPONDENT_EVIDENCE,
        State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
        State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED,
        State.LISTING
    );

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

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        State currentState = callback.getCaseDetails().getState();
        if (!supportedStates.contains(currentState)) {
            String errorMessage = "This event cannot be run on this case";
            response.addError(errorMessage);
        }

        if (!caseReceivedAfterLive(asylumCase)) {
            String errorMessage = "This event cannot be run on a case created before " + stf24wLiveDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            response.addError(errorMessage);
        }
        if (isAppellantInDetention(asylumCase)) {
            String errorMessage = "This event cannot be run on a detained case";
            response.addError(errorMessage);
        }
        if (isAppealOutOfCountry(asylumCase)) {
            String errorMessage = "This event cannot be run on an out of country case";
            response.addError(errorMessage);
        }

        return response;
    }

    private boolean caseReceivedAfterLive(AsylumCase asylumCase) {
        Optional<String> tribunalReceivedDate = asylumCase.read(TRIBUNAL_RECEIVED_DATE);
        Optional<String> appealSubmissionDate = asylumCase.read(APPEAL_SUBMISSION_DATE);

        return (tribunalReceivedDate.isPresent() && !LocalDate.parse(tribunalReceivedDate.get()).isBefore(stf24wLiveDate))
            || (tribunalReceivedDate.isEmpty() && appealSubmissionDate.isPresent() && !LocalDate.parse(appealSubmissionDate.get()).isBefore(stf24wLiveDate));
    }
}
