package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RecordAllocatedJudgeHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private Appender<String> appender;

    private RecordAllocatedJudgeHandler recordAllocatedJudgeHandler;

    @BeforeEach
    public void setUp() {
        recordAllocatedJudgeHandler =
            new RecordAllocatedJudgeHandler(appender);

        when(callback.getEvent()).thenReturn(Event.RECORD_ALLOCATED_JUDGE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_set_allocated_judge_field_first_time() {

        String allocatedJudge = "Judge nr 1";

        when(asylumCase.read(AsylumCaseFieldDefinition.ALLOCATED_JUDGE_EDIT, String.class))
            .thenReturn(Optional.of(allocatedJudge));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordAllocatedJudgeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(AsylumCaseFieldDefinition.ALLOCATED_JUDGE_EDIT, String.class);
        verify(asylumCase).read(AsylumCaseFieldDefinition.ALLOCATED_JUDGE, String.class);
        verify(asylumCase).write(AsylumCaseFieldDefinition.ALLOCATED_JUDGE, allocatedJudge);
        verify(asylumCase).write(AsylumCaseFieldDefinition.JUDGE_ALLOCATION_EXISTS, YesOrNo.YES);

    }

    @Test
    void should_override_allocated_judge_field_and_update_history() {

        String allocatedJudgeNr1 = "Judge nr 1";
        String allocatedJudgeNr2 = "Judge nr 2";
        List<IdValue<String>> previousJudgeAllocations = newArrayList(new IdValue<>("1", allocatedJudgeNr1));

        when(asylumCase.read(AsylumCaseFieldDefinition.ALLOCATED_JUDGE, String.class))
            .thenReturn(Optional.of(allocatedJudgeNr1));
        when(asylumCase.read(AsylumCaseFieldDefinition.ALLOCATED_JUDGE_EDIT, String.class))
            .thenReturn(Optional.of(allocatedJudgeNr2));
        when(appender.append(allocatedJudgeNr1, Collections.emptyList())).thenReturn(previousJudgeAllocations);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordAllocatedJudgeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(AsylumCaseFieldDefinition.ALLOCATED_JUDGE_EDIT, String.class);
        verify(asylumCase).read(AsylumCaseFieldDefinition.ALLOCATED_JUDGE, String.class);
        verify(asylumCase).write(AsylumCaseFieldDefinition.PREVIOUS_JUDGE_ALLOCATIONS, previousJudgeAllocations);
        verify(asylumCase).write(AsylumCaseFieldDefinition.ALLOCATED_JUDGE, allocatedJudgeNr2);
        verify(asylumCase).write(AsylumCaseFieldDefinition.JUDGE_ALLOCATION_EXISTS, YesOrNo.YES);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordAllocatedJudgeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordAllocatedJudgeHandler.canHandle(callbackStage, callback);

                if (event == Event.RECORD_ALLOCATED_JUDGE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordAllocatedJudgeHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAllocatedJudgeHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAllocatedJudgeHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAllocatedJudgeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
