package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREV_JOURNEY_TYPE;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.*;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PostNotificationSender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdCaseAssignment;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChangeRepresentationConfirmationTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CcdCaseAssignment ccdCaseAssignment;
    @Mock PostNotificationSender<AsylumCase> postNotificationSender;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private RoleAssignmentService roleAssignmentService;

    private static final long CASE_ID = 1234567890L;
    private ChangeRepresentationConfirmation changeRepresentationConfirmation;

    @BeforeEach
    public void setUp() throws Exception {

        changeRepresentationConfirmation = new ChangeRepresentationConfirmation(
            ccdCaseAssignment,
            postNotificationSender,
            roleAssignmentService
        );
    }

    @Test
    void should_apply_noc_for_remove_representation() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_REPRESENTATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(ccdCaseAssignment, times(1)).applyNoc(callback);

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You have stopped representing this client");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "We've sent you an email confirming you're no longer representing this client.\n"
                + "You have been removed from this case and no longer have access to it.\n\n"
                + "[View case list](/cases)"
            );
    }

    @Test
    void should_apply_noc_for_remove_legal_representative() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_LEGAL_REPRESENTATIVE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(ccdCaseAssignment, times(1)).applyNoc(callback);

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You have removed the legal representative from this appeal");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("All parties will be notified.");
    }

    @Test
    void should_apply_noc_for_change_legal_representative() {

        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);

        assertNotNull(callbackResponse);

        verify(ccdCaseAssignment, times(1)).applyNoc(callback);

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You have started representing this client");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("All parties will be notified.");
    }

    @Test
    void should_revoke_appellant_access_to_case() {

        String assignmentId = "assignmentId";
        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(List.of(RoleType.CASE))
            .roleName(List.of(RoleName.CREATOR))
            .roleCategory(List.of(RoleCategory.CITIZEN))
            .attributes(Map.of(
                Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                Attributes.CASE_TYPE, List.of("Asylum"),
                Attributes.CASE_ID, List.of(String.valueOf(CASE_ID))
            )).build();

        RoleAssignmentResource roleAssignmentResource =
            new RoleAssignmentResource(Collections.singletonList(Assignment.builder().id(assignmentId).build()));

        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(PREV_JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(roleAssignmentService.queryRoleAssignments(queryRequest)).thenReturn(roleAssignmentResource);

        changeRepresentationConfirmation.handle(callback);

        verify(roleAssignmentService, times(1)).queryRoleAssignments(queryRequest);
        verify(roleAssignmentService, times(1)).deleteRoleAssignment(assignmentId);
    }

    @Test
    void should_revoke_eldest_appellant_access_to_case_if_multiple() {

        String assignmentIdOld = "assignmentId";
        String assignmentIdMiddle = "assignmentIdMiddle";
        String assignmentIdNew = "assignmentIdNew";
        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(List.of(RoleType.CASE))
            .roleName(List.of(RoleName.CREATOR))
            .roleCategory(List.of(RoleCategory.CITIZEN))
            .attributes(Map.of(
                Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                Attributes.CASE_TYPE, List.of("Asylum"),
                Attributes.CASE_ID, List.of(String.valueOf(CASE_ID))
            )).build();
        Assignment assignmentOld = Assignment.builder().id(assignmentIdOld)
            .created(LocalDateTime.of(2025, 2, 10, 0, 0, 0)).build();
        Assignment assignmentMiddle = Assignment.builder().id(assignmentIdMiddle)
            .created(LocalDateTime.of(2025, 2, 11, 0, 0, 0)).build();
        Assignment assignmentNew = Assignment.builder().id(assignmentIdNew)
            .created(LocalDateTime.of(2025, 2, 12, 0, 0, 0)).build();
        List<Assignment> assignments = List.of(assignmentMiddle, assignmentOld, assignmentNew);
        RoleAssignmentResource roleAssignmentResource = new RoleAssignmentResource(assignments);

        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(PREV_JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(roleAssignmentService.queryRoleAssignments(queryRequest)).thenReturn(roleAssignmentResource);

        changeRepresentationConfirmation.handle(callback);

        verify(roleAssignmentService, times(1)).queryRoleAssignments(queryRequest);
        verify(roleAssignmentService, times(1)).deleteRoleAssignment(assignmentIdOld);
        verify(roleAssignmentService, never()).deleteRoleAssignment(assignmentIdNew);
        verify(roleAssignmentService, never()).deleteRoleAssignment(assignmentIdMiddle);
    }

    @Test
    void should_not_revoke_appellant_access_to_case_for_non_noc_request_event() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_REPRESENTATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        changeRepresentationConfirmation.handle(callback);

        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(List.of(RoleType.CASE))
            .roleName(List.of(RoleName.CREATOR))
            .roleCategory(List.of(RoleCategory.CITIZEN))
            .attributes(Map.of(
                Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                Attributes.CASE_TYPE, List.of("Asylum"),
                Attributes.CASE_ID, List.of(String.valueOf(CASE_ID))
            )).build();

        verify(roleAssignmentService, times(0)).queryRoleAssignments(queryRequest);
        verify(roleAssignmentService, times(0)).deleteRoleAssignment("assignmentId");
    }

    @Test
    void should_not_revoke_appellant_access_to_case_for_lr_to_lr_noc() {
        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        changeRepresentationConfirmation.handle(callback);

        QueryRequest queryRequest = QueryRequest.builder()
            .roleType(List.of(RoleType.CASE))
            .roleName(List.of(RoleName.CREATOR))
            .roleCategory(List.of(RoleCategory.CITIZEN))
            .attributes(Map.of(
                Attributes.JURISDICTION, List.of(Jurisdiction.IA.name()),
                Attributes.CASE_TYPE, List.of("Asylum"),
                Attributes.CASE_ID, List.of(String.valueOf(CASE_ID))
            )).build();

        verify(roleAssignmentService, times(0)).queryRoleAssignments(queryRequest);
        verify(roleAssignmentService, times(0)).deleteRoleAssignment("assignmentId");
    }

    @Test
    void should_handle_when_rest_exception_thrown_for_apply_noc() {

        when(callback.getEvent()).thenReturn(Event.REMOVE_REPRESENTATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        RestClientResponseException restClientResponseEx = mock(RestClientResponseException.class);
        doThrow(restClientResponseEx).when(ccdCaseAssignment).applyNoc(callback);

        PostSubmitCallbackResponse callbackResponse =
            changeRepresentationConfirmation.handle(callback);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("Something went wrong");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeRepresentationConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = changeRepresentationConfirmation.canHandle(callback);

            if (event == Event.REMOVE_REPRESENTATION
                || event == Event.REMOVE_LEGAL_REPRESENTATIVE
                || event == Event.NOC_REQUEST) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> changeRepresentationConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeRepresentationConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
