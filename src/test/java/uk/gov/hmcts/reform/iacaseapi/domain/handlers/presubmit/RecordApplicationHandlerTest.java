package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationDecision.GRANTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationDecision.REFUSED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class RecordApplicationHandlerTest {

    private final List<State> editListingStates = newArrayList(
        State.PREPARE_FOR_HEARING,
        State.FINAL_BUNDLING,
        State.PRE_HEARING,
        State.DECISION
    );

    private final List<State> timeExtensionSates = newArrayList(
        State.AWAITING_RESPONDENT_EVIDENCE,
        State.CASE_BUILDING,
        State.CASE_UNDER_REVIEW,
        State.RESPONDENT_REVIEW,
        State.SUBMIT_HEARING_REQUIREMENTS
    );

    private final List<State> updateHearingRequirementsStates = newArrayList(
        State.PRE_HEARING,
        State.FINAL_BUNDLING,
        State.PREPARE_FOR_HEARING,
        State.DECISION
    );

    @Mock private NotificationSender<AsylumCase> notificationSender;
    @Mock private Appender<Application> appender;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private AsylumCase asylumCaseWithNotifications;
    @Mock private DateProvider dateProvider;
    @Mock private UserDetailsProvider userProvider;
    @Mock private Application existingApplication;
    @Mock private List allAppendedApplications;
    @Mock private UserDetails userDetails;
    @Mock private List<IdValue<Document>> newApplicationDocuments;

    @Captor private ArgumentCaptor<List<IdValue<Application>>> existingApplicationsCaptor;
    @Captor private ArgumentCaptor<Application> newApplicationCaptor;

    private final LocalDate now = LocalDate.now();
    private final List<Application> existingApplications = singletonList(existingApplication);

    private String applicationSupplier = "The respondent";
    private String applicationType = TIME_EXTENSION.toString();
    private String applicationReason = "some-reason";
    private String applicationDate = "31/01/2019";
    private String applicationDecision = REFUSED.toString();
    private String applicationDecisionReason = "some-decision-reason";
    private String applicationStatus = "Completed";

    private RecordApplicationHandler recordApplicationHandler;

    @Before
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_APPLICATION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getState()).thenReturn(State.CASE_BUILDING);

        when(dateProvider.now()).thenReturn(now);

        when(asylumCase.read(APPLICATIONS)).thenReturn(Optional.of(existingApplications));

        when(asylumCase.read(APPLICATION_SUPPLIER, String.class)).thenReturn(Optional.of(applicationSupplier));
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(applicationType));
        when(asylumCase.read(APPLICATION_REASON, String.class)).thenReturn(Optional.of(applicationReason));
        when(asylumCase.read(APPLICATION_DATE, String.class)).thenReturn(Optional.of(applicationDate));
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(applicationDecision));
        when(asylumCase.read(APPLICATION_DECISION_REASON, String.class)).thenReturn(Optional.of(applicationDecisionReason));
        when(asylumCase.read(APPLICATION_DOCUMENTS)).thenReturn(Optional.of(newApplicationDocuments));

        when(appender.append(any(Application.class), anyList()))
            .thenReturn(allAppendedApplications);

        when(notificationSender.send(any(Callback.class))).thenReturn(asylumCaseWithNotifications);

        recordApplicationHandler = new RecordApplicationHandler(appender, dateProvider, notificationSender);
    }

    @Test
    public void should_append_new_application_to_existing_applications() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(appender, times(1)).append(
            newApplicationCaptor.capture(),
            existingApplicationsCaptor.capture()
        );

        Application capturedApplication = newApplicationCaptor.getValue();

        assertThat(capturedApplication.getApplicationSupplier()).isEqualTo(applicationSupplier);
        assertThat(capturedApplication.getApplicationType()).isEqualTo(applicationType);
        assertThat(capturedApplication.getApplicationReason()).isEqualTo(applicationReason);
        assertThat(capturedApplication.getApplicationDate()).isEqualTo(applicationDate);
        assertThat(capturedApplication.getApplicationDecision()).isEqualTo(applicationDecision);
        assertThat(capturedApplication.getApplicationDecisionReason()).isEqualTo(applicationDecisionReason);
        assertThat(capturedApplication.getApplicationDocuments()).isEqualTo(newApplicationDocuments);

        assertThat(capturedApplication.getApplicationDateOfDecision()).isEqualTo(now.toString());
        assertThat(capturedApplication.getApplicationStatus()).isEqualTo(applicationStatus);

        assertThat(existingApplicationsCaptor.getValue()).isEqualTo(existingApplications);

        verify(asylumCase, times(1)).write(APPLICATIONS, allAppendedApplications);

        verify(notificationSender).send(any(Callback.class));
        verify(asylumCaseWithNotifications, times(1)).clear(APPLICATION_SUPPLIER);
        verify(asylumCaseWithNotifications, times(1)).clear(APPLICATION_REASON);
        verify(asylumCaseWithNotifications, times(1)).clear(APPLICATION_DATE);
        verify(asylumCaseWithNotifications, times(1)).clear(APPLICATION_DECISION_REASON);
        verify(asylumCaseWithNotifications, times(1)).clear(APPLICATION_DOCUMENTS);

        assertThat(callbackResponse.getData()).isEqualTo(asylumCaseWithNotifications);
    }

    @Test
    public void should_add_new_flag_for_time_extension() {
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(TIME_EXTENSION.toString()));
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(GRANTED.toString()));
        when(asylumCase.read(APPLICATION_WITHDRAW_EXISTS, String.class)).thenReturn(Optional.empty());

        recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(APPLICATION_TIME_EXTENSION_EXISTS, "Yes");
        verify(asylumCase, times(1)).write(DISABLE_OVERVIEW_PAGE, "Yes");
        verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.UNKNOWN);
    }

    @Test
    public void should_not_add_new_flag_for_time_extension_when_withdraw_exists() {
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(TIME_EXTENSION.toString()));
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(GRANTED.toString()));
        when(asylumCase.read(APPLICATION_WITHDRAW_EXISTS, String.class)).thenReturn(Optional.of("Yes"));

        recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).write(APPLICATION_TIME_EXTENSION_EXISTS, "Yes");
        verify(asylumCase, times(1)).write(DISABLE_OVERVIEW_PAGE, "Yes");
        verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.UNKNOWN);
    }

    @Test
    public void should_add_new_flag_for_edit_listing() {
        when(caseDetails.getState()).thenReturn(State.PREPARE_FOR_HEARING);
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(TRANSFER.toString()));
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(GRANTED.toString()));
        when(asylumCase.read(APPLICATION_WITHDRAW_EXISTS, String.class)).thenReturn(Optional.empty());

        recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(APPLICATION_EDIT_LISTING_EXISTS, "Yes");
        verify(asylumCase, times(1)).write(DISABLE_OVERVIEW_PAGE, "Yes");
        verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.UNKNOWN);
    }

    @Test
    public void should_not_add_new_flag_for_edit_listing_when_withdraw_exists() {
        when(caseDetails.getState()).thenReturn(State.PREPARE_FOR_HEARING);
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(TRANSFER.toString()));
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(GRANTED.toString()));
        when(asylumCase.read(APPLICATION_WITHDRAW_EXISTS, String.class)).thenReturn(Optional.of("Yes"));

        recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(0)).write(APPLICATION_EDIT_LISTING_EXISTS, "Yes");
        verify(asylumCase, times(1)).write(DISABLE_OVERVIEW_PAGE, "Yes");
        verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.UNKNOWN);
    }

    @Test
    public void should_add_new_flag_for_withdraw_and_remove_other_flags() {

        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(WITHDRAW.toString()));
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(GRANTED.toString()));

        recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(APPLICATION_WITHDRAW_EXISTS, "Yes");
        verify(asylumCase, times(1)).write(APPLICATION_TIME_EXTENSION_EXISTS, "No");
        verify(asylumCase, times(1)).write(APPLICATION_EDIT_LISTING_EXISTS, "No");
        verify(asylumCase, times(1)).write(DISABLE_OVERVIEW_PAGE, "Yes");
        verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.UNKNOWN);
    }

    @Test
    public void should_add_new_flag_for_adjour_or_expedite() {
        when(caseDetails.getState()).thenReturn(State.PREPARE_FOR_HEARING);
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(ADJOURN.toString()));
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(GRANTED.toString()));

        recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(APPLICATION_EDIT_LISTING_EXISTS, "Yes");
        verify(asylumCase, times(1)).write(DISABLE_OVERVIEW_PAGE, "Yes");
        verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.UNKNOWN);
    }

    @Test
    public void should_add_new_flag_for_update_hearing_requirements_and_remove_other_flags() {

        when(callback.getCaseDetails().getState()).thenReturn(State.FINAL_BUNDLING);
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(UPDATE_HEARING_REQUIREMENTS.toString()));
        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.of(GRANTED.toString()));

        recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1)).write(APPLICATION_UPDATE_HEARING_REQUIREMENTS_EXISTS, "Yes");
        verify(asylumCase, times(1)).write(DISABLE_OVERVIEW_PAGE, "Yes");
        verify(asylumCase, times(1)).write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, State.UNKNOWN);
    }

    @Test
    public void should_return_client_error_when_application_type_does_not_suit_to_case_state() {

        for (ApplicationType type : ApplicationType.values()) {

            when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(type.toString()));

            for (State state : State.values()) {

                when(caseDetails.getState()).thenReturn(state);

                PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                    recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

                if ((type.equals(ApplicationType.TIME_EXTENSION) && !timeExtensionSates.contains(state))
                    ||
                    ((type.equals(ApplicationType.ADJOURN) || type.equals(ApplicationType.EXPEDITE) || type.equals(ApplicationType.TRANSFER)) && !editListingStates.contains(state))
                    || (type.equals(ApplicationType.UPDATE_HEARING_REQUIREMENTS) && !updateHearingRequirementsStates.contains(state))) {

                    assertThat(callbackResponse.getErrors().size()).isEqualTo(1);
                    assertThat(callbackResponse.getErrors().iterator().next()).isEqualTo("You can't record application with '" + type + "' type when case is in '" + state.name() + "' state");
                } else {

                    assertThat(callbackResponse.getErrors().size()).isEqualTo(0);
                }
            }
        }
    }

    @Test
    public void should_throw_when_application_supplier_is_not_present() {

        when(asylumCase.read(APPLICATION_SUPPLIER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("applicationSupplier is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_application_type_is_not_present() {

        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("applicationType is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_application_reason_is_not_present() {

        when(asylumCase.read(APPLICATION_REASON, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("applicationReason is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_application_date_is_not_present() {

        when(asylumCase.read(APPLICATION_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("applicationDate is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_application_decision_is_not_present() {

        when(asylumCase.read(APPLICATION_DECISION, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("applicationDecision is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_application_decision_reason_is_not_present() {

        when(asylumCase.read(APPLICATION_DECISION_REASON, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("applicationDecisionReason is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_application_documents_is_not_present() {

        when(asylumCase.read(APPLICATION_DOCUMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("applicationDocuments is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordApplicationHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && event.equals(Event.RECORD_APPLICATION)) {

                    assertThat(canHandle).isEqualTo(true);
                } else {
                    assertThat(canHandle).isEqualTo(false);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordApplicationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordApplicationHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordApplicationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordApplicationHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
