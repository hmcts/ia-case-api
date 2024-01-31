package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isPanelRequired;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AutoRequestHearingService;


@Component
@RequiredArgsConstructor
public class ListCaseWithoutHearingRequirementsConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private static final String WHAT_HAPPENS_NEXT_LABEL = "#### What happens next\n\n";

    private final AutoRequestHearingService autoRequestHearingService;

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return LIST_CASE_WITHOUT_HEARING_REQUIREMENTS == callback.getEvent();
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse response = new PostSubmitCallbackResponse();

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        boolean isAutoRequestHearing = autoRequestHearingService
            .shouldAutoRequestHearing(asylumCase, !isPanelRequired(asylumCase));

        Map<String, String> confirmation = isAutoRequestHearing
            ? autoRequestHearingService
                .buildAutoHearingRequestConfirmation(asylumCase, callback.getCaseDetails().getId())
            : buildConfirmationResponse(isPanelRequired(asylumCase));

        response.setConfirmationHeader(confirmation.get("header"));
        response.setConfirmationBody(confirmation.get("body"));

        return response;
    }

    private Map<String, String> buildConfirmationResponse(boolean panelRequired) {

        Map<String, String> confirmation = new HashMap<>();
        if (panelRequired) {
            confirmation.put("header", "# List without requirements complete");
            confirmation.put("body", WHAT_HAPPENS_NEXT_LABEL
                                     + "The listing team will now list the case. All parties will be notified when "
                                     + "the Hearing Notice is available to view");
        } else {
            confirmation.put("header", "# You've recorded the agreed hearing adjustments");
            confirmation.put("body", WHAT_HAPPENS_NEXT_LABEL
                + "The listing team will now list the case."
                + " All parties will be notified when the Hearing Notice is available to view.<br><br>"
            );
        }

        return confirmation;
    }
}
