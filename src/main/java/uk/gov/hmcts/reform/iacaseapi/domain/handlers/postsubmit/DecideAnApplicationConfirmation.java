package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ReasonForLinkAppealOptions;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class DecideAnApplicationConfirmation implements PostSubmitCallbackHandler<AsylumCase> {
    @Override
    public boolean canHandle(Callback<AsylumCase> callback) {

        requireNonNull(callback, "callback must not be null");
        return callback.getEvent() == Event.DECIDE_AN_APPLICATION;
    }

    @Override
    public PostSubmitCallbackResponse handle(Callback<AsylumCase> callback) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String applicationId = asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("Application id is not present"));

        Optional<List<IdValue<MakeAnApplication>>> mayBeMakeAnApplications = asylumCase.read(MAKE_AN_APPLICATIONS);
        Optional<IdValue<MakeAnApplication>> maybeMakeAnApplication =
            mayBeMakeAnApplications
            .orElse(Collections.emptyList())
            .stream()
            .filter(idValue -> idValue.getId().equals(applicationId))
            .findAny();

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();
        postSubmitResponse.setConfirmationHeader("# You have decided an application");

        if (maybeMakeAnApplication.isEmpty()) {
            return postSubmitResponse;
        }

        MakeAnApplication application = maybeMakeAnApplication.get().getValue();
        String commonBody = """
            #### What happens next

            The application decision has been recorded and is now available in the applications tab.\s""";

        if (!"Granted".equals(application.getDecision())) {
            postSubmitResponse.setConfirmationBody(
                commonBody + "Both parties will be notified that the application was refused."
            );
            return postSubmitResponse;
        }

        final long id = callback.getCaseDetails().getId();
        String linkCommon = "(/case/IA/Asylum/" + id + "/trigger/";
        String body = switch (MakeAnApplicationTypes.valueOf(application.getType())) {
            case LINK_OR_UNLINK -> {
                final Optional<ReasonForLinkAppealOptions> reasonForLinkAppeal =
                    asylumCase.read(REASON_FOR_LINK_APPEAL, ReasonForLinkAppealOptions.class);
                yield commonBody + "You must now [link the appeal]" + linkCommon + "linkAppeal) or "
                    + (reasonForLinkAppeal.isPresent()
                        ? "[unlink the appeal]" + linkCommon + "unlinkAppeal)."
                        : "unlink the appeal");
            }
            // todo check works
            case ADJOURN -> commonBody
                + "You must now [record the details of the adjournment]" + linkCommon + "recordAdjournmentDetails).";
            case TRANSFER, EXPEDITE -> commonBody
                + "You must now [update the hearing request]" + linkCommon + "updateHearingRequest).";
            case JUDGE_REVIEW ->  commonBody
                + "Both parties will receive a notification detailing your decision.";
            case REINSTATE ->  commonBody
                + "You now need to [reinstate the appeal]" + linkCommon + "reinstateAppeal)";
            case TIME_EXTENSION ->  commonBody
                + "You must now [change the direction's due date]" + linkCommon + "changeDirectionDueDate)";
            case UPDATE_APPEAL_DETAILS ->  commonBody
                + "You must now [update the appeal details]" + linkCommon + "editAppealAfterSubmit)";
            case UPDATE_HEARING_REQUIREMENTS ->  commonBody
                + "You must now [update the hearing requirements]" + linkCommon + "updateHearingRequirements)";
            case WITHDRAW -> commonBody
                + "You must now [end the appeal]" + linkCommon + "endAppeal)";
            default -> commonBody;
        };

        postSubmitResponse.setConfirmationBody(body);
        return postSubmitResponse;
    }
}
