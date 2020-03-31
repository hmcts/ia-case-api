package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class FtpaAppellantPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;

    private FtpaAppellantPreparer ftpaAppellantPreparer;


    @Before
    public void setUp() {
        ftpaAppellantPreparer =
            new FtpaAppellantPreparer(dateProvider, 14);
    }

    @Test
    public void should_perform_mid_event_and_set_out_of_date_submission_state_no() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String appealDate = dateProvider.now().minusDays(1).toString();
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.of(appealDate));

        ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.NO);

    }

    @Test
    public void should_perform_mid_event_and_set_out_of_date_submission_state_no_when_no_appeal_date() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.empty());

        ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.NO);

    }

    @Test
    public void should_perform_mid_event_and_set_out_of_date_submission_state_to_yes() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.APPLY_FOR_FTPA_APPELLANT);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(LocalDate.now());
        final String appealDate = dateProvider.now().minusDays(15).toString();
        when(asylumCase.read(APPEAL_DATE)).thenReturn(Optional.of(appealDate));

        ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YesOrNo.YES);

    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = ftpaAppellantPreparer.canHandle(callbackStage, callback);

                if (event == Event.APPLY_FOR_FTPA_APPELLANT
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> ftpaAppellantPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ftpaAppellantPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
