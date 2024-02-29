package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision.ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import java.util.List;
import java.util.Optional;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UpdateTribunalDecisionRule31MidEventTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Captor
    private ArgumentCaptor<DynamicList> asylumValueCaptor;
    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;
    private UpdateTribunalDecisionRule31MidEvent updateTribunalDecisionRule31MidEvent;
    private String testPage = "tribunalDecisionType";

    @BeforeEach
    public void setUp() {
        updateTribunalDecisionRule31MidEvent = new UpdateTribunalDecisionRule31MidEvent();

        when(callback.getPageId()).thenReturn(testPage);
        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, String.class))
                .thenReturn(Optional.of("underRule31"));
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(testPage);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = updateTribunalDecisionRule31MidEvent.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_TRIBUNAL_DECISION
                    && callbackStage == PreSubmitCallbackStage.MID_EVENT
                    && callback.getPageId().equals("tribunalDecisionType")) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> updateTribunalDecisionRule31MidEvent.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> updateTribunalDecisionRule31MidEvent.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> updateTribunalDecisionRule31MidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalDecisionRule31MidEvent.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "ALLOWED, 'Yes, change decision to Dismissed'",
        "DISMISSED, 'Yes, change decision to Allowed'"
    })
    void handler_should_populate_dynamic_current_decision(AppealDecision decision, String expectedValue) {

        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(callback.getPageId()).thenReturn(testPage);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
                .thenReturn(Optional.of(decision));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                updateTribunalDecisionRule31MidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(1)).write(asylumExtractorCaptor.capture(), asylumValueCaptor.capture());

        DynamicList dynamicList;

        if (decision.equals(ALLOWED)) {
            dynamicList = new DynamicList(new Value("", ""),
                    List.of(new Value("dismissed", expectedValue), new Value("allowed", "No")));
        } else {
            dynamicList = new DynamicList(new Value("", ""),
                    List.of(new Value("allowed", expectedValue), new Value("dismissed", "No")));
        }

        assertNotNull(callbackResponse);
        assertThat(asylumExtractorCaptor.getValue()).isEqualTo(TYPES_OF_UPDATE_TRIBUNAL_DECISION);
        assertThat(asylumValueCaptor.getValue()).isEqualTo(dynamicList);
    }
}