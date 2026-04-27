package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVOKE_ACCESS_DL;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.User;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RevokeCitizenPreparerTest {

    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private IdamService idamService;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private RoleAssignmentResource roleAssignmentResource;
    @Mock
    private Assignment assignment;
    @Mock
    private Assignment assignment2;
    @Mock
    private User user;
    @Mock
    private User user2;
    @Captor
    private ArgumentCaptor<DynamicList> captor;

    private RevokeCitizenPreparer preparer;

    private final long caseId = 12345L;
    private final String userId1 = "user-1";
    private final String userName1 = "User One";
    private final String userId2 = "user-2";
    private final String userName2 = "User Two";
    
    @BeforeEach
    void setUp() {
        preparer = new RevokeCitizenPreparer(roleAssignmentService, idamService);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REVOKE_CITIZEN_ACCESS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
    }

    @Test
    void should_populate_dynamic_list_when_assignments_exist() {
        when(roleAssignmentService.getUsersAssignedToCase(caseId)).thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse()).thenReturn(List.of(assignment, assignment2));
        when(assignment.getActorId()).thenReturn(userId1);
        when(assignment2.getActorId()).thenReturn(userId2);
        when(idamService.getUserFromId(userId1)).thenReturn(user);
        when(idamService.getUserFromId(userId2)).thenReturn(user2);
        when(user.toValueId()).thenReturn(userId1);
        when(user.toString()).thenReturn(userName1);
        when(user.getRoles()).thenReturn(List.of("citizen"));
        when(user.isActive()).thenReturn(true);
        when(user2.toValueId()).thenReturn(userId2);
        when(user2.toString()).thenReturn(userName2);
        when(user2.getRoles()).thenReturn(List.of("citizen"));
        when(user2.isActive()).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> response =
            preparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase).write(eq(REVOKE_ACCESS_DL), captor.capture());
        DynamicList actualDl = captor.getValue();
        assertNull(actualDl.getValue());
        assertEquals(2, actualDl.getListItems().size());
        Value value1 = actualDl.getListItems().get(0);
        Value value2 = actualDl.getListItems().get(1);
        assertEquals(userId1, value1.getCode());
        assertEquals(userName1, value1.getLabel());
        assertEquals(userId2, value2.getCode());
        assertEquals(userName2, value2.getLabel());
    }


    @Test
    void should_populate_dynamic_list_when_assignments_exist_and_filter_correctly() {
        String userId3 = "user-3";
        String userId4 = "user-4";
        Assignment assignment3 = mock(Assignment.class);
        Assignment assignment4 = mock(Assignment.class);
        User user3 = new User(userId3, "User", "3", "", true, List.of());
        User user4 = null;
        when(roleAssignmentService.getUsersAssignedToCase(caseId)).thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse()).thenReturn(List.of(assignment, assignment2, assignment3, assignment4));
        when(assignment.getActorId()).thenReturn(userId1);
        when(assignment2.getActorId()).thenReturn(userId2);
        when(assignment3.getActorId()).thenReturn(userId3);
        when(assignment4.getActorId()).thenReturn(userId4);
        when(idamService.getUserFromId(userId1)).thenReturn(user);
        when(idamService.getUserFromId(userId2)).thenReturn(user2);
        when(idamService.getUserFromId(userId3)).thenReturn(user3);
        when(idamService.getUserFromId(userId4)).thenReturn(user4);
        when(user.toValueId()).thenReturn(userId1);
        when(user.toString()).thenReturn(userName1);
        when(user.getRoles()).thenReturn(List.of("citizen"));
        when(user.isActive()).thenReturn(false);
        when(user2.toValueId()).thenReturn(userId2);
        when(user2.toString()).thenReturn(userName2);
        when(user2.getRoles()).thenReturn(List.of("citizen"));
        when(user2.isActive()).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> response =
            preparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase).write(eq(REVOKE_ACCESS_DL), captor.capture());
        DynamicList actualDl = captor.getValue();
        assertNull(actualDl.getValue());
        assertEquals(1, actualDl.getListItems().size());
        Value value = actualDl.getListItems().get(0);
        assertEquals(userId2, value.getCode());
        assertEquals(userName2, value.getLabel());
    }


    @Test
    void should_error_if_assignments_exist_but_no_citizens_found() {
        when(roleAssignmentService.getUsersAssignedToCase(caseId)).thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse()).thenReturn(List.of(assignment));
        when(assignment.getActorId()).thenReturn("user-1");
        when(idamService.getUserFromId("user-1")).thenReturn(user);
        when(user.getId()).thenReturn("user-1");
        when(user.toString()).thenReturn("User One");
        when(user.getRoles()).thenReturn(Collections.emptyList());
        when(user.isActive()).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> response =
            preparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertFalse(response.getErrors().isEmpty());
        verify(asylumCase, never()).write(eq(REVOKE_ACCESS_DL), any());
        assertTrue(response.getErrors().iterator().next()
            .contains("No citizen/non LR users with case access were found: " + caseId));
    }

    @Test
    void should_error_if_assignments_exist_but_no_active_itizens_found() {
        when(roleAssignmentService.getUsersAssignedToCase(caseId)).thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse()).thenReturn(List.of(assignment));
        when(assignment.getActorId()).thenReturn("user-1");
        when(idamService.getUserFromId("user-1")).thenReturn(user);
        when(user.getId()).thenReturn("user-1");
        when(user.toString()).thenReturn("User One");
        when(user.getRoles()).thenReturn(List.of("citizen"));
        when(user.isActive()).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> response =
            preparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertFalse(response.getErrors().isEmpty());
        verify(asylumCase, never()).write(eq(REVOKE_ACCESS_DL), any());
        assertTrue(response.getErrors().iterator().next()
            .contains("No citizen/non LR users with case access were found: " + caseId));
    }

    @Test
    void should_return_error_when_no_assignments_found() {
        when(roleAssignmentService.getUsersAssignedToCase(caseId)).thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse()).thenReturn(Collections.emptyList());

        PreSubmitCallbackResponse<AsylumCase> response =
            preparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertFalse(response.getErrors().isEmpty());
        assertTrue(response.getErrors().iterator().next().contains("No users have case access with caseId: " + caseId));
        verify(asylumCase, never()).write(eq(REVOKE_ACCESS_DL), any());
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> preparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> preparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(Event.class)
    void can_handle_only_for_correct_event_and_stage(Event event) {
        when(callback.getEvent()).thenReturn(event);
        for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
            boolean canHandle = preparer.canHandle(stage, callback);
            if (event == Event.REVOKE_CITIZEN_ACCESS && stage == PreSubmitCallbackStage.ABOUT_TO_START) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

    @Test
    void should_throw_if_cannot_handle_callback() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> preparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.REVOKE_CITIZEN_ACCESS);
        assertThatThrownBy(() -> preparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}