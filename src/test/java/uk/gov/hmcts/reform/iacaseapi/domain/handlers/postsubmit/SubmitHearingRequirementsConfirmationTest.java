package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SubmitHearingRequirementsConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private final SubmitHearingRequirementsConfirmation submitHearingRequirementsConfirmation =
        new SubmitHearingRequirementsConfirmation();

    @Test
    void should_return_confirmation_appeal_journey() {

        when(callback.getEvent()).thenReturn(Event.DRAFT_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PostSubmitCallbackResponse callbackResponse =
            submitHearingRequirementsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertTrue(callbackResponse.getConfirmationHeader().get().contains("You've submitted your hearing requirements"));

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "The Tribunal will review your hearing requirements and any additional requests for adjustments.<br><br>")
            .contains(
                "We'll notify you when the hearing is listed. You'll then be able to review the hearing requirements.");
    }

    @Test
    void should_return_confirmation_ada() {

        when(callback.getEvent()).thenReturn(Event.DRAFT_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PostSubmitCallbackResponse callbackResponse =
            submitHearingRequirementsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertTrue(callbackResponse.getConfirmationHeader().get().contains("You've submitted your hearing requirements"));

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "The Tribunal will review your hearing requirements and any additional requests for adjustments.<br><br>")
            .contains(
                "You’ll be able to see any agreed adjustments in the hearing and appointment tab.");
    }

    @Test
    void should_return_confirmation_24w() {

        when(callback.getEvent()).thenReturn(Event.DRAFT_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseFieldDefinition.STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        PostSubmitCallbackResponse callbackResponse =
            submitHearingRequirementsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertTrue(callbackResponse.getConfirmationHeader().get().contains("You've submitted your hearing requirements"));

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "The Tribunal will review your hearing requirements and any additional requests for adjustments.")
            .doesNotContain(
                "You’ll be able to see any agreed adjustments in the hearing and appointment tab.")
            .doesNotContain(
                "We'll notify you when the hearing is listed. You'll then be able to review the hearing requirements"
            );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> submitHearingRequirementsConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"DRAFT_HEARING_REQUIREMENTS"})
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertTrue(submitHearingRequirementsConfirmation.canHandle(callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"DRAFT_HEARING_REQUIREMENTS"}, mode = EnumSource.Mode.EXCLUDE)
    void it_cannot_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(submitHearingRequirementsConfirmation.canHandle(callback));
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> submitHearingRequirementsConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> submitHearingRequirementsConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
