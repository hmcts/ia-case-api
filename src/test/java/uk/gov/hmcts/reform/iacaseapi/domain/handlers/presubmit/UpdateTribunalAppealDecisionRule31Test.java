package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.UpdateTribunalRules.UNDER_RULE_31;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UpdateTribunalRules;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import java.util.Optional;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateTribunalAppealDecisionRule31Test {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;

    private UpdateTribunalAppealDecisionRule31 updateTribunalAppealDecisionRule31;

    @BeforeEach
    public void setUp() {
        updateTribunalAppealDecisionRule31 = new UpdateTribunalAppealDecisionRule31(
            dateProvider);

        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class))
                .thenReturn(Optional.of(UNDER_RULE_31));
    }

    @Test
    void should_write_updated_appeal_decision_dismissed() {
        final DynamicList dynamicList = new DynamicList(
                new Value("dismissed", "Yes, change decision to Dismissed"),
                newArrayList()
        );

        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicList));

        LocalDate currentDate = LocalDate.now();
        when(dateProvider.now()).thenReturn(currentDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            updateTribunalAppealDecisionRule31.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(UPDATED_APPEAL_DECISION, "Dismissed");
        verify(asylumCase, times(1)).write(UPDATE_TRIBUNAL_DECISION_DATE, currentDate.toString());
    }

    @Test
    void should_write_updated_appeal_decision_allowed() {
        final DynamicList dynamicList = new DynamicList(
                new Value("allowed", "Yes, change decision to Allowed"),
                newArrayList()
        );
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.of(dynamicList));

        LocalDate currentDate = LocalDate.now();
        when(dateProvider.now()).thenReturn(currentDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalAppealDecisionRule31.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(UPDATED_APPEAL_DECISION, "Allowed");
        verify(asylumCase, times(1)).write(UPDATE_TRIBUNAL_DECISION_DATE, currentDate.toString());
    }

    @Test
    void should_throw_on_missing_types_of_update_tribunal_decision() {
        when(asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateTribunalAppealDecisionRule31.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("typesOfUpdateTribunalDecision is not present");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateTribunalAppealDecisionRule31.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = updateTribunalAppealDecisionRule31.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_TRIBUNAL_DECISION
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

        assertThatThrownBy(() -> updateTribunalAppealDecisionRule31.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalAppealDecisionRule31.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalAppealDecisionRule31.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalAppealDecisionRule31.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
