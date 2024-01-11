package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

@Component
@RequiredArgsConstructor
public class ReviewHearingRequirementsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private static final String WHAT_HAPPENS_NEXT_LABEL = "#### What happens next\n\n";
    private final LocationBasedFeatureToggler locationBasedFeatureToggler;

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return REVIEW_HEARING_REQUIREMENTS == callback.getEvent();
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase) == YES) {
            boolean hearingRequestSuccessful = asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)
                .map(manualCreateRequired -> NO == manualCreateRequired)
                .orElse(true);

            return buildAutoHearingRequestConfirmationResponse(
                callback.getCaseDetails().getId(), hearingRequestSuccessful);
        } else {
            return buildConfirmationResponse(callback.getCaseDetails().getId());
        }
    }

    private PostSubmitCallbackResponse buildAutoHearingRequestConfirmationResponse(
        long caseId, boolean hearingRequestSuccessful) {

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        if (hearingRequestSuccessful) {
            postSubmitResponse.setConfirmationHeader("# Hearing listed");
            postSubmitResponse.setConfirmationBody(WHAT_HAPPENS_NEXT_LABEL
                                                   + "The hearing request has been created and is visible on the [Hearings tab]"
                                                   + "(/cases/case-details/" + caseId + "/hearings)");
        } else {
            postSubmitResponse.setConfirmationHeader("");
            postSubmitResponse.setConfirmationBody(
                "![Hearing could not be listed](https://raw.githubusercontent.com/hmcts/"
                + "ia-appeal-frontend/master/app/assets/images/hearingCouldNotBeListed.png)"
                + "\n\n"
                + WHAT_HAPPENS_NEXT_LABEL
                + "The hearing could not be auto-requested. Please manually request the "
                + "hearing via the [Hearings tab](/cases/case-details/" + caseId + "/hearings)");
        }

        return postSubmitResponse;
    }

    private PostSubmitCallbackResponse buildConfirmationResponse(long caseId) {

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        String addCaseFlagUrl = "/case/IA/Asylum/" + caseId + "/trigger/createFlag";
        String manageCaseFlagUrl = "/case/IA/Asylum/" + caseId + "/trigger/manageFlags";

        postSubmitResponse.setConfirmationHeader("# You've recorded the agreed hearing adjustments");
        postSubmitResponse.setConfirmationBody(
            WHAT_HAPPENS_NEXT_LABEL
            + "You should ensure that the case flags reflect the hearing requests that have been approved. "
            + "This may require adding new case flags or making active flags inactive.\n\n"
            + "[Add case flag](" + addCaseFlagUrl + ")<br>"
            + "[Manage case flags](" + manageCaseFlagUrl + ")<br><br>"
            + "The listing team will now list the case. "
            + "All parties will be notified when the Hearing Notice is available to view.<br><br>"
        );

        return postSubmitResponse;
    }
}
