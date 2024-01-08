package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_ADJUSTMENTS;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;


@Component
public class ReviewHearingRequirementsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private LocationBasedFeatureToggler locationBasedFeatureToggler;

    public ReviewHearingRequirementsConfirmation(LocationBasedFeatureToggler locationBasedFeatureToggler) {
        this.locationBasedFeatureToggler = locationBasedFeatureToggler;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Event event = callback.getEvent();

        boolean isAutoHearingRequestDisabled = !Objects.equals(
            locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase),
            YesOrNo.YES);

        return Set.of(REVIEW_HEARING_REQUIREMENTS, UPDATE_HEARING_ADJUSTMENTS).contains(event)
               || (LIST_CASE_WITHOUT_HEARING_REQUIREMENTS.equals(event) && isAutoHearingRequestDisabled);
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've recorded the agreed hearing adjustments");
        StringBuilder messageContentString = new StringBuilder("#### What happens next\n\n");

        if (Arrays.asList(
                REVIEW_HEARING_REQUIREMENTS,
                UPDATE_HEARING_ADJUSTMENTS
        ).contains(callback.getEvent())) {
            String addCaseFlagUrl =
                    "/case/IA/Asylum/"
                            + callback.getCaseDetails().getId()
                            + "/trigger/createFlag";

            String manageCaseFlagUrl =
                    "/case/IA/Asylum/"
                            + callback.getCaseDetails().getId()
                            + "/trigger/manageFlags";

            messageContentString.append("You should ensure that the case flags reflect the hearing requests that have been approved. This may require adding new case flags or making active flags inactive.\n\n"
                    + "[Add case flag](" + addCaseFlagUrl + ")<br>"
                    + "[Manage case flags](" + manageCaseFlagUrl + ")<br><br>");
        }
        messageContentString.append("The listing team will now list the case. All parties will be notified when the Hearing Notice is available to view.<br><br>");
        postSubmitResponse.setConfirmationBody(messageContentString.toString());

        return postSubmitResponse;
    }
}
