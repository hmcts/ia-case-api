package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALLOCATE_THE_CASE_TO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_WORKER_LOCATION_LIST;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AllocateTheCaseToCaseWorkerHandlerTest {

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
    @Mock
    private AllocateTheCaseService allocateTheCaseService;

    private AllocateTheCaseToCaseWorkerHandler allocateTheCaseToCaseWorkerHandler;

    @BeforeEach
    public void createHandler() {
        allocateTheCaseToCaseWorkerHandler = new AllocateTheCaseToCaseWorkerHandler(
            roleAssignmentService,
            featureToggler,
            allocateTheCaseService
        );
        when(featureToggler.getValue("allocate-a-case-feature", false)).thenReturn(true);
        when(allocateTheCaseService.isAllocateToCaseWorkerOption(any(AsylumCase.class))).thenReturn(true);
    }

    @Test
    void assigns_role() {
        when(callback.getEvent()).thenReturn(ALLOCATE_THE_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_WORKER_NAME_LIST, DynamicList.class))
            .thenReturn(Optional.of(caseWorkerNameList));
        when(caseWorkerNameList.getValue()).thenReturn(caseWorkerName);

        allocateTheCaseToCaseWorkerHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).read(CASE_WORKER_NAME_LIST, DynamicList.class);
        verify(roleAssignmentService).assignRole(caseDetails.getId(), caseWorkerNameList.getValue().getCode());
        verify(asylumCase).write(CASE_WORKER_NAME, caseWorkerName.getLabel());
        verify(asylumCase).clear(eq(CASE_WORKER_NAME_LIST));
        verify(asylumCase).clear(eq(CASE_WORKER_LOCATION_LIST));
        verify(asylumCase).clear(eq(ALLOCATE_THE_CASE_TO));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> allocateTheCaseToCaseWorkerHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> allocateTheCaseToCaseWorkerHandler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = allocateTheCaseToCaseWorkerHandler.canHandle(callbackStage, callback);

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
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        boolean canHandle = allocateTheCaseToCaseWorkerHandler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> allocateTheCaseToCaseWorkerHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> allocateTheCaseToCaseWorkerHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> allocateTheCaseToCaseWorkerHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> allocateTheCaseToCaseWorkerHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void when_case_worker_name_list_is_not_present_then_throw_exception() {
        when(callback.getEvent()).thenReturn(ALLOCATE_THE_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_WORKER_NAME_LIST, DynamicList.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allocateTheCaseToCaseWorkerHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("caseWorkerNameList field is not present on the caseData")
            .isExactlyInstanceOf(RuntimeException.class);
    }
}
