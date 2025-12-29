package uk.gov.hmcts.reform.bailcaseapi.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.CcdEventAuthorizor;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class PostSubmitCallbackDispatcherTest {

    @Mock
    private PostSubmitCallbackHandler<CaseData> handler1;

    @Mock
    private PostSubmitCallbackHandler<CaseData> handler2;

    @Mock
    private PostSubmitCallbackHandler<CaseData> handler3;

    @Mock
    private CcdEventAuthorizor ccdEventAuthorizor;

    @Mock
    private Callback<CaseData> callback;

    @Mock
    private PostSubmitCallbackResponse response;

    private PostSubmitCallbackDispatcher<CaseData> postSubmitCallbackDispatcher;

    @BeforeEach
    public void setUp() {
        postSubmitCallbackDispatcher = new PostSubmitCallbackDispatcher<>(
            ccdEventAuthorizor,
            Arrays.asList(
                handler1,
                handler2,
                handler3
            )
        );
    }

    @Test
    void should_call_all_handlers_and_get_confirmation_in_end() {

        Optional<String> expectedConfirmationHeader = Optional.of("Expected Header");
        Optional<String> expectedConfirmationBody = Optional.of("Expected Body");

        when(response.getConfirmationBody()).thenReturn(expectedConfirmationBody);
        when(response.getConfirmationHeader()).thenReturn(expectedConfirmationHeader);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);


        when(handler1.canHandle(callback)).thenReturn(true);
        when(handler1.handle(callback)).thenReturn(response);

        when(handler2.canHandle(callback)).thenReturn(false);

        when(handler3.canHandle(callback)).thenReturn(true);
        when(handler3.handle(callback)).thenReturn(response);

        response = postSubmitCallbackDispatcher.handle(callback);

        assertNotNull(response);
        assertEquals(expectedConfirmationBody, response.getConfirmationBody());
        assertEquals(expectedConfirmationHeader, response.getConfirmationHeader());

        verify(handler1, times(1)).canHandle(callback);
        verify(handler1, times(1)).handle(callback);
        verify(handler2, times(1)).canHandle(callback);
        verify(handler2, times(0)).handle(callback);
        verify(handler3, times(1)).canHandle(callback);
        verify(handler3, times(1)).handle(callback);
        verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.SUBMIT_APPLICATION);

    }

    @Test
    void should_not_fail_for_no_handlers() {
        postSubmitCallbackDispatcher = new PostSubmitCallbackDispatcher<>(
            ccdEventAuthorizor,
            Arrays.asList()
        );

        assertDoesNotThrow(() -> response = postSubmitCallbackDispatcher.handle(callback));
        assertNotNull(response);
        assertEquals(Optional.empty(), response.getConfirmationHeader());
        assertEquals(Optional.empty(), response.getConfirmationBody());
    }

    @Test
    void should_fail_for_null_handlers() {
        assertThatThrownBy(() -> postSubmitCallbackDispatcher = new PostSubmitCallbackDispatcher<>(null, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackHandlers cannot be null");
    }
}
