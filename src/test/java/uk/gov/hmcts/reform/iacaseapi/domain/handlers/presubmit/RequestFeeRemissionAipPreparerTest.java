package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.REJECTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@ExtendWith(MockitoExtension.class)
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
    private RequestFeeRemissionAipPreparer requestFeeRemissionAipPreparer;

    @BeforeEach
    void setUp() {
        when(featureToggler.getValue("dlrm-refund-feature-flag", false)).thenReturn(true);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        requestFeeRemissionAipPreparer = new RequestFeeRemissionAipPreparer(featureToggler);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.REQUEST_FEE_REMISSION);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_event_is_incorrect() {
        when(callback.getEvent()).thenReturn(Event.NOC_REQUEST);

        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_journey_is_not_aip() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));

        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_toggler_is_off() {
        when(featureToggler.getValue("dlrm-refund-feature-flag", false)).thenReturn(false);

        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.iacaseapi.utils.TestUtils#eventAndCallbackStages")
    void it_can_handle_callback(Event event, PreSubmitCallbackStage callbackStage) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        boolean canHandle = requestFeeRemissionAipPreparer.canHandle(callbackStage, callback);
        if (event == Event.REQUEST_FEE_REMISSION
            && callbackStage == ABOUT_TO_START) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @Test
    void handling_should_throw_if_appeal_type_is_not_present() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestFeeRemissionAipPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Appeal type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, mode = EnumSource.Mode.EXCLUDE, names = {"EA", "HU", "PA", "EU"})
    void handle_should_error_for_non_payment_appeal_types(
        AppealType appealType
    ) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        PreSubmitCallbackResponse<AsylumCase> response = requestFeeRemissionAipPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertEquals(1, response.getErrors().size());
        assertTrue(response.getErrors().contains("You cannot request a fee remission for this appeal"));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "PA", "EU"})
    void handle_should_not_error_for_payment_appeal_types_without_remission(
        AppealType appealType
    ) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        PreSubmitCallbackResponse<AsylumCase> response = requestFeeRemissionAipPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase).clear(LATE_REMISSION_TYPE);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "PA", "EU"})
    void handle_should_not_error_for_payment_appeal_types_with_remission_decision(
        AppealType appealType
    ) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(ASYLUM_SUPPORT_FROM_HOME_OFFICE));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(REJECTED));

        PreSubmitCallbackResponse<AsylumCase> response = requestFeeRemissionAipPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(response);
        assertNotNull(response.getErrors());
        assertTrue(response.getErrors().isEmpty());
        verify(asylumCase).clear(LATE_REMISSION_TYPE);
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {"EA", "HU", "PA", "EU"})
    void should_return_error_if_has_remission_without_decision(AppealType appealType) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(REMISSION_OPTION, RemissionOption.class)).thenReturn(Optional.of(ASYLUM_SUPPORT_FROM_HOME_OFFICE));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = requestFeeRemissionAipPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertNotNull(callbackResponse.getErrors());
        assertThat(callbackResponse.getErrors()).contains("You cannot request a fee remission at this time because another fee remission request for this appeal has yet to be decided.");
    }
}