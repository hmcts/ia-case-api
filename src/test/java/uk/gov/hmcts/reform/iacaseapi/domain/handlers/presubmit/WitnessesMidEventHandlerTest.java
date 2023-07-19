package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DRAFT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicMultiSelectList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class WitnessesMidEventHandlerTest {

    private static final String IS_WITNESSES_ATTENDING = "isWitnessesAttending";
    private static final String IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID = "isAnyWitnessInterpreterRequired";
    private static final String WITNESSES_NUMBER_EXCEEDED_ERROR = "Maximum number of witnesses is 10";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private WitnessDetails witnessDetails;
    private String witnessName = "name";
    private String witnessFamilyName = "lastName";

    private WitnessesMidEventHandler witnessesMidEventHandler;

    @BeforeEach
    public void setup() {
        witnessesMidEventHandler = new WitnessesMidEventHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(witnessDetails.getWitnessName()).thenReturn(witnessName);
        when(witnessDetails.getWitnessFamilyName()).thenReturn(witnessFamilyName);
        when(callback.getPageId()).thenReturn(IS_WITNESSES_ATTENDING);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "DRAFT_HEARING_REQUIREMENTS", "UPDATE_HEARING_REQUIREMENTS"})
    void should_add_error_when_witnesses_more_than_ten(Event event) {
        List<WitnessDetails> elevenWitnesses = Collections.nCopies(11, witnessDetails);

        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(elevenWitnesses));

        PreSubmitCallbackResponse<AsylumCase> response = witnessesMidEventHandler.handle(MID_EVENT, callback);

        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains(WITNESSES_NUMBER_EXCEEDED_ERROR));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "DRAFT_HEARING_REQUIREMENTS", "UPDATE_HEARING_REQUIREMENTS"})
    void should_not_add_error_when_witnesses_are_ten_or_less(Event event) {
        List<WitnessDetails> elevenWitnesses = Collections.nCopies(10, witnessDetails);

        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(elevenWitnesses));

        PreSubmitCallbackResponse<AsylumCase> response = witnessesMidEventHandler.handle(MID_EVENT, callback);

        assertTrue(response.getErrors().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "DRAFT_HEARING_REQUIREMENTS", "UPDATE_HEARING_REQUIREMENTS"})
    void should_fill_witness_list_elements(Event event) {
        List<IdValue<WitnessDetails>> witnesses = Collections
            .nCopies(10, new IdValue<>("1", witnessDetails));

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(IS_ANY_WITNESS_INTERPRETER_REQUIRED_PAGE_ID);
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnesses));

        PreSubmitCallbackResponse<AsylumCase> response = witnessesMidEventHandler.handle(MID_EVENT, callback);

        DynamicMultiSelectList dynamicMultiSelectListEmpty = new DynamicMultiSelectList();
        DynamicMultiSelectList dynamicMultiSelectList = new DynamicMultiSelectList(Collections.emptyList(),
            List.of(new Value("name lastName", "name lastName"))
        );

        verify(asylumCase, times(2)).write(eq(WITNESS_1), any(WitnessDetails.class));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_1), eq(dynamicMultiSelectListEmpty));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_1), eq(dynamicMultiSelectList));
        verify(asylumCase, times(2)).write(eq(WITNESS_2), any(WitnessDetails.class));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_2), eq(dynamicMultiSelectListEmpty));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_2), eq(dynamicMultiSelectList));
        verify(asylumCase, times(2)).write(eq(WITNESS_3), any(WitnessDetails.class));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_3), eq(dynamicMultiSelectListEmpty));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_3), eq(dynamicMultiSelectList));
        verify(asylumCase, times(2)).write(eq(WITNESS_4), any(WitnessDetails.class));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_4), eq(dynamicMultiSelectListEmpty));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_4), eq(dynamicMultiSelectList));
        verify(asylumCase, times(2)).write(eq(WITNESS_5), any(WitnessDetails.class));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_5), eq(dynamicMultiSelectListEmpty));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_5), eq(dynamicMultiSelectList));
        verify(asylumCase, times(2)).write(eq(WITNESS_6), any(WitnessDetails.class));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_6), eq(dynamicMultiSelectListEmpty));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_6), eq(dynamicMultiSelectList));
        verify(asylumCase, times(2)).write(eq(WITNESS_7), any(WitnessDetails.class));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_7), eq(dynamicMultiSelectListEmpty));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_7), eq(dynamicMultiSelectList));
        verify(asylumCase, times(2)).write(eq(WITNESS_8), any(WitnessDetails.class));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_8), eq(dynamicMultiSelectListEmpty));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_8), eq(dynamicMultiSelectList));
        verify(asylumCase, times(2)).write(eq(WITNESS_9), any(WitnessDetails.class));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_9), eq(dynamicMultiSelectListEmpty));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_9), eq(dynamicMultiSelectList));
        verify(asylumCase, times(2)).write(eq(WITNESS_10), any(WitnessDetails.class));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_10), eq(dynamicMultiSelectListEmpty));
        verify(asylumCase, times(1)).write(eq(WITNESS_LIST_ELEMENT_10), eq(dynamicMultiSelectList));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> witnessesMidEventHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(SEND_DIRECTION);
        assertThatThrownBy(
            () -> witnessesMidEventHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(IS_WITNESSES_ATTENDING);

            for (PreSubmitCallbackStage callbackStage : values()) {

                boolean canHandle = witnessesMidEventHandler.canHandle(callbackStage, callback);

                if (Set.of(DRAFT_HEARING_REQUIREMENTS, UPDATE_HEARING_REQUIREMENTS).contains(event)
                    && callbackStage == MID_EVENT
                    && callback.getPageId().equals(IS_WITNESSES_ATTENDING)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}
