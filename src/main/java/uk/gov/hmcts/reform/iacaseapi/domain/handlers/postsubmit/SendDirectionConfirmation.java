package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@Component
public class SendDirectionConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.SEND_DIRECTION;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String hoAmendBundleReadyInstructStatus = "";

        if (callback.getCaseDetails().getState() == State.AWAITING_RESPONDENT_EVIDENCE
            && getLatestNonStandardRespondentDirection(asylumCase).isPresent()) {
            hoAmendBundleReadyInstructStatus = asylumCase.read(AsylumCaseFieldDefinition.HOME_OFFICE_AMEND_BUNDLE_INSTRUCT_STATUS,
                String.class).orElse("");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        if (hoAmendBundleReadyInstructStatus.equalsIgnoreCase("FAIL")) {

            postSubmitResponse.setConfirmationBody(
                "![Respondent notification failed confirmation]"
                + "(https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/respondent_notification_failed.svg)\n"
                + "#### Do this next\n\n"
                + "Contact the respondent to tell them what has changed, including any action they need to take.\n"
            );
        } else {

            String directionsTabUrl =
                "/case/IA/Asylum/"
                + callback.getCaseDetails().getId()
                + "#directions";

            postSubmitResponse.setConfirmationHeader("# You have sent a direction");
            postSubmitResponse.setConfirmationBody(
                "#### What happens next\n\n"
                + "You can see the status of the direction in the "
                + "[directions tab](" + directionsTabUrl + ")"
            );
        }

        return postSubmitResponse;
    }

    protected Optional<Direction> getLatestNonStandardRespondentDirection(AsylumCase asylumCase) {

        Optional<List<IdValue<Direction>>> maybeExistingDirections = asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS);

        return maybeExistingDirections
            .orElseThrow(() -> new IllegalStateException("directions not present"))
            .stream()
            .max(Comparator.comparingInt(s -> Integer.parseInt(s.getId())))
            .filter(idValue -> idValue.getValue().getTag().equals(DirectionTag.NONE))
            .filter(idValue -> idValue.getValue().getParties().equals(Parties.RESPONDENT))
            .map(IdValue::getValue);
    }
}
