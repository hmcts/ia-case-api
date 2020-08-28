package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.linkappeal;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
class LinkAppealConfirmationTest {

    @Mock private
    Callback<AsylumCase> callback;

    LinkAppealConfirmation linkAppealConfirmation = new LinkAppealConfirmation();

    @ParameterizedTest
    @MethodSource("generateCanHandleScenarios")
    void canHandle(Event event, boolean canHandleExpectedResult) {
        when(callback.getEvent()).thenReturn(event);

        boolean result = linkAppealConfirmation.canHandle(callback);

        Assertions.assertThat(result).isEqualTo(canHandleExpectedResult);
    }

    private static Stream<Arguments> generateCanHandleScenarios() {

        List<Arguments> scenarios = new ArrayList<>();

        for (Event e : Event.values()) {
            if (Event.LINK_APPEAL.equals(e)) {
                scenarios.add(Arguments.of(e, true));
            } else {
                scenarios.add(Arguments.of(e, false));
            }
        }

        return scenarios.stream();
    }

    @Test
    void handle() {
        when(callback.getEvent()).thenReturn(Event.LINK_APPEAL);

        PostSubmitCallbackResponse actualResponse = linkAppealConfirmation.handle(callback);

        assertTrue(actualResponse.getConfirmationHeader().isPresent());
        assertTrue(actualResponse.getConfirmationBody().isPresent());

        assertThat(
            actualResponse.getConfirmationHeader().get())
            .contains("# You have linked this appeal");

        assertThat(
            actualResponse.getConfirmationBody().get())
            .contains("#### What happens next\r\n\r\n");

        assertThat(
            actualResponse.getConfirmationBody().get())
            .contains(
                "The appeal will now be considered as part of a set of linked appeals. "
                    + "You must update the linked appeal spreadsheet to reflect this change.");

    }

    @Test
    void should_throw_exception() {
        assertThatThrownBy(() -> linkAppealConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        when(callback.getEvent()).thenReturn(Event.ADD_CASE_NOTE);
        assertThatThrownBy(() -> linkAppealConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}