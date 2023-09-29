package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PartyFlagIdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class RemoveCaseFlagHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private RemoveCaseFlagHandler removeCaseFlagHandler;
    private final String interpreterPartyId1 = "interpreterPartyId1";
    private final String interpreterName1 = "interpreterName1";
    private final String interpreterFamilyName1 = "interpreterFamilyName1";
    private final String interpreterPartyId2 = "interpreterPartyId2";
    private final String interpreterName2 = "interpreterName2";
    private final String interpreterFamilyName2 = "interpreterFamilyName2";

    private StrategicCaseFlag interpreterFlag1;
    private StrategicCaseFlag interpreterFlag2;
    @Captor
    private ArgumentCaptor<List<PartyFlagIdValue>> interpreterFlagsCaptor;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPDATE_HEARING_REQUIREMENTS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        final String fullName1 = interpreterName1 + " " + interpreterFamilyName1;
        final String fullName2 = interpreterName2 + " " + interpreterFamilyName2;
        interpreterFlag1 = new StrategicCaseFlag(
                fullName1, StrategicCaseFlag.ROLE_ON_CASE_INTERPRETER, Collections.emptyList());
        interpreterFlag2 = new StrategicCaseFlag(
                fullName2, StrategicCaseFlag.ROLE_ON_CASE_INTERPRETER, Collections.emptyList());

        removeCaseFlagHandler = new RemoveCaseFlagHandler();

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_not_remove_any_interpreter_flags(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(INTERPRETER_LEVEL_FLAGS)).thenReturn(Optional.of(List.of(
                new PartyFlagIdValue(interpreterPartyId1, interpreterFlag1), new PartyFlagIdValue(interpreterPartyId2, interpreterFlag2))));
        when(asylumCase.read(INTERPRETER_DETAILS))
                .thenReturn(Optional.of(List.of(
                        new IdValue<>(interpreterName1, new InterpreterDetails(interpreterPartyId1, "bookingRef1",
                                interpreterName1, interpreterFamilyName1, "0771222222", "test1@email.com", "")),
                        new IdValue<>(interpreterName2, new InterpreterDetails(interpreterPartyId2, "bookingRef2",
                                interpreterName2, interpreterFamilyName2, "0771222233", "test2@email.com", ""))
                )));

        final List<PartyFlagIdValue> expected = List.of(
                new PartyFlagIdValue(interpreterPartyId1, interpreterFlag1), new PartyFlagIdValue(interpreterPartyId2, interpreterFlag2));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                removeCaseFlagHandler.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(INTERPRETER_LEVEL_FLAGS), interpreterFlagsCaptor.capture());
        assertNotNull(interpreterFlagsCaptor.getValue());
        assertEquals(2, interpreterFlagsCaptor.getValue().size());
        assertTrue(interpreterFlagsCaptor.getValue().contains(expected.get(0)));
        assertTrue(interpreterFlagsCaptor.getValue().contains(expected.get(1)));

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_remove_one_interpreter_flag(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(INTERPRETER_LEVEL_FLAGS)).thenReturn(Optional.of(List.of(
                new PartyFlagIdValue(interpreterPartyId1, interpreterFlag1), new PartyFlagIdValue(interpreterPartyId2, interpreterFlag2))));
        when(asylumCase.read(INTERPRETER_DETAILS))
                .thenReturn(Optional.of(List.of(
                        new IdValue<>(interpreterName2, new InterpreterDetails(interpreterPartyId2, "bookingRef2",
                                interpreterName2, interpreterFamilyName2, "0771222233", "test2@email.com", ""))
                )));

        final List<PartyFlagIdValue> expected = List.of(
                new PartyFlagIdValue(interpreterPartyId2, interpreterFlag2));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                removeCaseFlagHandler.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(INTERPRETER_LEVEL_FLAGS), interpreterFlagsCaptor.capture());
        assertNotNull(interpreterFlagsCaptor.getValue());
        assertEquals(1, interpreterFlagsCaptor.getValue().size());
        assertTrue(interpreterFlagsCaptor.getValue().contains(expected.get(0)));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "UPDATE_HEARING_REQUIREMENTS"
    })
    void should_remove_all_interpreter_flags(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(INTERPRETER_LEVEL_FLAGS)).thenReturn(Optional.of(List.of(
                new PartyFlagIdValue(interpreterPartyId1, interpreterFlag1), new PartyFlagIdValue(interpreterPartyId2, interpreterFlag2))));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                removeCaseFlagHandler.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(INTERPRETER_LEVEL_FLAGS), interpreterFlagsCaptor.capture());
        assertNotNull(interpreterFlagsCaptor.getValue());
        assertEquals(0, interpreterFlagsCaptor.getValue().size());

        verify(asylumCase, times(0)).write(eq(WITNESS_LEVEL_FLAGS), any());
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = removeCaseFlagHandler.canHandle(callbackStage, callback);

                if (event == UPDATE_HEARING_REQUIREMENTS
                        && callbackStage == ABOUT_TO_START) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> removeCaseFlagHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> removeCaseFlagHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

}
