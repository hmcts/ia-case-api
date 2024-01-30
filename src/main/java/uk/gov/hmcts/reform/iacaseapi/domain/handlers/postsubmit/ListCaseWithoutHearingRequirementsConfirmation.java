package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isPanelRequired;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;


@Component
public class ListCaseWithoutHearingRequirementsConfirmation
    implements AutoRequestHearingConfirmation, PostSubmitCallbackHandler<AsylumCase> {

    private static final String WHAT_HAPPENS_NEXT_LABEL = "#### What happens next\n\n";
    private final LocationBasedFeatureToggler locationBasedFeatureToggler;

    public ListCaseWithoutHearingRequirementsConfirmation(LocationBasedFeatureToggler locationBasedFeatureToggler) {
        this.locationBasedFeatureToggler = locationBasedFeatureToggler;
    }

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

        if (locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase) == YES) {
            boolean hearingRequestSuccessful = asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)
                .map(manualCreateRequired -> NO == manualCreateRequired)
                .orElse(true);

            return buildAutoHearingRequestConfirmationResponse(
                callback.getCaseDetails().getId(),
                isPanelRequired(asylumCase),
                hearingRequestSuccessful,
                "List without requirements"
            );
        } else {
            return buildConfirmationResponse();
        }
    }

    private PostSubmitCallbackResponse buildConfirmationResponse() {

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        postSubmitResponse.setConfirmationHeader("# You've recorded the agreed hearing adjustments");
        postSubmitResponse.setConfirmationBody(
            WHAT_HAPPENS_NEXT_LABEL
            + "The listing team will now list the case."
            + " All parties will be notified when the Hearing Notice is available to view.<br><br>"
        );

        return postSubmitResponse;
    }
}
