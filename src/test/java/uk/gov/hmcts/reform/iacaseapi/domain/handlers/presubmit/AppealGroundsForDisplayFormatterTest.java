package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealGroundsForDisplayFormatterTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    AppealGroundsForDisplayFormatter appealGroundsForDisplayFormatter =
        new AppealGroundsForDisplayFormatter();

    @ParameterizedTest
    @MethodSource("appealTypesArguments")
    void should_format_appeal_grounds_for_display(AppealType appealType,
                                                  AsylumCaseFieldDefinition asylumCaseFieldDefinition,
                                                  CheckValues<String> appealGrounds,
                                                  List<String> expectedAppealGrounds) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);

        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(asylumCaseFieldDefinition)).thenReturn(Optional.of(appealGrounds));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealGroundsForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(APPEAL_GROUNDS_FOR_DISPLAY, expectedAppealGrounds);
    }


    private static Stream<Arguments> appealTypesArguments() {
        final CheckValues<String> appealGrounds1 =
            new CheckValues<>(Collections.singletonList(
                "ground1"
            ));

        final CheckValues<String> appealGrounds2 =
            new CheckValues<>(Arrays.asList(
                "ground1",
                "ground2"
            ));

        final List<String> expectedAppealGrounds1 =
            Arrays.asList(
                "ground1"
            );

        final List<String> expectedAppealGrounds2 =
            Arrays.asList(
                "ground1",
                "ground2"
            );

        return Stream.of(
            Arguments.of(AppealType.DC, APPEAL_GROUNDS_DEPRIVATION_HUMAN_RIGHTS, appealGrounds2, expectedAppealGrounds2),
            Arguments.of(AppealType.EA, APPEAL_GROUNDS_EU_REFUSAL, appealGrounds1, expectedAppealGrounds1),
            Arguments.of(AppealType.HU, APPEAL_GROUNDS_HUMAN_RIGHTS_REFUSAL, appealGrounds1, expectedAppealGrounds1),
            Arguments.of(AppealType.PA, APPEAL_GROUNDS_PROTECTION, appealGrounds2, expectedAppealGrounds2),
            Arguments.of(AppealType.RP, APPEAL_GROUNDS_REVOCATION, appealGrounds1, expectedAppealGrounds1)
        );
    }

    @Test
    void should_set_empty_grounds_if_ground_values_are_not_present() {

        final List<String> expectedAppealGrounds = Collections.emptyList();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealGroundsForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(APPEAL_GROUNDS_FOR_DISPLAY, expectedAppealGrounds);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appealGroundsForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealGroundsForDisplayFormatter.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL)) {
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

        assertThatThrownBy(() -> appealGroundsForDisplayFormatter.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealGroundsForDisplayFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealGroundsForDisplayFormatter.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealGroundsForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
