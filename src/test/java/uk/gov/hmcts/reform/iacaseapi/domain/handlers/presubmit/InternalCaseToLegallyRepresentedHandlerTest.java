package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.Organisation;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class InternalCaseToLegallyRepresentedHandlerTest {

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

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    @Mock
    OrganisationEntityResponse organisationEntityResponse;

    private InternalCaseToLegallyRepresentedHandler internalCaseToLegallyRepresentedHandler;

    @BeforeEach
    public void setUp() {
        internalCaseToLegallyRepresentedHandler = new InternalCaseToLegallyRepresentedHandler(professionalOrganisationRetriever);

        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

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
    void should_clear_internal_case_fields_and_set_initial_legal_rep_fields() {
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

        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getContactInformation()).thenReturn(addresses);
        when(organisationEntityResponse.getName()).thenReturn(companyName);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                internalCaseToLegallyRepresentedHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(IS_ADMIN), eq(YesOrNo.NO));

        verify(asylumCase, times(1)).clear(eq(APPEAL_SUBMISSION_INTERNAL_DATE));
        verify(asylumCase, times(1)).clear(eq(TRIBUNAL_RECEIVED_DATE));
        verify(asylumCase, times(1)).clear(eq(INTERNAL_APPELLANT_EMAIL));
        verify(asylumCase, times(1)).clear(eq(INTERNAL_APPELLANT_MOBILE_NUMBER));

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
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = internalCaseToLegallyRepresentedHandler.canHandle(callbackStage, callback);

                if (event == Event.NOC_REQUEST
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

}