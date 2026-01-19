package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.Organisation;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LegalRepBailOrganisationFormatterTest {

    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.LegalRepBailOrganisationFormatter legalRepBailOrganisationFormatter;
    private String companyName = "LBC";
    private long ccdCaseId = 12345L;
    private String organisationIdentifier = "ZE2KIWO";
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
    @Mock
    private org.slf4j.Logger log
            = org.slf4j.LoggerFactory.getLogger(uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.LegalRepBailOrganisationFormatter.class);


    @BeforeEach
    public void setUp() throws Exception {

        legalRepBailOrganisationFormatter = new uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.LegalRepBailOrganisationFormatter(
            professionalOrganisationRetriever,
            featureToggler
        );

        organisationPolicy =
            OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                    .organisationID(organisationIdentifier)
                    .build()
                )
                .orgPolicyCaseAssignedRole("[LEGALREPRESENTATIVE]")
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
            country
        );
        addresses.add(legRepAddressUk);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails().getId()).thenReturn(ccdCaseId);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getContactInformation()).thenReturn(addresses);
        when(organisationEntityResponse.getName()).thenReturn(companyName);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> response =
            legalRepBailOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );
        verify(log, times(0)).warn("Data fetched from Professional Ref data is empty, case ID: {}", ccdCaseId);
        verify(asylumCase, times(1)).write(LEGAL_REP_COMPANY_NAME, companyName);
        assertThat(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS).equals(legRepAddressUk));

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
    void should_respond_with_asylum_case_with_results_when_some_field_are_nulls() {
        List<LegRepAddressUk> addresses = new ArrayList<>();
        LegRepAddressUk legRepAddressUk = new LegRepAddressUk(
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );
        addresses.add(legRepAddressUk);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails().getId()).thenReturn(ccdCaseId);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getContactInformation()).thenReturn(addresses);
        when(organisationEntityResponse.getName()).thenReturn(companyName);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> response =
            legalRepBailOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        verify(asylumCase, times(1)).write(LEGAL_REP_COMPANY_NAME, companyName);
        verify(asylumCase, times(1)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }

    @Test
    void should_not_write_to_local_authority_policy_if_organisation_entity_response_is_null() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(professionalOrganisationRetriever.retrieve()).thenReturn(null);

        PreSubmitCallbackResponse<AsylumCase> response =
            legalRepBailOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        assertEquals(asylumCase, response.getData());

        verify(asylumCase, times(0)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }

    @Test
    void should_not_write_to_local_authority_policy_if_feature_not_enabled() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn("SomeId");
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> response =
            legalRepBailOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
            );

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(asylumCase, times(0)).write(LOCAL_AUTHORITY_POLICY, organisationPolicy);
    }

    @Test
    void should_not_write_to_local_authority_policy_if_org_id_is_null() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(null);
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);

        PreSubmitCallbackResponse<AsylumCase> response =
            legalRepBailOrganisationFormatter.handle(
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

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = legalRepBailOrganisationFormatter.canHandle(callbackStage, callback);

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
    void should_write_skeleton_local_authority_policy_for_aip_journey() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> response =
                legalRepBailOrganisationFormatter.handle(
                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                        callback
                );

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        OrganisationPolicy skeletonPolicy = OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                        .organisationID(null)
                        .build()
                )
                .orgPolicyCaseAssignedRole("[LEGALREPRESENTATIVE]")
                .build();

        verify(asylumCase, times(1)).write(LOCAL_AUTHORITY_POLICY, skeletonPolicy);
    }

    @Test
    void should_write_skeleton_local_authority_policy_for_internal_case_journey() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> response =
                legalRepBailOrganisationFormatter.handle(
                        PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                        callback
                );

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        OrganisationPolicy skeletonPolicy = OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                        .organisationID(null)
                        .build()
                )
                .orgPolicyCaseAssignedRole("[LEGALREPRESENTATIVE]")
                .build();

        verify(asylumCase, times(1)).write(LOCAL_AUTHORITY_POLICY, skeletonPolicy);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepBailOrganisationFormatter.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepBailOrganisationFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_get_empty_address_from_nulls() {
        List<LegRepAddressUk> addresses = new ArrayList<>();
        LegRepAddressUk legRepAddressUk = new LegRepAddressUk(
                null,null,null,null,null,null,null
        );
        addresses.add(legRepAddressUk);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails().getId()).thenReturn(ccdCaseId);

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getContactInformation()).thenReturn(addresses);
        when(organisationEntityResponse.getName()).thenReturn(companyName);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(featureToggler.getValue("share-case-feature", false)).thenReturn(true);

        AddressUk emptyAddressUk = new AddressUk(
                "","","","","","",""
        );
        legalRepBailOrganisationFormatter.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        );
        assertThat(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS).equals(emptyAddressUk));
    }
}
