package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
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

        boolean isRepJourney = callback.getCaseDetails().getCaseData()
            .read(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.class)
            .map(journeyType -> journeyType == JourneyType.REP)
            .orElse(true);

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.START_APPEAL
               && isRepJourney;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final OrganisationEntityResponse organisationEntityResponse =
            professionalOrganisationRetriever.retrieve();

        if (organisationEntityResponse == null) {
            log.warn("Data fetched from Professional Ref data is empty, case ID: {}", callback.getCaseDetails().getId());
        }

        if (organisationEntityResponse != null
            && StringUtils.isNotBlank(organisationEntityResponse.getOrganisationIdentifier())
            && featureToggler.getValue("share-case-feature", false)) {

            log.info("PRD endpoint called for caseId [{}] orgId[{}]",
                callback.getCaseDetails().getId(), organisationEntityResponse.getOrganisationIdentifier());

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
            organisationName = organisationEntityResponse.getName() == null
                ? "" : organisationEntityResponse.getName();
            List<LegRepAddressUk> addresses = organisationEntityResponse.getContactInformation() == null
                ? Collections.emptyList() : organisationEntityResponse.getContactInformation();
            if (!addresses.isEmpty()) {
                LegRepAddressUk legRepAddressUk = addresses.get(0);
                addressUk = new AddressUk(
                    legRepAddressUk.getAddressLine1() == null ? "" : legRepAddressUk.getAddressLine1(),
                    legRepAddressUk.getAddressLine2() == null ? "" : legRepAddressUk.getAddressLine2(),
                    legRepAddressUk.getAddressLine3() == null ? "" : legRepAddressUk.getAddressLine3(),
                    legRepAddressUk.getTownCity() == null ? "" : legRepAddressUk.getTownCity(),
                    legRepAddressUk.getCounty() == null ? "" : legRepAddressUk.getCounty(),
                    legRepAddressUk.getPostCode() == null ? "" : legRepAddressUk.getPostCode(),
                    legRepAddressUk.getCountry() == null ? "" : legRepAddressUk.getCountry()
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
