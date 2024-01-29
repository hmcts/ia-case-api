package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTO_REQUEST_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isPanelRequired;

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
public class ReviewHearingRequirementsConfirmation
    implements AutoRequestHearingConfirmation, PostSubmitCallbackHandler<AsylumCase> {

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

        if (shouldAutoRequestHearing(asylumCase)) {
            boolean hearingRequestSuccessful = asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)
                .map(manualCreateRequired -> NO == manualCreateRequired)
                .orElse(true);

            return buildAutoHearingRequestConfirmationResponse(
                callback.getCaseDetails().getId(),
                isPanelRequired(asylumCase),
                hearingRequestSuccessful,
                "Hearing requirements");
        } else {
            return buildConfirmationResponse(callback.getCaseDetails().getId());
        }
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

    private boolean shouldAutoRequestHearing(AsylumCase asylumCase) {
        boolean autoRequestHearing = asylumCase.read(AUTO_REQUEST_HEARING, YesOrNo.class)
            .map(autoRequest -> YES == autoRequest).orElse(false);
        boolean autoRequestHearingEnabled = locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase) == YES;

        return autoRequestHearingEnabled && autoRequestHearing;
    }
}
