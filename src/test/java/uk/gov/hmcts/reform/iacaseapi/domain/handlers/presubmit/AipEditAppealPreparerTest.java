package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL_AFTER_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipEditAppealPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;

    private String homeOfficeReferenceNumber = "someHomeOfficeReference";

    private AipEditAppealPreparer aipEditAppealPreparer;

    @BeforeEach
    public void setUp() {
        aipEditAppealPreparer = new AipEditAppealPreparer(featureToggler);
    }

    @Test
    void handler_checks_out_of_country_feature_flag_set_value() {

        when(callback.getEvent()).thenReturn(EDIT_APPEAL_AFTER_SUBMIT);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        PreSubmitCallbackResponse<AsylumCase> response =
            aipEditAppealPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void handler_should_write_home_office_reference_number_before_edit(boolean hoUanFeatureFlag) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(hoUanFeatureFlag);
        when(callback.getEvent()).thenReturn(EDIT_APPEAL_AFTER_SUBMIT);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(homeOfficeReferenceNumber));

        PreSubmitCallbackResponse<AsylumCase> response =
                aipEditAppealPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);

        if (hoUanFeatureFlag) {
            Mockito.verify(asylumCase, Mockito.times(1))
                    .write(HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT, homeOfficeReferenceNumber);
        } else {
            Mockito.verify(asylumCase, Mockito.times(0))
                    .write(HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT, homeOfficeReferenceNumber);
        }
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = aipEditAppealPreparer.canHandle(callbackStage, callback);
                if (callbackStage == ABOUT_TO_START
                    && callback.getEvent() == EDIT_APPEAL_AFTER_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> aipEditAppealPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> aipEditAppealPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> aipEditAppealPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> aipEditAppealPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> aipEditAppealPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(EDIT_APPEAL_AFTER_SUBMIT);
        assertThatThrownBy(() -> aipEditAppealPreparer.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(NullPointerException.class);

    }

}
