package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_DEPRIVATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_EU_REFUSAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_GROUNDS_REVOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealGroundsForDisplayFormatterTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private final AppealGroundsForDisplayFormatter appealGroundsForDisplayFormatter =
        new AppealGroundsForDisplayFormatter();

    @Test
    void should_format_appeal_grounds_for_display() {

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
                List.of(
                        "ground1"
                );

        final List<String> expectedAppealGrounds2 =
            List.of(
                "ground1",
                "ground2"
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);

        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(APPEAL_GROUNDS_DEPRIVATION)).thenReturn(Optional.of(appealGrounds2));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealGroundsForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(asylumCase).clear(APPEAL_GROUNDS_FOR_DISPLAY);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(APPEAL_GROUNDS_FOR_DISPLAY, expectedAppealGrounds2);

        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(AppealType.PA));
        callbackResponse =
            appealGroundsForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(APPEAL_GROUNDS_FOR_DISPLAY, expectedAppealGrounds2);

        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(APPEAL_GROUNDS_EU_REFUSAL)).thenReturn(Optional.of(appealGrounds1));
        callbackResponse =
            appealGroundsForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(APPEAL_GROUNDS_FOR_DISPLAY, expectedAppealGrounds1);

        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(AppealType.RP));
        when(asylumCase.read(APPEAL_GROUNDS_REVOCATION)).thenReturn(Optional.of(appealGrounds1));
        callbackResponse =
            appealGroundsForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(2)).write(APPEAL_GROUNDS_FOR_DISPLAY, expectedAppealGrounds1);

        when(asylumCase.read(APPEAL_TYPE)).thenReturn(Optional.of(AppealType.HU));
        callbackResponse =
            appealGroundsForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(2)).write(APPEAL_GROUNDS_FOR_DISPLAY, expectedAppealGrounds1);
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

        assertThatThrownBy(
            () -> appealGroundsForDisplayFormatter.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
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

        assertThatThrownBy(
            () -> appealGroundsForDisplayFormatter.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
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
