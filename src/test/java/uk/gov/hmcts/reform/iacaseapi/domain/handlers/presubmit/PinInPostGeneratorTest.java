package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PIN_IN_POST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_AIP_TRANSFER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PinInPostGeneratorTest {

    private static final long ACCESS_CODE_EXPIRY_DAYS = 90;

    private PinInPostGenerator pinInPostGenerator;

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Captor
    private ArgumentCaptor<PinInPostDetails> pipDetailsCaptor;


    @BeforeEach
    void setUp() throws Exception {
        pinInPostGenerator = new PinInPostGenerator(ACCESS_CODE_EXPIRY_DAYS);
    }

    static Stream<Arguments> scenarios() {
        return Stream.of(
            Arguments.of(Event.REMOVE_REPRESENTATION, JourneyType.AIP, 0),
            Arguments.of(Event.GENERATE_PIN_IN_POST, JourneyType.AIP, 1),
            Arguments.of(Event.REMOVE_REPRESENTATION, JourneyType.REP, 0),
            Arguments.of(Event.GENERATE_PIN_IN_POST, JourneyType.REP, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void appellantPinInPost_is_generated_and_aip_transfer_set_appropriately(Event event, JourneyType journeyType, int expectedAipTransferWrites) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));

        pinInPostGenerator.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        verify(asylumCase).write(eq(APPELLANT_PIN_IN_POST), pipDetailsCaptor.capture());
        PinInPostDetails details = pipDetailsCaptor.getValue();
        assertNotNull(details.getAccessCode());
        assertEquals(12, details.getAccessCode().length());
        assertEquals(LocalDate.now().plusDays(ACCESS_CODE_EXPIRY_DAYS).toString(), details.getExpiryDate());
        assertEquals(YesOrNo.NO, details.getPinUsed());

        verify(asylumCase, times(expectedAipTransferWrites)).write(IS_AIP_TRANSFER, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"REMOVE_REPRESENTATION", "REMOVE_LEGAL_REPRESENTATIVE", "GENERATE_PIN_IN_POST"})
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertTrue(pinInPostGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }


    @ParameterizedTest
    @EnumSource(value = Event.class, mode = EnumSource.Mode.EXCLUDE,
        names = {"REMOVE_REPRESENTATION", "REMOVE_LEGAL_REPRESENTATIVE", "GENERATE_PIN_IN_POST"})
    void it_cannot_handle_callback_invalid_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(pinInPostGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, mode = EnumSource.Mode.EXCLUDE, names = {"ABOUT_TO_SUBMIT"})
    void it_cannot_handle_callback_invalid_callbackStage(PreSubmitCallbackStage callbackStage) {
        when(callback.getEvent()).thenReturn(Event.REMOVE_REPRESENTATION);
        assertFalse(pinInPostGenerator.canHandle(callbackStage, callback));
    }

    @Test
    void should_throw_exception_when_cannot_handle() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> pinInPostGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
        assertEquals("Cannot handle callback", exception.getMessage());
    }
}
