package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECIDE_AN_APPLICATION_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REASON_FOR_LINK_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplicationTypes.getTypeFrom;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ReasonForLinkAppealOptions;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class DecideAnApplicationConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public static final String NOTIFY_LISTING_TEAM_NOTICE = "You need to tell the listing team to relist the case. "
        + "Once the case is relisted a new Notice of Hearing will be sent to all parties.";

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

        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();

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
        String body = "Granted".equals(application.getDecision())
            ? getGrantedConfirmationBody(application.getType(), asylumCase, caseDetails.getId())
            : "Both parties will be notified that the application was refused.";

        postSubmitResponse.setConfirmationBody("""
            #### What happens next

            The application decision has been recorded and is now available in the applications tab.\s""" + body);
        return postSubmitResponse;
    }

    private static String getGrantedConfirmationBody(String applicationType, AsylumCase asylumCase, long caseId) {
        boolean isListAssisted = HandlerUtils.isIntegrated(asylumCase);

        String body = getTypeFrom(applicationType).map(type -> switch (type) {
            case LINK_OR_UNLINK -> {
                final Optional<ReasonForLinkAppealOptions> reasonForAppeal =
                    asylumCase.read(REASON_FOR_LINK_APPEAL, ReasonForLinkAppealOptions.class);
                yield "You must now [link the appeal](LINK)linkAppeal) or " + (reasonForAppeal.isPresent()
                    ? "[unlink the appeal](LINK)unlinkAppeal)."
                    : "unlink the appeal");
            }
            case ADJOURN -> isListAssisted
                ? "You must now [record the details of the adjournment](LINK)recordAdjournmentDetails)."
                : NOTIFY_LISTING_TEAM_NOTICE;
            case TRANSFER, EXPEDITE -> isListAssisted
                ? "You must now [update the hearing request](LINK)updateHearingRequest)."
                : NOTIFY_LISTING_TEAM_NOTICE;
            case JUDGE_REVIEW -> "Both parties will receive a notification detailing your decision.";
            case REINSTATE -> "You now need to [reinstate the appeal](LINK)reinstateAppeal)";
            case TIME_EXTENSION -> "You must now [change the direction's due date](LINK)changeDirectionDueDate)";
            case UPDATE_APPEAL_DETAILS -> "You must now [update the appeal details](LINK)editAppealAfterSubmit)";
            case UPDATE_HEARING_REQUIREMENTS ->
                "You must now [update the hearing requirements](LINK)updateHearingRequirements)";
            case WITHDRAW -> "You must now [end the appeal](LINK)endAppeal)";
            default -> "";
        }).orElse("");

        return body.replace("(LINK)", "(/case/IA/Asylum/" + caseId + "/trigger/");
    }
}
