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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTION_EDIT_PARTIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.Arrays;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class HomeOfficeCaseNotificationsHandlerTest {

    @Mock private HomeOfficeApi<AsylumCase> homeOfficeApi;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeatureToggler featureToggler;
    @Mock private Direction nonStandardDirection;

    private HomeOfficeCaseNotificationsHandler homeOfficeCaseNotificationsHandler;

    private IdValue originalDirection8 = new IdValue(
        "8",
        new Direction("explanation8", Parties.LEGAL_REPRESENTATIVE, "2020-01-02",
            "2020-01-01", DirectionTag.NONE, Collections.emptyList())
    );

    private IdValue originalDirection9 = new IdValue(
        "9",
        new Direction("explanation9", Parties.RESPONDENT, "2020-01-02",
            "2020-01-01", DirectionTag.NONE, Collections.emptyList())
    );

    private IdValue originalDirection10 = new IdValue(
        "10",
        new Direction("explanation10", Parties.LEGAL_REPRESENTATIVE, "2020-01-02",
            "2020-01-01", DirectionTag.RESPONDENT_REVIEW, Collections.emptyList())
    );

    private IdValue originalDirection11 = new IdValue(
        "11",
        new Direction("explanation11", Parties.RESPONDENT, "2020-01-02",
            "2020-01-01", DirectionTag.NONE, Collections.emptyList())
    );

    @BeforeEach
    void setUp() {
        homeOfficeCaseNotificationsHandler =
            new HomeOfficeCaseNotificationsHandler(featureToggler, homeOfficeApi);
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REQUEST_RESPONDENT_EVIDENCE",
        "REQUEST_RESPONDENT_REVIEW",
        "LIST_CASE",
        "EDIT_CASE_LISTING",
        "ADJOURN_HEARING_WITHOUT_DATE",
        "SEND_DECISION_AND_REASONS",
        "APPLY_FOR_FTPA_APPELLANT",
        "APPLY_FOR_FTPA_RESPONDENT",
        "LEADERSHIP_JUDGE_FTPA_DECISION",
        "RESIDENT_JUDGE_FTPA_DECISION",
        "END_APPEAL"
    })
    void should_call_home_office_api_and_update_the_case_for_list_case(Event event) {

        when(callback.getEvent()).thenReturn(event);
        when(homeOfficeApi.call(callback)).thenReturn(asylumCase);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(1)).call(callback);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "RESPONDENT_REVIEW",
        "AWAITING_RESPONDENT_EVIDENCE"
    })
    void should_call_home_office_api_and_update_the_case_for_direction_due_date(State state) {

        when(callback.getEvent()).thenReturn(Event.CHANGE_DIRECTION_DUE_DATE);
        when(homeOfficeApi.call(callback)).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getState()).thenReturn(state);
        when(asylumCase.read(DIRECTION_EDIT_PARTIES, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(1)).call(callback);
    }

    @Test
    void should_return_true_for_respondent_direction() {

        when(asylumCase.read(DIRECTION_EDIT_PARTIES, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));
        assertTrue(homeOfficeCaseNotificationsHandler.isDirectionForRespondentParties(asylumCase));
    }

    @Test
    void should_return_false_for_non_respondent_direction() {

        when(asylumCase.read(DIRECTION_EDIT_PARTIES, String.class)).thenReturn(Optional.of(Parties.LEGAL_REPRESENTATIVE.toString()));
        assertFalse(homeOfficeCaseNotificationsHandler.isDirectionForRespondentParties(asylumCase));
    }

    @Test
    void should_return_right_direction_for_multiple_send_direction() {

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(originalDirection8);
        directionList.add(originalDirection9);
        directionList.add(originalDirection10);
        directionList.add(originalDirection11);
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(Optional.of((directionList)));

        Optional<Direction> selectedDirection = homeOfficeCaseNotificationsHandler.getLatestNonStandardRespondentDirection(asylumCase);

        assertTrue(selectedDirection.isPresent());
        assertEquals(Parties.RESPONDENT, selectedDirection.get().getParties());
        assertEquals("explanation11", selectedDirection.get().getExplanation());
    }

    @Test
    void should_return_empty_direction_for_invalid_send_direction() {

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(originalDirection9);
        directionList.add(originalDirection10);
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(Optional.of((directionList)));

        Optional<Direction> selectedDirection = homeOfficeCaseNotificationsHandler.getLatestNonStandardRespondentDirection(asylumCase);

        assertTrue(selectedDirection.isEmpty());
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        for (Event event : Event.values()) {

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                for (State state : State.values()) {

                    when(callback.getEvent()).thenReturn(event);
                    when(callback.getCaseDetails()).thenReturn(caseDetails);
                    when(caseDetails.getCaseData()).thenReturn(asylumCase);
                    when(callback.getCaseDetails().getState()).thenReturn(state);
                    when(asylumCase.read(DIRECTION_EDIT_PARTIES, String.class)).thenReturn(Optional.of(Parties.RESPONDENT.toString()));

                    List<IdValue<Direction>> directionList = new ArrayList<>();
                    directionList.add(originalDirection8);
                    directionList.add(originalDirection9);
                    when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(Optional.of((directionList)));

                    boolean canHandle = homeOfficeCaseNotificationsHandler.canHandle(callbackStage, callback);

                    if (callbackStage == ABOUT_TO_SUBMIT
                        && (Arrays.asList(
                                Event.REQUEST_RESPONDENT_EVIDENCE,
                                Event.REQUEST_RESPONDENT_REVIEW,
                                Event.LIST_CASE,
                                Event.EDIT_CASE_LISTING,
                                Event.ADJOURN_HEARING_WITHOUT_DATE,
                                Event.SEND_DECISION_AND_REASONS,
                                Event.APPLY_FOR_FTPA_APPELLANT,
                                Event.APPLY_FOR_FTPA_RESPONDENT,
                                Event.LEADERSHIP_JUDGE_FTPA_DECISION,
                                Event.RESIDENT_JUDGE_FTPA_DECISION,
                                Event.END_APPEAL,
                                Event.REQUEST_RESPONSE_AMEND
                            ).contains(callback.getEvent())
                            || (event == Event.SEND_DIRECTION
                                && state == State.AWAITING_RESPONDENT_EVIDENCE)
                            || (event == Event.CHANGE_DIRECTION_DUE_DATE
                                && (Arrays.asList(
                                    State.AWAITING_RESPONDENT_EVIDENCE,
                                    State.RESPONDENT_REVIEW
                                    ).contains(callback.getCaseDetails().getState()))
                                )
                            )
                        )
                    {
                        assertTrue(canHandle);
                    } else {
                        assertFalse(canHandle);
                    }
                }
            }
        }
        reset(callback);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handler_throws_error_if_feature_not_enabled() {

        homeOfficeCaseNotificationsHandler = new HomeOfficeCaseNotificationsHandler(
            featureToggler,
            homeOfficeApi
        );

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }
}
