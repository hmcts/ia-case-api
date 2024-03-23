package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestFeeRemissionPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private FeatureToggler featureToggler;

    private RequestFeeRemissionPreparer requestFeeRemissionPreparer;

    @BeforeEach
    void setUp() {
        requestFeeRemissionPreparer = new RequestFeeRemissionPreparer(featureToggler);
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
        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Asylum support"));

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
        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.of("Asylum support"));

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
        when(asylumCase.read(FEE_REMISSION_TYPE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestFeeRemissionPreparer.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Previous fee remission type is not present");
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
