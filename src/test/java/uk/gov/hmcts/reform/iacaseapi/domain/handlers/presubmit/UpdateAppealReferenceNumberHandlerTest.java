package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Value;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;

@RunWith(JUnitParamsRunner.class)
public class UpdateAppealReferenceNumberHandlerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.LENIENT);

    @Mock
    private AppealReferenceNumberGenerator appealReferenceNumberGenerator;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Spy
    private final AsylumCase asylumCase = new AsylumCase();

    @InjectMocks
    private UpdateAppealReferenceNumberHandler handler;

    @Test
    @Parameters(method = "generateCanHandleScenarios")
    public void canHandle(CanHandleScenario scenario) {
        given(callback.getEvent()).willReturn(scenario.getEvent());

        boolean result = handler.canHandle(scenario.getCallbackStage(), callback);

        Assertions.assertThat(result).isEqualTo(scenario.isCanHandleExpectedResult());
    }

    public List<CanHandleScenario> generateCanHandleScenarios() {
        return CanHandleScenario.builder();
    }

    @Value
    private static class CanHandleScenario {
        PreSubmitCallbackStage callbackStage;
        Event event;
        boolean canHandleExpectedResult;

        private static List<CanHandleScenario> builder() {
            List<CanHandleScenario> scenarios = new ArrayList<>();
            for (Event event : Event.values()) {
                if (event.equals(Event.EDIT_APPEAL_AFTER_SUBMIT)) {
                    scenarios.add(new CanHandleScenario(ABOUT_TO_SUBMIT, event, true));
                    scenarios.add(new CanHandleScenario(ABOUT_TO_START, event, false));
                    scenarios.add(new CanHandleScenario(MID_EVENT, event, false));
                } else {
                    scenarios.add(new CanHandleScenario(ABOUT_TO_START, event, false));
                    scenarios.add(new CanHandleScenario(ABOUT_TO_SUBMIT, event, false));
                    scenarios.add(new CanHandleScenario(MID_EVENT, event, false));
                }
            }
            return scenarios;
        }

    }

    @Test
    public void given_null_args_should_throw_exception() {
        Assertions.assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        Assertions.assertThatThrownBy(() -> handler.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        Assertions.assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        Assertions.assertThatThrownBy(() -> handler.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    @Parameters({
        "PA/50137/2020, HU, HU/50137/2020",
        "RP/50234/2020, EA, EA/50234/2020"
    })
    public void handle(String existingAppealReferenceNumber, AppealType appealTypeUpdate,
                       String expectedAppealReferenceNumber) {
        mockCallback();
        mockAsylumCaseWithGivenParams(existingAppealReferenceNumber, appealTypeUpdate);
        given(appealReferenceNumberGenerator.update(anyLong(), any(AppealType.class)))
            .willReturn(expectedAppealReferenceNumber);

        PreSubmitCallbackResponse<AsylumCase> actualCallback = handler.handle(ABOUT_TO_SUBMIT, callback);

        then(asylumCase).should(times(1)).read(APPEAL_REFERENCE_NUMBER);
        then(asylumCase).should(times(1)).read(APPEAL_TYPE, AppealType.class);

        String actualAppealReferenceNumberUpdate = actualCallback.getData().read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new RuntimeException("Appeal Reference Number should be present"));
        assertThat(actualAppealReferenceNumberUpdate).isEqualTo(expectedAppealReferenceNumber);
    }

    private void mockAsylumCaseWithGivenParams(String existingAppealReferenceNumber, AppealType appealTypeUpdate) {
        asylumCase.write(APPEAL_REFERENCE_NUMBER, existingAppealReferenceNumber);
        asylumCase.write(APPEAL_TYPE, appealTypeUpdate);
        given(caseDetails.getCaseData()).willReturn(asylumCase);
    }

    private void mockCallback() {
        given(callback.getEvent()).willReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);
        given(callback.getCaseDetails()).willReturn(caseDetails);
    }

    @Test
    @Parameters({
        "ABOUT_TO_START, EDIT_APPEAL_AFTER_SUBMIT",
        "MID_EVENT, EDIT_APPEAL_AFTER_SUBMIT",
        "ABOUT_TO_SUBMIT, REMOVE_FLAG",
    })
    public void given_handle_method_cannot_be_handled_should_throw_exception(PreSubmitCallbackStage callbackStage,
                                                                             Event event) {
        given(callback.getEvent()).willReturn(event);

        assertThatThrownBy(() -> handler.handle(callbackStage, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }
}