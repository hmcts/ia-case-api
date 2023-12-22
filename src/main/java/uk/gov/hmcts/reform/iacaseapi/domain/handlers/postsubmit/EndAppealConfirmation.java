package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CoreCaseDataService;

@Component
@Slf4j
@RequiredArgsConstructor
public class EndAppealConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final CoreCaseDataService coreCaseDataService;

    public static final String HEARING_CANCEL_SUCCEED = "#### What happens next\n\n"
        + "A notification has been sent to all parties.<br><br>"
        + "Any hearings requested or listed in List Assist have been automatically cancelled.";

    public static final String HEARING_CANCEL_FAILED = "#### What happens next\n\n"
        + "A notification has been sent to all parties.<br><br>"
        + "The hearing could not be automatically cancelled.<br><br>"
        + "[Cancel the hearing on the Hearings tab](/cases/case-details/%s/hearings)";

    public static final String NOTIFICATION_FAILED = "![Respondent notification failed confirmation]"
        + "(https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/respondent_notification_failed.svg)\n"
        + "#### Do this next\n\n"
        + "Contact the respondent to tell them what has changed, including any action they need to take.\n";


    @Override
    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.END_APPEAL;
    }

    @Override
    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final String hoEndAppealInstructStatus =
            asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_END_APPEAL_INSTRUCT_STATUS, String.class).orElse("");

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        if (hoEndAppealInstructStatus.equalsIgnoreCase("FAIL")) {
            postSubmitResponse.setConfirmationBody(NOTIFICATION_FAILED);
        } else {
            populateConfirmationPage(callback, postSubmitResponse, asylumCase);
        }

        boolean shouldTriggerTask = asylumCase.read(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YesOrNo.class)
            .map(relist -> YES == relist)
            .orElse(false);

        if (shouldTriggerTask) {
            createReviewInterpreterBookingTask(callback);
        }

        return postSubmitResponse;
    }

    private void createReviewInterpreterBookingTask(Callback<AsylumCase> callback) {
        String caseId = String.valueOf(callback.getCaseDetails().getId());
        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, caseId);

        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        log.info("Sending `{}` event for  Case ID `{}`", TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, caseId);
        coreCaseDataService.triggerSubmitEvent(
            TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, caseId, startEventResponse, asylumCase);
    }

    private static void populateConfirmationPage(Callback<AsylumCase> callback, PostSubmitCallbackResponse postSubmitResponse,
                                  AsylumCase asylumCase) {
        postSubmitResponse.setConfirmationHeader("# You have ended the appeal");

        if (asylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED).isPresent()) {
            postSubmitResponse.setConfirmationBody(
                String.format(HEARING_CANCEL_FAILED, callback.getCaseDetails().getId())
            );
        } else {
            postSubmitResponse.setConfirmationBody(HEARING_CANCEL_SUCCEED);
        }
    }
}
