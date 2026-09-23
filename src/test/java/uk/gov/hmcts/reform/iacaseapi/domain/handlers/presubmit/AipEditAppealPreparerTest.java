package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL_AFTER_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipEditAppealPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private AipEditAppealPreparer aipEditAppealPreparer;

    @BeforeEach
    public void setUp() {
        aipEditAppealPreparer = new AipEditAppealPreparer();
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

    @Test
    void handler_should_write_home_office_reference_number_before_edit() {

        when(callback.getEvent()).thenReturn(EDIT_APPEAL_AFTER_SUBMIT);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        String homeOfficeReferenceNumber = "someHomeOfficeReference";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(homeOfficeReferenceNumber));

        PreSubmitCallbackResponse<AsylumCase> response =
            aipEditAppealPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);

        Mockito.verify(asylumCase, Mockito.times(1))
            .write(HOME_OFFICE_REFERENCE_NUMBER_BEFORE_EDIT, homeOfficeReferenceNumber);

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_APPEAL_AFTER_SUBMIT", "EDIT_APPELLANT_PERSONAL_DATA"})
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertTrue(aipEditAppealPreparer.canHandle(ABOUT_TO_START, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_APPEAL_AFTER_SUBMIT", "EDIT_APPELLANT_PERSONAL_DATA"}, mode = EnumSource.Mode.EXCLUDE)
    void it_cannot_handle_callback_bad_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(aipEditAppealPreparer.canHandle(ABOUT_TO_START, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_START"}, mode = EnumSource.Mode.EXCLUDE)
    void it_cannot_handle_callback_bad_stage(PreSubmitCallbackStage stage) {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL_AFTER_SUBMIT);
        assertFalse(aipEditAppealPreparer.canHandle(stage, callback));
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
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        assertThatThrownBy(() -> aipEditAppealPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

}
