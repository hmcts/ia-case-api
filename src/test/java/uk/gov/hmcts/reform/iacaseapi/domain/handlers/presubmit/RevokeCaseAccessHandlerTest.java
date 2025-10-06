package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.ActorIdType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleAssignmentResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleCategory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleName;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.RoleType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdCaseAssignment;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RevokeCaseAccessHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CcdCaseAssignment ccdCaseAssignment;
    @Mock
    private IdamService idamService;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private RoleAssignmentResource roleAssignmentResource;
    private RevokeCaseAccessHandler revokeCaseAccessHandler;
    private final long caseId = 1234;
    private final String userId = UUID.randomUUID().toString();
    private final String orgId = "org123";

    @BeforeEach
    public void setUp() {
        revokeCaseAccessHandler = new RevokeCaseAccessHandler(roleAssignmentService, ccdCaseAssignment, idamService);
        when(callback.getEvent()).thenReturn(Event.REVOKE_CASE_ACCESS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_revoke_case_access_when_legal_rep_have_access() {

        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ID, String.class))
                .thenReturn(Optional.of(userId));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ORG_ID, String.class))
                .thenReturn(Optional.of(orgId));

        when(roleAssignmentService.getCaseRoleAssignmentsForUser(caseId, userId))
                .thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse()).thenReturn(
                List.of(Assignment.builder().id("1")
                        .roleCategory(RoleCategory.PROFESSIONAL)
                        .actorId(userId)
                        .roleName(RoleName.LEGAL_REPRESENTATIVE)
                        .roleType(RoleType.CASE)
                        .actorIdType(ActorIdType.IDAM)
                        .build()));

        doNothing().when(ccdCaseAssignment).revokeLegalRepAccessToCase(caseId, userId, orgId);

        // when
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                revokeCaseAccessHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        // Then
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(roleAssignmentService).getCaseRoleAssignmentsForUser(caseId, userId);

        verify(ccdCaseAssignment).revokeLegalRepAccessToCase(caseId, userId, orgId);
        verifyNoMoreInteractions(roleAssignmentService);
        verify(asylumCase).write(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ID, null);
        verify(asylumCase).write(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ORG_ID, null);
    }

    @Test
    void should_revoke_case_access_when_citizen_have_access() {

        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ID, String.class))
                .thenReturn(Optional.of(userId));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ORG_ID, String.class))
                .thenReturn(Optional.empty());

        when(roleAssignmentService.getCaseRoleAssignmentsForUser(caseId, userId))
                .thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse()).thenReturn(
                List.of(Assignment.builder().id("1")
                        .roleCategory(RoleCategory.CITIZEN)
                        .actorId(userId)
                        .roleName(RoleName.CREATOR)
                        .roleType(RoleType.CASE)
                        .actorIdType(ActorIdType.IDAM)
                        .build()));

        doNothing().when(roleAssignmentService).deleteRoleAssignment(eq("1"), any());

        // when
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                revokeCaseAccessHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        // Then
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(roleAssignmentService).getCaseRoleAssignmentsForUser(caseId, userId);
        verify(asylumCase).write(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ID, null);
        verify(asylumCase).write(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ORG_ID, null);
        verifyNoInteractions(ccdCaseAssignment);
    }

    @Test
    void should_return_error_when_legal_rep_idam_user_id_is_not_provided() {
        // given
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ID, String.class))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> revokeCaseAccessHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("IDAM user ID is not present.")
                .isExactlyInstanceOf(IllegalStateException.class);
        verifyNoInteractions(roleAssignmentService, ccdCaseAssignment);
    }

    @Test
    void should_return_error_when_legal_rep_does_not_have_role_assignments_to_case() {
        // given
        when(caseDetails.getId()).thenReturn(caseId);
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ID, String.class))
                .thenReturn(Optional.of(userId));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_FOR_USER_ORG_ID, String.class))
                .thenReturn(Optional.of(orgId));

        when(roleAssignmentService.getCaseRoleAssignmentsForUser(caseId, userId))
                .thenReturn(roleAssignmentResource);
        when(roleAssignmentResource.getRoleAssignmentResponse()).thenReturn(Collections.emptyList());

        // when
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                revokeCaseAccessHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        // then
        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
                .contains(String.format("User doesn't have access to case: %s caseId: %s", userId, caseId));

        verifyNoInteractions(ccdCaseAssignment);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = revokeCaseAccessHandler.canHandle(callbackStage, callback);
                if (event == Event.REVOKE_CASE_ACCESS
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> revokeCaseAccessHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> revokeCaseAccessHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> revokeCaseAccessHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> revokeCaseAccessHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actual_handle() {
        assertThatThrownBy(() -> revokeCaseAccessHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(
                () -> revokeCaseAccessHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

}