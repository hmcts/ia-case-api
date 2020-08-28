package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.unlinkappeal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UNLINK_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
class UnlinkAppealPreparerHandlerTest {

    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;

    final UnlinkAppealPreparerHandler unlinkAppealPreparerHandler = new UnlinkAppealPreparerHandler();

    @ParameterizedTest
    @MethodSource("canHandleScenarioTestData")
    void canHandle(Event event, PreSubmitCallbackStage callbackStage, boolean canHandleExpectedResult) {
        if (canHandleExpectedResult) {
            when(callback.getEvent()).thenReturn(event);
        }

        boolean result = unlinkAppealPreparerHandler.canHandle(callbackStage, callback);

        Assertions.assertThat(result).isEqualTo(canHandleExpectedResult);
    }

    private static Stream<Arguments> canHandleScenarioTestData() {

        List<Arguments> scenarios = new ArrayList<>();

        Arrays.stream(PreSubmitCallbackStage.values()).forEach(c -> {
            Arrays.stream(Event.values()).forEach(e -> {
                if (e == UNLINK_APPEAL && c == ABOUT_TO_START) {
                    scenarios.add(Arguments.of(e, c, true));
                } else {
                    scenarios.add(Arguments.of(e, c, false));
                }
            });
        });

        return scenarios.stream();
    }

    @Test
    void handle() {
        when(callback.getEvent()).thenReturn(UNLINK_APPEAL);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(new AsylumCase());

        PreSubmitCallbackResponse<AsylumCase> actualResponse =
            unlinkAppealPreparerHandler.handle(ABOUT_TO_START, callback);

        verify(callback, times(1)).getCaseDetails();
        verify(caseDetails, times(1)).getCaseData();

        assertEquals(1, actualResponse.getErrors().size());
        assertTrue(actualResponse.getErrors().contains("This appeal is not linked and so cannot be unlinked"));
    }

    @Test
    void should_throw_exception() {

        assertThatThrownBy(() -> unlinkAppealPreparerHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> unlinkAppealPreparerHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> unlinkAppealPreparerHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> unlinkAppealPreparerHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> unlinkAppealPreparerHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

}