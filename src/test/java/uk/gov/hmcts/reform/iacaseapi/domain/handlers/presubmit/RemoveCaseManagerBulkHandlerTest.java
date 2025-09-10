package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMOVE_CASE_MANAGER_CASE_ID_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REMOVE_CASE_MANAGER_BULK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class RemoveCaseManagerBulkHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @InjectMocks
    private RemoveCaseManagerBulkHandler removeCaseManagerBulkHandler;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REMOVE_CASE_MANAGER_BULK);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        removeCaseManagerBulkHandler = new RemoveCaseManagerBulkHandler(roleAssignmentService);

    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            boolean canHandle = removeCaseManagerBulkHandler.canHandle(callbackStage, callback);
            if (event == REMOVE_CASE_MANAGER_BULK
                && callbackStage == ABOUT_TO_SUBMIT) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> removeCaseManagerBulkHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> removeCaseManagerBulkHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> removeCaseManagerBulkHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
        when(callback.getEvent()).thenReturn(Event.UPDATE_INTERPRETER_DETAILS);
        assertThatThrownBy(() -> removeCaseManagerBulkHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_remove_case_manager_role() {
        String removeCaseManageCaseIdList = "123,456,789";
        when(asylumCase.read(REMOVE_CASE_MANAGER_CASE_ID_LIST, String.class))
            .thenReturn(Optional.of(removeCaseManageCaseIdList));
        PreSubmitCallbackResponse<AsylumCase> response =
            removeCaseManagerBulkHandler.handle(ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        ArgumentCaptor<String> caseIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(roleAssignmentService, times(3)).removeCaseManagerRole(caseIdCaptor.capture());
        List<String> capturedCaseIds = caseIdCaptor.getAllValues();
        assertEquals(3, capturedCaseIds.size());
        assertEquals("123", capturedCaseIds.get(0));
        assertEquals("456", capturedCaseIds.get(1));
        assertEquals("789", capturedCaseIds.get(2));
        verify(asylumCase).clear(REMOVE_CASE_MANAGER_CASE_ID_LIST);
    }

}
