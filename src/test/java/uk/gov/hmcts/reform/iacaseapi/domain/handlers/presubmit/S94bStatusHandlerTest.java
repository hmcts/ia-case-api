package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.DC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.RP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_S94B_STATUS_UPDATABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.S94B_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL_AFTER_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_S94B_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class S94bStatusHandlerTest {

    private final static String ERROR = "\'Update s94b status\' not available for this appeal type";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private S94bStatusHandler s94bStatusHandler;

    @BeforeEach
    void setUp() {
        s94bStatusHandler = new S94bStatusHandler();
    }

    private static Stream<Arguments> scenariosForEventEnablingFlagToBeSetToYes() {
        return Stream.of(
            Arguments.of(START_APPEAL, HU, YES, Optional.empty()),
            Arguments.of(EDIT_APPEAL, HU, YES, Optional.empty()),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, HU, YES, Optional.empty()),
            Arguments.of(START_APPEAL, HU, NO, Optional.empty()),
            Arguments.of(EDIT_APPEAL, HU, NO, Optional.empty()),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, HU, NO, Optional.empty()),
            Arguments.of(START_APPEAL, EA, NO, Optional.empty()),
            Arguments.of(EDIT_APPEAL, EA, NO, Optional.empty()),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, EA, NO, Optional.empty()),
            Arguments.of(START_APPEAL, HU, YES, Optional.of(YES)),
            Arguments.of(EDIT_APPEAL, HU, YES, Optional.of(YES)),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, HU, YES, Optional.of(YES)),
            Arguments.of(START_APPEAL, HU, NO, Optional.of(YES)),
            Arguments.of(EDIT_APPEAL, HU, NO, Optional.of(YES)),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, HU, NO, Optional.of(YES)),
            Arguments.of(START_APPEAL, EA, NO, Optional.of(YES)),
            Arguments.of(EDIT_APPEAL, EA, NO, Optional.of(YES)),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, EA, NO, Optional.of(YES))
        );
    }

    private static Stream<Arguments> scenariosForEventEnablingFlagToBeSetToNo() {
        return Stream.of(
            Arguments.of(START_APPEAL, EA, YES),
            Arguments.of(EDIT_APPEAL, EA, YES),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, EA, YES),

            Arguments.of(START_APPEAL, EU, YES),
            Arguments.of(EDIT_APPEAL, EU, YES),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, EU, YES),
            Arguments.of(START_APPEAL, EU, NO),
            Arguments.of(EDIT_APPEAL, EU, NO),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, EU, NO),
            Arguments.of(START_APPEAL, PA, YES),
            Arguments.of(EDIT_APPEAL, PA, YES),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, PA, YES),
            Arguments.of(START_APPEAL, PA, NO),
            Arguments.of(EDIT_APPEAL, PA, NO),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, PA, NO),
            Arguments.of(START_APPEAL, DC, YES),
            Arguments.of(EDIT_APPEAL, DC, YES),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, DC, YES),
            Arguments.of(START_APPEAL, DC, NO),
            Arguments.of(EDIT_APPEAL, DC, NO),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, DC, NO),
            Arguments.of(START_APPEAL, RP, YES),
            Arguments.of(EDIT_APPEAL, RP, YES),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, RP, YES),
            Arguments.of(START_APPEAL, RP, NO),
            Arguments.of(EDIT_APPEAL, RP, NO),
            Arguments.of(EDIT_APPEAL_AFTER_SUBMIT, RP, NO)
        );
    }

    @ParameterizedTest
    @MethodSource("scenariosForEventEnablingFlagToBeSetToYes")
    void should_set_event_enabling_flag_to_yes_and_populate_field(
        Event event,
        AppealType appealType,
        YesOrNo inUk,
        Optional<YesOrNo> s94bStatusOptional
    ) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(inUk));
        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(s94bStatusOptional);

        s94bStatusHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(IS_S94B_STATUS_UPDATABLE, YES);
        if (s94bStatusOptional.isEmpty()) {
            verify(asylumCase, times(1)).write(S94B_STATUS, NO);
        } else {
            verify(asylumCase, never()).write(eq(S94B_STATUS), any(YesOrNo.class));
        }
    }

    @ParameterizedTest
    @MethodSource("scenariosForEventEnablingFlagToBeSetToNo")
    void should_set_event_enabling_flag_to_no(Event event, AppealType appealType, YesOrNo inUk) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(inUk));

        s94bStatusHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(IS_S94B_STATUS_UPDATABLE, NO);
    }

    @Test
    void should_add_error_if_scenario_does_not_qualify_for_event() {
        when(callback.getEvent()).thenReturn(UPDATE_S94B_STATUS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(EA));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YES));

        PreSubmitCallbackResponse<AsylumCase> response =
            s94bStatusHandler.handle(ABOUT_TO_START, callback);

        assertTrue(response.getErrors().contains(ERROR));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> s94bStatusHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = s94bStatusHandler.canHandle(callbackStage, callback);

                if ((callbackStage == ABOUT_TO_SUBMIT
                    && Set.of(START_APPEAL, EDIT_APPEAL, EDIT_APPEAL_AFTER_SUBMIT).contains(callback.getEvent()))
                    ||
                    callbackStage == ABOUT_TO_START
                    && callback.getEvent() == UPDATE_S94B_STATUS) {
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

        assertThatThrownBy(() -> s94bStatusHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> s94bStatusHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
