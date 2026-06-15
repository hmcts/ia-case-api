package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_ARRIVAL_IN_UK;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_HEARING_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class DateValidationHandlerTest {

    private static final Set<Event> eventsToHandle = Set.of(Event.START_APPLICATION,
                                                            Event.EDIT_BAIL_APPLICATION,
                                                            Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT,
                                                            Event.MAKE_NEW_APPLICATION);

    private static final Set<BailCaseFieldDefinition> fieldsToHandle = Set.of(APPLICANT_ARRIVAL_IN_UK,
                                                                              BAIL_HEARING_DATE,
                                                                              SUPPORTER_DOB,
                                                                              SUPPORTER_2_DOB,
                                                                              SUPPORTER_3_DOB,
                                                                              SUPPORTER_4_DOB);

    private static final Map<String, BailCaseFieldDefinition> pageIdsToHandle = fieldsToHandle.stream()
        .collect(Collectors.toMap(BailCaseFieldDefinition::value, def -> def));

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private BailCase bailCase;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private DateProvider dateProvider;

    private DateValidationHandler dateValidationHandler;

    private final LocalDate now = LocalDate.now();

    private final LocalDate tomorrow = now.plusDays(1);
    private final String tomorrowString = tomorrow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    private final LocalDate yesterday = now.minusDays(1);
    private final String yesterdayString = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    private static final String FUTURE_DATE_ERROR_MESSAGE = "The date must not be a future date.";

    @BeforeEach
    public void setUp() {
        dateValidationHandler = new DateValidationHandler(dateProvider);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(dateProvider.now()).thenReturn(now);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_error_if_applicantArrivalInUKDate_is_future(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("applicantArrivalInUKDate");
        when(bailCase.read(APPLICANT_ARRIVAL_IN_UK, String.class)).thenReturn(Optional.of(tomorrowString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals(FUTURE_DATE_ERROR_MESSAGE, response.getErrors().stream().findFirst().get());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_applicantArrivalInUKDate_is_past(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("applicantArrivalInUKDate");
        when(bailCase.read(APPLICANT_ARRIVAL_IN_UK, String.class)).thenReturn(Optional.of(yesterdayString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_error_if_applicantDateOfBirth_is_future(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("applicantDateOfBirth");
        when(bailCase.read(APPLICANT_DOB, String.class)).thenReturn(Optional.of(tomorrowString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals(FUTURE_DATE_ERROR_MESSAGE, response.getErrors().stream().findFirst().get());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_applicantDateOfBirth_is_past(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("applicantDateOfBirth");
        when(bailCase.read(APPLICANT_DOB, String.class)).thenReturn(Optional.of(yesterdayString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_error_if_bailHearingDate_is_future(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("bailHearingDate");
        when(bailCase.read(BAIL_HEARING_DATE, String.class)).thenReturn(Optional.of(tomorrowString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals(FUTURE_DATE_ERROR_MESSAGE, response.getErrors().stream().findFirst().get());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_bailHearingDate_is_past(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("bailHearingDate");
        when(bailCase.read(APPLICANT_ARRIVAL_IN_UK, String.class)).thenReturn(Optional.of(yesterdayString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_bailHearingDate_is_blank(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("bailHearingDate");
        when(bailCase.read(APPLICANT_ARRIVAL_IN_UK, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_error_if_supporterDob_is_future(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporterDOB");
        when(bailCase.read(SUPPORTER_DOB, String.class)).thenReturn(Optional.of(tomorrowString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals(FUTURE_DATE_ERROR_MESSAGE, response.getErrors().stream().findFirst().get());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_supporterDob_is_past(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporterDOB");
        when(bailCase.read(SUPPORTER_DOB, String.class)).thenReturn(Optional.of(yesterdayString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_supporterDob_is_blank(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporterDOB");
        when(bailCase.read(SUPPORTER_DOB, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_error_if_supporter2Dob_is_future(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporter2DOB");
        when(bailCase.read(SUPPORTER_2_DOB, String.class)).thenReturn(Optional.of(tomorrowString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals(FUTURE_DATE_ERROR_MESSAGE, response.getErrors().stream().findFirst().get());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_supporter2Dob_is_past(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporter2DOB");
        when(bailCase.read(SUPPORTER_2_DOB, String.class)).thenReturn(Optional.of(yesterdayString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_supporter2Dob_is_blank(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporter2DOB");
        when(bailCase.read(SUPPORTER_2_DOB, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_error_if_supporter3Dob_is_future(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporter3DOB");
        when(bailCase.read(SUPPORTER_3_DOB, String.class)).thenReturn(Optional.of(tomorrowString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals(FUTURE_DATE_ERROR_MESSAGE, response.getErrors().stream().findFirst().get());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_supporter3Dob_is_past(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporter3DOB");
        when(bailCase.read(SUPPORTER_3_DOB, String.class)).thenReturn(Optional.of(yesterdayString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_supporter3Dob_is_blank(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporter3DOB");
        when(bailCase.read(SUPPORTER_3_DOB, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_add_error_if_supporter4Dob_is_future(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporter4DOB");
        when(bailCase.read(SUPPORTER_4_DOB, String.class)).thenReturn(Optional.of(tomorrowString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals(FUTURE_DATE_ERROR_MESSAGE, response.getErrors().stream().findFirst().get());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_supporter4Dob_is_past(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporter4DOB");
        when(bailCase.read(SUPPORTER_4_DOB, String.class)).thenReturn(Optional.of(yesterdayString));

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }

    @ParameterizedTest
    @EnumSource(value = Event.class,
        names = {"START_APPLICATION",
            "EDIT_BAIL_APPLICATION",
            "EDIT_BAIL_APPLICATION_AFTER_SUBMIT",
            "MAKE_NEW_APPLICATION"})
    void should_not_add_error_if_supporter4Dob_is_blank(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("supporter4DOB");
        when(bailCase.read(SUPPORTER_4_DOB, String.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<BailCase> response = dateValidationHandler
            .handle(MID_EVENT, callback);

        assertNotNull(response);
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(bailCase);
        assertEquals(0, response.getErrors().size());

    }


    @Test
    void handler_can_handle_callback() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = dateValidationHandler.canHandle(callbackStage, callback);
                if (callbackStage == PreSubmitCallbackStage.MID_EVENT
                    && eventsToHandle.contains(callback.getEvent())
                    && pageIdsToHandle.containsKey(callback.getPageId())) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {
        //invalid stage
        Assertions.assertThatThrownBy(() -> dateValidationHandler.handle(ABOUT_TO_START, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);

        //invalid event
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        Assertions.assertThatThrownBy(() -> dateValidationHandler.handle(ABOUT_TO_SUBMIT, callback)).hasMessage(
            "Cannot handle callback").isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> dateValidationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> dateValidationHandler
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> dateValidationHandler
            .handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> dateValidationHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

}

