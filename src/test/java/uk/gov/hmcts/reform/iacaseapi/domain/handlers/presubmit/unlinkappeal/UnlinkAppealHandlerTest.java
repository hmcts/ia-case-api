package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.unlinkappeal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REASON_FOR_LINK_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ReasonForLinkAppealOptions.FAMILIAL;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ReasonForLinkAppealOptions;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
class UnlinkAppealHandlerTest {

    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;

    final UnlinkAppealHandler unlinkAppealHandler = new UnlinkAppealHandler();

    @ParameterizedTest
    @MethodSource("canHandleScenarioTestData")
    void canHandle(Event event, PreSubmitCallbackStage callbackStage, boolean canHandleExpectedResult) {
        if (canHandleExpectedResult) {
            when(callback.getEvent()).thenReturn(event);
        }

        boolean result = unlinkAppealHandler.canHandle(callbackStage, callback);

        Assertions.assertThat(result).isEqualTo(canHandleExpectedResult);
    }

    private static Stream<Arguments> canHandleScenarioTestData() {

        List<Arguments> scenarios = new ArrayList<>();

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            for (Event event : Event.values()) {
                if (Event.UNLINK_APPEAL.equals(event)
                    && PreSubmitCallbackStage.ABOUT_TO_SUBMIT.equals(callbackStage)) {
                    scenarios.add(Arguments.of(event, callbackStage, true));
                } else {
                    scenarios.add(Arguments.of(event, callbackStage, false));
                }
            }
        }

        return scenarios.stream();
    }

    @Test
    void handle() {
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
    void should_throw_exception() {

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