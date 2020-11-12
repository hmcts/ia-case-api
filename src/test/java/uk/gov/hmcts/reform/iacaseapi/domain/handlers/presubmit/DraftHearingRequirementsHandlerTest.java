package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DraftHearingRequirementsHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeatureToggler featureToggler;

    private DraftHearingRequirementsHandler draftHearingRequirementsHandler;

    @Before
    public void setUp() {
        draftHearingRequirementsHandler = new DraftHearingRequirementsHandler(featureToggler);

        when(callback.getEvent()).thenReturn(Event.DRAFT_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_set_witness_count_to_zero_and_available_fields() {

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            draftHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(WITNESS_DETAILS);
        verify(asylumCase, times(1)).write(eq(AsylumCaseFieldDefinition.WITNESS_COUNT), eq(0));
        verify(asylumCase, times(1)).write(eq(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE), eq(YesOrNo.YES));
    }

    @Test
    public void should_clear_previous_agreed_adjustment_fields_for_reheard_appeal() {

        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            draftHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.NO);
        verify(asylumCase, times(0)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_LENGTH_VISIBLE, YesOrNo.NO);
        verify(asylumCase, times(1)).clear(MULTIMEDIA_TRIBUNAL_RESPONSE);
        verify(asylumCase, times(1)).clear(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase, times(1)).clear(IN_CAMERA_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase, times(1)).clear(VULNERABILITIES_TRIBUNAL_RESPONSE);
        verify(asylumCase, times(1)).clear(ADDITIONAL_TRIBUNAL_RESPONSE);
        verify(asylumCase, times(1)).clear(HEARING_REQUIREMENTS);
    }

    @Test
    public void should_set_current_hearing_details_visibility_to_yes_for_normal_case() {

        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            draftHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.NO);
        verify(asylumCase, times(0)).write(LIST_CASE_HEARING_LENGTH_VISIBLE, YesOrNo.NO);
        verify(asylumCase, times(0)).clear(MULTIMEDIA_TRIBUNAL_RESPONSE);
        verify(asylumCase, times(0)).clear(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase, times(0)).clear(IN_CAMERA_COURT_TRIBUNAL_RESPONSE);
        verify(asylumCase, times(0)).clear(VULNERABILITIES_TRIBUNAL_RESPONSE);
        verify(asylumCase, times(0)).clear(ADDITIONAL_TRIBUNAL_RESPONSE);
        verify(asylumCase, times(0)).clear(HEARING_REQUIREMENTS);
    }

    @Test
    public void should_set_witness_count_and_available_fields() {

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(Arrays.asList(new IdValue("1", new WitnessDetails("cap")), new IdValue("2", new WitnessDetails("Pan")))));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            draftHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(WITNESS_DETAILS);
        verify(asylumCase, times(1)).write(eq(AsylumCaseFieldDefinition.WITNESS_COUNT), eq(2));
        verify(asylumCase, times(1)).write(eq(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE), eq(YesOrNo.YES));
    }

    @Test
    public void should_set_current_hearing_details_visibility_to_no_for_case_flag() {

        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            draftHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(eq(AsylumCaseFieldDefinition.CURRENT_HEARING_DETAILS_VISIBLE), eq(YesOrNo.NO));
    }

    @Test
    public void should_set_current_hearing_details_visibility_to_yes_for_case_flag() {

        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            draftHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(eq(AsylumCaseFieldDefinition.CURRENT_HEARING_DETAILS_VISIBLE), eq(YesOrNo.YES));
    }

    @Test
    public void should_set_current_hearing_details_visibility_to_yes_when_feature_flag_disabled() {

        when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            draftHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(eq(AsylumCaseFieldDefinition.CURRENT_HEARING_DETAILS_VISIBLE), eq(YesOrNo.YES));
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> draftHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = draftHearingRequirementsHandler.canHandle(callbackStage, callback);

                if (event == Event.DRAFT_HEARING_REQUIREMENTS && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> draftHearingRequirementsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> draftHearingRequirementsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> draftHearingRequirementsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> draftHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
