package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DatesToAvoid;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
public class ReviewCmaRequirementsPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private List<IdValue<InterpreterLanguage>> interpreterLanguage;
    @Mock private List<IdValue<DatesToAvoid>> datesToAvoid;

    private ReviewCmaRequirementsPreparer reviewCmaRequirementsPreparer;

    @Before
    public void setUp() {
        reviewCmaRequirementsPreparer =
            new ReviewCmaRequirementsPreparer();

        when(callback.getEvent()).thenReturn(Event.REVIEW_CMA_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_review_hearing_requirements() {
        interpreterLanguage = Arrays.asList(
            new IdValue<>("1", new InterpreterLanguage("Irish", "N/A"))
        );

        datesToAvoid = Arrays.asList(
            new IdValue<>("1", new DatesToAvoid(LocalDate.parse("2020-01-01"), "Reason to avoid this date"))
        );
        when(asylumCase.read(INTERPRETER_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));
        when(asylumCase.read(DATES_TO_AVOID)).thenReturn(Optional.of(datesToAvoid));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewCmaRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(INTERPRETER_LANGUAGE);

        verify(asylumCase, times(1)).write(
            INTERPRETER_LANGUAGE_READONLY,
            "Language\t\t\tIrish\nDialect\t\t\tN/A\n");

        verify(asylumCase, times(1)).read(DATES_TO_AVOID);

        verify(asylumCase, times(1)).write(
            DATES_TO_AVOID_READONLY,
            "Date\t\t\t2020-01-01\nReason\t\t\tReason to avoid this date\n");
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> reviewCmaRequirementsPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        assertThatThrownBy(() -> reviewCmaRequirementsPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> reviewCmaRequirementsPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewCmaRequirementsPreparer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewCmaRequirementsPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewCmaRequirementsPreparer.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
