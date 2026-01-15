package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTODIAL_SENTENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_CLIENT_LEAVE_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_CUSTODIAL_SENTENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_ENTRY_CLEARANCE_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EMAIL_RETYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MOBILE_NUMBER_RETYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OUT_OF_COUNTRY_DECISION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_AUTHORISATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_EMAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUITABILITY_HEARING_TYPE_YES_OR_NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPPER_TRIBUNAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.StartAppealMidEvent.APPELLANTS_ADDRESS_ADMIN_J_PAGE_ID;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Slf4j
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class StartAppealMidEventTest {

    private static final String HOME_OFFICE_REFERENCE_NUMBER_PAGE_ID = "homeOfficeReferenceNumber";
    private static final String OUT_OF_COUNTRY_PAGE_ID = "outOfCountry";
    private static final String DETENTION_FACILITY_PAGE_ID = "detentionFacility";
    private static final String SUITABILITY_ATTENDANCE_PAGE_ID = "suitabilityAppellantAttendance";
    private static final String UPPER_TRIBUNAL_REFERENCE_NUMBER_PAGE_ID = "utReferenceNumber";
    private static final String APPELLANTS_ADDRESS_PAGE_ID = "appellantAddress";
    private static final String INTERNAL_APPELLANTS_CONTACT_DETAILS = "appellantContactPreference";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private AsylumCase asylumCaseBefore;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCase;

    private String correctHomeOfficeReferenceFormatCid = "123456789";
    private String correctHomeOfficeReferenceFormatUan = "1234-5678-9876-5432";
    private String wrongHomeOfficeReferenceFormat = "A234567";
    private String callbackErrorMessage =
        "Enter the Home office reference or Case ID in the correct format. The Home office reference or Case ID cannot include letters and must be either 9 digits or 16 digits with dashes.";
    private String detentionFacilityErrorMessage = "You cannot update the detention location to a prison because this is an accelerated detained appeal.";
    private String getCallbackErrorMessageOutOfCountry = "This option is currently unavailable";
    private String correctUpperTribunalReferenceFormat = "UI-2020-123456";
    private String wrongUpperTribunalReferenceFormat = "UI-123456-2020";
    private String utReferenceErrorMessage = "Enter the Upper Tribunal reference number in the format UI-Year of submission-6 digit number. For example, UI-2020-123456.";
    private String providePostalAddressError = "The appellant must have provided a postal address";
    private String contactDetailsDoNotMatch = "The details given do not match";
    private StartAppealMidEvent startAppealMidEvent;

    @BeforeEach
    public void setUp() {
        startAppealMidEvent = new StartAppealMidEvent();

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {HOME_OFFICE_REFERENCE_NUMBER_PAGE_ID, OUT_OF_COUNTRY_PAGE_ID, DETENTION_FACILITY_PAGE_ID, APPELLANTS_ADDRESS_PAGE_ID, ""})
    void it_can_handle_callback(String pageId) {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(pageId);

            for (PreSubmitCallbackStage callbackStage : values()) {

                boolean canHandle = startAppealMidEvent.canHandle(callbackStage, callback);

                if ((event == Event.START_APPEAL || event == Event.EDIT_APPEAL || event == Event.EDIT_APPEAL_AFTER_SUBMIT
                    || event == Event.UPDATE_DETENTION_LOCATION)
                    && callbackStage == MID_EVENT
                    && (callback.getPageId().equals(DETENTION_FACILITY_PAGE_ID)
                        || callback.getPageId().equals(HOME_OFFICE_REFERENCE_NUMBER_PAGE_ID)
                        || callback.getPageId().equals(APPELLANTS_ADDRESS_PAGE_ID)
                        || callback.getPageId().equals(OUT_OF_COUNTRY_PAGE_ID))) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> startAppealMidEvent.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> startAppealMidEvent.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> startAppealMidEvent.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> startAppealMidEvent.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_error_when_home_office_reference_format_is_wrong() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(wrongHomeOfficeReferenceFormat));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1).containsOnly(callbackErrorMessage);
    }

    @Test
    void should_error_when_trying_to_select_out_of_country_path_for_internal_case_creation() {
        when(callback.getPageId()).thenReturn(OUT_OF_COUNTRY_PAGE_ID);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1).containsOnly(getCallbackErrorMessageOutOfCountry);
    }

    @Test
    void should_successfully_validate_when_home_office_reference_format_is_correct_cid() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(correctHomeOfficeReferenceFormatCid));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_successfully_validate_when_home_office_reference_format_is_correct_uan() {
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(correctHomeOfficeReferenceFormatUan));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_not_touch_home_office_reference_numbers_when_ooc_and_refusal_of_human_rights_is_decided() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(REFUSAL_OF_HUMAN_RIGHTS));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(any(),any());
    }

    @Test
    void should_not_touch_home_office_reference_numbers_when_ooc_and_refuse_Permit_is_decided() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
                .thenReturn(Optional.of(OutOfCountryDecisionType.REFUSE_PERMIT));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(any(),any());
    }

    @ParameterizedTest
    @EnumSource(DetentionFacility.class)
    void should_set_is_accelerated_detained_value_to_no_if_prison_or_other_detention_facility(DetentionFacility detentionFacility) {
        when(callback.getPageId()).thenReturn(DETENTION_FACILITY_PAGE_ID);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (Arrays.asList(DetentionFacility.PRISON.toString(), DetentionFacility.PRISON.toString()).contains(detentionFacility)) {
            assertEquals(YesOrNo.NO, callbackResponse.getData().get("isAcceleratedDetainedAppeal"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {HOME_OFFICE_REFERENCE_NUMBER_PAGE_ID, OUT_OF_COUNTRY_PAGE_ID, DETENTION_FACILITY_PAGE_ID})
    void should_only_set_is_accelerated_detained_if_correct_page_id(String pageId) {
        when(callback.getPageId()).thenReturn(pageId);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(correctHomeOfficeReferenceFormatCid));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = startAppealMidEvent.handle(MID_EVENT, callback);

        if (pageId.equals(DETENTION_FACILITY_PAGE_ID)) {
            verify(asylumCase, times(1)).write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.NO);
        } else {
            verify(asylumCase, never()).write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class);
        }
    }


    @Test
    void should_error_when_detention_facility_for_ada_is_changed() {
        when(callback.getEvent()).thenReturn(Event.UPDATE_DETENTION_LOCATION);
        when(callback.getPageId()).thenReturn("detentionFacility");

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class))
                .thenReturn(Optional.of("prison"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1).containsOnly(detentionFacilityErrorMessage);
    }

    @Test
    void should_clear_out_of_country_fields_when_switched_to_in_country() {
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);
        when(callback.getPageId()).thenReturn(OUT_OF_COUNTRY_PAGE_ID);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCaseBefore.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).clear(OUT_OF_COUNTRY_DECISION_TYPE);
        verify(asylumCase, times(1)).clear(GWF_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(DATE_ENTRY_CLEARANCE_DECISION);
        verify(asylumCase, times(1)).clear(DATE_CLIENT_LEAVE_UK);
        verify(asylumCase, times(1)).clear(HAS_SPONSOR);
        verify(asylumCase, times(1)).clear(SPONSOR_GIVEN_NAMES);
        verify(asylumCase, times(1)).clear(SPONSOR_FAMILY_NAME);
        verify(asylumCase, times(1)).clear(SPONSOR_ADDRESS);
        verify(asylumCase, times(1)).clear(SPONSOR_CONTACT_PREFERENCE);
        verify(asylumCase, times(1)).clear(SPONSOR_EMAIL);
        verify(asylumCase, times(1)).clear(SPONSOR_MOBILE_NUMBER);
        verify(asylumCase, times(1)).clear(SPONSOR_AUTHORISATION);


    }

    @Test
    void should_error_when_internal_appellant_emails_do_not_match() {
        when(callback.getPageId()).thenReturn(INTERNAL_APPELLANTS_CONTACT_DETAILS);

        when(asylumCase.read(EMAIL, String.class))
                .thenReturn(Optional.of("email@test.com"));
        when(asylumCase.read(EMAIL_RETYPE, String.class))
                .thenReturn(Optional.of("wrongemail@test.com"));

        when(asylumCase.read(MOBILE_NUMBER, String.class))
                .thenReturn(Optional.of("07898999999"));
        when(asylumCase.read(MOBILE_NUMBER_RETYPE, String.class))
                .thenReturn(Optional.of("07898999991"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
//        assertEquals(asylumCase, callbackResponse.getData());
//        final Set<String> errors = callbackResponse.getErrors();
//        assertThat(errors).hasSize(1).containsOnly(contactDetailsDoNotMatch);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_write_no_value_for_appellant_attendance_2_field_when_hearing_type_is_yes(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(SUITABILITY_ATTENDANCE_PAGE_ID);

        when(asylumCase.read(SUITABILITY_HEARING_TYPE_YES_OR_NO, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2, YesOrNo.NO);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_write_no_value_for_appellant_attendance_1_field_when_hearing_type_is_no(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(SUITABILITY_ATTENDANCE_PAGE_ID);

        when(asylumCase.read(SUITABILITY_HEARING_TYPE_YES_OR_NO, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, times(1)).write(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1, YesOrNo.NO);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_error_when_upper_tribunal_reference_number_format_is_wrong(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(UPPER_TRIBUNAL_REFERENCE_NUMBER_PAGE_ID);
        when(asylumCase.read(UPPER_TRIBUNAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(wrongUpperTribunalReferenceFormat));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1).containsOnly(utReferenceErrorMessage);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT"
    })
    void should_validate_as_correct_format_for_upper_tribunal_reference_number(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(UPPER_TRIBUNAL_REFERENCE_NUMBER_PAGE_ID);
        when(asylumCase.read(UPPER_TRIBUNAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(correctUpperTribunalReferenceFormat));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @Test
    void should_validate_if_upper_tribunal_reference_number_is_missing() {
        when(callback.getPageId()).thenReturn(UPPER_TRIBUNAL_REFERENCE_NUMBER_PAGE_ID);
        when(asylumCase.read(UPPER_TRIBUNAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> startAppealMidEvent.handle(MID_EVENT, callback))
            .hasMessage("upperTribunalReferenceNumber is missing")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT" })
    void should_error_when_appellant_has_no_fixed_address(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(APPELLANTS_ADDRESS_PAGE_ID);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(1).containsOnly(providePostalAddressError);
    }

    @Test
    void should_pass_the_validation_if_user_is_not_admin() {
        when(callback.getPageId()).thenReturn(APPELLANTS_ADDRESS_ADMIN_J_PAGE_ID);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(0);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT" })
    void should_pass_the_validation_if_user_is_not_admin(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(APPELLANTS_ADDRESS_PAGE_ID);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT" })
    void should_validate_when_appellant_has_fixed_address(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(APPELLANTS_ADDRESS_PAGE_ID);
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT" })
    void should_validate_when_appellant_admin_journey_has_fixed_address(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(APPELLANTS_ADDRESS_ADMIN_J_PAGE_ID);
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(0);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "START_APPEAL", "EDIT_APPEAL"})
    void should_clear_custodial_sentence_immigrationRemovalCentre(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(DETENTION_FACILITY_PAGE_ID);
        when(asylumCase.read(DETENTION_FACILITY, String.class))
                .thenReturn(Optional.of("immigrationRemovalCentre"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(0);
        verify(asylumCase, times(1)).write(CUSTODIAL_SENTENCE, YesOrNo.NO);
        verify(asylumCase, times(1)).clear(DATE_CUSTODIAL_SENTENCE);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "START_APPEAL", "EDIT_APPEAL"})
    void should_not_clear_custodial_sentence_not_immigrationRemovalCentre(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(DETENTION_FACILITY_PAGE_ID);
        when(asylumCase.read(DETENTION_FACILITY, String.class))
                .thenReturn(Optional.of("prison"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(0);
        verify(asylumCase, times(0)).write(CUSTODIAL_SENTENCE, YesOrNo.NO);
        verify(asylumCase, times(0)).clear(DATE_CUSTODIAL_SENTENCE);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "START_APPEAL", "EDIT_APPEAL"})
    void should_not_clear_custodial_sentence_detentionFacility_empty(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(DETENTION_FACILITY_PAGE_ID);
        when(asylumCase.read(DETENTION_FACILITY, String.class))
                .thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(0);
        verify(asylumCase, times(0)).write(CUSTODIAL_SENTENCE, YesOrNo.NO);
        verify(asylumCase, times(0)).clear(DATE_CUSTODIAL_SENTENCE);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "EDIT_APPEAL_AFTER_SUBMIT"})
    void should_not_clear_custodial_sentence_wrong_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn(DETENTION_FACILITY_PAGE_ID);
        when(asylumCase.read(DETENTION_FACILITY, String.class))
                .thenReturn(Optional.of("immigrationRemovalCentre"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                startAppealMidEvent.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        final Set<String> errors = callbackResponse.getErrors();
        assertThat(errors).hasSize(0);
        verify(asylumCase, times(0)).write(CUSTODIAL_SENTENCE, YesOrNo.NO);
        verify(asylumCase, times(0)).clear(DATE_CUSTODIAL_SENTENCE);
    }
}
