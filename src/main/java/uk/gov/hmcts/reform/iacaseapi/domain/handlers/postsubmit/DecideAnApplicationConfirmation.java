package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ReasonForLinkAppealOptions;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class DecideAnApplicationConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    @Override
    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {

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

        maybeMakeAnApplication
            .ifPresent(application -> {
                String decision = application.getValue().getDecision();
                String typeOfApplication = application.getValue().getType();
                String whatHappensNextHeader = "#### What happens next\n\n";
                String decisionRecordedText = "The application decision has been recorded and is now available in the applications tab. ";

                if (decision.equals("Granted")) {
                    switch (typeOfApplication) {
                        case "Adjourn":
                        case "Expedite":
                        case "Transfer":
                            postSubmitResponse.setConfirmationBody(
                                whatHappensNextHeader
                                + decisionRecordedText
                                + "You need to tell the listing team to relist the case. Once the case is relisted a new Notice of Hearing "
                                + "will be sent to all parties."
                            );
                            break;

                        case "Link/unlink appeals":
                            final Optional<ReasonForLinkAppealOptions> reasonForLinkAppeal =
                                asylumCase.read(REASON_FOR_LINK_APPEAL, ReasonForLinkAppealOptions.class);
                            String body =
                                reasonForLinkAppeal.isPresent() == true
                                    ? whatHappensNextHeader
                                      + decisionRecordedText
                                      + "You must now [link the appeal](/case/IA/Asylum/"
                                      + callback.getCaseDetails().getId() + "/trigger/linkAppeal)"
                                      + " or [unlink the appeal](/case/IA/Asylum/"
                                      + callback.getCaseDetails().getId() + "/trigger/unlinkAppeal)."
                                    : whatHappensNextHeader
                                      + decisionRecordedText
                                      + "You must now [link the appeal](/case/IA/Asylum/"
                                      + callback.getCaseDetails().getId() + "/trigger/linkAppeal)"
                                      + " or unlink the appeal";
                            postSubmitResponse.setConfirmationBody(body);

                            break;

                        case "Judge's review of application decision":
                            postSubmitResponse.setConfirmationBody(
                                whatHappensNextHeader
                                + decisionRecordedText
                                + "Both parties will receive a notification detailing your decision."
                            );
                            break;

                        case "Reinstate an ended appeal":
                            postSubmitResponse.setConfirmationBody(
                                whatHappensNextHeader
                                + decisionRecordedText
                                + "You now need to [reinstate the appeal](/case/IA/Asylum/"
                                + callback.getCaseDetails().getId() + "/trigger/reinstateAppeal)"
                            );
                            break;

                        case "Time extension":
                            postSubmitResponse.setConfirmationBody(
                                whatHappensNextHeader
                                + decisionRecordedText
                                + "You must now [change the direction's due date](/case/IA/Asylum/"
                                + callback.getCaseDetails().getId() + "/trigger/changeDirectionDueDate)"
                            );
                            break;

                        case "Update appeal details":
                            postSubmitResponse.setConfirmationBody(
                                whatHappensNextHeader
                                + decisionRecordedText
                                + "You must now [update the appeal details](/case/IA/Asylum/"
                                + callback.getCaseDetails().getId() + "/trigger/editAppealAfterSubmit)"
                            );
                            break;

                        case "Update hearing requirements":
                            postSubmitResponse.setConfirmationBody(
                                whatHappensNextHeader
                                + decisionRecordedText
                                + "You must now [update the hearing requirements](/case/IA/Asylum/"
                                + callback.getCaseDetails().getId() + "/trigger/updateHearingRequirements)"
                            );
                            break;

                        case "Withdraw":
                            postSubmitResponse.setConfirmationBody(
                                whatHappensNextHeader
                                + decisionRecordedText
                                + "You must now [end the appeal](/case/IA/Asylum/"
                                + callback.getCaseDetails().getId() + "/trigger/endAppeal)"
                            );
                            break;

                        default:
                            postSubmitResponse.setConfirmationBody(
                                whatHappensNextHeader
                                + decisionRecordedText
                            );
                    }
                } else {
                    postSubmitResponse.setConfirmationBody(
                        whatHappensNextHeader
                        + decisionRecordedText
                        + "Both parties will be notified that the application was refused."
                    );
                }
            });

        return postSubmitResponse;
    }
}
