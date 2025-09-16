package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

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

    private HomeOfficeReferenceHandler homeOfficeReferenceHandler;

    @BeforeEach
    public void setUp() {
        homeOfficeReferenceHandler = new HomeOfficeReferenceHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn("homeOfficeDecision");
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456789", "1234567890123456"})
    void should_accept_valid_home_office_reference_numbers(String validReference) {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        
        try (MockedStatic<HandlerUtils> mockedHandlerUtils = mockStatic(HandlerUtils.class)) {
            mockedHandlerUtils.when(() -> HandlerUtils.isRepJourney(asylumCase)).thenReturn(true);
            mockedHandlerUtils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);
            mockedHandlerUtils.when(() -> HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)).thenReturn(false);
            mockedHandlerUtils.when(() -> HandlerUtils.isEntryClearanceDecision(asylumCase)).thenReturn(false);
            
            when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(validReference));
            when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

            PreSubmitCallbackResponse<AsylumCase> response = 
                homeOfficeReferenceHandler.handle(MID_EVENT, callback);

            assertNotNull(response);
            assertEquals(asylumCase, response.getData());
            assertTrue(response.getErrors().isEmpty());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "12345678901234567", "abcdefghi", "123-456-789", "", " "})
    void should_reject_invalid_home_office_reference_numbers(String invalidReference) {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        
        try (MockedStatic<HandlerUtils> mockedHandlerUtils = mockStatic(HandlerUtils.class)) {
            mockedHandlerUtils.when(() -> HandlerUtils.isRepJourney(asylumCase)).thenReturn(true);
            mockedHandlerUtils.when(() -> HandlerUtils.isInternalCase(asylumCase)).thenReturn(false);
            mockedHandlerUtils.when(() -> HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)).thenReturn(false);
            mockedHandlerUtils.when(() -> HandlerUtils.isEntryClearanceDecision(asylumCase)).thenReturn(false);
            
            when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(invalidReference));
            when(asylumCase.read(AGE_ASSESSMENT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

            PreSubmitCallbackResponse<AsylumCase> response = 
                homeOfficeReferenceHandler.handle(MID_EVENT, callback);

            assertNotNull(response);
            assertEquals(1, response.getErrors().size());
            assertTrue(response.getErrors().contains("Home Office Reference must be either 9 or 16 digits"));
        }
    }

    @Test
    void should_throw_exception_when_home_office_reference_missing() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        
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

    private static Stream<Arguments> eventAndStageData() {
        return Stream.of(
            Arguments.of(START_APPEAL, MID_EVENT, true),
            Arguments.of(EDIT_APPEAL, MID_EVENT, true),
            Arguments.of(START_APPEAL, PreSubmitCallbackStage.ABOUT_TO_START, false),
            Arguments.of(START_APPEAL, PreSubmitCallbackStage.ABOUT_TO_SUBMIT, false),
            Arguments.of(Event.SUBMIT_APPEAL, MID_EVENT, false)
        );
    }

    @ParameterizedTest
    @MethodSource("eventAndStageData")
    void should_handle_callback_correctly(Event event, PreSubmitCallbackStage stage, boolean shouldHandle) {
        when(callback.getEvent()).thenReturn(event);
        
        try (MockedStatic<HandlerUtils> mockedHandlerUtils = mockStatic(HandlerUtils.class)) {
            mockedHandlerUtils.when(() -> HandlerUtils.isRepJourney(asylumCase)).thenReturn(true);

            boolean canHandle = homeOfficeReferenceHandler.canHandle(stage, callback);

            assertEquals(shouldHandle, canHandle);
        }
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
    void should_not_handle_when_wrong_page_id() {
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getPageId()).thenReturn("differentPage");
        
        try (MockedStatic<HandlerUtils> mockedHandlerUtils = mockStatic(HandlerUtils.class)) {
            mockedHandlerUtils.when(() -> HandlerUtils.isRepJourney(asylumCase)).thenReturn(true);

            boolean canHandle = homeOfficeReferenceHandler.canHandle(MID_EVENT, callback);

            assertFalse(canHandle);
        }
    }

    @Test
    void should_not_handle_when_journey_type_missing() {
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

        assertThatThrownBy(() -> homeOfficeReferenceHandler.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeReferenceHandler.handle(null, callback))
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

}
