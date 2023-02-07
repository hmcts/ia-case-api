package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class EditAppealAfterSubmitHandlerTest {

    private static final int APPEAL_OUT_OF_TIME_DAYS_UK = 14;
    private static final int APPEAL_OUT_OF_TIME_DAYS_OOC = 28;
    private static final int APPEAL_OUT_OF_TIME_ADA_WORKING_DAYS = 5;
    private static final String HOME_OFFICE_DECISION_PAGE_ID = "homeOfficeDecision";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private DueDateService dueDateService;
    @Captor
    private ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;

    private String applicationSupplier = "Legal representative";
    private String applicationReason = "applicationReason";
    private String applicationDate = "30/01/2019";
    private String applicationDecision = "Granted";
    private String applicationDecisionReason = "Granted";
    private String applicationDateOfDecision = "31/01/2019";
    private String applicationStatus = "In progress";
    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractor;
    @Captor
    private ArgumentCaptor<YesOrNo> outOfTime;
    @Captor
    private ArgumentCaptor<YesOrNo> recordedOutOfTimeDecision;

    private List<IdValue<Application>> applications = newArrayList(new IdValue<>("1", new Application(
        Collections.emptyList(),
        applicationSupplier,
        ApplicationType.EDIT_APPEAL_AFTER_SUBMIT.toString(),
        applicationReason,
        applicationDate,
        applicationDecision,
        applicationDecisionReason,
        applicationDateOfDecision,
        applicationStatus
    )));

    private EditAppealAfterSubmitHandler editAppealAfterSubmitHandler;

    @BeforeEach
    public void setUp() {
        editAppealAfterSubmitHandler = new EditAppealAfterSubmitHandler(dateProvider,dueDateService,APPEAL_OUT_OF_TIME_DAYS_UK,APPEAL_OUT_OF_TIME_DAYS_OOC,APPEAL_OUT_OF_TIME_ADA_WORKING_DAYS);

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.AWAITING_RESPONDENT_EVIDENCE));
        when(callback.getPageId()).thenReturn(HOME_OFFICE_DECISION_PAGE_ID);
    }

    @Test
    void should_set_current_case_state_visible_to_case_officer_and_clear_application_flags_when_in_time() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of("2020-04-08"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(SUBMISSION_OUT_OF_TIME), eq(YesOrNo.NO));
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_DOCUMENT);

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        verify(asylumCase).clear(APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS);
        verify(asylumCase).clear(RECORDED_OUT_OF_TIME_DECISION);
        verify(asylumCase).read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class);
        verify(asylumCase)
            .write(eq(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER), eq(State.AWAITING_RESPONDENT_EVIDENCE));

        verify(asylumCase).clear(NEW_MATTERS);

        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_set_current_case_state_visible_to_case_officer_and_clear_application_flags_when_in_time_ooc() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE)).thenReturn(Optional.of("2020-04-08"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(SUBMISSION_OUT_OF_TIME, YesOrNo.NO);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_DOCUMENT);

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        verify(asylumCase).clear(APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS);
        verify(asylumCase).read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class);
        verify(asylumCase)
            .write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.AWAITING_RESPONDENT_EVIDENCE);

        verify(asylumCase).clear(NEW_MATTERS);

        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_set_current_case_state_visible_to_case_officer_and_clear_application_flags_when_out_of_time() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of("2020-03-08"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(SUBMISSION_OUT_OF_TIME), eq(YesOrNo.YES));

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        verify(asylumCase).clear(APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS);
        verify(asylumCase).read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class);
        verify(asylumCase)
            .write(eq(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER), eq(State.AWAITING_RESPONDENT_EVIDENCE));

        verify(asylumCase).clear(NEW_MATTERS);

        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_set_current_case_state_visible_to_case_officer_and_clear_application_flags_when_ooc_and_removal_of_client_is_decided() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE)).thenReturn(Optional.of("2020-03-08"));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        verify(asylumCase).clear(APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS);
        verify(asylumCase).read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class);
        verify(asylumCase)
            .write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.AWAITING_RESPONDENT_EVIDENCE);

        verify(asylumCase).clear(NEW_MATTERS);

        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_set_current_case_state_visible_to_case_officer_and_clear_application_flags_when_ooc_and_refusal_of_human_rights_is_decided() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));
        when(asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION)).thenReturn(Optional.of("2020-03-08"));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        verify(asylumCase).clear(APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS);
        verify(asylumCase).read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class);
        verify(asylumCase)
            .write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.AWAITING_RESPONDENT_EVIDENCE);

        verify(asylumCase).clear(NEW_MATTERS);

        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_set_current_case_state_visible_to_case_officer_and_clear_application_flags_when_ooc_and_refusal_of_protection_is_decided() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_PROTECTION));
        when(asylumCase.read(DATE_CLIENT_LEAVE_UK)).thenReturn(Optional.of("2020-03-08"));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

        verify(asylumCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
        verify(asylumCase).clear(APPLICATION_EDIT_APPEAL_AFTER_SUBMIT_EXISTS);
        verify(asylumCase).read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class);
        verify(asylumCase)
            .write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.AWAITING_RESPONDENT_EVIDENCE);

        verify(asylumCase).clear(NEW_MATTERS);

        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_set_submission_out_of_time_when_out_of_time_mid_event() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of("2020-03-08"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(SUBMISSION_OUT_OF_TIME, YesOrNo.YES);
    }

    @Test
    void should_set_submission_out_of_time_when_out_of_time_mid_event_ooc() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE)).thenReturn(Optional.of("2020-03-08"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(SUBMISSION_OUT_OF_TIME), eq(YesOrNo.YES));
    }

    @Test
    void should_set_submission_in_time_mid_event() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of("2020-04-08"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(SUBMISSION_OUT_OF_TIME), eq(YesOrNo.NO));
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_DOCUMENT);
    }

    @Test
    void should_validate_home_office_decision_date_when_ooc_and_refusal_of_human_rights_is_decided() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION)).thenReturn(Optional.of("2020-04-08"));

        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(SUBMISSION_OUT_OF_TIME, YesOrNo.NO);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_DOCUMENT);
    }

    @Test
    void should_set_submission_in_time_mid_event_ooc() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE)).thenReturn(Optional.of("2020-04-08"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(SUBMISSION_OUT_OF_TIME, YesOrNo.NO);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_DOCUMENT);
    }

    @Test
    void should_set_submission_in_time_mid_event_for_decision_letter_date_received() {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE)).thenReturn(Optional.of("2020-04-08"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(SUBMISSION_OUT_OF_TIME, YesOrNo.NO);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_DOCUMENT);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_exception_when_out_of_country_decision_letter_date_received_is_missing() {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("decisionLetterReceivedDate is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("decisionLetterReceivedDate is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    void should_throw_exception_when_out_of_country_date_client_leave_uk_is_missing() {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_PROTECTION));
        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("dateClientLeaveUk is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("dateClientLeaveUk is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    void should_throw_exception_when_out_of_country_and_date_entry_clearance_decision_is_missing() {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));
        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("dateEntryClearanceDecision is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("dateEntryClearanceDecision is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    void handling_should_throw_if_missing_home_office_decision_date() {

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("homeOfficeDecisionDate is missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("homeOfficeDecisionDate is missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { HOME_OFFICE_DECISION_PAGE_ID, "" })
    @SuppressWarnings("unchecked")
    void it_can_handle_callback(String pageId) {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getPageId()).thenReturn(pageId);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = editAppealAfterSubmitHandler.canHandle(callbackStage, callback);

                if ((event == Event.EDIT_APPEAL_AFTER_SUBMIT
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    || callbackStage == PreSubmitCallbackStage.MID_EVENT)
                    && callback.getPageId().equals(HOME_OFFICE_DECISION_PAGE_ID)) {

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

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handles_ada_case_when_in_time() {

        final String receivedLetterDate = "2022-11-18";
        final String dueDate = "2022-11-23";
        final String nowDate = "2022-11-20";
        final ZonedDateTime zonedDateTime = LocalDate.parse(receivedLetterDate).atStartOfDay(ZoneOffset.UTC);
        final ZonedDateTime zonedDueDateTime = LocalDate.parse(dueDate).atStartOfDay(ZoneOffset.UTC);

        when(dateProvider.now()).thenReturn(LocalDate.parse(nowDate));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE)).thenReturn(Optional.of(receivedLetterDate));
        when(dueDateService.calculateDueDate(zonedDateTime, APPEAL_OUT_OF_TIME_ADA_WORKING_DAYS)).thenReturn(zonedDueDateTime);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(NO);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_EXPLANATION);
        verify(asylumCase).clear(APPLICATION_OUT_OF_TIME_DOCUMENT);
        verify(asylumCase).clear(RECORDED_OUT_OF_TIME_DECISION);
    }

    @Test
    void handles_ada_case_when_out_of_time() {

        final String receivedLetterDate = "2022-11-10";
        final String dueDate = "2022-11-15";
        final String nowDate = "2022-11-20";
        final ZonedDateTime zonedDateTime = LocalDate.parse(receivedLetterDate).atStartOfDay(ZoneOffset.UTC);
        final ZonedDateTime zonedDueDateTime = LocalDate.parse(dueDate).atStartOfDay(ZoneOffset.UTC);

        when(dateProvider.now()).thenReturn(LocalDate.parse(nowDate));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE)).thenReturn(Optional.of(receivedLetterDate));
        when(dueDateService.calculateDueDate(zonedDateTime, APPEAL_OUT_OF_TIME_ADA_WORKING_DAYS)).thenReturn(zonedDueDateTime);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(2)).write(asylumExtractor.capture(), outOfTime.capture());
        verify(asylumCase, times(2)).write(asylumExtractor.capture(), recordedOutOfTimeDecision.capture());

        assertThat(asylumExtractor.getAllValues().contains(SUBMISSION_OUT_OF_TIME));
        assertThat(asylumExtractor.getValue()).isEqualTo(RECORDED_OUT_OF_TIME_DECISION);
        assertThat(outOfTime.getValue()).usingRecursiveComparison().getRecursiveComparisonConfiguration().equals(YES);
        assertThat(recordedOutOfTimeDecision.getValue()).usingRecursiveComparison().getRecursiveComparisonConfiguration().equals(NO);
    }

    @Test
    void should_throw_exception_when_ada_decision_received_date_is_missing() {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("decisionLetterReceivedDate is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

    }
}
