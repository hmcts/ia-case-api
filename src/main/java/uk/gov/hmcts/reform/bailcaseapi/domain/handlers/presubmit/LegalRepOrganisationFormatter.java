package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.LegRepAddressUk;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.AddressUK;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

@Slf4j
@Service
public class LegalRepOrganisationFormatter implements PreSubmitCallbackHandler<BailCase> {

    private final ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    private UserDetails userDetails;
    private UserDetailsHelper userDetailsHelper;

    public LegalRepOrganisationFormatter(
        ProfessionalOrganisationRetriever professionalOrganisationRetriever,
        UserDetails userDetails,
        UserDetailsHelper userDetailsHelper
    ) {
        this.professionalOrganisationRetriever = professionalOrganisationRetriever;
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.START_APPLICATION
               && isLegalRep(userDetails, userDetailsHelper);
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final OrganisationEntityResponse organisationEntityResponse =
            professionalOrganisationRetriever.retrieve();

        if (organisationEntityResponse == null) {
            log.warn("Data fetched from Professional Ref data is empty, case ID: {}",
                     callback.getCaseDetails().getId());
        }

        if (organisationEntityResponse != null
            && StringUtils.isNotBlank(organisationEntityResponse.getOrganisationIdentifier())) {

            log.info("PRD endpoint called for caseId [{}] orgId[{}]",
                     callback.getCaseDetails().getId(), organisationEntityResponse.getOrganisationIdentifier());

            setupCaseCreation(callback, organisationEntityResponse.getOrganisationIdentifier());
            setupLegalRepCompanyAddress(callback, organisationEntityResponse);
        }

        return new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
    }

    private void setupCaseCreation(Callback<BailCase> callback, String organisationIdentifier) {

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        final OrganisationPolicy organisationPolicy =
            OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                                  .organisationID(organisationIdentifier)
                                  .build()
                )
                .orgPolicyCaseAssignedRole("[LEGALREPRESENTATIVE]")
                .build();

        bailCase.write(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }

    private void setupLegalRepCompanyAddress(Callback<BailCase> callback,
                                             OrganisationEntityResponse organisationEntityResponse) {
        BailCase bailCase = callback.getCaseDetails().getCaseData();
        AddressUK addressUk;
        List<LegRepAddressUk> addresses = organisationEntityResponse.getContactInformation() == null
            ? Collections.emptyList() : organisationEntityResponse.getContactInformation();

        if (!addresses.isEmpty()) {
            LegRepAddressUk legRepAddressUk = addresses.get(0);
            addressUk = new AddressUK(
                legRepAddressUk.getAddressLine1() == null ? "" : legRepAddressUk.getAddressLine1(),
                legRepAddressUk.getAddressLine2() == null ? "" : legRepAddressUk.getAddressLine2(),
                legRepAddressUk.getAddressLine3() == null ? "" : legRepAddressUk.getAddressLine3(),
                legRepAddressUk.getTownCity() == null ? "" : legRepAddressUk.getTownCity(),
                legRepAddressUk.getCounty() == null ? "" : legRepAddressUk.getCounty(),
                legRepAddressUk.getPostCode() == null ? "" : legRepAddressUk.getPostCode(),
                legRepAddressUk.getCountry() == null ? "" : legRepAddressUk.getCountry()
            );
        } else {
            addressUk = new AddressUK(
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            );
        }

        bailCase.write(BailCaseFieldDefinition.LEGAL_REP_COMPANY_ADDRESS, addressUk);
    }

    private boolean isLegalRep(UserDetails userDetails, UserDetailsHelper userDetailsHelper) {
        UserRoleLabel userRoleLabel = userDetailsHelper.getLoggedInUserRoleLabel(userDetails);
        return userRoleLabel.equals(UserRoleLabel.LEGAL_REPRESENTATIVE);
    }
}

