package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CcdDataService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdCaseAssignment;

@Slf4j
@Component
public class ChangeRepresentationConfirmation implements PostSubmitCallbackHandler<BailCase> {

    private final CcdCaseAssignment ccdCaseAssignment;
    private final PostNotificationSender<BailCase> postNotificationSender;
    private final CcdDataService ccdDataService;

    public ChangeRepresentationConfirmation(
        CcdCaseAssignment ccdCaseAssignment,
        PostNotificationSender<BailCase> postNotificationSender,
        CcdDataService ccdDataService
    ) {
        this.ccdCaseAssignment = ccdCaseAssignment;
        this.postNotificationSender = postNotificationSender;
        this.ccdDataService = ccdDataService;
    }

    public boolean canHandle(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        return (callback.getEvent() == Event.NOC_REQUEST
                || callback.getEvent() == Event.REMOVE_BAIL_LEGAL_REPRESENTATIVE
                || callback.getEvent() == Event.STOP_LEGAL_REPRESENTING);
    }

    /**
     * the confirmation message and the error message are coming from ExUI and cannot be customised.
     */
    public PostSubmitCallbackResponse handle(
        Callback<BailCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        try {
            ccdCaseAssignment.applyNoc(callback);

            if (callback.getEvent() == Event.NOC_REQUEST) {
                sendNotification(callback);
                String caseReference = callback.getCaseDetails().getCaseData()
                    .read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse("");

                postSubmitResponse.setConfirmationHeader(
                    "# You're now representing a client on case " + caseReference
                );
            }

            if (callback.getEvent() == Event.REMOVE_BAIL_LEGAL_REPRESENTATIVE) {
                ccdDataService.clearLegalRepDetails(callback);
                postSubmitResponse.setConfirmationHeader(
                    "# You have removed the legal representative from this case"
                );

                postSubmitResponse.setConfirmationBody(
                    "### What happens next\n\n"
                    + "This legal representative will no longer have access to this case."
                );
            }

            if (callback.getEvent() == Event.STOP_LEGAL_REPRESENTING) {
                postNotificationSender.send(callback);
                ccdDataService.clearLegalRepDetails(callback);
                postSubmitResponse.setConfirmationHeader(
                    "# You have stopped representing this client"
                );

                postSubmitResponse.setConfirmationBody(
                    "### What happens next\n\n"
                        + "We've sent you an email confirming you're no longer representing this client. You have been "
                        + "removed from this case and no longer have access to it.\n\n\n\n"
                        + "[View case list](/cases)"
                );
            }
        } catch (Exception e) {
            log.error("Unable to change representation (apply noc) for case id {} with error message: {}",
                      callback.getCaseDetails().getId(), e.getMessage());

            postSubmitResponse.setConfirmationBody(
                "### Something went wrong\n\n"
            );
        }

        return postSubmitResponse;
    }

    private void sendNotification(Callback<BailCase> callback) {
        //NOC_REQUEST is an event in notification-api, which is used by asylum.
        // In order to separate bail NOC, we are sending the notification request
        // with a new Event name NOC_REQUEST_BAIL.
        Callback<BailCase> callbackForNotification = new Callback<>(
            callback.getCaseDetails(),
            callback.getCaseDetailsBefore(),
            Event.NOC_REQUEST_BAIL
        );

        postNotificationSender.send(callbackForNotification);
    }
}
