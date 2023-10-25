package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TypesOfAppliedCosts.UNREASONABLE_COSTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TypesOfAppliedCosts.WASTED_COSTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplyForCostsMidEventTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private UserDetails userDetails;

    @Captor
    private ArgumentCaptor<Object> appliesForCostsTypesCaptor;
    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private String applyForCostsType1 = "UNREASONABLE_COSTS";
    private String applyForCostsType2 = "WASTED_COSTS";

    private ApplyForCostsMidEvent applyForCostsMidEvent;

    @BeforeEach
    public void setUp() {
        applyForCostsMidEvent =
                new ApplyForCostsMidEvent(userDetails);
    }

    @Test
    void should_set_types_of_applied_costs() {
        List<Value> values = new ArrayList<>();
        values.add(new Value(UNREASONABLE_COSTS.name(), UNREASONABLE_COSTS.toString()));
        values.add(new Value(WASTED_COSTS.name(), WASTED_COSTS.toString()));
        DynamicList typesOfAppliedCosts = new DynamicList(new Value("", ""), values);

        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(userDetails.getRoles()).thenReturn(List.of("caseworker-ia-legalrep-solicitor"));
        when(asylumCase.read(AsylumCaseFieldDefinition.APPLIED_COSTS_TYPES, DynamicList.class))
                .thenReturn(Optional.of(typesOfAppliedCosts));

        applyForCostsMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(1)).write(asylumExtractorCaptor.capture(), appliesForCostsTypesCaptor.capture());
        DynamicList exactAppliesForCostsTypes = (DynamicList) appliesForCostsTypesCaptor.getAllValues().get(0);
        assertEquals(applyForCostsType1, exactAppliesForCostsTypes.getListItems().get(0).getCode());
        assertEquals(applyForCostsType2, exactAppliesForCostsTypes.getListItems().get(1).getCode());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> applyForCostsMidEvent.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> applyForCostsMidEvent.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> applyForCostsMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = applyForCostsMidEvent.canHandle(callbackStage, callback);

                if (event == Event.APPLY_FOR_COSTS
                        && callbackStage == PreSubmitCallbackStage.MID_EVENT) {

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
        assertThatThrownBy(() -> applyForCostsMidEvent.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsMidEvent.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

}