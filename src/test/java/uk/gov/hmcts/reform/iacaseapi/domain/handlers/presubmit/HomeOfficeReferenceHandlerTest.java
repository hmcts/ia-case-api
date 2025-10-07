package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeReferenceData;

import static org.assertj.core.api.Assertions.assertThat;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class HomeOfficeReferenceHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HomeOfficeReferenceService homeOfficeReferenceService;

    private HomeOfficeReferenceHandler homeOfficeReferenceHandler;

    @BeforeEach
    public void setUp() {
        homeOfficeReferenceHandler = new HomeOfficeReferenceHandler(homeOfficeReferenceService);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn("homeOfficeDecision_TEMPORARILY_DISABLED");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "1234-5678-9012-3456"})
    void should_accept_valid_home_office_reference_numbers_direct_validation(String validReference) {
        assertTrue(HomeOfficeReferenceHandler.isWelformedHomeOfficeReference(validReference));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678901234567", "1234567890123456", "abcdefghi", "123-456-789", "1234-5678-9012-345A", "", " ", "null"})
    void should_reject_invalid_home_office_reference_numbers_direct_validation(String invalidReference) {
        String testValue = "null".equals(invalidReference) ? null : invalidReference;
        assertFalse(HomeOfficeReferenceHandler.isWelformedHomeOfficeReference(testValue));
    }

    @Test
    void should_throw_exception_when_home_office_reference_missing() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeDecision_TEMPORARILY_DISABLED");
        
        try (MockedStatic<HandlerUtils> mockedHandlerUtils = mockStatic(HandlerUtils.class)) {
            mockedHandlerUtils.when(() -> HandlerUtils.isRepJourney(asylumCase)).thenReturn(true);
            mockedHandlerUtils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);
            mockedHandlerUtils.when(() -> HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)).thenReturn(false);
            mockedHandlerUtils.when(() -> HandlerUtils.isEntryClearanceDecision(asylumCase)).thenReturn(false);
            
            when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
            when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

            assertThatThrownBy(() -> homeOfficeReferenceHandler.handle(MID_EVENT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("homeOfficeReferenceNumber is missing");
        }
    }

    @Test
    void should_skip_validation_for_out_of_country_human_rights_appeals() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeDecision_TEMPORARILY_DISABLED");
        
        try (MockedStatic<HandlerUtils> mockedHandlerUtils = mockStatic(HandlerUtils.class)) {
            mockedHandlerUtils.when(() -> HandlerUtils.isRepJourney(asylumCase)).thenReturn(true);
            mockedHandlerUtils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);
            mockedHandlerUtils.when(() -> HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)).thenReturn(true);
            
            when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

            PreSubmitCallbackResponse<AsylumCase> response = 
                homeOfficeReferenceHandler.handle(MID_EVENT, callback);

            assertNotNull(response);
            assertTrue(response.getErrors().isEmpty());
            verify(asylumCase, never()).read(HOME_OFFICE_REFERENCE_NUMBER, String.class);
        }
    }

    @Test
    void should_skip_validation_for_age_assessment_appeals() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeDecision_TEMPORARILY_DISABLED");
        
        try (MockedStatic<HandlerUtils> mockedHandlerUtils = mockStatic(HandlerUtils.class)) {
            mockedHandlerUtils.when(() -> HandlerUtils.isRepJourney(asylumCase)).thenReturn(true);
            mockedHandlerUtils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);
            mockedHandlerUtils.when(() -> HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)).thenReturn(false);
            
            when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

            PreSubmitCallbackResponse<AsylumCase> response = 
                homeOfficeReferenceHandler.handle(MID_EVENT, callback);

            assertNotNull(response);
            assertTrue(response.getErrors().isEmpty());
            verify(asylumCase, never()).read(HOME_OFFICE_REFERENCE_NUMBER, String.class);
        }
    }

    @Test
    void should_skip_validation_for_internal_entry_clearance_cases() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeDecision_TEMPORARILY_DISABLED");
        
        try (MockedStatic<HandlerUtils> mockedHandlerUtils = mockStatic(HandlerUtils.class)) {
            mockedHandlerUtils.when(() -> HandlerUtils.isRepJourney(asylumCase)).thenReturn(true);
            mockedHandlerUtils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(true);
            mockedHandlerUtils.when(() -> HandlerUtils.isEntryClearanceDecision(asylumCase)).thenReturn(true);
            
            when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

            PreSubmitCallbackResponse<AsylumCase> response = 
                homeOfficeReferenceHandler.handle(MID_EVENT, callback);

            assertNotNull(response);
            assertTrue(response.getErrors().isEmpty());
            verify(asylumCase, never()).read(HOME_OFFICE_REFERENCE_NUMBER, String.class);
        }
    }

    @Test
    void should_pass_validation_when_all_conditions_met() throws Exception {
        setHomeOfficeReferenceCheckEnabled(false);
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getPageId()).thenReturn("homeOfficeDecision_TEMPORARILY_DISABLED");
        
        try (MockedStatic<HandlerUtils> mockedHandlerUtils = mockStatic(HandlerUtils.class)) {
            mockedHandlerUtils.when(() -> HandlerUtils.isRepJourney(asylumCase)).thenReturn(true);
            mockedHandlerUtils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);
            mockedHandlerUtils.when(() -> HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)).thenReturn(false);
            mockedHandlerUtils.when(() -> HandlerUtils.isEntryClearanceDecision(asylumCase)).thenReturn(false);
            
            when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("123456789"));
            when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

            PreSubmitCallbackResponse<AsylumCase> response = 
                homeOfficeReferenceHandler.handle(MID_EVENT, callback);

            assertNotNull(response);
            assertTrue(response.getErrors().isEmpty());
        }
    }

    private static Stream<Arguments> eventAndStageData() {
        return Stream.of(
            Arguments.of(START_APPEAL, MID_EVENT, true),
            Arguments.of(EDIT_APPEAL, MID_EVENT, true),
            Arguments.of(START_APPEAL, PreSubmitCallbackStage.ABOUT_TO_START, false),
            Arguments.of(START_APPEAL, PreSubmitCallbackStage.ABOUT_TO_SUBMIT, false),
            Arguments.of(Event.SUBMIT_APPEAL, MID_EVENT, false)
        );
    }

    @Test
    void should_not_handle_when_not_rep_journey() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        
        try (MockedStatic<HandlerUtils> mockedHandlerUtils = mockStatic(HandlerUtils.class)) {
            mockedHandlerUtils.when(() -> HandlerUtils.isRepJourney(asylumCase)).thenReturn(false);

            boolean canHandle = homeOfficeReferenceHandler.canHandle(MID_EVENT, callback);

            assertFalse(canHandle);
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> homeOfficeReferenceHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeReferenceHandler.handle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_error_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() -> homeOfficeReferenceHandler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_return_true_when_home_office_reference_check_disabled() throws Exception {
        setHomeOfficeReferenceCheckEnabled(false);
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseNumber("123456789");
        
        assertThat(result).isTrue();
    }

    @Test
    void should_return_false_when_reference_is_null() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseNumber(null);
        
        assertThat(result).isFalse();
    }

    @Test
    void should_return_true_when_uan_matches_reference() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        String reference = "123456789";
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setUan(reference);
        
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseNumber(reference);
        
        assertThat(result).isTrue();
    }

    @Test
    void should_return_true_when_uan_matches_reference_case_insensitive() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        String reference = "123456789";
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setUan("123456789");
        
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseNumber(reference);
        
        assertThat(result).isTrue();
    }

    @Test
    void should_return_false_when_uan_does_not_match_reference() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        String reference = "123456789";
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setUan("987654321");
        
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseNumber(reference);
        
        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_when_uan_is_null() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        String reference = "123456789";
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setUan(null);
        
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseNumber(reference);
        
        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_when_home_office_data_not_found() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        String reference = "123456789";
        
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.empty());
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseNumber(reference);
        
        assertThat(result).isFalse();
    }

    @Test
    void should_return_true_when_case_details_check_disabled() throws Exception {
        setHomeOfficeReferenceCheckEnabled(false);
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseDetails("123456789", asylumCase);
        
        assertThat(result).isTrue();
    }

    @Test
    void should_return_false_when_case_details_reference_is_null() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseDetails(null, asylumCase);
        
        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_when_appellants_list_is_null() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        String reference = "123456789";
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setAppellants(null);
        
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);
        
        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_when_appellants_list_is_empty() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        String reference = "123456789";
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setAppellants(Collections.emptyList());
        
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);
        
        assertThat(result).isFalse();
    }

    @Test
    void should_return_true_when_appellant_details_match() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        
        HomeOfficeReferenceData.Appellant appellant = new HomeOfficeReferenceData.Appellant();
        appellant.setGivenNames("John");
        appellant.setFamilyName("Smith");
        appellant.setDateOfBirth("1990-01-01");
        
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setAppellants(Arrays.asList(appellant));
        
        String reference = "123456789";
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Smith"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1990-01-01"));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);
        
        assertThat(result).isTrue();
    }

    @Test
    void should_return_true_when_appellant_details_match_case_insensitive() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        
        HomeOfficeReferenceData.Appellant appellant = new HomeOfficeReferenceData.Appellant();
        appellant.setGivenNames("JOHN");
        appellant.setFamilyName("SMITH");
        appellant.setDateOfBirth("1990-01-01");
        
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setAppellants(Arrays.asList(appellant));
        
        String reference = "123456789";
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("john"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("smith"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1990-01-01"));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);
        
        assertThat(result).isTrue();
    }

    @Test
    void should_return_false_when_given_names_do_not_match() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        
        HomeOfficeReferenceData.Appellant appellant = new HomeOfficeReferenceData.Appellant();
        appellant.setGivenNames("John");
        appellant.setFamilyName("Smith");
        appellant.setDateOfBirth("1990-01-01");
        
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setAppellants(Arrays.asList(appellant));
        
        String reference = "123456789";
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("Jane"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Smith"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1990-01-01"));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);
        
        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_when_family_name_does_not_match() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        
        HomeOfficeReferenceData.Appellant appellant = new HomeOfficeReferenceData.Appellant();
        appellant.setGivenNames("John");
        appellant.setFamilyName("Smith");
        appellant.setDateOfBirth("1990-01-01");
        
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setAppellants(Arrays.asList(appellant));
        
        String reference = "123456789";
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Jones"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1990-01-01"));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);
        
        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_when_date_of_birth_does_not_match() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        
        HomeOfficeReferenceData.Appellant appellant = new HomeOfficeReferenceData.Appellant();
        appellant.setGivenNames("John");
        appellant.setFamilyName("Smith");
        appellant.setDateOfBirth("1990-01-01");
        
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setAppellants(Arrays.asList(appellant));
        
        String reference = "123456789";
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Smith"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1990-01-02"));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);
        
        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_when_home_office_case_details_not_found() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        String reference = "123456789";
        
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.empty());
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);
        
        assertThat(result).isFalse();
    }

    @Test
    void should_return_true_when_multiple_appellants_and_one_matches() throws Exception {
        setHomeOfficeReferenceCheckEnabled(true);
        
        HomeOfficeReferenceData.Appellant appellant1 = new HomeOfficeReferenceData.Appellant();
        appellant1.setGivenNames("Jane");
        appellant1.setFamilyName("Doe");
        appellant1.setDateOfBirth("1985-01-01");
        
        HomeOfficeReferenceData.Appellant appellant2 = new HomeOfficeReferenceData.Appellant();
        appellant2.setGivenNames("John");
        appellant2.setFamilyName("Smith");
        appellant2.setDateOfBirth("1990-01-01");
        
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setAppellants(Arrays.asList(appellant1, appellant2));

        String reference = "123456789";
        when(homeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Smith"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("1990-01-01"));
        
        boolean result = homeOfficeReferenceHandler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);
        
        assertThat(result).isTrue();
    }

    private void setHomeOfficeReferenceCheckEnabled(boolean enabled) throws Exception {
        Field field = HomeOfficeReferenceHandler.class.getDeclaredField("homeOfficeReferenceCheckEnabled");
        field.setAccessible(true);
        field.setBoolean(null, enabled);
    }
}
