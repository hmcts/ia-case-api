package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ListCasePreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;

    private ListCasePreparer listCasePreparer;

    @BeforeEach
    public void setUp() {

        listCasePreparer =
            new ListCasePreparer(featureToggler);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_set_default_list_case_hearing_centre_field() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER);
    }

    @Test
    void should_set_glasgow_as_list_case_hearing_centre_field() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
                .thenReturn(Optional.of(HearingCentre.GLASGOW));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.GLASGOW_TRIBUNALS_CENTRE);
    }

    @Test
    void should_not_set_default_list_case_hearing_centre_if_case_hearing_centre_not_present() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(any(), any());
    }

    @Test
    void should_set_default_list_case_hearing_centre_for_aaa_cases_to_harmondsworth() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
            .thenReturn(Optional.of(HearingCentre.HARMONDSWORTH));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.HARMONDSWORTH);
    }

    @Test
    void should_set_error_when_requirements_not_reviewed() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        Assertions.assertThat(callbackResponse.getErrors()).hasSize(1);
        Assertions.assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "You've made an invalid request. You cannot list the case until the hearing requirements have been reviewed.");

        verify(asylumCase, times(1))
            .read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class);
        verify(asylumCase, times(1)).read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class);
        verify(asylumCase, never()).write(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER);
    }

    @Test
    void should_set_error_when_reviewed_requirements_flag_not_set() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        Assertions.assertThat(callbackResponse.getErrors()).hasSize(1);
        Assertions.assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "You've made an invalid request. You cannot list the case until the hearing requirements have been reviewed.");

        verify(asylumCase, times(1))
            .read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class);
        verify(asylumCase, times(1)).read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class);
        verify(asylumCase, never()).write(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER);
    }

    @Test
    void should_not_set_error_when_requirements_have_been_reviewed() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        Assertions.assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase, times(1))
            .read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class);
        verify(asylumCase, times(1)).read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER);
    }

    @Test
    void should_set_error_when_transferred_out_of_ada_after_listing() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseFieldDefinition.CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        Assertions.assertThat(callbackResponse.getErrors()).hasSize(1);
        Assertions.assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrder(
            "Case was listed before being transferred out of ADA. Edit case listing instead.");
    }

    @Test
    void should_work_for_old_flow_when_requirements_not_captured() {

        when(asylumCase.read(AsylumCaseFieldDefinition.HEARING_CENTRE))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class))
            .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        Assertions.assertThat(callbackResponse.getErrors()).isEmpty();

        verify(asylumCase, times(1))
            .read(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class);
        verify(asylumCase, never()).read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.MANCHESTER);
    }

    @Test
    void should_clear_hearing_details_when_reheard_case_listed() {

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(1)).clear(LIST_CASE_HEARING_CENTRE);
        verify(asylumCase, times(1)).clear(LIST_CASE_HEARING_DATE);
        verify(asylumCase, times(1)).clear(LIST_CASE_HEARING_LENGTH);
    }

    @Test
    void should_not_clear_hearing_details_when_not_a_reheard_case_listed() {

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(0)).clear(LIST_CASE_HEARING_CENTRE);
        verify(asylumCase, times(0)).clear(LIST_CASE_HEARING_DATE);
        verify(asylumCase, times(0)).clear(LIST_CASE_HEARING_LENGTH);
    }

    @Test
    void should_not_clear_hearing_details_when_feature_flag_disabled() {

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(0)).clear(LIST_CASE_HEARING_CENTRE);
        verify(asylumCase, times(0)).clear(LIST_CASE_HEARING_DATE);
        verify(asylumCase, times(0)).clear(LIST_CASE_HEARING_LENGTH);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = listCasePreparer.canHandle(callbackStage, callback);

                if (event == Event.LIST_CASE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

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

        assertThatThrownBy(() -> listCasePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCasePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCasePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCasePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
