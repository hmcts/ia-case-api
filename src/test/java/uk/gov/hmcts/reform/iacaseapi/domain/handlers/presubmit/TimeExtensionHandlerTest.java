package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.TimeExtensionAppender;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class TimeExtensionHandlerTest {

    @Mock private TimeExtensionAppender timeExtensionAppender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor ArgumentCaptor<List<IdValue<TimeExtension>>> existingTimeExtensionsCaptor;

    TimeExtensionHandler timeExtensionHandler;

    @BeforeEach
    void setUp() {

        timeExtensionHandler =
            new TimeExtensionHandler(timeExtensionAppender);
    }

    @Test
    void should_append_new_timeExtension_to_existing_timeExtensions_for_the_case() {

        final List<IdValue<TimeExtension>> existingTimeExtensions = new ArrayList<>();
        final List<IdValue<TimeExtension>> allTimeExtensions = new ArrayList<>();

        final String newTimeExtensionReason = "I requested more time because..";
        List<IdValue<Document>> newTimeExtensionEvidence = Collections.emptyList();

        final Event event = Event.SUBMIT_TIME_EXTENSION;
        final State caseState = State.AWAITING_REASONS_FOR_APPEAL;

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getState()).thenReturn(caseState);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(TIME_EXTENSIONS)).thenReturn(Optional.of(existingTimeExtensions));
        when(asylumCase.read(SUBMIT_TIME_EXTENSION_REASON, String.class)).thenReturn(Optional.of(newTimeExtensionReason));
        when(asylumCase.read(SUBMIT_TIME_EXTENSION_EVIDENCE)).thenReturn(Optional.of(newTimeExtensionEvidence));

        when(timeExtensionAppender.append(
            existingTimeExtensions,
            caseState,
            newTimeExtensionReason,
            newTimeExtensionEvidence
        )).thenReturn(allTimeExtensions);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            timeExtensionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(SUBMIT_TIME_EXTENSION_REASON, String.class);
        verify(asylumCase, times(1)).read(SUBMIT_TIME_EXTENSION_EVIDENCE);

        verify(timeExtensionAppender, times(1)).append(
            existingTimeExtensions,
            caseState,
            newTimeExtensionReason,
            newTimeExtensionEvidence
        );

        verify(asylumCase, times(1)).write(TIME_EXTENSIONS, allTimeExtensions);

        verify(asylumCase, times(1)).clear(SUBMIT_TIME_EXTENSION_REASON);
        verify(asylumCase, times(1)).clear(SUBMIT_TIME_EXTENSION_EVIDENCE);
    }

    @Test
    void should_throw_when_submit_timeExtension_reason_is_not_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_TIME_EXTENSION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(SUBMIT_TIME_EXTENSION_REASON, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeExtensionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("timeExtensionReason is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> timeExtensionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> timeExtensionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = timeExtensionHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    &&
                    Arrays.asList(
                        Event.SUBMIT_TIME_EXTENSION
                    ).contains(event)) {

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

        assertThatThrownBy(() -> timeExtensionHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> timeExtensionHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> timeExtensionHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> timeExtensionHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
