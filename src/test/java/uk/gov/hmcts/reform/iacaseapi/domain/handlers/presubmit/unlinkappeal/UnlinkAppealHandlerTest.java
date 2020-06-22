package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.unlinkappeal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REASON_FOR_LINK_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ReasonForLinkAppealOptions.FAMILIAL;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Value;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ReasonForLinkAppealOptions;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@RunWith(JUnitParamsRunner.class)
public class UnlinkAppealHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    private final UnlinkAppealHandler unlinkAppealHandler = new UnlinkAppealHandler();

    @Test
    @Parameters(method = "generateCanHandleScenarios")
    public void canHandle(CanHandleScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.event);

        boolean result = unlinkAppealHandler.canHandle(scenario.callbackStage, callback);

        Assertions.assertThat(result).isEqualTo(scenario.canHandleExpectedResult);
    }

    private List<CanHandleScenario> generateCanHandleScenarios() {
        return CanHandleScenario.builder();
    }

    @Value
    private static class CanHandleScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean canHandleExpectedResult;

        public static List<CanHandleScenario> builder() {
            List<CanHandleScenario> scenarios = new ArrayList<>();
            for (PreSubmitCallbackStage cb : PreSubmitCallbackStage.values()) {
                for (Event e : Event.values()) {
                    if (Event.UNLINK_APPEAL.equals(e)
                        && PreSubmitCallbackStage.ABOUT_TO_SUBMIT.equals(cb)) {
                        scenarios.add(new CanHandleScenario(e, cb, true));
                    } else {
                        scenarios.add(new CanHandleScenario(e, cb, false));
                    }
                }
            }
            return scenarios;
        }
    }

    @Test
    public void handle() {
        when(callback.getEvent()).thenReturn(Event.UNLINK_APPEAL);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        AsylumCase asylum = new AsylumCase();
        asylum.write(REASON_FOR_LINK_APPEAL, FAMILIAL);
        when(caseDetails.getCaseData()).thenReturn(asylum);

        PreSubmitCallbackResponse<AsylumCase> actualResponse =
            unlinkAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(callback, times(1)).getCaseDetails();
        verify(caseDetails, times(1)).getCaseData();

        Optional<ReasonForLinkAppealOptions> actualReasonForLinkAppeal = actualResponse.getData().read(
            REASON_FOR_LINK_APPEAL, ReasonForLinkAppealOptions.class);

        if (actualReasonForLinkAppeal.isPresent()) {
            fail();
        }
    }

    @Test
    public void should_throw_exception() {

        assertThatThrownBy(() -> unlinkAppealHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> unlinkAppealHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> unlinkAppealHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> unlinkAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        when(callback.getEvent()).thenReturn(Event.ADD_CASE_NOTE);
        assertThatThrownBy(() -> unlinkAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

}