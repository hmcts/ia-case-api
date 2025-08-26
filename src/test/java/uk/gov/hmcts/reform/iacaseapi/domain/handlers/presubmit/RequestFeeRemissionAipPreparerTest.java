package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.HELP_WITH_FEES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
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
    @Mock
    private static List<IdValue<DocumentWithMetadata>> mockDocMetadataList;
    @Mock
    private static List<IdValue<Document>> mockDocList;
    @Mock
    private static Document mockDoc;
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
    @EnumSource(value = AppealType.class, names = {"RP", "DC"})
    void handle_should_error_for_non_payment_appeal_types(
        AppealType appealType
    ) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(ASYLUM_SUPPORT_FROM_HOME_OFFICE));
        when(asylumCase.read(PREVIOUS_REMISSION_DETAILS)).thenReturn(Optional.of(Collections.emptyList()));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(APPROVED));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("8000"));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("8000"));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("0"));
        PreSubmitCallbackResponse<AsylumCase> response = requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains("You cannot request a fee remission for this appeal"));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, mode = EnumSource.Mode.EXCLUDE, names = {"RP", "DC"})
    void handle_should_not_error_for_payment_appeal_types(
        AppealType appealType
    ) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(ASYLUM_SUPPORT_FROM_HOME_OFFICE));
        when(asylumCase.read(PREVIOUS_REMISSION_DETAILS)).thenReturn(Optional.of(Collections.emptyList()));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(APPROVED));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("8000"));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("8000"));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("0"));
        when(asylumCase.read(REMISSION_REQUESTED_BY, UserRoleLabel.class)).thenReturn(Optional.of(UserRoleLabel.CITIZEN));
        when(asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class)).thenReturn(Optional.of(WANT_TO_APPLY));
        when(asylumCase.read(HELP_WITH_FEES_REF_NUMBER, String.class)).thenReturn(Optional.of("HWF123"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(I_WANT_TO_GET_HELP_WITH_FEES));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(APPROVED));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("8000"));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("8000"));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("0"));
        when(asylumCase.read(REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of("reason"));
        PreSubmitCallbackResponse<AsylumCase> response = requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback);
        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("remissionOptionsWithSupportDocuments")
    void handle_should_append_previous_remission_details_approved_citizen(
        RemissionOption previousRemissionOption,
        Document mockDocument,
        List<IdValue<DocumentWithMetadata>> localAuthorityLetters
    ) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(previousRemissionOption));
        when(asylumCase.read(PREVIOUS_REMISSION_DETAILS)).thenReturn(Optional.of(Collections.emptyList()));
        String feeAmount = "8000";
        String amountLeftToPay = "0";
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(APPROVED));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(feeAmount));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        when(asylumCase.read(REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of("reason"));
        RemissionDetails expectedRemissionDetails = prepareExpectedRemissionDetailsAppellant(
            previousRemissionOption, mockDocument, localAuthorityLetters);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, never()).write(eq(REMISSION_TYPE), any());
        assertNotNull(callbackResponse);
        assertNotNull(expectedRemissionDetails);
        assertEquals(callbackResponse.getData(), asylumCase);
        assertRemissionAppended(expectedRemissionDetails);
        RemissionDetails appendedRemissionDetails = remissionDetailsAppender.getRemissions().get(0).getValue();
        assertEquals("Approved", appendedRemissionDetails.getRemissionDecision());
        assertEquals(feeAmount, appendedRemissionDetails.getFeeAmount());
        assertEquals(feeAmount, appendedRemissionDetails.getAmountRemitted());
        assertEquals(amountLeftToPay, appendedRemissionDetails.getAmountLeftToPay());
        assertNull(appendedRemissionDetails.getRemissionDecisionReason());
    }

    @ParameterizedTest
    @MethodSource("remissionOptionsWithSupportDocuments")
    void handle_should_append_previous_remission_details_partial_approve_citizen(
        RemissionOption previousRemissionOption,
        Document mockDocument,
        List<IdValue<DocumentWithMetadata>> localAuthorityLetters
    ) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(previousRemissionOption));
        when(asylumCase.read(PREVIOUS_REMISSION_DETAILS)).thenReturn(Optional.of(Collections.emptyList()));
        String feeAmount = "8000";
        String amountRemitted = "6000";
        String amountLeftToPay = "2000";
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(amountRemitted));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        when(asylumCase.read(REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of("reason"));
        RemissionDetails expectedRemissionDetails = prepareExpectedRemissionDetailsAppellant(
            previousRemissionOption, mockDocument, localAuthorityLetters);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertNotNull(expectedRemissionDetails);
        assertEquals(callbackResponse.getData(), asylumCase);
        assertRemissionAppended(expectedRemissionDetails);
        RemissionDetails appendedRemissionDetails = remissionDetailsAppender.getRemissions().get(0).getValue();

        assertEquals("Partially approved", appendedRemissionDetails.getRemissionDecision());
        assertEquals(feeAmount, appendedRemissionDetails.getFeeAmount());
        assertEquals(amountRemitted, appendedRemissionDetails.getAmountRemitted());
        assertEquals(amountLeftToPay, appendedRemissionDetails.getAmountLeftToPay());
        assertEquals("reason", appendedRemissionDetails.getRemissionDecisionReason());
    }

    @ParameterizedTest
    @MethodSource("remissionOptionsWithSupportDocuments")
    void handle_should_append_previous_remission_details_rejected_citizen(
        RemissionOption previousRemissionOption,
        Document mockDocument,
        List<IdValue<DocumentWithMetadata>> localAuthorityLetters
    ) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(previousRemissionOption));
        when(asylumCase.read(PREVIOUS_REMISSION_DETAILS)).thenReturn(Optional.of(Collections.emptyList()));
        String feeAmount = "8000";
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(REJECTED));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("0"));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("8000"));
        when(asylumCase.read(REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of("reason"));
        RemissionDetails expectedRemissionDetails = prepareExpectedRemissionDetailsAppellant(
            previousRemissionOption, mockDocument, localAuthorityLetters);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertNotNull(expectedRemissionDetails);
        assertEquals(callbackResponse.getData(), asylumCase);
        assertRemissionAppended(expectedRemissionDetails);
        RemissionDetails appendedRemissionDetails = remissionDetailsAppender.getRemissions().get(0).getValue();
        assertEquals("Rejected", appendedRemissionDetails.getRemissionDecision());
        assertEquals(feeAmount, appendedRemissionDetails.getFeeAmount());
        assertNull(appendedRemissionDetails.getAmountRemitted());
        assertNull(appendedRemissionDetails.getAmountLeftToPay());
        assertEquals("reason", appendedRemissionDetails.getRemissionDecisionReason());
    }

    @ParameterizedTest
    @MethodSource("remissionTypesWithSupportDocuments")
    void handle_should_append_previous_remission_details_approved_non_citizen(
        RemissionType previousRemissionType,
        String remissionClaim,
        Document mockDoc,
        List<IdValue<Document>> documentList
    ) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EU));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(previousRemissionType));
        when(asylumCase.read(PREVIOUS_REMISSION_DETAILS)).thenReturn(Optional.of(Collections.emptyList()));
        String feeAmount = "8000";
        String amountLeftToPay = "0";
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(APPROVED));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmount));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(feeAmount));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        when(asylumCase.read(REMISSION_DECISION_REASON, String.class)).thenReturn(Optional.of("reason"));
        RemissionDetails expectedRemissionDetails = prepareExpectedRemissionDetailsNonAppellant(
            previousRemissionType, remissionClaim, mockDoc, documentList);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionAipPreparer.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(REMISSION_TYPE, previousRemissionType);
        assertNotNull(callbackResponse);
        assertNotNull(expectedRemissionDetails);
        assertEquals(callbackResponse.getData(), asylumCase);
        assertRemissionAppended(expectedRemissionDetails);
        RemissionDetails appendedRemissionDetails = remissionDetailsAppender.getRemissions().get(0).getValue();
        assertEquals("Approved", appendedRemissionDetails.getRemissionDecision());
        assertEquals(feeAmount, appendedRemissionDetails.getFeeAmount());
        assertEquals(feeAmount, appendedRemissionDetails.getAmountRemitted());
        assertEquals(amountLeftToPay, appendedRemissionDetails.getAmountLeftToPay());
        assertNull(appendedRemissionDetails.getRemissionDecisionReason());
    }

    @Test
    void handle_should_throw_exception_if_help_with_fees_option_is_not_present() {
        AppealType appealType = AppealType.HU;
        RemissionOption previousRemissionOption = I_WANT_TO_GET_HELP_WITH_FEES;
        RemissionDetails remissionDetails = new RemissionDetails(previousRemissionOption.toString(), WANT_TO_APPLY.toString(), "HWF123");

        List<IdValue<RemissionDetails>> previousRemissionDetails = new ArrayList<>();
        previousRemissionDetails.add(new IdValue<>("id1", remissionDetails));
        when(asylumCase.read(REMISSION_REQUESTED_BY, UserRoleLabel.class)).thenReturn(Optional.of(UserRoleLabel.CITIZEN));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(previousRemissionOption));
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

    void assertRemissionAppended(RemissionDetails remissionDetails) {
        List<IdValue<RemissionDetails>> remissionDetailsList = remissionDetailsAppender.getRemissions();
        verify(asylumCase, times(1)).write(PREVIOUS_REMISSION_DETAILS, remissionDetailsList);
        assertEquals(1, remissionDetailsList.size());
        RemissionDetails appendedRemissionDetails = remissionDetailsList.get(0).getValue();
        assertEquals(remissionDetails.getFeeRemissionType(), appendedRemissionDetails.getFeeRemissionType());
        assertEquals(remissionDetails.getAsylumSupportReference(), appendedRemissionDetails.getAsylumSupportReference());
        assertEquals(remissionDetails.getAsylumSupportDocument(), appendedRemissionDetails.getAsylumSupportDocument());
        assertEquals(remissionDetails.getLegalAidAccountNumber(), appendedRemissionDetails.getLegalAidAccountNumber());
        assertEquals(remissionDetails.getSection17Document(), appendedRemissionDetails.getSection17Document());
        assertEquals(remissionDetails.getSection20Document(), appendedRemissionDetails.getSection20Document());
        assertEquals(remissionDetails.getHomeOfficeWaiverDocument(), appendedRemissionDetails.getHomeOfficeWaiverDocument());
        assertEquals(remissionDetails.getHelpWithFeesReferenceNumber(), appendedRemissionDetails.getHelpWithFeesReferenceNumber());
        assertEquals(remissionDetails.getHelpWithFeesOption(), appendedRemissionDetails.getHelpWithFeesOption());
        assertEquals(remissionDetails.getExceptionalCircumstances(), appendedRemissionDetails.getExceptionalCircumstances());
        assertEquals(remissionDetails.getRemissionEcEvidenceDocuments(), appendedRemissionDetails.getRemissionEcEvidenceDocuments());
        assertEquals(remissionDetails.getLocalAuthorityLetters(), appendedRemissionDetails.getLocalAuthorityLetters());
    }

    //If user selected "Ask for a fee remission, but previously we had 'No remission' state". In that case we need to create a new app, by overwriting previous values.
    @ParameterizedTest
    @MethodSource("previousRemissionTestData")
    void should_create_new_request_in_not_decided_state(
        AppealType appealType,
        RemissionOption remissionOption
    ) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(LATE_REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(remissionOption));

        if (remissionOption == I_WANT_TO_GET_HELP_WITH_FEES) {
            when(asylumCase.read(LATE_HELP_WITH_FEES_OPTION, HelpWithFeesOption.class))
                .thenReturn(Optional.of(HelpWithFeesOption.ALREADY_APPLIED));
            when(asylumCase.read(LATE_HELP_WITH_FEES_REF_NUMBER, String.class))
                .thenReturn(Optional.of("HWF-A1B-23"));
        }

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
        verify(asylumCase, times(1)).write(eq(FEE_REMISSION_TYPE), anyString());
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


    private static Stream<Arguments> remissionOptionsWithSupportDocuments() {
        return Stream.of(
            Arguments.of(ASYLUM_SUPPORT_FROM_HOME_OFFICE, null, null),
            Arguments.of(FEE_WAIVER_FROM_HOME_OFFICE, null, null),
            Arguments.of(UNDER_18_GET_SUPPORT, null, null),
            Arguments.of(PARENT_GET_SUPPORT, null, null),
            Arguments.of(I_WANT_TO_GET_HELP_WITH_FEES, null, null),
            Arguments.of(ASYLUM_SUPPORT_FROM_HOME_OFFICE, mockDoc, null),
            Arguments.of(FEE_WAIVER_FROM_HOME_OFFICE, mockDoc, null),
            Arguments.of(UNDER_18_GET_SUPPORT, null, mockDocMetadataList),
            Arguments.of(PARENT_GET_SUPPORT, null, mockDocMetadataList)
        );
    }

    private static Stream<Arguments> remissionTypesWithSupportDocuments() {
        return Stream.of(
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", null, null),
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", mockDoc, null),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", null, null),
            Arguments.of(HO_WAIVER_REMISSION, "section17", null, null),
            Arguments.of(HO_WAIVER_REMISSION, "section17", mockDoc, null),
            Arguments.of(HO_WAIVER_REMISSION, "section20", null, null),
            Arguments.of(HO_WAIVER_REMISSION, "section20", mockDoc, null),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", null, null),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", mockDoc, null),
            Arguments.of(HELP_WITH_FEES, null, null, null),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, null, null, null),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, null, null, mockDocList)
        );
    }

    String getExpectedFeeRemissionType(RemissionOption remissionOption) {
        return switch (remissionOption) {
            case ASYLUM_SUPPORT_FROM_HOME_OFFICE -> FeeRemissionType.ASYLUM_SUPPORT;
            case FEE_WAIVER_FROM_HOME_OFFICE -> FeeRemissionType.HO_WAIVER;
            case UNDER_18_GET_SUPPORT, PARENT_GET_SUPPORT -> FeeRemissionType.LOCAL_AUTHORITY_SUPPORT;
            case I_WANT_TO_GET_HELP_WITH_FEES -> FeeRemissionType.HELP_WITH_FEES;
            default -> "";
        };
    }

    String getExpectedFeeRemissionType(RemissionType remissionType, String remissionClaim) {
        return switch (remissionType) {
            case HO_WAIVER_REMISSION -> switch (remissionClaim) {
                case "asylumSupport" -> FeeRemissionType.ASYLUM_SUPPORT;
                case "legalAid" -> FeeRemissionType.LEGAL_AID;
                case "section17" -> FeeRemissionType.SECTION_17;
                case "section20" -> FeeRemissionType.SECTION_20;
                case "homeOfficeWaiver" -> FeeRemissionType.HO_WAIVER;
                default -> "";
            };
            case HELP_WITH_FEES -> FeeRemissionType.HELP_WITH_FEES;
            case EXCEPTIONAL_CIRCUMSTANCES_REMISSION -> FeeRemissionType.EXCEPTIONAL_CIRCUMSTANCES;
            default -> "";
        };
    }

    RemissionDetails prepareExpectedRemissionDetailsAppellant(
        RemissionOption previousRemissionOption,
        Document mockDocument,
        List<IdValue<DocumentWithMetadata>> localAuthorityLetters
    ) {
        String asylumSupportRef = "123";
        String helpWithFees = "HWF123";
        when(asylumCase.read(REMISSION_REQUESTED_BY, UserRoleLabel.class)).thenReturn(Optional.of(UserRoleLabel.CITIZEN));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(previousRemissionOption));
        when(asylumCase.read(ASYLUM_SUPPORT_REF_NUMBER, String.class)).thenReturn(Optional.of(asylumSupportRef));
        when(asylumCase.read(ASYLUM_SUPPORT_DOCUMENT, Document.class)).thenReturn(Optional.ofNullable(mockDocument));
        when(asylumCase.read(HOME_OFFICE_WAIVER_DOCUMENT, Document.class)).thenReturn(Optional.ofNullable(mockDocument));
        when(asylumCase.read(LOCAL_AUTHORITY_LETTERS)).thenReturn(Optional.ofNullable(localAuthorityLetters));
        when(asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class)).thenReturn(Optional.of(WANT_TO_APPLY));
        when(asylumCase.read(HELP_WITH_FEES_REF_NUMBER, String.class)).thenReturn(Optional.of(helpWithFees));
        RemissionDetails.RemissionDetailsBuilder expectedRemissionDetailsBuilder = RemissionDetails.builder()
            .feeRemissionType(getExpectedFeeRemissionType(previousRemissionOption))
            .asylumSupportReference(previousRemissionOption.equals(ASYLUM_SUPPORT_FROM_HOME_OFFICE) ? asylumSupportRef : null)
            .asylumSupportDocument(previousRemissionOption.equals(ASYLUM_SUPPORT_FROM_HOME_OFFICE) ? mockDocument : null)
            .helpWithFeesOption(previousRemissionOption.equals(I_WANT_TO_GET_HELP_WITH_FEES) ? WANT_TO_APPLY.toString() : null)
            .helpWithFeesReferenceNumber(previousRemissionOption.equals(I_WANT_TO_GET_HELP_WITH_FEES) ? helpWithFees : null)
            .localAuthorityLetters(previousRemissionOption.equals(UNDER_18_GET_SUPPORT) || previousRemissionOption.equals(PARENT_GET_SUPPORT) ? localAuthorityLetters : null)
            .homeOfficeWaiverDocument(previousRemissionOption.equals(FEE_WAIVER_FROM_HOME_OFFICE) ? mockDocument : null);

        return expectedRemissionDetailsBuilder.build();
    }


    RemissionDetails prepareExpectedRemissionDetailsNonAppellant(
        RemissionType previousRemissionType,
        String remissionClaim,
        Document mockDocument,
        List<IdValue<Document>> mockDocumentList
    ) {
        String asylumSupportReference = "123";
        String legalAidAccountNumber = "234";
        String exceptionalCircumstances = "Exceptional Circumstances";
        String helpWithFeesReferenceNumber = "HWF123";
        RemissionDetails.RemissionDetailsBuilder expectedRemissionDetailsBuilder = RemissionDetails.builder();
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(previousRemissionType));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.ofNullable(remissionClaim));
        when(asylumCase.read(ASYLUM_SUPPORT_REFERENCE, String.class)).thenReturn(Optional.of(asylumSupportReference));
        when(asylumCase.read(ASYLUM_SUPPORT_DOCUMENT, Document.class)).thenReturn(Optional.ofNullable(mockDocument));
        when(asylumCase.read(LEGAL_AID_ACCOUNT_NUMBER, String.class)).thenReturn(Optional.of(legalAidAccountNumber));
        when(asylumCase.read(SECTION17_DOCUMENT, Document.class)).thenReturn(Optional.ofNullable(mockDocument));
        when(asylumCase.read(SECTION20_DOCUMENT, Document.class)).thenReturn(Optional.ofNullable(mockDocument));
        when(asylumCase.read(HOME_OFFICE_WAIVER_DOCUMENT, Document.class)).thenReturn(Optional.ofNullable(mockDocument));
        when(asylumCase.read(EXCEPTIONAL_CIRCUMSTANCES, String.class)).thenReturn(Optional.of(exceptionalCircumstances));
        when(asylumCase.read(REMISSION_EC_EVIDENCE_DOCUMENTS)).thenReturn(Optional.ofNullable(mockDocumentList));
        when(asylumCase.read(HELP_WITH_FEES_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(helpWithFeesReferenceNumber));
        expectedRemissionDetailsBuilder
            .feeRemissionType(getExpectedFeeRemissionType(previousRemissionType, remissionClaim))
            .asylumSupportReference(previousRemissionType.equals(HO_WAIVER_REMISSION) && remissionClaim.equals("asylumSupport") ? asylumSupportReference : null)
            .asylumSupportDocument(previousRemissionType.equals(HO_WAIVER_REMISSION) && remissionClaim.equals("asylumSupport") ? mockDocument : null)
            .legalAidAccountNumber(previousRemissionType.equals(HO_WAIVER_REMISSION) && remissionClaim.equals("legalAid") ? legalAidAccountNumber : null)
            .section17Document(previousRemissionType.equals(HO_WAIVER_REMISSION) && remissionClaim.equals("section17") ? mockDocument : null)
            .section20Document(previousRemissionType.equals(HO_WAIVER_REMISSION) && remissionClaim.equals("section20") ? mockDocument : null)
            .homeOfficeWaiverDocument(previousRemissionType.equals(HO_WAIVER_REMISSION) && remissionClaim.equals("homeOfficeWaiver") ? mockDocument : null)
            .helpWithFeesReferenceNumber(previousRemissionType.equals(HELP_WITH_FEES) ? helpWithFeesReferenceNumber : null)
            .exceptionalCircumstances(previousRemissionType.equals(EXCEPTIONAL_CIRCUMSTANCES_REMISSION) ? exceptionalCircumstances : null)
            .remissionEcEvidenceDocuments(previousRemissionType.equals(EXCEPTIONAL_CIRCUMSTANCES_REMISSION) ? mockDocumentList : null);
        if (previousRemissionType.equals(HO_WAIVER_REMISSION) && remissionClaim.equals("legalAid")) {
            expectedRemissionDetailsBuilder.helpWithFeesReferenceNumber("");
        }
        return expectedRemissionDetailsBuilder.build();
    }
}