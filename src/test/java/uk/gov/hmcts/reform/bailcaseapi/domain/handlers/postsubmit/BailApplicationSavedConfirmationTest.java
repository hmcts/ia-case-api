package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.OrganisationPolicy;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserRoleLabel;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ref.OrganisationEntityResponse;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdCaseAssignment;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BailApplicationSavedConfirmationTest {

    @Mock private Callback<BailCase> callback;

    @Mock private CaseDetails<BailCase> caseDetails;

    @Mock private BailCase bailCase;

    @Mock private ProfessionalOrganisationRetriever professionalOrganisationRetriever;

    @Mock private CcdCaseAssignment ccdCaseAssignment;

    @Mock private OrganisationPolicy organisationPolicy;

    @Mock private OrganisationEntityResponse organisationEntityResponse;

    @Mock private UserDetails userDetails;

    @Mock private UserDetailsHelper userDetailsHelper;

    private BailApplicationSavedConfirmation bailApplicationSavedConfirmation;

    @BeforeEach
    public void setUp() {
        bailApplicationSavedConfirmation = new BailApplicationSavedConfirmation(professionalOrganisationRetriever,
                                                                                ccdCaseAssignment,
                                                                                userDetails,
                                                                                userDetailsHelper);
        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
    }

    @Test
    void should_set_header_body() {
        long caseId = 1234L;
        String organisationIdentifier = "someOrgIdentifier";

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(userDetailsHelper.getLoggedInUserRoleLabel(any())).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);
        when(bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)).thenReturn(
            Optional.of(organisationPolicy));
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);

        PostSubmitCallbackResponse response = bailApplicationSavedConfirmation.handle(callback);

        assertNotNull(response.getConfirmationBody(), "Confirmation Body is null");
        assertThat(response.getConfirmationBody().get()).contains("# Do this next");

        assertNotNull(response.getConfirmationHeader(), "Confirmation Header is null");
        assertThat(response.getConfirmationHeader().get()).isEqualTo("# You have saved this application");
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> bailApplicationSavedConfirmation.handle(null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_handle_invalid_callback() {
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION); //Invalid event for this handler

        assertThatThrownBy(() -> bailApplicationSavedConfirmation.handle(callback)).isExactlyInstanceOf(
            IllegalStateException.class);
    }

    @Test
    void should_return_confirmation_on_Bail_Save() {
        long caseId = 1234L;
        String organisationIdentifier = "someOrgIdentifier";

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(any())).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);
        when(bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)).thenReturn(
            Optional.of(organisationPolicy));
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);

        PostSubmitCallbackResponse response = bailApplicationSavedConfirmation.handle(callback);

        assertNotNull(response);
        assertThat(response.getConfirmationBody().isPresent());
        assertThat(response.getConfirmationHeader().isPresent());

        assertThat(response.getConfirmationBody().get()).contains(
            "Review and [edit the application](/case/IA/Bail/"
            + caseId
            + "/trigger/editBailApplication) if necessary. [Submit the application](/case/IA/Bail/"
            + caseId
            + "/trigger/submitApplication) when you’re ready.");
    }

    @Test
    void should_assign_and_revoke_access_to_case_when_user_is_Lr() {
        long caseId = 1234L;
        String organisationIdentifier = "someOrgIdentifier";

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(callback.getCaseDetails().getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(any())).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);
        when(bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)).thenReturn(
            Optional.of(organisationPolicy));
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);

        bailApplicationSavedConfirmation.handle(callback);

        verify(ccdCaseAssignment, times(1)).assignAccessToCase(callback);
        verify(ccdCaseAssignment, times(1)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_not_assign_and_revoke_access_to_case_when_user_is_not_LR() {
        long caseId = 1234L;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(callback.getCaseDetails().getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(any())).thenReturn(UserRoleLabel.ADMIN_OFFICER);
        when(bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)).thenReturn(
            Optional.of(organisationPolicy));

        bailApplicationSavedConfirmation.handle(callback);

        verify(ccdCaseAssignment, never()).assignAccessToCase(callback);
        verify(ccdCaseAssignment, never()).revokeAccessToCase(any(), anyString());
    }
}
