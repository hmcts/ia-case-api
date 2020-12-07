package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AMOUNT_LEFT_TO_PAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AMOUNT_REMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RecordRemissionDecisionMidEventTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private FeatureToggler featureToggler;

    private RecordRemissionDecisionMidEvent recordRemissionDecisionMidEvent;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        recordRemissionDecisionMidEvent = new RecordRemissionDecisionMidEvent(featureToggler);

    }

    @Test
    void handle_should_throw_for_no_remission_decision() {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        assertThatThrownBy(() -> recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Remission decision is not present");
    }

    @ParameterizedTest
    @EnumSource(value = RemissionDecision.class, names = {"APPROVED", "PARTIALLY_APPROVED"})
    void handle_should_throw_for_remitted_amount_not_present(RemissionDecision decision) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(decision));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithoutHearing"));
        when(asylumCase.read(FEE_WITHOUT_HEARING, String.class)).thenReturn(Optional.of("8000"));

        assertThatThrownBy(() -> recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Amount remitted is not present");
    }

    @ParameterizedTest
    @EnumSource(value = RemissionDecision.class, names = {"APPROVED", "PARTIALLY_APPROVED"})
    void handle_should_throw_for_missing_fee_without_hearing(RemissionDecision decision) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(decision));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of("decisionWithoutHearing"));

        assertThatThrownBy(() -> recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Fee without hearing is not present");
    }

    @ParameterizedTest
    @EnumSource(value = RemissionDecision.class, names = {"APPROVED", "PARTIALLY_APPROVED"})
    void handle_should_throw_for_missing_fee_with_hearing(RemissionDecision decision) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(decision));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));

        assertThatThrownBy(() -> recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Fee with hearing is not present");
    }

    @ParameterizedTest
    @EnumSource(value = RemissionDecision.class, names = {"APPROVED", "PARTIALLY_APPROVED"})
    void handle_should_throw_for_amount_left_to_pay_not_present(RemissionDecision decision) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(decision));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(asylumCase.read(FEE_WITH_HEARING, String.class)).thenReturn(Optional.of("14000"));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("14000"));

        assertThatThrownBy(() -> recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Amount left to pay is not present");
    }

    @ParameterizedTest
    @ValueSource(strings = {"decisionWithHearing", "decisionWithoutHearing"})
    void should_handle_remission_approved_decision(String hearingType) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(hearingType));
        if (hearingType.equals("decisionWithHearing")) {
            when(asylumCase.read(FEE_WITH_HEARING, String.class)).thenReturn(Optional.of("140"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("14000"));
        } else {
            when(asylumCase.read(FEE_WITHOUT_HEARING, String.class)).thenReturn(Optional.of("80"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("8000"));
        }

        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("0"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(callbackResponse.getErrors()).isEmpty();

        if (hearingType.equals("decisionWithHearing")) {
            verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, "14000");
        } else {
            verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, "8000");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"decisionWithHearing", "decisionWithoutHearing"})
    void handle_should_error_for_amount_left_to_pay_greater_than_zero_on_remission_approved(String hearingType) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(hearingType));
        if (hearingType.equals("decisionWithHearing")) {
            when(asylumCase.read(FEE_WITH_HEARING, String.class)).thenReturn(Optional.of("140"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("14000"));
        } else {
            when(asylumCase.read(FEE_WITHOUT_HEARING, String.class)).thenReturn(Optional.of("80"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("8000"));
        }

        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("40"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("The amount left to pay must be 0");
    }

    @ParameterizedTest
    @ValueSource(strings = {"decisionWithHearing", "decisionWithoutHearing"})
    void handle_should_error_for_incorrect_remitted_amount_on_remission_approved(String hearingType) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(hearingType));
        if (hearingType.equals("decisionWithHearing")) {
            when(asylumCase.read(FEE_WITH_HEARING, String.class)).thenReturn(Optional.of("140"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("10000"));
        } else {
            when(asylumCase.read(FEE_WITHOUT_HEARING, String.class)).thenReturn(Optional.of("80"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("5000"));
        }

        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("0"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("The Amount remitted and the amount left to pay must equal the full fee amount");

        if (hearingType.equals("decisionWithHearing")) {
            verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, "14000");
        } else {
            verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, "8000");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"decisionWithHearing", "decisionWithoutHearing"})
    void should_handle_remission_partially_approved_decision(String hearingType) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(hearingType));
        if (hearingType.equals("decisionWithHearing")) {
            when(asylumCase.read(FEE_WITH_HEARING, String.class)).thenReturn(Optional.of("140"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("10000"));
            when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("4000"));
        } else {
            when(asylumCase.read(FEE_WITHOUT_HEARING, String.class)).thenReturn(Optional.of("80"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("5000"));
            when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("3000"));
        }

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(callbackResponse.getErrors()).isEmpty();

        if (hearingType.equals("decisionWithHearing")) {
            verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, "14000");
        } else {
            verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, "8000");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"decisionWithHearing", "decisionWithoutHearing"})
    void handle_should_error_for_incorrect_remitted_amount_on_remission_partially_approved(String hearingType) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(hearingType));
        if (hearingType.equals("decisionWithHearing")) {
            when(asylumCase.read(FEE_WITH_HEARING, String.class)).thenReturn(Optional.of("140"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("9000"));
            when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("4000"));
        } else {
            when(asylumCase.read(FEE_WITHOUT_HEARING, String.class)).thenReturn(Optional.of("80"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("2000"));
            when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("3000"));
        }

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("The Amount remitted and the amount left to pay must equal the full fee amount");

        if (hearingType.equals("decisionWithHearing")) {
            verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, "14000");
        } else {
            verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, "8000");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"decisionWithHearing", "decisionWithoutHearing"})
    void handle_should_error_for_incorrect_amount_left_to_pay_on_remission_partially_approved(String hearingType) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.RECORD_REMISSION_DECISION);

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(hearingType));
        if (hearingType.equals("decisionWithHearing")) {
            when(asylumCase.read(FEE_WITH_HEARING, String.class)).thenReturn(Optional.of("140"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("14000"));
            when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("0"));
        } else {
            when(asylumCase.read(FEE_WITHOUT_HEARING, String.class)).thenReturn(Optional.of("80"));
            when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("8000"));
            when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("0"));
        }

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertThat(callbackResponse).isNotNull();
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors())
            .contains("The Amount remitted cannot be equal to the full fee amount");
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordRemissionDecisionMidEvent.canHandle(callbackStage, callback);

                if ((event == Event.RECORD_REMISSION_DECISION)
                    && callbackStage == PreSubmitCallbackStage.MID_EVENT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordRemissionDecisionMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordRemissionDecisionMidEvent.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordRemissionDecisionMidEvent.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordRemissionDecisionMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
