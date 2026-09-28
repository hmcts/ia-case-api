package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AutoRequestHearingService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousRequirementsAndRequestsAppender;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ListCaseWithoutHearingRequirementsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private PreviousRequirementsAndRequestsAppender previousRequirementsAndRequestsAppender;
    @Mock
    private AutoRequestHearingService autoRequestHearingService;
    @Mock
    private PreSubmitCallbackResponse<AsylumCase> preSubmitCallbackResponse;

    private ListCaseWithoutHearingRequirementsHandler listCaseWithoutHearingRequirementsHandler;

    @BeforeEach
    public void setUp() {
        listCaseWithoutHearingRequirementsHandler =
            new ListCaseWithoutHearingRequirementsHandler(
                previousRequirementsAndRequestsAppender,
                autoRequestHearingService
            );

        when(callback.getEvent()).thenReturn(Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_set_witness_count_and_available_fields() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, preSubmitCallbackResponse);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
            .write(eq(AsylumCaseFieldDefinition.SUBMIT_HEARING_REQUIREMENTS_AVAILABLE), eq(YesOrNo.YES));
        verify(asylumCase, times(1))
            .write(eq(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS), eq(YesOrNo.YES));
        verify(asylumCase, times(1))
            .write(eq(AsylumCaseFieldDefinition.CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS), eq(YesOrNo.YES));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback, preSubmitCallbackResponse))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_clear_previous_attendance_and_duration_fields_when_set_aside_reheard_flag_exists() {

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, preSubmitCallbackResponse);

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
    void should_hold_on_to_previous_attendance_and_duration_fields_when_set_aside_reheard_flag_does_not_exist() {

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, preSubmitCallbackResponse);

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
    void should_hold_on_to_previous_attendance_and_duration_fields_when_feature_flag_disabled() {

        listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, preSubmitCallbackResponse);

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
    void should_auto_request_hearing() {
        when(autoRequestHearingService.shouldAutoRequestHearing(asylumCase, true))
            .thenReturn(true);
        when(autoRequestHearingService.autoCreateHearing(callback))
            .thenReturn(asylumCase);

        listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, preSubmitCallbackResponse);

        verify(autoRequestHearingService, times(1))
            .autoCreateHearing(callback);
    }

    @Test
    void should_not_auto_request_hearing() {
        when(autoRequestHearingService.shouldAutoRequestHearing(asylumCase, true))
            .thenReturn(false);

        listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, preSubmitCallbackResponse);

        verify(autoRequestHearingService, never())
            .autoCreateHearing(callback);
    }

    @Test
    void it_can_handle_callback() {
        assertTrue(listCaseWithoutHearingRequirementsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = "LIST_CASE_WITHOUT_HEARING_REQUIREMENTS", mode = EnumSource.Mode.EXCLUDE)
    void cannot_handle_callback_invalid_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(listCaseWithoutHearingRequirementsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = "ABOUT_TO_SUBMIT", mode = EnumSource.Mode.EXCLUDE)
    void cannot_handle_callback_invalid_stage(PreSubmitCallbackStage stage) {
        assertFalse(listCaseWithoutHearingRequirementsHandler.canHandle(stage, callback));
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> listCaseWithoutHearingRequirementsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listCaseWithoutHearingRequirementsHandler.handle(null, callback, preSubmitCallbackResponse))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null, preSubmitCallbackResponse))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void state_should_be_set_to_listing_for_non_24w() {
        when(autoRequestHearingService.shouldAutoRequestHearing(asylumCase, true))
            .thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> response =
            listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, preSubmitCallbackResponse);

        assertNotNull(response);
        assertEquals(State.LISTING, response.getState());
    }

    @Test
    void state_should_be_set_to_respondent_review_for_24w() {
        when(autoRequestHearingService.shouldAutoRequestHearing(asylumCase, true))
            .thenReturn(false);
        when(asylumCase.read(STF_24W_PREVIOUS_STATUS_WAS_YES_AUTO_GENERATED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        PreSubmitCallbackResponse<AsylumCase> response =
            listCaseWithoutHearingRequirementsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback, preSubmitCallbackResponse);

        assertNotNull(response);
        assertEquals(State.RESPONDENT_REVIEW, response.getState());
    }
}
