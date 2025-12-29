package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.CompanyNameProvider;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY;

@Slf4j
@Component
public class LegalRepresentativeDetailsAppender implements PreSubmitCallbackHandler<BailCase> {

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;
    private final ProfessionalOrganisationRetriever professionalOrganisationRetriever;

    private final CompanyNameProvider companyNameProvider;

    public LegalRepresentativeDetailsAppender(UserDetails userDetails, UserDetailsHelper userDetailsHelper,
                                              CompanyNameProvider companyNameProvider,
                                              ProfessionalOrganisationRetriever professionalOrganisationRetriever) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
        this.companyNameProvider = companyNameProvider;
        this.professionalOrganisationRetriever = professionalOrganisationRetriever;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && (callback.getEvent() == Event.START_APPLICATION
                || callback.getEvent() == Event.MAKE_NEW_APPLICATION)
            || callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.MAKE_NEW_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        String email = userDetails.getEmailAddress();

        UserRoleLabel userRoleLabel = userDetailsHelper.getLoggedInUserRoleLabel(userDetails, true);

        if (userRoleLabel.equals(UserRoleLabel.LEGAL_REPRESENTATIVE)) {
            companyNameProvider.prepareCompanyName(callback);
            bailCase.write(BailCaseFieldDefinition.LEGAL_REP_EMAIL_ADDRESS, email);

            final OrganisationEntityResponse organisationEntityResponse =
                professionalOrganisationRetriever.retrieve();

            if (organisationEntityResponse != null
                && StringUtils.isNotBlank(organisationEntityResponse.getOrganisationIdentifier())) {
                setupLocalAuthorityPolicy(callback, organisationEntityResponse.getOrganisationIdentifier());
            }

        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void setupLocalAuthorityPolicy(Callback<BailCase> callback, String organisationIdentifier) {

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        final OrganisationPolicy organisationPolicy =
            OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                                  .organisationID(organisationIdentifier)
                                  .build()
                )
                .orgPolicyCaseAssignedRole("[LEGALREPRESENTATIVE]")
                .build();

        bailCase.write(LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }
}
