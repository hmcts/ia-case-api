package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousRequirementsAndRequestsAppender;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ListCaseWithoutHearingRequirementsHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private PreviousRequirementsAndRequestsAppender previousRequirementsAndRequestsAppender;
    @Mock private FeatureToggler featureToggler;

    private ListCaseWithoutHearingRequirementsHandler listCaseWithoutHearingRequirementsHandler;

    @Before
    public void setUp() {
        listCaseWithoutHearingRequirementsHandler =
            new ListCaseWithoutHearingRequirementsHandler(previousRequirementsAndRequestsAppender, featureToggler);

        when(callback.getEvent()).thenReturn(Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_set_witness_count_and_available_fields() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE), eq(YesOrNo.YES));
        verify(asylumCase, times(1)).write(eq(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS), eq(YesOrNo.YES));
        verify(asylumCase, times(1)).write(eq(AsylumCaseFieldDefinition.CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS), eq(YesOrNo.YES));
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_clear_previous_attendance_and_duration_fields_when_set_aside_reheard_flag_exists() {

        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.NO);
        verify(asylumCase, times(1)).clear(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED);
        verify(asylumCase, times(1)).clear(ATTENDING_TCW);
        verify(asylumCase, times(1)).clear(ATTENDING_JUDGE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANT);
        verify(asylumCase, times(1)).clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ACTUAL_CASE_HEARING_LENGTH);
        verify(asylumCase, times(1)).clear(HEARING_CONDUCTION_OPTIONS);
        verify(asylumCase, times(1)).clear(HEARING_RECORDING_DOCUMENTS);
        verify(asylumCase, times(1)).clear(HEARING_REQUIREMENTS);
    }

    @Test
    public void should_hold_on_to_previous_attendance_and_duration_fields_when_set_aside_reheard_flag_does_not_exist() {

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).write(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.NO);
        verify(asylumCase, times(0)).clear(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED);
        verify(asylumCase, times(0)).clear(ATTENDING_TCW);
        verify(asylumCase, times(0)).clear(ATTENDING_JUDGE);
        verify(asylumCase, times(0)).clear(ATTENDING_APPELLANT);
        verify(asylumCase, times(0)).clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(0)).clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(0)).clear(ACTUAL_CASE_HEARING_LENGTH);
        verify(asylumCase, times(0)).clear(HEARING_CONDUCTION_OPTIONS);
        verify(asylumCase, times(0)).clear(HEARING_RECORDING_DOCUMENTS);
        verify(asylumCase, times(0)).clear(HEARING_REQUIREMENTS);
    }

    @Test
    public void should_hold_on_to_previous_attendance_and_duration_fields_when_feature_flag_disabled() {

        listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).write(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        verify(asylumCase, times(1)).write(CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.YES);
        verify(asylumCase, times(0)).write(CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.NO);
        verify(asylumCase, times(0)).clear(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED);
        verify(asylumCase, times(0)).clear(ATTENDING_TCW);
        verify(asylumCase, times(0)).clear(ATTENDING_JUDGE);
        verify(asylumCase, times(0)).clear(ATTENDING_APPELLANT);
        verify(asylumCase, times(0)).clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(0)).clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(0)).clear(ACTUAL_CASE_HEARING_LENGTH);
        verify(asylumCase, times(0)).clear(HEARING_CONDUCTION_OPTIONS);
        verify(asylumCase, times(0)).clear(HEARING_RECORDING_DOCUMENTS);
        verify(asylumCase, times(0)).clear(HEARING_REQUIREMENTS);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = listCaseWithoutHearingRequirementsHandler.canHandle(callbackStage, callback);

                if (event == Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
