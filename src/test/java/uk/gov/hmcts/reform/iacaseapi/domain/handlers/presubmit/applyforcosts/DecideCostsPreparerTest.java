package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECIDE_COSTS_APPLICATION_LIST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ApplyForCostsProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DecideCostsPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private ApplyForCostsProvider applyForCostsProvider;
    private DecideCostsPreparer decideCostsPreparer;

    @BeforeEach
    public void setUp() {
        decideCostsPreparer = new DecideCostsPreparer(applyForCostsProvider);
        when(callback.getEvent()).thenReturn(Event.DECIDE_COSTS_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_write_dynamic_list_to_the_field() {
        List<Value> applyForCostsList = new ArrayList<>();
        applyForCostsList.add(new Value("1", "Costs 1, Wasted costs, 13 Nov 2023"));
        applyForCostsList.add(new Value("2", "Costs 2, Unreasonable costs, 10 Nov 2023"));
        when(applyForCostsProvider.getApplyForCostsForJudgeDecision(asylumCase)).thenReturn(applyForCostsList);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideCostsPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        verify(asylumCase, times(1)).write(DECIDE_COSTS_APPLICATION_LIST, new DynamicList(applyForCostsList.get(0), applyForCostsList));
    }

    @Test
    void should_add_error_if_dynamic_list_is_empty() {
        when(applyForCostsProvider.getApplyForCostsForJudgeDecision(asylumCase)).thenReturn(Collections.emptyList());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideCostsPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).containsExactly("You do not have any cost applications to decide.");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(
            () -> decideCostsPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = decideCostsPreparer.canHandle(callbackStage, callback);

                if (event == Event.DECIDE_COSTS_APPLICATION
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {
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
        assertThatThrownBy(() -> decideCostsPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideCostsPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}