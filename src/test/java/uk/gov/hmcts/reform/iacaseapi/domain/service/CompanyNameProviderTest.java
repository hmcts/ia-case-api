package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_LEGAL_REP_COMPANY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;


@ExtendWith(MockitoExtension.class)
class CompanyNameProviderTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private ProfessionalOrganisationRetriever professionalOrganisationRetriever;
    @Mock
    private OrganisationEntityResponse organisationResponse;

    private CompanyNameProvider companyNameProvider;
    private final String organisationIdentifier = "some identifier";
    private final String organisationName = "some company name";

    @BeforeEach
    public void setup() {

        companyNameProvider = new CompanyNameProvider(professionalOrganisationRetriever);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationResponse);
    }

    @Test
    void should_write_to_asylum_case_field_for_start_appeal_event() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(organisationResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(organisationResponse.getName()).thenReturn(organisationName);

        companyNameProvider.prepareCompanyName(callback);

        verify(asylumCase, times(1)).write(LEGAL_REP_COMPANY, organisationName);
        verify(asylumCase, times(0)).write(UPDATE_LEGAL_REP_COMPANY, organisationName);
    }

    @Test
    void should_write_to_asylum_case_field_for_update_representative_details_event() {

        when(callback.getEvent()).thenReturn(Event.UPDATE_LEGAL_REPRESENTATIVES_DETAILS);
        when(organisationResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        when(organisationResponse.getName()).thenReturn(organisationName);

        companyNameProvider.prepareCompanyName(callback);

        verify(asylumCase, times(0)).write(LEGAL_REP_COMPANY, organisationName);
        verify(asylumCase, times(1)).write(UPDATE_LEGAL_REP_COMPANY, organisationName);
    }

    @Test
    void should_not_write_to_asylum_case_field_when_organisation_response_is_null() {

        when(professionalOrganisationRetriever.retrieve()).thenReturn(null);

        companyNameProvider.prepareCompanyName(callback);

        verify(asylumCase, times(0)).write(LEGAL_REP_COMPANY, organisationName);
        verify(asylumCase, times(0)).write(UPDATE_LEGAL_REP_COMPANY, organisationName);
    }
}
