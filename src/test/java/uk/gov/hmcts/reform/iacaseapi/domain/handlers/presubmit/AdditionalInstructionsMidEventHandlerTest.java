package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_INSTRUCTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_INSTRUCTIONS_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADDITIONAL_INSTRUCTION_ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_ADJUSTMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AdditionalInstructionsMidEventHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private AdditionalInstructionsMidEventHandler additionalInstructionsMidEventHandler;

    @BeforeEach
    void setUp() {
        additionalInstructionsMidEventHandler = new AdditionalInstructionsMidEventHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_clear_additional_instructions_description() {
        when(callback.getEvent()).thenReturn(LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);
        when(callback.getPageId()).thenReturn("additionalInstructions");
        when(asylumCase.read(ADDITIONAL_INSTRUCTIONS, YesOrNo.class)).thenReturn(Optional.of(NO));

        PreSubmitCallbackResponse<AsylumCase> response =
            additionalInstructionsMidEventHandler.handle(MID_EVENT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).clear(ADDITIONAL_INSTRUCTIONS_DESCRIPTION);
    }

    @ParameterizedTest
    @CsvSource({
        "REVIEW_HEARING_REQUIREMENTS, adjustAdditionalInstructionsRequirements",
        "UPDATE_HEARING_REQUIREMENTS, additionalInstructions",
        "UPDATE_HEARING_ADJUSTMENTS, additionalInstructions"
    })
    void should_clear_additional_instructions_tribunal_response(Event event, String pageId) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(pageId);
        when(asylumCase.read(IS_ADDITIONAL_INSTRUCTION_ALLOWED, YesOrNo.class)).thenReturn(Optional.of(NO));

        PreSubmitCallbackResponse<AsylumCase> response =
            additionalInstructionsMidEventHandler.handle(MID_EVENT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).clear(ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE);
    }

    @Test
    void should_not_clear_additional_instructions_description() {
        when(callback.getEvent()).thenReturn(LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);
        when(callback.getPageId()).thenReturn("additionalInstructions");
        when(asylumCase.read(ADDITIONAL_INSTRUCTIONS, YesOrNo.class)).thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> response =
            additionalInstructionsMidEventHandler.handle(MID_EVENT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, never()).clear(ADDITIONAL_INSTRUCTIONS_DESCRIPTION);
    }

    @ParameterizedTest
    @CsvSource({
        "REVIEW_HEARING_REQUIREMENTS, adjustAdditionalInstructionsRequirements",
        "UPDATE_HEARING_REQUIREMENTS, additionalInstructions",
        "UPDATE_HEARING_ADJUSTMENTS, additionalInstructions"
    })
    void should_not_clear_additional_instructions_tribunal_response(Event event, String pageId) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(pageId);
        when(asylumCase.read(IS_ADDITIONAL_INSTRUCTION_ALLOWED, YesOrNo.class)).thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> response =
            additionalInstructionsMidEventHandler.handle(MID_EVENT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, never()).clear(ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void it_can_handle_callback(Event event) {

        List<Event> targetEvents = List.of(
            REVIEW_HEARING_REQUIREMENTS,
            UPDATE_HEARING_REQUIREMENTS,
            UPDATE_HEARING_ADJUSTMENTS,
            LIST_CASE_WITHOUT_HEARING_REQUIREMENTS
        );

        for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn("additionalInstructions");
            boolean canHandle = additionalInstructionsMidEventHandler.canHandle(stage, callback);

            if (stage == MID_EVENT && targetEvents.contains(event)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_ADJUSTMENTS",
        "LIST_CASE_WITHOUT_HEARING_REQUIREMENTS"
    })
    void should_not_allow_null_arguments(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> additionalInstructionsMidEventHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> additionalInstructionsMidEventHandler.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> additionalInstructionsMidEventHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> additionalInstructionsMidEventHandler.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_REQUIREMENTS",
        "UPDATE_HEARING_ADJUSTMENTS",
        "LIST_CASE_WITHOUT_HEARING_REQUIREMENTS"
    })
    void handling_should_throw_if_cannot_actually_handle(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> additionalInstructionsMidEventHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> additionalInstructionsMidEventHandler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
