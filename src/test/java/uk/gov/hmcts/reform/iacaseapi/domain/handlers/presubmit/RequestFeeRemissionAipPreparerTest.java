package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption.WANT_TO_APPLY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.REJECTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption.I_WANT_TO_GET_HELP_WITH_FEES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption.PARENT_GET_SUPPORT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption.UNDER_18_GET_SUPPORT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RemissionDetailsAppender;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestFeeRemissionAipPreparerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private IdValue<DocumentWithMetadata> previousDocuments;
    @Mock
    private DateProvider dateProvider;
    private RemissionDetailsAppender remissionDetailsAppender;
    private final LocalDate now = LocalDate.now();
    private RequestFeeRemissionAipPreparer requestFeeRemissionAipPreparer;

    @BeforeEach
    void setUp() {
        when(featureToggler.getValue("dlrm-refund-feature-flag", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        remissionDetailsAppender = new RemissionDetailsAppender();
        requestFeeRemissionAipPreparer = new RequestFeeRemissionAipPreparer(featureToggler, remissionDetailsAppender, dateProvider);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);
        when(dateProvider.now()).thenReturn(now);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "PA"})
    void should_return_error_if_has_remission_without_decision(AppealType appealType) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(ASYLUM_SUPPORT_FROM_HOME_OFFICE));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertThat(callbackResponse.getErrors()).contains("You cannot request a fee remission at this time because another fee remission request for this appeal has yet to be decided.");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_event_is_incorrect() {
        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);

        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_journey_is_not_aip() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));

        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_toggler_is_off() {
        when(featureToggler.getValue("dlrm-refund-feature-flag", false)).thenReturn(false);

        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = requestFeeRemissionAipPreparer.canHandle(callbackStage, callback);
                if (event == Event.REQUEST_FEE_REMISSION
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
    void handling_should_throw_if_appeal_type_is_not_present() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Appeal type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @MethodSource("previousRemissionDecisionTestData")
    void handle_should_append_previous_remission_details(
        AppealType appealType,
        RemissionDecision remissionDecision,
        String feeAmount,
        String amountRemitted,
        String amountLeftToPay,
        String remissionDecisionReason,
        RemissionOption previousRemissionOptionOption
    ) {
        RemissionDetails remissionDetails = null;

        switch (previousRemissionOptionOption) {
            case ASYLUM_SUPPORT_FROM_HOME_OFFICE:
                when(asylumCase.read(ASYLUM_SUPPORT_REF_NUMBER, String.class)).thenReturn(Optional.of("123"));
                remissionDetails = new RemissionDetails(previousRemissionOptionOption.toString(), "123");
                break;

            case FEE_WAIVER_FROM_HOME_OFFICE:
                remissionDetails = new RemissionDetails(previousRemissionOptionOption.toString());
                break;

            case UNDER_18_GET_SUPPORT:
            case PARENT_GET_SUPPORT:
                when(asylumCase.read(LOCAL_AUTHORITY_LETTERS)).thenReturn(Optional.of(List.of(previousDocuments)));
                List<IdValue<DocumentWithMetadata>> localAuthorityLetterMock = mock(List.class);
                remissionDetails = new RemissionDetails(previousRemissionOptionOption.toString(), localAuthorityLetterMock);
                break;

            case I_WANT_TO_GET_HELP_WITH_FEES:
                when(asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class)).thenReturn(Optional.of(WANT_TO_APPLY));
                when(asylumCase.read(HELP_WITH_FEES_REF_NUMBER, String.class)).thenReturn(Optional.of("HWF123"));
                remissionDetails = new RemissionDetails(previousRemissionOptionOption.toString(), WANT_TO_APPLY.toString(), "HWF123");
                break;

            default:
                break;
        }

        List<IdValue<RemissionDetails>> previousRemissionDetails = new ArrayList<>();
        previousRemissionDetails.add(new IdValue<>("id1", remissionDetails));

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(previousRemissionOptionOption));
        when(asylumCase.read(PREVIOUS_REMISSION_DETAILS)).thenReturn(Optional.of(previousRemissionDetails));

        switch (remissionDecision) {
            case APPROVED:
                when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(APPROVED));
                when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));
                when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(amountRemitted));
                when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
                break;

            case PARTIALLY_APPROVED:
                when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));
                when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));
                when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(amountRemitted));
                when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
                when(asylumCase.read(REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of(remissionDecisionReason));
                break;

            case REJECTED:
                when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(REJECTED));
                when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));
                when(asylumCase.read(REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of(remissionDecisionReason));
                break;

            default:
                break;
        }

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(callbackResponse.getData(), asylumCase);

        switch (remissionDecision) {
            case APPROVED:
                previousRemissionDetails.get(0).getValue().getAmountRemitted();

                assertEquals("Approved", remissionDetails.getRemissionDecision());
                assertEquals(feeAmount, remissionDetails.getFeeAmount());
                assertEquals(amountRemitted, remissionDetails.getAmountRemitted());
                assertEquals(amountLeftToPay, remissionDetails.getAmountLeftToPay());
                assertEquals(remissionDecisionReason, remissionDetails.getRemissionDecisionReason());

                break;

            case PARTIALLY_APPROVED:
                assertEquals("Partially approved", remissionDetails.getRemissionDecision());
                assertEquals(feeAmount, remissionDetails.getFeeAmount());
                assertEquals(amountRemitted, remissionDetails.getAmountRemitted());
                assertEquals(amountLeftToPay, remissionDetails.getAmountLeftToPay());
                assertEquals(remissionDecisionReason, remissionDetails.getRemissionDecisionReason());

                break;

            case REJECTED:
                assertEquals("Rejected", remissionDetails.getRemissionDecision());
                assertEquals(remissionDecisionReason, remissionDetails.getRemissionDecisionReason());

                break;

            default:
                break;
        }
    }

    @Test
    void handle_should_thow_exception_if_help_with_fees_option_is_not_present(
    ) {
        AppealType appealType = AppealType.HU;
        RemissionOption previousRemissionOptionOption = I_WANT_TO_GET_HELP_WITH_FEES;
        RemissionDetails remissionDetails = new RemissionDetails(previousRemissionOptionOption.toString(), WANT_TO_APPLY.toString(), "HWF123");

        List<IdValue<RemissionDetails>> previousRemissionDetails = new ArrayList<>();
        previousRemissionDetails.add(new IdValue<>("id1", remissionDetails));

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(previousRemissionOptionOption));
        when(asylumCase.read(PREVIOUS_REMISSION_DETAILS)).thenReturn(Optional.of(previousRemissionDetails));

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("8000"));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("4000"));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("4000"));
        when(asylumCase.read(REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of("A partially approved reason"));

        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Help with fees option is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    //If user selected "Ask for a fee remission, but previously we had 'No remission' state". In that case we need to create a new app, by overwriting previous values.
    @ParameterizedTest
    @MethodSource("previousRemissionTestData")
    void should_create_new_request_in_not_decided_state(
        AppealType appealType,
        RemissionOption remissionOption
    ) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(RemissionOption.NO_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(callbackResponse.getData(), asylumCase);
        assertNull(remissionDetailsAppender.getRemissions());

        verify(asylumCase, times(1)).read(LATE_REMISSION_OPTION, RemissionOption.class);
        verify(asylumCase, times(1)).read(JOURNEY_TYPE, JourneyType.class);
        verify(asylumCase, times(1)).read(APPEAL_TYPE, AppealType.class);
        verify(asylumCase, times(1)).read(REMISSION_OPTION, RemissionOption.class);
        verify(asylumCase, times(1)).read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);
        verify(asylumCase, times(1)).read(REMISSION_DECISION, RemissionDecision.class);

        //assign block
        verify(asylumCase, times(1)).read(LATE_REMISSION_OPTION, RemissionOption.class);
        verify(asylumCase, times(1)).read(LATE_ASYLUM_SUPPORT_REF_NUMBER, String.class);
        verify(asylumCase, times(1)).read(LATE_HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);
        verify(asylumCase, times(1)).read(LATE_HELP_WITH_FEES_REF_NUMBER, String.class);
        verify(asylumCase, times(1)).read(LATE_LOCAL_AUTHORITY_LETTERS);

        //clear block
        verify(asylumCase, times(1)).clear(LATE_REMISSION_OPTION);
        verify(asylumCase, times(1)).clear(LATE_ASYLUM_SUPPORT_REF_NUMBER);
        verify(asylumCase, times(1)).clear(LATE_HELP_WITH_FEES_OPTION);
        verify(asylumCase, times(1)).clear(LATE_HELP_WITH_FEES_REF_NUMBER);
        verify(asylumCase, times(1)).clear(LATE_LOCAL_AUTHORITY_LETTERS);

        verify(asylumCase, times(1)).write(eq(AsylumCaseFieldDefinition.REQUEST_FEE_REMISSION_DATE), anyString());
    }

    private static Stream<Arguments> previousRemissionDecisionTestData() {
        return Stream.of(
            Arguments.of(AppealType.EA, APPROVED, "8000", "8000", "0", null, ASYLUM_SUPPORT_FROM_HOME_OFFICE),
            Arguments.of(AppealType.HU, APPROVED, "8000", "8000", "0", null, FEE_WAIVER_FROM_HOME_OFFICE),
            Arguments.of(AppealType.PA, APPROVED, "8000", "8000", "0", null, UNDER_18_GET_SUPPORT),
            Arguments.of(AppealType.EU, PARTIALLY_APPROVED, "8000", "4000", "4000", "A partially approved reason", PARENT_GET_SUPPORT),
            Arguments.of(AppealType.HU, PARTIALLY_APPROVED, "8000", "4000", "4000", "A partially approved reason", I_WANT_TO_GET_HELP_WITH_FEES),
            Arguments.of(AppealType.PA, PARTIALLY_APPROVED, "8000", "4000", "4000", "A partially approved reason", ASYLUM_SUPPORT_FROM_HOME_OFFICE),
            Arguments.of(AppealType.EA, REJECTED, "8000", null, null, "A rejected reason", FEE_WAIVER_FROM_HOME_OFFICE),
            Arguments.of(AppealType.HU, REJECTED, "8000", null, null, "A rejected reason", UNDER_18_GET_SUPPORT),
            Arguments.of(AppealType.EU, REJECTED, "8000", null, null, "A rejected reason", PARENT_GET_SUPPORT)
        );
    }

    private static Stream<Arguments> previousRemissionTestData() {
        return Stream.of(
            Arguments.of(AppealType.EA, ASYLUM_SUPPORT_FROM_HOME_OFFICE),
            Arguments.of(AppealType.HU, FEE_WAIVER_FROM_HOME_OFFICE),
            Arguments.of(AppealType.PA, UNDER_18_GET_SUPPORT),
            Arguments.of(AppealType.EU, PARENT_GET_SUPPORT),
            Arguments.of(AppealType.HU, I_WANT_TO_GET_HELP_WITH_FEES)
        );
    }



}