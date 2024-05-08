package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.applyforcosts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_APPLY_FOR_COSTS_OOT;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplyForCostsConfirmationTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    private ApplyForCostsConfirmation applyForCostsConfirmation = new ApplyForCostsConfirmation();

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void should_return_confirmation(YesOrNo isApplyForCostsOot) {
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_APPLY_FOR_COSTS_OOT, YesOrNo.class)).thenReturn(Optional.ofNullable(isApplyForCostsOot));

        PostSubmitCallbackResponse callbackResponse =
                applyForCostsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        if (isApplyForCostsOot.equals(YesOrNo.YES)) {
            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains("![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeApplyForCostsConfirmation.svg)\n\n"
                            +
                            "## What happens next\n\n"
                            + "The Tribunal will consider the reason it has been submitted out of time.\n\n"
                            + "If the Tribunal accepts your reason, it will consider your application and make a decision shortly.");
        } else {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("You've made a costs application");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains(
                            "## What happens next\n\n"
                                    + "Both you and the other party will receive an email notification confirming your application.\n\n"
                                    + "The other party has 14 days to respond to the claim.\n\n"
                                    + "If you have requested a hearing, the Tribunal will consider your request.\n\n"
                                    + "You can review the details of your application in the [Costs tab](/cases/case-details/" + callback.getCaseDetails().getId() + "#Costs). ");
        }

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> applyForCostsConfirmation.handle(callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = applyForCostsConfirmation.canHandle(callback);

            if (event == Event.APPLY_FOR_COSTS) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> applyForCostsConfirmation.canHandle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsConfirmation.handle(null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}