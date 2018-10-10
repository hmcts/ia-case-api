package uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.presubmit;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.Callback;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.events.domain.entities.CallbackStage;
import uk.gov.hmcts.reform.iacaseapi.events.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.events.domain.service.AppealDeadlineCalculator;

@Component
public class OutOfTimeAugmentor implements PreSubmitCallbackHandler<AsylumCase> {

    private final AppealDeadlineCalculator appealDeadlineCalculator;

    public OutOfTimeAugmentor(
        @Autowired AppealDeadlineCalculator appealDeadlineCalculator
    ) {
        this.appealDeadlineCalculator = appealDeadlineCalculator;
    }

    public boolean canHandle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return callbackStage == CallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEventId() == EventId.START_APPEAL
                   || callback.getEventId() == EventId.CHANGE_APPEAL
                   || callback.getEventId() == EventId.SUBMIT_APPEAL);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        CallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> preSubmitResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        Optional<LocalDate> appealDeadline =
            appealDeadlineCalculator.calculate(asylumCase);

        if (appealDeadline.isPresent()) {

            if (LocalDate.now().isAfter(appealDeadline.get())) {
                asylumCase.setApplicationOutOfTime("Yes");
            } else {
                asylumCase.setApplicationOutOfTime("No");
            }
        }

        return preSubmitResponse;
    }
}
