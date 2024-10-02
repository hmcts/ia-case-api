package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeTribunalAction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment.FeesHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_TYPE_CHANGED_WITH_REFUND_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DISPLAY_FEE_UPDATE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_COMPLETED_STAGES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_RECORDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_UPDATE_TRIBUNAL_ACTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATED_DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeTribunalAction.ADDITIONAL_PAYMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeTribunalAction.NO_ACTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeTribunalAction.REFUND;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason.APPEAL_NOT_VALID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason.APPEAL_WITHDRAWN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason.DECISION_TYPE_CHANGED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeUpdateReason.FEE_REMISSION_CHANGED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ManageFeeUpdateHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private FeeService feeService;

    private ManageFeeUpdateHandler manageFeeUpdateHandler;

    @BeforeEach
    void setUp() {

        manageFeeUpdateHandler = new ManageFeeUpdateHandler(featureToggler, feeService);
    }

    @Test
    void handling_should_return_selected_fee_update_statuses() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Collections.singletonList(
                "Fee update recorded"
            ));

        final List<String> expectedFeeUpdateStatus =
            Arrays.asList(
                "Fee update recorded"
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);

        when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(feeUpdateStatus));
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(DECISION_TYPE_CHANGED));
        when(asylumCase.read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class)).thenReturn(Optional.of(REFUND));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_UPDATE_COMPLETED_STAGES, expectedFeeUpdateStatus);
        verify(asylumCase, times(1)).write(DISPLAY_FEE_UPDATE_STATUS, YesOrNo.YES);
    }

    @Test
    void handling_should_return_selected_fee_update_statuses_after_fee_update_recorded() {
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(DECISION_TYPE_CHANGED));
        when(asylumCase.read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class)).thenReturn(Optional.of(REFUND));
        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        final CheckValues<String> feeUpdateStatus =
            new CheckValues<>(Arrays.asList(
                "Refund approved",
                "Fee update not required"
            ));

        final List<String> completedStagesFeeUpdateStatus =
            Arrays.asList(
                "Fee update recorded",
                "Refund approved"
            );
        final List<String> expectedFeeUpdateStatus =
            Arrays.asList(
                "Fee update recorded",
                "Refund approved",
                "Fee update not required"
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);

        when(asylumCase.read(FEE_UPDATE_STATUS)).thenReturn(Optional.of(feeUpdateStatus));
        when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(completedStagesFeeUpdateStatus));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            manageFeeUpdateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(FEE_UPDATE_COMPLETED_STAGES, expectedFeeUpdateStatus);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> manageFeeUpdateHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = manageFeeUpdateHandler.canHandle(callbackStage, callback);

                if ((event == Event.MANAGE_FEE_UPDATE)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("provideParameterValues")
    void should_write_flag_value_if_decision_type_changed_with_refund(FeeUpdateReason feeUpdateReason,
                                                                      FeeTribunalAction feeTribunalAction) {
        when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);

        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(feeUpdateReason));
        when(asylumCase.read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class)).thenReturn(Optional.of(feeTribunalAction));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionHearingFeeOption"));

        manageFeeUpdateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        if (feeUpdateReason.equals(DECISION_TYPE_CHANGED) && feeTribunalAction.equals(REFUND)) {
            verify(asylumCase, times(1)).write(DECISION_TYPE_CHANGED_WITH_REFUND_FLAG, YES);
        } else {
            verify(asylumCase, times(0)).write(DECISION_TYPE_CHANGED_WITH_REFUND_FLAG, YES);
        }
    }

    @Test
    void should_write_previous_and_updated_hearing_fee_options() {
        try (MockedStatic<FeesHelper> mockedStaticFeesHelper = Mockito.mockStatic(FeesHelper.class)) {
            Mockito.when(FeesHelper.findFeeByHearingType(feeService, asylumCase)).thenReturn(mock(Fee.class));
            when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);

            when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(DECISION_TYPE_CHANGED));
            when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
            when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("8000"));
            when(asylumCase.read(UPDATED_DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithoutHearing"));
            when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(new ArrayList<>()));
            when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(new CheckValues<>(Collections.emptyList())));
            when(asylumCase.read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class)).thenReturn(Optional.empty());

            manageFeeUpdateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            verify(asylumCase, times(1)).read(FEE_UPDATE_COMPLETED_STAGES);
            verify(asylumCase, times(1)).read(FEE_UPDATE_STATUS);
            verify(asylumCase, times(1)).write(FEE_UPDATE_COMPLETED_STAGES, Collections.emptyList());
            verify(asylumCase, times(1)).read(FEE_UPDATE_REASON, FeeUpdateReason.class);
            verify(asylumCase, times(1)).read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class);
            verify(asylumCase, times(1)).read(DECISION_HEARING_FEE_OPTION, String.class);
            verify(asylumCase, times(1)).read(UPDATED_DECISION_HEARING_FEE_OPTION, String.class);
            verify(asylumCase, times(1)).write(PREVIOUS_DECISION_HEARING_FEE_OPTION, "decisionWithHearing");
            verify(asylumCase, times(1)).write(PREVIOUS_FEE_AMOUNT_GBP, "8000");
            verify(asylumCase, times(1)).write(DECISION_HEARING_FEE_OPTION, "decisionWithoutHearing");

            mockedStaticFeesHelper.verify(() -> FeesHelper.findFeeByHearingType(feeService, asylumCase), times(1));
        }
    }

    @Test
    void should_write_previous_and_updated_hearing_fee_options_when_update_decision_hearing_fee_option_is_not_present() {
        try (MockedStatic<FeesHelper> mockedStaticFeesHelper = Mockito.mockStatic(FeesHelper.class)) {
            Mockito.when(FeesHelper.findFeeByHearingType(feeService, asylumCase)).thenReturn(mock(Fee.class));
            when(featureToggler.getValue("manage-fee-update-feature", false)).thenReturn(true);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getEvent()).thenReturn(Event.MANAGE_FEE_UPDATE);

            when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(DECISION_TYPE_CHANGED));
            when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
            when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of("8000"));
            when(asylumCase.read(FEE_UPDATE_COMPLETED_STAGES)).thenReturn(Optional.of(new ArrayList<>()));
            when(asylumCase.read(FEE_UPDATE_RECORDED)).thenReturn(Optional.of(new CheckValues<>(Collections.emptyList())));
            when(asylumCase.read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class)).thenReturn(Optional.of(ADDITIONAL_PAYMENT));

            manageFeeUpdateHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            verify(asylumCase, times(1)).read(FEE_UPDATE_COMPLETED_STAGES);
            verify(asylumCase, times(1)).read(FEE_UPDATE_STATUS);
            verify(asylumCase, times(1)).write(FEE_UPDATE_COMPLETED_STAGES, Collections.emptyList());
            verify(asylumCase, times(1)).read(FEE_UPDATE_REASON, FeeUpdateReason.class);
            verify(asylumCase, times(1)).read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class);
            verify(asylumCase, times(1)).read(DECISION_HEARING_FEE_OPTION, String.class);
            verify(asylumCase, times(1)).read(UPDATED_DECISION_HEARING_FEE_OPTION, String.class);
            verify(asylumCase, times(1)).write(PREVIOUS_FEE_AMOUNT_GBP, "8000");

            mockedStaticFeesHelper.verify(() -> FeesHelper.findFeeByHearingType(feeService, asylumCase), times(1));
        }
    }

    private static Stream<Arguments> provideParameterValues() {
        return Stream.of(
            Arguments.of(DECISION_TYPE_CHANGED, REFUND),
            Arguments.of(DECISION_TYPE_CHANGED, ADDITIONAL_PAYMENT),
            Arguments.of(DECISION_TYPE_CHANGED, NO_ACTION),
            Arguments.of(APPEAL_NOT_VALID, REFUND),
            Arguments.of(APPEAL_NOT_VALID, ADDITIONAL_PAYMENT),
            Arguments.of(APPEAL_NOT_VALID, NO_ACTION),
            Arguments.of(FEE_REMISSION_CHANGED, REFUND),
            Arguments.of(FEE_REMISSION_CHANGED, ADDITIONAL_PAYMENT),
            Arguments.of(FEE_REMISSION_CHANGED, NO_ACTION),
            Arguments.of(APPEAL_WITHDRAWN, REFUND),
            Arguments.of(APPEAL_WITHDRAWN, ADDITIONAL_PAYMENT),
            Arguments.of(APPEAL_WITHDRAWN, NO_ACTION)
        );
    }
}
