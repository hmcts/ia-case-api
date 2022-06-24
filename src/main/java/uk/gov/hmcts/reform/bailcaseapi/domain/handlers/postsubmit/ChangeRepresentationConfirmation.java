package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdCaseAssignment;

@Slf4j
@Component
public class ChangeRepresentationConfirmation implements PostSubmitCallbackHandler<BailCase> {

    private final CcdCaseAssignment ccdCaseAssignment;

    public ChangeRepresentationConfirmation(
        CcdCaseAssignment ccdCaseAssignment
    ) {
        this.ccdCaseAssignment = ccdCaseAssignment;
    }

    public boolean canHandle(
        Callback<BailCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");
        return (callback.getEvent() == Event.NOC_REQUEST
                || callback.getEvent() == Event.REMOVE_BAIL_LEGAL_REPRESENTATIVE);
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

                String caseReference = callback.getCaseDetails().getCaseData()
                    .read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse("");

                postSubmitResponse.setConfirmationHeader(
                    "# You're now representing a client on case " + caseReference
                );
            }

            if (callback.getEvent() == Event.REMOVE_BAIL_LEGAL_REPRESENTATIVE) {
                postSubmitResponse.setConfirmationHeader(
                    "# You have removed the legal representative from this case"
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
}
