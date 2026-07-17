package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RevokeCitizenAccessHandlerTest {

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

    private RevokeCitizenAccessHandler handler;

    private final long caseId = 12345L;
    private final String idamId = "user-123";
    private final String roleAssignmentId = "role-456";

    @BeforeEach
    void setUp() {
        handler = new RevokeCitizenAccessHandler(roleAssignmentService, idamService);
        when(callback.getEvent()).thenReturn(Event.REVOKE_CITIZEN_ACCESS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
    }

    @Test
    void should_revoke_access_when_role_assignment_exists() {
        Value value = new Value(idamId, "User Name");
        DynamicList dynamicList = new DynamicList(value, List.of(value));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));

        Assignment assignment = Assignment.builder().id(roleAssignmentId).build();
        when(roleAssignmentService.getCaseRoleAssignmentsForUser(caseId, idamId))
            .thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse())
            .thenReturn(List.of(assignment));
        when(idamService.getServiceUserToken()).thenReturn("token");

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());

        verify(roleAssignmentService).getCaseRoleAssignmentsForUser(caseId, idamId);
        verify(roleAssignmentService).deleteRoleAssignment(roleAssignmentId, "token");
    }

    @Test
    void should_clear_nlr_details_if_idam_id_equal_when_role_assignment_exists() {
        Value value = new Value(idamId, "User Name");
        DynamicList dynamicList = new DynamicList(value, List.of(value));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));
        Assignment assignment = Assignment.builder().id(roleAssignmentId).build();
        when(roleAssignmentService.getCaseRoleAssignmentsForUser(caseId, idamId))
            .thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse())
            .thenReturn(List.of(assignment));
        when(idamService.getServiceUserToken()).thenReturn("token");

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());

        verify(roleAssignmentService).getCaseRoleAssignmentsForUser(caseId, idamId);
        verify(roleAssignmentService).deleteRoleAssignment(roleAssignmentId, "token");
    }


    @Test
    void should_not_clear_nlr_details_if_idam_id_equal_when_role_assignment_exists() {
        Value value = new Value(idamId, "User Name");
        DynamicList dynamicList = new DynamicList(value, List.of(value));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));
        Assignment assignment = Assignment.builder().id(roleAssignmentId).build();
        when(roleAssignmentService.getCaseRoleAssignmentsForUser(caseId, idamId))
            .thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse())
            .thenReturn(List.of(assignment));
        when(idamService.getServiceUserToken()).thenReturn("token");

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());

        verify(roleAssignmentService).getCaseRoleAssignmentsForUser(caseId, idamId);
        verify(roleAssignmentService).deleteRoleAssignment(roleAssignmentId, "token");
    }

    @Test
    void should_return_error_when_no_role_assignments_found() {
        Value value = new Value(idamId, "User Name");
        DynamicList dynamicList = new DynamicList(value, List.of(value));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));

        when(roleAssignmentService.getCaseRoleAssignmentsForUser(caseId, idamId))
            .thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse())
            .thenReturn(Collections.emptyList());

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors())
            .contains("User doesn't have access to case idamId: " + idamId + " caseId: " + caseId);

        verify(roleAssignmentService).getCaseRoleAssignmentsForUser(caseId, idamId);
        verify(roleAssignmentService, never()).deleteRoleAssignment(anyString(), anyString());
    }

    @Test
    void should_throw_when_dynamic_list_is_missing() {
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, DynamicList.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Dynamic list of users to revoke access from is not present.");

        verifyNoInteractions(roleAssignmentService);
    }

    @Test
    void can_handle_only_for_correct_event_and_stage() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean canHandle = handler.canHandle(stage, callback);
                if (event == Event.REVOKE_CITIZEN_ACCESS && stage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_throw_if_cannot_handle_callback() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        when(callback.getEvent()).thenReturn(Event.REVOKE_CITIZEN_ACCESS);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }
}