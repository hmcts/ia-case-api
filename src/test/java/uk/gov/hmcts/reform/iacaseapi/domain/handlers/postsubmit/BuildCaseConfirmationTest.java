package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
class BuildCaseConfirmationTest {

    @Mock private
    Callback<AsylumCase> callback;
    @Mock private
    CaseDetails<AsylumCase> caseDetails;

    BuildCaseConfirmation buildCaseConfirmation = new BuildCaseConfirmation();

    @Test
    void should_return_confirmation() {
        ReflectionTestUtils.setField(buildCaseConfirmation, "isSaveAndContinueEnabled", true);

        long caseId = 1234;

        when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);

        PostSubmitCallbackResponse callbackResponse =
            buildCaseConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("Upload saved");

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You still need to submit your case");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("What happens next");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "[submit your case]"
                + "(/case/IA/Asylum/" + caseId + "/trigger/submitCase)"
            );

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "[build your case]"
                + "(/case/IA/Asylum/" + caseId + "/trigger/buildCase)"
            );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> buildCaseConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @MethodSource("canHandleTestData")
    void it_can_handle_callback(Event event, boolean flag, boolean expected) {
        ReflectionTestUtils.setField(buildCaseConfirmation, "isSaveAndContinueEnabled", flag);
        when(callback.getEvent()).thenReturn(event);

        boolean canHandle = buildCaseConfirmation.canHandle(callback);

        Assertions.assertThat(canHandle).isEqualTo(expected);
    }

    private static Stream<Arguments> canHandleTestData() {

        List<Arguments> scenarios = new ArrayList<>();

        for (Event event : Event.values()) {
            if (event.equals(Event.BUILD_CASE)) {
                scenarios.add(Arguments.of(event, true, true));
            } else {
                scenarios.add(Arguments.of(event, true, false));
            }
            scenarios.add(Arguments.of(event, false, false));
        }

        return scenarios.stream();
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> buildCaseConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> buildCaseConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
