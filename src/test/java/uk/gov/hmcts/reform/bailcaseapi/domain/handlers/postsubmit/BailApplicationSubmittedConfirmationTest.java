package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
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
import uk.gov.hmcts.reform.bailcaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdCaseAssignment;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.ProfessionalOrganisationRetriever;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.TimedEvent;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BailApplicationSubmittedConfirmationTest {

    @Mock
    private Callback<BailCase> callback;

    @Mock
    private CaseDetails<BailCase> caseDetails;

    @Mock
    private BailCase bailCase;

    @Mock
    private ProfessionalOrganisationRetriever professionalOrganisationRetriever;

    @Mock
    private CcdCaseAssignment ccdCaseAssignment;

    @Mock
    private OrganisationPolicy organisationPolicy;

    @Mock
    private OrganisationEntityResponse organisationEntityResponse;

    @Mock
    private UserDetails userDetails;

    @Mock
    private UserDetailsHelper userDetailsHelper;
    private boolean timedEventServiceEnabled;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private Scheduler scheduler;

    private BailApplicationSubmittedConfirmation bailApplicationSubmittedConfirmation;

    @BeforeEach
    public void setUp() {
        bailApplicationSubmittedConfirmation =
            new BailApplicationSubmittedConfirmation(
                professionalOrganisationRetriever,
                ccdCaseAssignment,
                userDetails,
                userDetailsHelper,
                false,
                dateProvider,
                scheduler
            );

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
    }

    @Test
    void should_set_header_body() {
        long caseId = 1234L;
        String organisationIdentifier = "someOrgIdentifier";

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(userDetailsHelper.getLoggedInUserRoleLabel(any(), any())).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);
        when(bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)).thenReturn(
            Optional.of(organisationPolicy));
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);

        PostSubmitCallbackResponse response = bailApplicationSubmittedConfirmation.handle(callback);

        assertNotNull(response.getConfirmationBody(), "Confirmation Body is null");
        assertThat(response.getConfirmationBody().get()).contains("### What happens next");

        assertNotNull(response.getConfirmationHeader(), "Confirmation Header is null");
        assertThat(response.getConfirmationHeader().get()).isEqualTo("# You have submitted this application");
        verify(scheduler, never()).schedule(any(TimedEvent.class));
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> bailApplicationSubmittedConfirmation.handle(null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_handle_invalid_callback() {
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION); //Invalid event for this handler

        assertThatThrownBy(() -> bailApplicationSubmittedConfirmation.handle(callback)).isExactlyInstanceOf(
            IllegalStateException.class);
    }

    @Test
    void should_return_confirmation_on_Bail_Save() {
        long caseId = 1234L;
        String organisationIdentifier = "someOrgIdentifier";

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(any(), any())).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);
        when(bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)).thenReturn(
            Optional.of(organisationPolicy));
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);

        PostSubmitCallbackResponse response = bailApplicationSubmittedConfirmation.handle(callback);

        assertNotNull(response);
        assertThat(response.getConfirmationBody().isPresent());
        assertThat(response.getConfirmationHeader().isPresent());

        assertThat(response.getConfirmationBody().get()).contains(
            "### What happens next\n\n"
                + "All parties will be notified that the application has been submitted.");
    }

    @Test
    void should_assign_and_revoke_access_to_case_when_user_is_Lr() {
        long caseId = 1234L;
        String organisationIdentifier = "someOrgIdentifier";

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(callback.getCaseDetails().getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(any(), any())).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);
        when(bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)).thenReturn(
            Optional.of(organisationPolicy));
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);

        bailApplicationSubmittedConfirmation.handle(callback);

        verify(ccdCaseAssignment, times(1)).assignAccessToCase(callback);
        // TODO: verify(ccdCaseAssignment, times(1)).revokeAccessToCase(callback, organisationIdentifier);
    }

    @Test
    void should_not_assign_and_revoke_access_to_case_when_user_is_not_LR() {
        long caseId = 1234L;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(callback.getCaseDetails().getCaseData()).thenReturn(bailCase);
        when(userDetailsHelper.getLoggedInUserRoleLabel(any(), any())).thenReturn(UserRoleLabel.ADMIN_OFFICER);
        when(bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)).thenReturn(
            Optional.of(organisationPolicy));

        bailApplicationSubmittedConfirmation.handle(callback);

        verify(ccdCaseAssignment, never()).assignAccessToCase(callback);
        verify(ccdCaseAssignment, never()).revokeAccessToCase(any(), anyString());
    }

    @Test
    void testTestTimedEventScheduleEvent() {
        bailApplicationSubmittedConfirmation =
            new BailApplicationSubmittedConfirmation(
                professionalOrganisationRetriever,
                ccdCaseAssignment,
                userDetails,
                userDetailsHelper,
                true,
                dateProvider,
                scheduler
            );
        long caseId = 1234L;
        String organisationIdentifier = "someOrgIdentifier";

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(userDetailsHelper.getLoggedInUserRoleLabel(any(), any())).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);
        when(bailCase.read(BailCaseFieldDefinition.LOCAL_AUTHORITY_POLICY, OrganisationPolicy.class)).thenReturn(
            Optional.of(organisationPolicy));
        when(professionalOrganisationRetriever.retrieve()).thenReturn(organisationEntityResponse);
        when(organisationEntityResponse.getOrganisationIdentifier()).thenReturn(organisationIdentifier);
        LocalDateTime someTime = LocalDateTime.of(2024, 10, 10, 10, 10, 10);
        when(dateProvider.nowWithTime()).thenReturn(someTime);
        ZonedDateTime someTimeDelayed = ZonedDateTime.of(
            someTime,
            ZoneId.systemDefault()
        ).plusMinutes(1);
        bailApplicationSubmittedConfirmation.handle(callback);
        verify(dateProvider).nowWithTime();
        TimedEvent timedEvent = new TimedEvent(
            "",
            Event.TEST_TIMED_EVENT_SCHEDULE,
            someTimeDelayed,
            "IA",
            "Bail",
            callback.getCaseDetails().getId()
        );
        verify(scheduler).schedule(refEq(timedEvent));
    }
}
