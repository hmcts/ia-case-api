package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DecisionAndReasonsStartedConfirmationTest {

    public static final String CONFIRMATION_HEADER_TEXT = "# You have started the decision and reasons process";
    public static final String COMPLETE_DECISION_TEXT =
        "The judge can now download and complete the decision and reasons document.";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private LocationBasedFeatureToggler locationBasedFeatureToggler;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails caseDetails;
    @InjectMocks
    private DecisionAndReasonsStartedConfirmation decisionAndReasonsStartedConfirmation;


    @Test
    void should_return_confirmation_if_auto_hearing_request_disabled() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase))
            .thenReturn(NO);
        when(callback.getEvent()).thenReturn(Event.DECISION_AND_REASONS_STARTED);

        PostSubmitCallbackResponse callbackResponse =
            decisionAndReasonsStartedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains(CONFIRMATION_HEADER_TEXT);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(COMPLETE_DECISION_TEXT);
    }

    @Test
    void should_return_confirmation_if_auto_hearing_request_enabled_and_hearing_created_successfully() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(1L);
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase))
            .thenReturn(YES);
        when(callback.getEvent()).thenReturn(Event.DECISION_AND_REASONS_STARTED);
        when(asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(NO));

        PostSubmitCallbackResponse callbackResponse =
            decisionAndReasonsStartedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains(CONFIRMATION_HEADER_TEXT);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The hearing request has been created and is visible on the [Hearings tab]"
                + "(/cases/case-details/1/hearings)"
                + "\n\n"
                + COMPLETE_DECISION_TEXT);
    }

    @Test
    void should_return_confirmation_if_auto_hearing_request_enabled_and_failed_to_create_hearing() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(1L);
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase))
            .thenReturn(YES);
        when(callback.getEvent()).thenReturn(Event.DECISION_AND_REASONS_STARTED);
        when(asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YES));

        PostSubmitCallbackResponse callbackResponse =
            decisionAndReasonsStartedConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains(CONFIRMATION_HEADER_TEXT);

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The hearing could not be auto-requested. Please manually request the "
                + "hearing via the [Hearings tab](/cases/case-details/1/hearings)"
                + "\n\n"
                + COMPLETE_DECISION_TEXT);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> decisionAndReasonsStartedConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = decisionAndReasonsStartedConfirmation.canHandle(callback);

            if (event == Event.DECISION_AND_REASONS_STARTED) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> decisionAndReasonsStartedConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decisionAndReasonsStartedConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
