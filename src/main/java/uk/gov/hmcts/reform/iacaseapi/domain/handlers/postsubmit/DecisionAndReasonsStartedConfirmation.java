package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AutoRequestHearingService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

@Component
@RequiredArgsConstructor
public class DecisionAndReasonsStartedConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private static final String WHAT_HAPPENS_NEXT_LABEL = "#### What happens next\n\n";
    public static final String CONFIRMATION_HEADER_TEXT = "# You have started the decision and reasons process";
    public static final String COMPLETE_DECISION_TEXT = "The judge can now download and complete the decision and reasons document.";

    private final AutoRequestHearingService autoRequestHearingService;

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.DECISION_AND_REASONS_STARTED;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (autoRequestHearingService.shouldAutoRequestHearing(asylumCase)) {
            boolean hearingRequestSuccessful = asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)
                .map(manualCreateRequired -> NO == manualCreateRequired)
                .orElse(true);

            return buildAutoHearingRequestConfirmationResponse(callback.getCaseDetails().getId(), hearingRequestSuccessful);
        } else {
            return buildConfirmationResponse();
        }
    }

    private PostSubmitCallbackResponse buildAutoHearingRequestConfirmationResponse(long caseId, boolean hearingRequestSuccessful) {

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        if (hearingRequestSuccessful) {
            postSubmitResponse.setConfirmationHeader(CONFIRMATION_HEADER_TEXT);
            postSubmitResponse.setConfirmationBody(
                WHAT_HAPPENS_NEXT_LABEL
                    + "The hearing request has been created and is visible on the [Hearings tab]"
                    + "(/cases/case-details/" + caseId + "/hearings)"
                    + "\n\n"
                    + COMPLETE_DECISION_TEXT);
        } else {
            postSubmitResponse.setConfirmationHeader(CONFIRMATION_HEADER_TEXT);
            postSubmitResponse.setConfirmationBody(
                WHAT_HAPPENS_NEXT_LABEL
                    + "The hearing could not be auto-requested. Please manually request the "
                    + "hearing via the [Hearings tab](/cases/case-details/" + caseId + "/hearings)"
                    + "\n\n"
                    + COMPLETE_DECISION_TEXT);
        }

        return postSubmitResponse;
    }



    private static PostSubmitCallbackResponse buildConfirmationResponse() {
        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader(CONFIRMATION_HEADER_TEXT);
        postSubmitResponse.setConfirmationBody(WHAT_HAPPENS_NEXT_LABEL + COMPLETE_DECISION_TEXT);

        return postSubmitResponse;
    }
}
