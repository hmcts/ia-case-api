package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EditAppealAfterSubmitHandlerTest {

    static final int APPEAL_OUT_OF_TIME_DAYS = 14;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;
    @Captor ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;

    String applicationSupplier = "Legal representative";
    String applicationReason = "applicationReason";
    String applicationDate = "30/01/2019";
    String applicationDecision = "Granted";
    String applicationDecisionReason = "Granted";
    String applicationDateOfDecision = "31/01/2019";
    String applicationStatus = "In progress";

    List<IdValue<Application>> applications = newArrayList(new IdValue<>("1", new Application(
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

    EditAppealAfterSubmitHandler editAppealAfterSubmitHandler;

    @BeforeEach
    void setUp() {

        editAppealAfterSubmitHandler = new EditAppealAfterSubmitHandler(dateProvider, APPEAL_OUT_OF_TIME_DAYS);
    }

    @Test
    void should_set_current_case_state_visible_to_case_officer_and_clear_application_flags_when_in_time() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.AWAITING_RESPONDENT_EVIDENCE));

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
        verify(asylumCase).read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class);
        verify(asylumCase).write(eq(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER), eq(State.AWAITING_RESPONDENT_EVIDENCE));

        verify(asylumCase).clear(NEW_MATTERS);

        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_set_current_case_state_visible_to_case_officer_and_clear_application_flags_when_out_of_time() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.AWAITING_RESPONDENT_EVIDENCE));

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
        verify(asylumCase).write(eq(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER), eq(State.AWAITING_RESPONDENT_EVIDENCE));

        verify(asylumCase).clear(NEW_MATTERS);

        assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
    }

    @Test
    void should_set_submission_out_of_time_when_out_of_time_mid_event() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(dateProvider.now()).thenReturn(LocalDate.parse("2020-04-08"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of("2020-03-08"));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).write(eq(SUBMISSION_OUT_OF_TIME), eq(YesOrNo.YES));
    }

    @Test
    void should_set_submission_in_time_mid_event() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

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
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_missing_home_office_decision_date() {

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.AWAITING_RESPONDENT_EVIDENCE));

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .hasMessage("homeOfficeDecisionDate is missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

        assertThatThrownBy(() -> editAppealAfterSubmitHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("homeOfficeDecisionDate is missing")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = editAppealAfterSubmitHandler.canHandle(callbackStage, callback);

                if (event == Event.EDIT_APPEAL_AFTER_SUBMIT
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    || callbackStage == PreSubmitCallbackStage.MID_EVENT) {

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
}
