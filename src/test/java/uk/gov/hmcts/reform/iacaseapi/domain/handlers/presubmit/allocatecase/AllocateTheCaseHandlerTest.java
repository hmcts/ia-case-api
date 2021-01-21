package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_NAME_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ALLOCATE_THE_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.RoleAssignmentService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AllocateTheCaseHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DynamicList caseWorkerNameList;
    @Mock
    private Value caseWorkerName;

    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private FeatureToggler featureToggler;

    private AllocateTheCaseHandler allocateTheCaseHandler;

    @BeforeEach
    public void createHandler() {
        allocateTheCaseHandler = new AllocateTheCaseHandler(roleAssignmentService, featureToggler);
        when(featureToggler.getValue("allocate-a-case-feature", false)).thenReturn(true);
    }

    @Test
    void assigns_role() {
        when(callback.getEvent()).thenReturn(ALLOCATE_THE_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_WORKER_NAME_LIST, DynamicList.class))
            .thenReturn(Optional.of(caseWorkerNameList));
        when(caseWorkerNameList.getValue()).thenReturn(caseWorkerName);


        allocateTheCaseHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).read(CASE_WORKER_NAME_LIST, DynamicList.class);
        verify(roleAssignmentService).assignRole(caseDetails.getId(), caseWorkerNameList.getValue().getCode());
        verify(asylumCase).write(CASE_WORKER_NAME, caseWorkerName.getLabel());
        verify(asylumCase).clear(eq(CASE_WORKER_NAME_LIST));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> allocateTheCaseHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> allocateTheCaseHandler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = allocateTheCaseHandler.canHandle(callbackStage, callback);

                if (event == ALLOCATE_THE_CASE
                    && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void cannot_handle_if_feature_disabled() {
        when(callback.getEvent()).thenReturn(ALLOCATE_THE_CASE);
        when(featureToggler.getValue("allocate-a-case-feature", false)).thenReturn(false);
        boolean canHandle = allocateTheCaseHandler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> allocateTheCaseHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> allocateTheCaseHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> allocateTheCaseHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> allocateTheCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void when_case_worker_name_list_is_not_present_Then_throw_exception() {
        when(callback.getEvent()).thenReturn(ALLOCATE_THE_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_WORKER_NAME_LIST, DynamicList.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allocateTheCaseHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("caseWorkerNameList field is not present on the caseData")
            .isExactlyInstanceOf(RuntimeException.class);
    }
}
