package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CoreCaseDataService;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@Component
@Slf4j
@RequiredArgsConstructor
public class HearingsUpdateHearingRequestConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final CoreCaseDataService coreCaseDataService;

    public static final String HEARING_NEED_MANUAL_UPDATE =
        "The hearing could not be automatically updated. You must manually update the hearing in the "
            + "[Hearings tab](/cases/case-details/%s/hearings)\n\n"
            + "If required, parties will be informed of the changes to the hearing.";

    @Override
    public boolean canHandle(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return Event.UPDATE_HEARING_REQUEST == callback.getEvent();
    }

    public PostSubmitCallbackResponse handle(
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        PostSubmitCallbackResponse postSubmitResponse =
                new PostSubmitCallbackResponse();

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (asylumCase.read(MANUAL_UPDATE_HEARING_REQUIRED).isPresent()) {
            postSubmitResponse.setConfirmationBody(
                String.format(HEARING_NEED_MANUAL_UPDATE, callback.getCaseDetails().getId())
            );
        } else {
            postSubmitResponse.setConfirmationHeader("# You've updated the hearing");
            postSubmitResponse.setConfirmationBody(
                """
                        #### What happens next
                        The hearing will be updated as directed.
                        
                        If required, parties will be informed of the changes to the hearing."""
            );
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
}
