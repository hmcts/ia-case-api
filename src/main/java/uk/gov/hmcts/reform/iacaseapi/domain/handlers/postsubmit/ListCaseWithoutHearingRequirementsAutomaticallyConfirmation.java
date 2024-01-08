package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;

import java.util.Objects;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;


@Component
public class ListCaseWithoutHearingRequirementsAutomaticallyConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private static final String WHAT_HAPPENS_NEXT_LABEL = "### What happens next\n\n";
    private static final String HEARING_COULD_NOT_BE_LISTED_PNG =
        """
            ![Hearing could not be listed](https://raw.githubusercontent.com/hmcts/
            ia-appeal-frontend/master/app/assets/images/hearingCouldNotBeListed.png)
            """;
    private LocationBasedFeatureToggler locationBasedFeatureToggler;

    public ListCaseWithoutHearingRequirementsAutomaticallyConfirmation(LocationBasedFeatureToggler locationBasedFeatureToggler) {
        this.locationBasedFeatureToggler = locationBasedFeatureToggler;
    }


    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Event event = callback.getEvent();

        boolean isAutoHearingRequestEnabled = Objects.equals(
            locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase),
            YesOrNo.YES
        );

        return LIST_CASE_WITHOUT_HEARING_REQUIREMENTS.equals(event)
               && isAutoHearingRequestEnabled;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse response =
            new PostSubmitCallbackResponse();

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean hearingRequestFailed = asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)
            .map(yesOrNo -> yesOrNo.equals(YesOrNo.YES))
            .orElse(false);

        long caseId = callback.getCaseDetails().getId();

        if (hearingRequestFailed) {

            response.setConfirmationHeader("");
            response.setConfirmationBody(HEARING_COULD_NOT_BE_LISTED_PNG
                                         + WHAT_HAPPENS_NEXT_LABEL
                                         + "The hearing could not be auto-requested. Please manually request the "
                                         + "hearing via the [Hearings tab](/cases/case-details/" + caseId + "/hearings)");
        } else {
            response.setConfirmationHeader("# Hearing listed");
            response.setConfirmationBody("#### What happens next\n\n"
                                         + "The hearing request has been created and is visible on the [Hearings tab]"
                                         + "(/cases/case-details/" + caseId + "/hearings)");
        }

        return response;
    }
}
