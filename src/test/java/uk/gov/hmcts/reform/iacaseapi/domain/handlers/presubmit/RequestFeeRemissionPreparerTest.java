package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RemissionDetailsAppender;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestFeeRemissionPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private FeatureToggler featureToggler;
    @Mock private Document document;

    private RequestFeeRemissionPreparer requestFeeRemissionPreparer;
    private RemissionDetailsAppender remissionDetailsAppender;


    @BeforeEach
    void setUp() {

        remissionDetailsAppender = new RemissionDetailsAppender();

        requestFeeRemissionPreparer = new RequestFeeRemissionPreparer(featureToggler, remissionDetailsAppender);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA" })
    void should_handle_if_no_previous_remission_exists(AppealType appealType) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestFeeRemissionPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(callbackResponse.getData(), asylumCase);
    }

    @Test
    void handle_should_return_error_if_appeal_type_is_not_present() {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);

        assertThatThrownBy(() -> requestFeeRemissionPreparer.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal type is not present");

    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "DC", "RP" })
    void handle_should_return_error_for_invalid_appeal_types(AppealType appealType) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestFeeRemissionPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertThat(callbackResponse.getErrors()).contains("You cannot request a fee remission for this appeal");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA" })
    void handle_should_return_error_on_previous_remission_exists_and_not_decided(AppealType appealType) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(HO_WAIVER_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestFeeRemissionPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertThat(callbackResponse.getErrors())
            .contains("You cannot request a fee remission at this time because another fee remission request for this appeal "
                                                          + "has yet to be decided.");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "EA", "HU", "PA" })
    void handle_should_return_error_if_fee_remission_type_is_not_present(AppealType appealType) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(HO_WAIVER_REMISSION));
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(PARTIALLY_APPROVED));

        assertThatThrownBy(() -> requestFeeRemissionPreparer.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Previous fee remission type is not present");
    }

    @ParameterizedTest
    @MethodSource("previousRemissionDecisionTestData")
    void handle_should_append_previous_asylum_support_remission_details(
        AppealType appealType, RemissionDecision remissionDecision, String feeAmount,
        String amountRemitted, String amountLeftToPay, String remissionDecisionReason) {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(HO_WAIVER_REMISSION));
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.empty());

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

        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Asylum support"));
        when(asylumCase.read(ASYLUM_SUPPORT_REFERENCE, String.class)).thenReturn(Optional.of("123456"));
        when(asylumCase.read(ASYLUM_SUPPORT_DOCUMENT)).thenReturn(Optional.of(document));
        when(asylumCase.read(PREVIOUS_REMISSION_DETAILS)).thenReturn(Optional.of(Collections.emptyList()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestFeeRemissionPreparer.handle(ABOUT_TO_START, callback); //about to submit in handler

        AsylumCase responseData = callbackResponse.getData();
        Optional<List<IdValue<RemissionDetails>>> maybeExistingRemissionDetails = responseData.read(PREVIOUS_REMISSION_DETAILS);
        final List<IdValue<RemissionDetails>> existingRemissionDetails = maybeExistingRemissionDetails.orElse(Collections.emptyList());

        assertNotNull(callbackResponse);
        assertEquals(responseData, asylumCase);
        assertEquals(1, existingRemissionDetails.size());

        existingRemissionDetails
            .stream()
            .forEach(idValue -> {
                RemissionDetails remissionDetails = idValue.getValue();

                switch (remissionDecision) {
                    case APPROVED:
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

                assertEquals("Asylum support", remissionDetails.getFeeRemissionType());
                assertEquals("123456", remissionDetails.getAsylumSupportReference());
                assertEquals(document, remissionDetails.getAsylumSupportDocument());
            });
    }

    private static Stream<Arguments> previousRemissionDecisionTestData() {

        return Stream.of(
            Arguments.of(AppealType.EA, APPROVED, "8000", "8000", "0", null),
            Arguments.of(AppealType.HU, APPROVED, "8000", "8000", "0", null),
            Arguments.of(AppealType.PA, APPROVED, "8000", "8000", "0", null),
            Arguments.of(AppealType.EA, PARTIALLY_APPROVED, "8000", "4000", "4000", "A partially approved reason"),
            Arguments.of(AppealType.HU, PARTIALLY_APPROVED, "8000", "4000", "4000", "A partially approved reason"),
            Arguments.of(AppealType.PA, PARTIALLY_APPROVED, "8000", "4000", "4000", "A partially approved reason"),
            Arguments.of(AppealType.EA, REJECTED, "8000", null, null, "A rejected reason"),
            Arguments.of(AppealType.HU, REJECTED, "8000", null, null, "A rejected reason"),
            Arguments.of(AppealType.PA, REJECTED, "8000", null, null, "A rejected reason")
        );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> requestFeeRemissionPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        when(featureToggler.getValue("remissions-feature", false)).thenReturn(true);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestFeeRemissionPreparer.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_FEE_REMISSION
                    && callbackStage == ABOUT_TO_START) {

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

        assertThatThrownBy(() -> requestFeeRemissionPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestFeeRemissionPreparer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
