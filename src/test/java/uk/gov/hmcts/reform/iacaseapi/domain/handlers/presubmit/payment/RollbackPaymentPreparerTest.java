package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RollbackPaymentPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private RollbackPaymentPreparer rollbackPaymentPreparer;

    @BeforeEach
    public void setUp() {

        rollbackPaymentPreparer = new RollbackPaymentPreparer();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.MOVE_TO_PAYMENT_PENDING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {
        "PA", "RP", "DC"
    })
    void should_throw_error_on_handler_for_given_appeal_type(AppealType appealType) {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            rollbackPaymentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You cannot mark this type of appeal as unpaid."));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {
        "EA", "HU", "EU", "AG"
    })
    void should_not_throw_error_on_handler_for_given_appeal_type(AppealType appealType) {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            rollbackPaymentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(0, callbackResponse.getErrors().size());
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {
        "EA", "HU", "EU"
    })
    void should_return_error_on_handler_for_given_appeal_type_when_payment_option_is_empty(AppealType appealType) {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            rollbackPaymentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You cannot mark this appeal as not paid"));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {
        "EA", "HU", "EU", "AG"
    })
    void should_not_return_error_on_handler_for_given_appeal_type_when_payment_option_is_empty_and_remission_rejected(AppealType appealType) {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.REJECTED));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            rollbackPaymentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {
        "EA", "HU", "EU", "AG"
    })
    void should_return_error_on_handler_for_given_appeal_type_when_payment_option_is_card_payment(AppealType appealType) {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            rollbackPaymentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertEquals(1, callbackResponse.getErrors().size());
        assertTrue(callbackResponse.getErrors().contains("You cannot mark this appeal as not paid"));
    }

    @Test
    void should_throw_illegal_state_exception_when_appeal_type_is_empty() {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rollbackPaymentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("appealType is not set")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> rollbackPaymentPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> rollbackPaymentPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> rollbackPaymentPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> rollbackPaymentPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = rollbackPaymentPreparer.canHandle(callbackStage, callback);

                if (event == Event.MOVE_TO_PAYMENT_PENDING
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}
