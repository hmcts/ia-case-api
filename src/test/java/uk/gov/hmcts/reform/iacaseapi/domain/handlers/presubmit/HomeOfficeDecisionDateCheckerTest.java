package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.*;
import java.util.Arrays;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HomeOfficeDecisionDateCheckerTest {

    private static final int APPEAL_OUT_OF_TIME_DAYS_UK = 14;
    private static final int APPEAL_OUT_OF_TIME_DAYS_OOC = 28;
    private static final int APPEAL_OUT_OF_TIME_ADA_WORKING_DAYS = 5;

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

    private HomeOfficeDecisionDateChecker homeOfficeDecisionDateChecker;

    @Captor
    private ArgumentCaptor<YesOrNo> outOfTime;

    @Captor
    private ArgumentCaptor<YesOrNo> recordedOutOfTimeDecision;

    @Captor
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractor;

    @BeforeEach
    public void setUp() {

        homeOfficeDecisionDateChecker =
            new HomeOfficeDecisionDateChecker(
                dateProvider,
                dueDateService,
                APPEAL_OUT_OF_TIME_DAYS_UK,
                APPEAL_OUT_OF_TIME_DAYS_OOC,
                APPEAL_OUT_OF_TIME_ADA_WORKING_DAYS
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
    }

    @Test
    void handles_edge_case_when_in_time() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-01-15"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of("2019-01-01"));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(NO);
    }

    @Test
    void handles_edge_case_when_ooc_and_refusal_of_human_rights_is_decided() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-01-15"));
        when(asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION)).thenReturn(Optional.of("2019-01-01"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(NO);
    }

    @Test
    void handles_edge_case_when_ooc_and_refusal_of_protection_is_decided() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-01-15"));
        when(asylumCase.read(DATE_CLIENT_LEAVE_UK)).thenReturn(Optional.of("2019-01-01"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_PROTECTION));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(NO);
    }

    @Test
    void handles_edge_case_when_ooc_and_removal_of_client_is_decided() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-01-15"));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE)).thenReturn(Optional.of("2019-01-01"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(NO);
    }

    @Test
    void handles_edge_case_when_ooc_and_refusal_of_permit_is_decided() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-01-15"));
        when(asylumCase.read(DATE_ENTRY_CLEARANCE_DECISION)).thenReturn(Optional.of("2019-01-01"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
            .thenReturn(Optional.of(OutOfCountryDecisionType.REFUSE_PERMIT));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(NO);
    }

    @Test
    void handles_out_of_country_decision_letter_date_received_when_in_time() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2021-01-25"));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE)).thenReturn(Optional.of("2021-01-15"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(NO);
    }

    @Test
    void handles_out_of_country_decision_letter_date_received_when_easily_out_of_time() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2021-01-25"));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE)).thenReturn(Optional.of("2020-01-15"));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(2)).write(asylumExtractor.capture(), outOfTime.capture());
        verify(asylumCase, times(2)).write(asylumExtractor.capture(), recordedOutOfTimeDecision.capture());

        assertThat(asylumExtractor.getAllValues().contains(SUBMISSION_OUT_OF_TIME));
        assertThat(asylumExtractor.getAllValues().contains(RECORDED_OUT_OF_TIME_DECISION));
        assertThat(outOfTime.getValue()).usingRecursiveComparison().getRecursiveComparisonConfiguration().equals(YES);
        assertThat(recordedOutOfTimeDecision.getValue()).usingRecursiveComparison().getRecursiveComparisonConfiguration().equals(NO);

    }

    @Test
    void should_throw_exception_when_out_of_country_home_office_decision_date_is_missing() {

        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REMOVAL_OF_CLIENT));
        assertThatThrownBy(() -> homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("decisionLetterReceivedDate is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_PROTECTION));
        assertThatThrownBy(() -> homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("dateClientLeaveUk is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));
        assertThatThrownBy(() -> homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("dateEntryClearanceDecision is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    void handles_edge_case_when_easily_out_of_time() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-01-15"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of("2015-01-01"));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(2)).write(asylumExtractor.capture(), outOfTime.capture());
        verify(asylumCase, times(2)).write(asylumExtractor.capture(), recordedOutOfTimeDecision.capture());

        assertThat(asylumExtractor.getAllValues().contains(SUBMISSION_OUT_OF_TIME));
        assertThat(asylumExtractor.getAllValues().contains(RECORDED_OUT_OF_TIME_DECISION));
        assertThat(outOfTime.getValue()).usingRecursiveComparison().getRecursiveComparisonConfiguration().equals(YES);
        assertThat(recordedOutOfTimeDecision.getValue()).usingRecursiveComparison().getRecursiveComparisonConfiguration().equals(NO);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handles_edge_case_when_out_of_time() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-01-16"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of("2019-01-01"));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(2)).write(asylumExtractor.capture(), outOfTime.capture());
        verify(asylumCase, times(2)).write(asylumExtractor.capture(), recordedOutOfTimeDecision.capture());

        assertThat(asylumExtractor.getAllValues().contains(SUBMISSION_OUT_OF_TIME));
        assertThat(asylumExtractor.getValue()).isEqualTo(RECORDED_OUT_OF_TIME_DECISION);
        assertThat(outOfTime.getValue()).usingRecursiveComparison().getRecursiveComparisonConfiguration().equals(YES);
        assertThat(recordedOutOfTimeDecision.getValue()).usingRecursiveComparison().getRecursiveComparisonConfiguration().equals(NO);

    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = homeOfficeDecisionDateChecker.canHandle(callbackStage, callback);

                if (Arrays.asList(
                    Event.SUBMIT_APPEAL)
                    .contains(callback.getEvent())
                    && (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    || callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> homeOfficeDecisionDateChecker.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeDecisionDateChecker.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeDecisionDateChecker.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
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

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(NO);
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

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

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

        assertThatThrownBy(() -> homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("decisionLetterReceivedDate is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

    }

    @Test
    void handles_aaa_case_when_in_time() {

        final String decisionLetterDate = "2022-11-10";
        final String dueDate = "2022-11-24";
        final String nowDate = "2022-11-20";
        final ZonedDateTime zonedDateTime = LocalDate.parse(decisionLetterDate).atStartOfDay(ZoneOffset.UTC);
        final ZonedDateTime zonedDueDateTime = LocalDate.parse(dueDate).atStartOfDay(ZoneOffset.UTC);

        when(dateProvider.now()).thenReturn(LocalDate.parse(nowDate));
        when(asylumCase.read(DATE_ON_DECISION_LETTER, String.class)).thenReturn(Optional.of(decisionLetterDate));
        when(dueDateService.calculateDueDate(zonedDateTime, APPEAL_OUT_OF_TIME_DAYS_UK)).thenReturn(zonedDueDateTime);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(NO);
    }

    @Test
    void handles_aaa_case_when_out_of_time() {

        final String decisionLetterDate = "2022-11-10";
        final String dueDate = "2022-11-24";
        final String nowDate = "2022-11-25";
        final ZonedDateTime zonedDateTime = LocalDate.parse(decisionLetterDate).atStartOfDay(ZoneOffset.UTC);
        final ZonedDateTime zonedDueDateTime = LocalDate.parse(dueDate).atStartOfDay(ZoneOffset.UTC);

        when(dateProvider.now()).thenReturn(LocalDate.parse(nowDate));
        when(asylumCase.read(DATE_ON_DECISION_LETTER, String.class)).thenReturn(Optional.of(decisionLetterDate));
        when(dueDateService.calculateDueDate(zonedDateTime, APPEAL_OUT_OF_TIME_DAYS_UK)).thenReturn(zonedDueDateTime);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(2)).write(asylumExtractor.capture(), outOfTime.capture());
        verify(asylumCase, times(2)).write(asylumExtractor.capture(), recordedOutOfTimeDecision.capture());

        assertThat(asylumExtractor.getAllValues().contains(SUBMISSION_OUT_OF_TIME));
        assertThat(asylumExtractor.getValue()).isEqualTo(RECORDED_OUT_OF_TIME_DECISION);
        assertThat(outOfTime.getValue()).usingRecursiveComparison().getRecursiveComparisonConfiguration().equals(YES);
        assertThat(recordedOutOfTimeDecision.getValue()).usingRecursiveComparison().getRecursiveComparisonConfiguration().equals(NO);
    }

    @Test
    void should_throw_exception_when_aaa_decision_date_is_missing() {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));

        assertThatThrownBy(() -> homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("dateOnDecisionLetter is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2023-10-10", "2020-11-10"})
    void handles_ejp_case(String receivedLetterDate) {
        final String nowDate = "2023-10-20";
        when(dateProvider.now()).thenReturn(LocalDate.parse(nowDate));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE)).thenReturn(Optional.of(receivedLetterDate));
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(1)).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getAllValues().contains(SUBMISSION_OUT_OF_TIME));
        assertThat(outOfTime.getValue()).usingRecursiveComparison().getRecursiveComparisonConfiguration().equals(NO);
    }

}
