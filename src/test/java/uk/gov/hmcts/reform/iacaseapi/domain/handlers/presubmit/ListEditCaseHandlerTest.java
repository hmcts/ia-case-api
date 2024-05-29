package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ACTUAL_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_TCW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_HEARING_DETAILS_VISIBLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DOES_THE_CASE_NEED_TO_BE_RELISTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EPIMS_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CONDUCTION_OPTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_RECORDING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_CASE_USING_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REVIEWED_UPDATED_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ListEditCaseHandlerTest {

    private static final String NEWPORT_ADDRESS = "Newport (South Wales) Immigration and Asylum Tribunal, "
                            + "Langstone Business Park, Newport, NP18 2LX";
    private static final String COVENTRY_ADDRESS = "Coventry Magistrates Court, Little Park Street, CV1 2SQ";
    private static final String MANCHESTER_ADDRESS = "Manchester Crown Court (Crown Square), "
                                                  + "Courts of Justice, Crown Square, M3 3FL";

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
    @Mock private LocationRefDataService locationRefDataService;
    @Mock private DirectionAppender directionAppender;
    @Mock
    private List<IdValue<Direction>> listOfDirections;
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
            directionAppender,
            locationRefDataService);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_set_default_list_case_hearing_centre_field() {

        when(locationRefDataService.getHearingCentreAddress(HearingCentre.NEWPORT))
            .thenReturn(NEWPORT_ADDRESS);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE_ADDRESS, NEWPORT_ADDRESS);

        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YES);
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
    void should_set_flags_and_not_add_direction_if_edit_case_listing() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of("2022-12-01"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(ACCELERATED_DETAINED_APPEAL_LISTED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(ADA_EDIT_LISTING_AVAILABLE, YesOrNo.YES);
        verify(asylumCase, times(1)).write(LISTING_AVAILABLE_FOR_ADA, YesOrNo.NO);
        verify(directionAppender, times(0)).append(any(AsylumCase.class),
                anyList(),
                anyString(),
                any(Parties.class),
                anyString(),
                any(DirectionTag.class),
                anyString());
        verify(asylumCase, times(0)).write(DIRECTIONS, listOfDirections);
    }

    @Test
    void should_set_hearing_centre_for_remote_hearing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.COVENTRY));
        String remoteAddress = "Remote hearing";
        when(locationRefDataService.getHearingCentreAddress(HearingCentre.REMOTE_HEARING))
            .thenReturn(remoteAddress);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE_ADDRESS, remoteAddress);

        verify(asylumCase, times(1)).clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YES);
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
        when(locationRefDataService.getHearingCentreAddress(HearingCentre.MANCHESTER))
            .thenReturn(MANCHESTER_ADDRESS);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE_ADDRESS, MANCHESTER_ADDRESS);
        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).clear(REVIEWED_UPDATED_HEARING_REQUIREMENTS);
        verify(asylumCase, times(1)).clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YES);
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
        when(locationRefDataService.getHearingCentreAddress(HearingCentre.COVENTRY))
            .thenReturn(COVENTRY_ADDRESS);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE_ADDRESS, COVENTRY_ADDRESS);
        verify(asylumCase, times(0)).write(HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).clear(REVIEWED_UPDATED_HEARING_REQUIREMENTS);
        verify(asylumCase, times(1)).clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YES);
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

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"EDIT_CASE_LISTING", "LIST_CASE"})
    void should_keep_listing_location_and_list_case_hearing_centre_aligned(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(LISTING_LOCATION, DynamicList.class))
            .thenReturn(Optional.of(
                new DynamicList(
                    new Value("386417", "Hatton Cross Tribunal Hearing Centre"),
                    List.of(
                        new Value("386417", "Hatton Cross Tribunal Hearing Centre"),
                        new Value("698118", "Bradford Tribunal Hearing Centre"),
                        new Value("765324", "Taylor House Tribunal Hearing Centre"))
                )
            ));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.HATTON_CROSS);
    }


    @Test
    void should_update_designated_hearing_centre_if_list_case_hearing_centre_field_is_not_listing_only() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(hearingCentreFinder.hearingCentreIsActive(HearingCentre.MANCHESTER)).thenReturn(true);
        when(locationRefDataService.getHearingCentreAddress(HearingCentre.MANCHESTER))
            .thenReturn(MANCHESTER_ADDRESS);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(0)).write(LIST_CASE_HEARING_CENTRE, HearingCentre.NEWPORT);
        verify(asylumCase, times(1)).write(LIST_CASE_HEARING_CENTRE_ADDRESS, MANCHESTER_ADDRESS);
        verify(asylumCase, times(1)).write(HEARING_CENTRE, HearingCentre.MANCHESTER);
        verify(asylumCase, times(1)).clear(REVIEWED_UPDATED_HEARING_REQUIREMENTS);
        verify(asylumCase, times(1)).clear(DOES_THE_CASE_NEED_TO_BE_RELISTED);

        verify(asylumCase, times(1)).write(CURRENT_HEARING_DETAILS_VISIBLE, YES);
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

    @ParameterizedTest
    @EnumSource(value = HearingCentre.class, names = {
        "BIRMINGHAM",
        "BRADFORD",
        "COVENTRY",
        "GLASGOW_TRIBUNALS_CENTRE",
        "HATTON_CROSS",
        "MANCHESTER",
        "NEWCASTLE",
        "NEWPORT",
        "NOTTINGHAM",
        "TAYLOR_HOUSE",
        "BELFAST",
        "HARMONDSWORTH",
        "HENDON",
        "YARLS_WOOD",
        "BRADFORD_KEIGHLEY",
        "MCC_MINSHULL",
        "MCC_CROWN_SQUARE",
        "MANCHESTER_MAGS",
        "NTH_TYNE_MAGS",
        "LEEDS_MAGS",
        "ALLOA_SHERRIF"
    })
    void should_save_expected_epims_id_in_ccd_when_submit_list_case(HearingCentre hearingCentre) {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(hearingCentre));
        when(hearingCentreFinder.hearingCentreIsActive(hearingCentre)).thenReturn(true);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                listEditCaseHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(EPIMS_ID, hearingCentre.getEpimsId());
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
