package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ListEditCaseHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HearingCentreFinder hearingCentreFinder;
    @Mock
    private CaseManagementLocationService caseManagementLocationService;
    @Mock
    private DirectionAppender directionAppender;
    private int dueDaysSinceSubmission = 15;

    private String directionExplanation = "You have a direction for this case.\n"
                                          + "\n"
                                          + "The accelerated detained appeal has been listed and you should tell the Tribunal if the appellant has any hearing requirements.\n"
                                          + "\n"
                                          + "# Next steps\n"
                                          + "Log in to the service and select the case from your case list. You’ll be able to submit the hearing requirements by selecting Submit hearing requirements from the Next step dropdown on the overview tab.\n"
                                          + "\n"
                                          + "The Tribunal will review the hearing requirements and any requests for additional adjustments.\n"
                                          + "\n"
                                          + "If you do not submit the hearing requirements by the date indicated below, the Tribunal may not be able to accommodate the appellant’s needs for the hearing.";

    private ListEditCaseHandler listEditCaseHandler;

    @BeforeEach
    public void setUp() {

        listEditCaseHandler = new ListEditCaseHandler(hearingCentreFinder,
            caseManagementLocationService,
            dueDaysSinceSubmission,
            directionAppender);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_set_default_list_case_hearing_centre_field() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(ATTENDING_TCW);
        verify(asylumCase, times(1)).clear(ATTENDING_JUDGE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANT);
        verify(asylumCase, times(1)).clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ACTUAL_CASE_HEARING_LENGTH);
        verify(asylumCase, times(1)).clear(HEARING_CONDUCTION_OPTIONS);
        verify(asylumCase, times(1)).clear(HEARING_RECORDING_DOCUMENTS);
        verify(asylumCase, times(1)).clear(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS);
    }

    @Test
    void should_set_flags_and_add_direction_if_ada_legal_rep_jounrey() {
        Direction expectedDirection = new Direction(directionExplanation,
            Parties.LEGAL_REPRESENTATIVE,
            "2022-12-16",
            LocalDate.now().toString(),
            DirectionTag.ADA_LIST_CASE,
            Collections.emptyList(),
            Collections.emptyList(),
            "1",
            Event.LIST_CASE.toString());

        List<IdValue<Direction>> expectedListOfDirections = List.of(new IdValue<>("1", expectedDirection));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of("2022-12-01"));
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.empty());
        when(directionAppender.append(asylumCase,
            Collections.emptyList(),
            directionExplanation,
            Parties.LEGAL_REPRESENTATIVE,
            "2022-12-16",
            DirectionTag.ADA_LIST_CASE,
            Event.LIST_CASE.toString())).thenReturn(expectedListOfDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(ACCELERATED_DETAINED_APPEAL_LISTED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(ADA_EDIT_LISTING_AVAILABLE, YesOrNo.YES);
        verify(asylumCase, times(1)).write(LISTING_AVAILABLE_FOR_ADA, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DIRECTIONS, expectedListOfDirections);
    }

    @Test
    void should_set_flags_and_add_direction_if_ada_internal_case() {
        Direction expectedDirection = new Direction(directionExplanation,
                Parties.APPELLANT,
                "2022-12-16",
                LocalDate.now().toString(),
                DirectionTag.ADA_LIST_CASE,
                Collections.emptyList(),
                Collections.emptyList(),
                "1",
                Event.LIST_CASE.toString());

        List<IdValue<Direction>> expectedListOfDirections = List.of(new IdValue<>("1", expectedDirection));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of("2022-12-01"));
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.empty());
        when(directionAppender.append(asylumCase,
                Collections.emptyList(),
                directionExplanation,
                Parties.APPELLANT,
                "2022-12-16",
                DirectionTag.ADA_LIST_CASE,
                Event.LIST_CASE.toString())).thenReturn(expectedListOfDirections);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(ACCELERATED_DETAINED_APPEAL_LISTED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(ADA_EDIT_LISTING_AVAILABLE, YesOrNo.YES);
        verify(asylumCase, times(1)).write(LISTING_AVAILABLE_FOR_ADA, YesOrNo.NO);
        verify(asylumCase, times(1)).write(DIRECTIONS, expectedListOfDirections);
    }

    @Test
    void should_set_hearing_centre_for_remote_hearing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.COVENTRY));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(ATTENDING_TCW);
        verify(asylumCase, times(1)).clear(ATTENDING_JUDGE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANT);
        verify(asylumCase, times(1)).clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ACTUAL_CASE_HEARING_LENGTH);
        verify(asylumCase, times(1)).clear(HEARING_CONDUCTION_OPTIONS);
        verify(asylumCase, times(1)).clear(HEARING_RECORDING_DOCUMENTS);
        verify(asylumCase, times(1)).clear(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS);
    }

    @Test
    void should_set_default_if_listing_hearing_centre_is_not_active() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(hearingCentreFinder.hearingCentreIsActive(HearingCentre.MANCHESTER)).thenReturn(false);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).clear(REVIEWED_UPDATED_HEARING_REQUIREMENTS);
        verify(asylumCase, times(1)).clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(ATTENDING_TCW);
        verify(asylumCase, times(1)).clear(ATTENDING_JUDGE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANT);
        verify(asylumCase, times(1)).clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ACTUAL_CASE_HEARING_LENGTH);
        verify(asylumCase, times(1)).clear(HEARING_CONDUCTION_OPTIONS);
        verify(asylumCase, times(1)).clear(HEARING_RECORDING_DOCUMENTS);
        verify(asylumCase, times(1)).clear(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS);
    }

    @Test
    void should_not_update_designated_hearing_centre_if_list_case_hearing_centre_field_is_listing_only() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.COVENTRY));
        when(hearingCentreFinder.hearingCentreIsActive(HearingCentre.COVENTRY)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(0)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).clear(REVIEWED_UPDATED_HEARING_REQUIREMENTS);
        verify(asylumCase, times(1)).clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(ATTENDING_TCW);
        verify(asylumCase, times(1)).clear(ATTENDING_JUDGE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANT);
        verify(asylumCase, times(1)).clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ACTUAL_CASE_HEARING_LENGTH);
        verify(asylumCase, times(1)).clear(HEARING_CONDUCTION_OPTIONS);
        verify(asylumCase, times(1)).clear(HEARING_RECORDING_DOCUMENTS);
        verify(asylumCase, times(1)).clear(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS);
    }


    @Test
    void should_update_designated_hearing_centre_if_list_case_hearing_centre_field_is_not_listing_only() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(hearingCentreFinder.hearingCentreIsActive(HearingCentre.MANCHESTER)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.MANCHESTER);
        verify(asylumCase, times(1)).clear(REVIEWED_UPDATED_HEARING_REQUIREMENTS);
        verify(asylumCase, times(1)).clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(ATTENDING_TCW);
        verify(asylumCase, times(1)).clear(ATTENDING_JUDGE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANT);
        verify(asylumCase, times(1)).clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
        verify(asylumCase, times(1)).clear(ACTUAL_CASE_HEARING_LENGTH);
        verify(asylumCase, times(1)).clear(HEARING_CONDUCTION_OPTIONS);
        verify(asylumCase, times(1)).clear(HEARING_RECORDING_DOCUMENTS);
        verify(asylumCase, times(1)).clear(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = listEditCaseHandler.canHandle(callbackStage, callback);

                if ((event == Event.LIST_CASE || event == Event.EDIT_CASE_LISTING)
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
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> listEditCaseHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listEditCaseHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listEditCaseHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
