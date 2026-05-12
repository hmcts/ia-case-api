package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

@ExtendWith(MockitoExtension.class)
class RevokeCaseAccessConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private RevokeCaseAccessConfirmation revokeCaseAccessConfirmation;

    @BeforeEach
    void setUp() {
        revokeCaseAccessConfirmation = new RevokeCaseAccessConfirmation();
    }

    @Test
    void should_return_the_confirmation() {

        when(callback.getEvent()).thenReturn(Event.REVOKE_CASE_ACCESS);

        PostSubmitCallbackResponse callbackResponse =
            revokeCaseAccessConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());

        assertSame("# You have revoked case access for the appeal", callbackResponse.getConfirmationHeader().get());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> revokeCaseAccessConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"REVOKE_CASE_ACCESS", "REVOKE_CITIZEN_ACCESS"})
    void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertTrue(revokeCaseAccessConfirmation.canHandle(callback));

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, mode = EnumSource.Mode.EXCLUDE,
        names = {"REVOKE_CASE_ACCESS", "REVOKE_CITIZEN_ACCESS"})
    void it_cannot_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(revokeCaseAccessConfirmation.canHandle(callback));
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> revokeCaseAccessConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> revokeCaseAccessConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}