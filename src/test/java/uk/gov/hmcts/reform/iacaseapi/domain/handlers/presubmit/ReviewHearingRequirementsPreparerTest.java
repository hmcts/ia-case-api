package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
public class ReviewHearingRequirementsPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private List<IdValue<WitnessDetails>> witnessDetails;
    @Mock private List<IdValue<InterpreterLanguage>> interpreterLanguage;

    private ReviewHearingRequirementsPreparer reviewHearingRequirementsPreparer;

    @Before
    public void setUp() {
        reviewHearingRequirementsPreparer =
            new ReviewHearingRequirementsPreparer();

        when(callback.getEvent()).thenReturn(Event.REVIEW_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void should_review_hearing_requirements() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        witnessDetails = Arrays.asList(
            new IdValue<>("1", new WitnessDetails("Witness1")),
            new IdValue<>("2", new WitnessDetails("Witness2"))
        );

        interpreterLanguage = Arrays.asList(
            new IdValue<>("1", new InterpreterLanguage("Irish", "N/A"))
        );

        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));
        when(asylumCase.read(INTERPRETER_LANGUAGE)).thenReturn(Optional.of(interpreterLanguage));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(WITNESS_DETAILS);
        verify(asylumCase, times(1)).read(INTERPRETER_LANGUAGE);

        verify(asylumCase, times(1)).write(
            WITNESS_DETAILS_READONLY,
            "Name\t\tWitness1\nName\t\tWitness2");
        verify(asylumCase, times(1)).write(
            INTERPRETER_LANGUAGE_READONLY,
            "Language\t\tIrish\nDialect\t\t\tN/A\n");
    }


    @Test
    public void should_throw_error_review_flag_is_missing() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewHearingRequirementsPreparer.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("reviewHearingRequirements flag should be present");
    }

    @Test
    public void should_return_error_on_reviewing_hearing_requirements_twice() {
        when(asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            reviewHearingRequirementsPreparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callback);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals(callbackResponse.getErrors().iterator().next(),
            "You've made an invalid request. The hearing requirements have already been reviewed.");

        verify(asylumCase, times(0)).read(WITNESS_DETAILS);
        verify(asylumCase, times(0)).read(INTERPRETER_LANGUAGE);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> reviewHearingRequirementsPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        assertThatThrownBy(() -> reviewHearingRequirementsPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> reviewHearingRequirementsPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewHearingRequirementsPreparer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewHearingRequirementsPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewHearingRequirementsPreparer.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
