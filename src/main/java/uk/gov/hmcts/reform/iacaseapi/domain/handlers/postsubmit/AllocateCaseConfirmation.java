package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class AllocateCaseConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public static final String ALLOCATE_TO_ME = "Allocate to me";
    public static final String ALLOCATE_TO_A_COLLEAGUE = "Allocate to a colleague";

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.ALLOCATE_CASE;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You have allocated the case");

        AsylumCase asylum = callback.getCaseDetails().getCaseData();

        String allocationType = asylum.read(AsylumCaseFieldDefinition.ALLOCATION_TYPE, String.class)
            .orElseThrow(RuntimeException::new);

        postSubmitResponse.setConfirmationBody(getConfirmationBodyContent(callback, allocationType));
        return postSubmitResponse;
    }

    private String getConfirmationBodyContent(Callback<AsylumCase> callback, String allocationType) {
        String confirmationBodyContent;
        if (allocationType.equals(ALLOCATE_TO_ME)) {
            confirmationBodyContent = String.format("#### What happens next%n%n"
                + "###### The tasks for this case will now appear in your task list."
                + "%nShould you wish, you can write [a case note]"
                + "(/case/IA/Asylum/%s/trigger/addCaseNote).", callback.getCaseDetails().getId());

        } else if (allocationType.equals(ALLOCATE_TO_A_COLLEAGUE)) {
            confirmationBodyContent = String.format("#### What happens next%n%n"
                + "###### The tasks for this case will now appear in your colleague's task list."
                + "%nShould you wish, you can [write them a case note]"
                + "(/case/IA/Asylum/%s/trigger/addCaseNote)."
                + " They will be notified that this case has been allocated to them.", callback.getCaseDetails().getId());
        } else {
            throw new RuntimeException("allocationType is not recognised.");
        }
        return confirmationBodyContent;
    }
}
