package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class PostSubmitCallbackDispatcherTest {

    @Mock private PostSubmitCallbackHandler<CaseData> handler1;
    @Mock private PostSubmitCallbackHandler<CaseData> handler2;
    @Mock private PostSubmitCallbackHandler<CaseData> handler3;
    @Mock private Callback<CaseData> callback;
    @Mock private PostSubmitCallbackResponse response;

    private PostSubmitCallbackDispatcher<CaseData> postSubmitCallbackDispatcher;

    @Before
    public void setUp() {
        postSubmitCallbackDispatcher = new PostSubmitCallbackDispatcher<>(
            Arrays.asList(
                handler1,
                handler2,
                handler3
            )
        );
    }

    @Test
    public void should_dispatch_callback_to_first_eligible_handler_collecting_confirmation() {

        Optional<String> expectedConfirmationHeader = Optional.of("header");
        Optional<String> expectedConfirmationBody = Optional.of("body");

        when(response.getConfirmationHeader()).thenReturn(expectedConfirmationHeader);
        when(response.getConfirmationBody()).thenReturn(expectedConfirmationBody);

        when(handler1.canHandle(callback)).thenReturn(false);

        when(handler2.canHandle(callback)).thenReturn(true);
        when(handler2.handle(callback)).thenReturn(response);

        PostSubmitCallbackResponse callbackResponse =
            postSubmitCallbackDispatcher.handle(callback);

        assertNotNull(callbackResponse);
        assertEquals(expectedConfirmationHeader, callbackResponse.getConfirmationHeader());
        assertEquals(expectedConfirmationBody, callbackResponse.getConfirmationBody());

        verify(handler1, times(1)).canHandle(callback);
        verify(handler1, times(0)).handle(callback);

        verify(handler2, times(1)).canHandle(callback);
        verify(handler2, times(1)).handle(callback);

        verify(handler3, times(0)).canHandle(callback);
        verify(handler3, times(0)).handle(callback);
    }

    @Test
    public void should_not_error_if_no_handlers_are_provided() {

        PostSubmitCallbackDispatcher<CaseData> postSubmitCallbackDispatcher =
            new PostSubmitCallbackDispatcher<>(Collections.emptyList());

        try {

            PostSubmitCallbackResponse callbackResponse =
                postSubmitCallbackDispatcher.handle(callback);

            assertNotNull(callbackResponse);
            assertEquals(Optional.empty(), callbackResponse.getConfirmationHeader());
            assertEquals(Optional.empty(), callbackResponse.getConfirmationBody());

        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }
    }

    @Test
    public void should_not_allow_null_handlers() {

        assertThatThrownBy(() -> new PostSubmitCallbackDispatcher<>(null))
            .hasMessage("callbackHandlers must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_not_allow_null_parameters() {

        assertThatThrownBy(() -> postSubmitCallbackDispatcher.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
