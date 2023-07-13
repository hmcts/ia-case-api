package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class WitnessesMidEventHandlerTest {

    private static final String IS_WITNESSES_ATTENDING = "isWitnessesAttending";
    private static final String WITNESSES_NUMBER_EXCEEDED_ERROR = "Maximum number of witnesses is 10";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private WitnessDetails witnessDetails;

    private WitnessesMidEventHandler witnessesMidEventHandler;

    @BeforeEach
    public void setup() {
        witnessesMidEventHandler = new WitnessesMidEventHandler();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
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
