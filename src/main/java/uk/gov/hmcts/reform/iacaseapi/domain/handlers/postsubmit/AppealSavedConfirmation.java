package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdCaseAssignment;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;

@Slf4j
@Component
public class AppealSavedConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private final ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    private final CcdCaseAssignment ccdCaseAssignment;
    private final FeatureToggler featureToggler;

    public AppealSavedConfirmation(
        ProfessionalOrganisationRetriever professionalOrganisationRetriever,
        CcdCaseAssignment ccdCaseAssignment,
        FeatureToggler featureToggler
    ) {
        this.professionalOrganisationRetriever = professionalOrganisationRetriever;
        this.ccdCaseAssignment = ccdCaseAssignment;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return (callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL)
                && HandlerUtils.isRepJourney(callback.getCaseDetails().getCaseData());
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String paymentOption = null;

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("AppealType is not present"));

        Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);

        if (appealType == AppealType.PA) {
            paymentOption = asylumCase
                .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                .orElse("");
        }

        postSubmitResponse.setConfirmationHeader("# You have saved your appeal\n# You still need to submit it");
        postSubmitResponse.setConfirmationBody(
            "### Do this next\n\n"
            + "If you're ready to submit your appeal, select 'Submit your appeal' in " +
                "the 'Next step' dropdown list from your case details page.\n\n"
            + "#### Not ready to submit your appeal yet?\n"
            + "You can return to the case details page to make changes from the ‘Next step’ dropdown list."
        );

        if (asylumCase.read(AsylumCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class).isPresent()
            && callback.getEvent() == Event.START_APPEAL
            && featureToggler.getValue("share-case-feature", false)
            && !isInternalCase(asylumCase)) {

            final String organisationIdentifier =
                professionalOrganisationRetriever
                    .retrieve()
                    .getOrganisationIdentifier();

            log.info("PRD endpoint called for caseId [{}] orgId[{}]",
                callback.getCaseDetails().getId(), organisationIdentifier);

            ccdCaseAssignment.assignAccessToCase(callback);
            ccdCaseAssignment.revokeAccessToCase(callback, organisationIdentifier);
        }

        return postSubmitResponse;
    }
}
