package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.TestFixtures.submitAppealCallbackForProtectionAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CachingAppealReferenceNumberGenerator;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppealReferenceNumberHandlerTest {

    private final Callback callback = mock(Callback.class);

    private final CachingAppealReferenceNumberGenerator appealReferenceNumberGenerator =
            mock(CachingAppealReferenceNumberGenerator.class);

    private final AppealReferenceNumberHandler underTest =
            new AppealReferenceNumberHandler(appealReferenceNumberGenerator);

    private final AsylumCase caseData = mock(AsylumCase.class);
    private final CaseDetails caseDetails = mock(CaseDetails.class);

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = underTest.canHandle(callbackStage, callback);

                if (event == Event.SUBMIT_APPEAL
                        && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> underTest.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_get_next_appeal_reference_number() {

        String protectionAppealType = "protection";

        when(appealReferenceNumberGenerator.getNextAppealReferenceNumberFor(1, protectionAppealType))
                .thenReturn(of("the-next-appeal-reference-number"));

        PreSubmitCallbackResponse<AsylumCase> submitCallbackResponse =
                underTest.handle(ABOUT_TO_SUBMIT, submitAppealCallbackForProtectionAsylumCase());

        assertThat(submitCallbackResponse.getData().getAppealReferenceNumber().get())
                .isEqualTo("the-next-appeal-reference-number");
    }

    @Test
    public void doesnt_get_next_appeal_reference_number_if_already_present() {

        Optional<String> appealReference = of("some-unexpected-reference-number");

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(caseData);
        when(caseData.getAppealReferenceNumber()).thenReturn(appealReference);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        PreSubmitCallbackResponse preSubmitCallbackResponse = underTest.handle(ABOUT_TO_SUBMIT, callback);

        verifyZeroInteractions(appealReferenceNumberGenerator);

        assertThat(caseData.getAppealReferenceNumber()).isSameAs(appealReference);

        assertThat(preSubmitCallbackResponse.getErrors())
                .containsExactly("Sorry, there was a problem submitting your appeal case");
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> underTest.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> underTest.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> underTest.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> underTest.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_add_error_to_callback_response_if_unable_to_generate_appeal_reference() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(caseData);
        when(caseData.getAppealType()).thenReturn(Optional.of("protection"));

        PreSubmitCallbackResponse<AsylumCase> submitCallbackResponse =
                underTest.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(submitCallbackResponse.getErrors())
                .containsExactly("Sorry, there was a problem submitting your appeal case");
    }

}