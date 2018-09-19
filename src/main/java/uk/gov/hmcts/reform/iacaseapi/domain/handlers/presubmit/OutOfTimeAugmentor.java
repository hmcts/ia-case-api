package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealDeadlineCalculator;

@Component
public class OutOfTimeAugmentor implements CcdEventPreSubmitHandler<AsylumCase> {

    private final AppealDeadlineCalculator appealDeadlineCalculator;

    public OutOfTimeAugmentor(
        @Autowired AppealDeadlineCalculator appealDeadlineCalculator
    ) {
        this.appealDeadlineCalculator = appealDeadlineCalculator;
    }

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && (ccdEvent.getEventId() == EventId.START_APPEAL
                   || ccdEvent.getEventId() == EventId.CHANGE_APPEAL
                   || ccdEvent.getEventId() == EventId.SUBMIT_APPEAL);
    }

    public CcdEventPreSubmitResponse<AsylumCase> handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

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
