package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LOCAL_AUTHORITY_POLICY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.Organisation;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LegalRepOrganisationFormatterTest {

    private LegalRepOrganisationFormatter legalRepOrganisationFormatter;
    private String companyName = "LBC";
    private String organisationIdentifier = "ABC1234";
    private final String addressLine1 = "A";
    private final String addressLine2 = "B";
    private final String addressLine3 = "C";
    private final String townCity = "D";
    private final String county = "E";
    private final String postCode = "F";
    private final String country = "G";
    private OrganisationPolicy organisationPolicy;

    @Mock ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    @Mock OrganisationEntityResponse organisationEntityResponse;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeatureToggler featureToggler;


    @BeforeEach
    public void setUp() throws Exception {
        legalRepOrganisationFormatter = new LegalRepOrganisationFormatter(
            professionalOrganisationRetriever,
            featureToggler
        );

        organisationPolicy =
            OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                    .organisationID(organisationIdentifier)
                    .build()
                )
                .orgPolicyCaseAssignedRole("caseworker-ia-legalrep-solicitor")
                .build();
    }

    @Test
    void should_respond_with_asylum_case_with_results() {
        List<LegRepAddressUk> addresses = new ArrayList<>();
        LegRepAddressUk legRepAddressUk = new LegRepAddressUk(
            addressLine1,
            addressLine2,
            addressLine3,
            townCity,
            county,
            postCode,
            country,
            Arrays.asList("A", "B")
        );
        addresses.add(legRepAddressUk);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getContactInformation()).thenReturn(addresses);
        when(organisationEntityResponse.getName()).thenReturn(companyName);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> response =
            legalRepOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        verify(asylumCase, times(1)).write(LEGAL_REP_COMPANY_NAME, companyName);

        AddressUk addressUk = new AddressUk(
            addressLine1,
            addressLine2,
            addressLine3,
            townCity,
            county,
            postCode,
            country
        );
        assertEquals(addressLine1, addressUk.getAddressLine1().get());
        assertEquals(addressLine2, addressUk.getAddressLine2().get());
        assertEquals(addressLine3, addressUk.getAddressLine3().get());
        assertEquals(postCode, addressUk.getPostCode().get());
        assertEquals(townCity, addressUk.getPostTown().get());
        assertEquals(county, addressUk.getCounty().get());
        assertEquals(country, addressUk.getCountry().get());

        verify(asylumCase, times(1)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }

    @Test
    void should_not_write_to_local_authority_policy_if_feature_not_enabled() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> response =
            legalRepOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(asylumCase, times(0)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = legalRepOrganisationFormatter.canHandle(callbackStage, callback);

                if (event == Event.START_APPEAL
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepOrganisationFormatter.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepOrganisationFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
