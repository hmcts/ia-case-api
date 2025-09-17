package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JUDGE_APPLIED_COSTS_TYPES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TypesOfAppliedCosts.*;

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
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ConsiderMakingCostsOrderPreparerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private UserDetailsHelper userDetailsHelper;
    @Mock
    private UserDetails userDetails;
    @Captor
    private ArgumentCaptor<Object> appliesForCostsTypesCaptor;
    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private String applyForCostsType1 = "TRIBUNAL_COSTS";
    private String applyForCostsType2 = "UNREASONABLE_COSTS";
    private String applyForCostsType3 = "WASTED_COSTS";

    private ConsiderMakingCostsOrderPreparer considerMakingCostsOrderPreparer;

    @BeforeEach
    public void setUp() {
        considerMakingCostsOrderPreparer =
            new ConsiderMakingCostsOrderPreparer(userDetailsHelper, userDetails);
    }

    @Test
    void should_set_types_of_applied_costs() {
        List<Value> values = new ArrayList<>();
        values.add(new Value(TRIBUNAL_COSTS.name(), TRIBUNAL_COSTS.toString()));
        values.add(new Value(UNREASONABLE_COSTS.name(), UNREASONABLE_COSTS.toString()));
        values.add(new Value(WASTED_COSTS.name(), WASTED_COSTS.toString()));
        DynamicList typesOfAppliedCosts = new DynamicList(new Value("", ""), values);

        when(callback.getEvent()).thenReturn(Event.CONSIDER_MAKING_COSTS_ORDER);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(userDetails.getRoles()).thenReturn(List.of("caseworker-ia-iacjudge"));
        when(asylumCase.read(JUDGE_APPLIED_COSTS_TYPES, DynamicList.class)).thenReturn(Optional.of(typesOfAppliedCosts));
        when(userDetailsHelper.getLoggedInUserRole(userDetails)).thenReturn(UserRole.JUDGE);

        considerMakingCostsOrderPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(1)).write(asylumExtractorCaptor.capture(), appliesForCostsTypesCaptor.capture());
        DynamicList exactAppliesForCostsTypes = (DynamicList) appliesForCostsTypesCaptor.getAllValues().get(0);
        assertEquals(applyForCostsType1, exactAppliesForCostsTypes.getListItems().get(0).getCode());
        assertEquals(applyForCostsType2, exactAppliesForCostsTypes.getListItems().get(1).getCode());
        assertEquals(applyForCostsType3, exactAppliesForCostsTypes.getListItems().get(2).getCode());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> considerMakingCostsOrderPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> considerMakingCostsOrderPreparer.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> considerMakingCostsOrderPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = considerMakingCostsOrderPreparer.canHandle(callbackStage, callback);

                if (event == Event.CONSIDER_MAKING_COSTS_ORDER
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
        assertThatThrownBy(() -> considerMakingCostsOrderPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> considerMakingCostsOrderPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}