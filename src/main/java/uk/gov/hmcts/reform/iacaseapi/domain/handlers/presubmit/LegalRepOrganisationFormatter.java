package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.Organisation;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;


@Slf4j
@Service
public class LegalRepOrganisationFormatter implements PreSubmitCallbackHandler<AsylumCase> {

    private final ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    private final FeatureToggler featureToggler;

    public LegalRepOrganisationFormatter(
        ProfessionalOrganisationRetriever professionalOrganisationRetriever,
        FeatureToggler featureToggler
    ) {
        this.professionalOrganisationRetriever = professionalOrganisationRetriever;
        this.featureToggler = featureToggler;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.START_APPEAL;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final OrganisationEntityResponse organisationEntityResponse =
            professionalOrganisationRetriever.retrieve();

        if (organisationEntityResponse != null
            && featureToggler.getValue("share-case-feature", false)) {
            setupCaseCreation(callback, organisationEntityResponse.getOrganisationIdentifier());
        }

        return mapToAsylumCase(callback, organisationEntityResponse);
    }

    private PreSubmitCallbackResponse<AsylumCase> mapToAsylumCase(
        Callback<AsylumCase> callback,
        OrganisationEntityResponse organisationEntityResponse
    ) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        AddressUk addressUk;
        String organisationName = "";
        if (organisationEntityResponse != null) {
            organisationName = organisationEntityResponse.getName();
            List<LegRepAddressUk> addresses = organisationEntityResponse.getContactInformation();
            if (!organisationEntityResponse.getContactInformation().isEmpty()) {
                LegRepAddressUk legRepAddressUk = addresses.get(0);
                addressUk = new AddressUk(
                    legRepAddressUk.getAddressLine1(),
                    legRepAddressUk.getAddressLine2(),
                    legRepAddressUk.getAddressLine3(),
                    legRepAddressUk.getTownCity(),
                    legRepAddressUk.getCounty(),
                    legRepAddressUk.getPostCode(),
                    legRepAddressUk.getCountry()
                );
            } else {
                addressUk = new AddressUk(
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
                );
            }

        } else {
            addressUk = new AddressUk(
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            );
        }

        asylumCase.write(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_NAME, organisationName);
        asylumCase.write(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_ADDRESS, addressUk);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void setupCaseCreation(Callback<AsylumCase> callback, String organisationIdentifier) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final OrganisationPolicy organisationPolicy =
            OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                    .organisationID(organisationIdentifier)
                    .build()
                )
                .orgPolicyCaseAssignedRole("[LEGALREPRESENTATIVE]")
                .build();

        asylumCase.write(AsylumCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }
}
