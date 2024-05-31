package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isPanelRequired;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
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

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        boolean isAutoRequestHearing = autoRequestHearingService
            .shouldAutoRequestHearing(asylumCase, !isPanelRequired(asylumCase));

        boolean isAcceleratedDetainedAppeal = HandlerUtils.isAcceleratedDetainedAppeal(asylumCase);

        return isAutoRequestHearing
            ? autoRequestHearingService.buildAutoHearingRequestConfirmation(
                asylumCase, "# Hearing listed", callback.getCaseDetails().getId())
            : buildConfirmationResponse(isPanelRequired(asylumCase),
                isAcceleratedDetainedAppeal);
    }

    private PostSubmitCallbackResponse buildConfirmationResponse(boolean panelRequired,
                                                                 boolean isAcceleratedDetainedAppeal) {

        PostSubmitCallbackResponse response = new PostSubmitCallbackResponse();

        if (isAcceleratedDetainedAppeal) {
            response.setConfirmationHeader("# You've recorded the agreed hearing adjustments");
            response.setConfirmationBody(WHAT_HAPPENS_NEXT_LABEL
                    + "All parties will be notified of the agreed adjustments.<br><br>"
            );
        } else {
            if (panelRequired) {
                response.setConfirmationHeader("# List without requirements complete");
                response.setConfirmationBody(WHAT_HAPPENS_NEXT_LABEL
                        + "The listing team will now list the case. All parties will be notified when "
                        + "the Hearing Notice is available to view");
            } else {
                response.setConfirmationHeader("# You've recorded the agreed hearing adjustments");
                response.setConfirmationBody(WHAT_HAPPENS_NEXT_LABEL
                        + "The listing team will now list the case."
                        + " All parties will be notified when the Hearing Notice is available to view.<br><br>"
                );
            }
        }

        return response;
    }
}
