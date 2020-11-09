package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUBMISSION_OUT_OF_TIME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
public class HomeOfficeDecisionDateCheckerTest {

    private static final int APPEAL_OUT_OF_TIME_DAYS = 14;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;

    private HomeOfficeDecisionDateChecker homeOfficeDecisionDateChecker;

    private ArgumentCaptor<YesOrNo> outOfTime = ArgumentCaptor.forClass(YesOrNo.class);
    private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractor = ArgumentCaptor.forClass(AsylumCaseFieldDefinition.class);

    @Before
    public void setUp() {

        homeOfficeDecisionDateChecker =
            new HomeOfficeDecisionDateChecker(
                dateProvider,
                APPEAL_OUT_OF_TIME_DAYS
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_be_handled_at_earliest_point() {
        assertEquals(DispatchPriority.EARLIEST, homeOfficeDecisionDateChecker.getDispatchPriority());
    }

    @Test
    public void handles_edge_case_when_in_time() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-01-15"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of("2019-01-01"));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(NO);
    }

    @Test
    public void handles_edge_case_when_easily_out_of_time() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-01-15"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of("2015-01-01"));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(YES);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handles_edge_case_when_out_of_time() {

        when(dateProvider.now()).thenReturn(LocalDate.parse("2019-01-16"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE)).thenReturn(Optional.of("2019-01-01"));

        homeOfficeDecisionDateChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(asylumExtractor.capture(), outOfTime.capture());

        assertThat(asylumExtractor.getValue()).isEqualTo(SUBMISSION_OUT_OF_TIME);
        assertThat(outOfTime.getValue()).isEqualTo(YES);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = homeOfficeDecisionDateChecker.canHandle(callbackStage, callback);

                if (Arrays.asList(
                        Event.SUBMIT_APPEAL,
                        Event.PAY_AND_SUBMIT_APPEAL)
                        .contains(callback.getEvent())
                    && (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT || callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

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
}
