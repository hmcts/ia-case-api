package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.EventId;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPostSubmitHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealDeadlineCalculator;

@Component
public class DraftAppealSavedConfirmation implements CcdEventPostSubmitHandler<AsylumCase> {

    private final AppealDeadlineCalculator appealDeadlineCalculator;

    public DraftAppealSavedConfirmation(
        @Autowired AppealDeadlineCalculator appealDeadlineCalculator
    ) {
        this.appealDeadlineCalculator = appealDeadlineCalculator;
    }

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.SUBMITTED
               && (ccdEvent.getEventId() == EventId.COMPLETE_DRAFT_APPEAL
                   || ccdEvent.getEventId() == EventId.UPDATE_DRAFT_APPEAL);
    }

    public CcdEventPostSubmitResponse handle(
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

        CcdEventPostSubmitResponse postSubmitResponse =
            new CcdEventPostSubmitResponse();

        String editAppealUrl =
            "/case/SSCS/Asylum/" + ccdEvent.getCaseDetails().getId() + "/trigger/updateDraftAppeal";

        String submitAppealUrl =
            "/case/SSCS/Asylum/" + ccdEvent.getCaseDetails().getId() + "/trigger/submitAppeal";

        Optional<LocalDate> appealDeadline =
            appealDeadlineCalculator.calculate(asylumCase);

        String submitMessage;

        if (asylumCase.getApplicationOutOfTime().orElse("").equalsIgnoreCase("Yes")) {

            submitMessage = "You are out of time and need to [submit your appeal](" + submitAppealUrl + ") now.";
        } else {
            submitMessage = "If you are ready, you can [submit your appeal](" + submitAppealUrl + ") now.";
        }

        postSubmitResponse.setConfirmationHeader("# Draft appeal saved");
        postSubmitResponse.setConfirmationBody(
              "Submitting your appeal\n"
            + "----------------------\n"
            + submitMessage + "\n"
            + "\n"
            + "Editing your appeal\n"
            + "-------------------\n"
            + "Before you submit your appeal you can still [make changes](" + editAppealUrl + ") within the time allowed."
        );

        return postSubmitResponse;
    }
}
