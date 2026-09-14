package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeleteDraftAppealHandlerTest {

    @Mock
    private RoleAssignmentService roleAssignmentService;

    @Mock
    private IdamService idamService;

    @Mock
    private UserDetailsProvider userDetailsProvider;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private RoleAssignmentResource roleAssignmentResource;

    @Mock
    private UserDetails userDetails;

    private DeleteDraftAppealHandler handler;

    private final long caseId = 12345L;
    private final String idamId = "user-123";
    private final String roleAssignmentId = "role-456";

    @BeforeEach
    void setUp() {

        handler = new DeleteDraftAppealHandler(
            roleAssignmentService,
            idamService,
            userDetailsProvider
        );

        when(callback.getEvent()).thenReturn(Event.DELETE_DRAFT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(idamId);
    }

    @Test
    void should_delete_role_assignment_when_citizen_role_exists() {

        Assignment assignment = Assignment.builder()
            .id(roleAssignmentId)
            .roleCategory(RoleCategory.CITIZEN)
            .build();

        when(roleAssignmentService.getCaseRoleAssignmentsForUser(caseId, idamId))
            .thenReturn(roleAssignmentResource);

        when(roleAssignmentResource.getRoleAssignmentResponse())
            .thenReturn(List.of(assignment));

        when(idamService.getServiceUserToken()).thenReturn("token");

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());

        verify(roleAssignmentService)
            .getCaseRoleAssignmentsForUser(caseId, idamId);

        verify(roleAssignmentService)
            .deleteRoleAssignment(roleAssignmentId, "token");
    }

    @Test
    void should_not_delete_role_assignment_when_role_not_citizen() {

        Assignment assignment = Assignment.builder()
            .id(roleAssignmentId)
            .roleCategory(RoleCategory.PROFESSIONAL)
            .build();

        when(roleAssignmentService.getCaseRoleAssignmentsForUser(caseId, idamId))
            .thenReturn(roleAssignmentResource);

        when(roleAssignmentResource.getRoleAssignmentResponse())
            .thenReturn(List.of(assignment));

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());

        verify(roleAssignmentService)
            .getCaseRoleAssignmentsForUser(caseId, idamId);

        verify(roleAssignmentService, never())
            .deleteRoleAssignment(anyString(), anyString());
    }

    @Test
    void should_return_error_when_no_role_assignments_found() {

        when(roleAssignmentService.getCaseRoleAssignmentsForUser(caseId, idamId))
            .thenReturn(roleAssignmentResource);

        when(roleAssignmentResource.getRoleAssignmentResponse())
            .thenReturn(Collections.emptyList());

        PreSubmitCallbackResponse<AsylumCase> response =
            handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        assertFalse(response.getErrors().isEmpty());
        assertTrue(response.getErrors()
            .contains("User doesn't have access to case: " + idamId
                + " caseId: " + caseId));

        verify(roleAssignmentService)
            .getCaseRoleAssignmentsForUser(caseId, idamId);

        verify(roleAssignmentService, never())
            .deleteRoleAssignment(anyString(), anyString());
    }

    @Test
    void can_handle_only_for_delete_draft_event_and_about_to_submit_stage() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {

                boolean canHandle = handler.canHandle(stage, callback);

                if (event == Event.DELETE_DRAFT_APPEAL
                    && stage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);

                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_throw_if_callback_stage_null() {

        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> handler.canHandle(null, callback));
        assertEquals("callbackStage must not be null", exception.getMessage());
    }

    @Test
    void should_throw_if_callback_null() {

        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void should_throw_if_handler_cannot_handle_callback() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
        assertEquals("Cannot handle callback", exception.getMessage());

        when(callback.getEvent()).thenReturn(Event.DELETE_DRAFT_APPEAL);

        exception = assertThrows(IllegalStateException.class,
            () -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback));
        assertEquals("Cannot handle callback", exception.getMessage());
    }
}