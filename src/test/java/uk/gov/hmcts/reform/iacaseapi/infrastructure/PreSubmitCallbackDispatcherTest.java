package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.CcdEventAuthorizor;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class PreSubmitCallbackDispatcherTest {

    @Mock private CcdEventAuthorizor ccdEventAuthorizor;
    @Mock private PreSubmitCallbackHandler<CaseData> handler1;
    @Mock private PreSubmitCallbackHandler<CaseData> handler2;
    @Mock private PreSubmitCallbackHandler<CaseData> handler3;
    @Mock private Callback<CaseData> callback;
    @Mock private CaseDetails<CaseData> caseDetails;
    @Mock private CaseData caseData;
    @Mock private CaseData caseDataMutation1;
    @Mock private CaseData caseDataMutation2;
    @Mock private CaseData caseDataMutation3;
    @Mock private PreSubmitCallbackResponse<CaseData> response1;
    @Mock private PreSubmitCallbackResponse<CaseData> response2;
    @Mock private PreSubmitCallbackResponse<CaseData> response3;

    private PreSubmitCallbackDispatcher<CaseData> preSubmitCallbackDispatcher;

    @Before
    public void setUp() {
        when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
        when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
        when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.LATE);

        preSubmitCallbackDispatcher = new PreSubmitCallbackDispatcher<>(
            ccdEventAuthorizor,
            Arrays.asList(
                handler1,
                handler2,
                handler3
            )
        );
    }

    @Test
    public void should_dispatch_callback_to_handlers_according_to_priority_collecting_any_error_messages() {

        Set<String> expectedErrors =
            ImmutableSet.of("error1", "error2", "error3", "error4");

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
            when(callback.getCaseDetails()).thenReturn(caseDetails);

            when(response1.getData()).thenReturn(caseDataMutation1);
            when(response1.getErrors()).thenReturn(ImmutableSet.of("error1"));

            when(response2.getData()).thenReturn(caseDataMutation2);
            when(response2.getErrors()).thenReturn(ImmutableSet.of("error2", "error3"));

            when(response3.getData()).thenReturn(caseDataMutation3);
            when(response3.getErrors()).thenReturn(ImmutableSet.of("error4"));

            when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler1.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler1.handle(eq(callbackStage), any(Callback.class))).thenReturn(response1);

            when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
            when(handler2.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler2.handle(eq(callbackStage), any(Callback.class))).thenReturn(response2);

            when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler3.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler3.handle(eq(callbackStage), any(Callback.class))).thenReturn(response3);

            // re-assing to use handler dispatch priority sorting feature
            preSubmitCallbackDispatcher = new PreSubmitCallbackDispatcher<>(
                ccdEventAuthorizor,
                Arrays.asList(
                    handler1,
                    handler2,
                    handler3
                )
            );

            PreSubmitCallbackResponse<CaseData> callbackResponse =
                preSubmitCallbackDispatcher.handle(callbackStage, callback);

            assertNotNull(callbackResponse);
            assertEquals(caseDataMutation2, callbackResponse.getData());
            assertThat(callbackResponse.getErrors(), is(expectedErrors));

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.BUILD_CASE);

            InOrder inOrder = inOrder(handler1, handler3, handler2);

            inOrder.verify(handler1, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            inOrder.verify(handler1, times(1)).handle(eq(callbackStage), any(Callback.class));

            inOrder.verify(handler3, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            inOrder.verify(handler3, times(1)).handle(eq(callbackStage), any(Callback.class));

            inOrder.verify(handler2, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            inOrder.verify(handler2, times(1)).handle(eq(callbackStage), any(Callback.class));

            reset(ccdEventAuthorizor, handler1, handler2, handler3);
        }
    }

    @Test
    public void should_only_dispatch_callback_to_handlers_that_can_handle_it() {

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(response1.getData()).thenReturn(caseData);
            when(response1.getErrors()).thenReturn(Collections.emptySet());

            when(response2.getData()).thenReturn(caseData);
            when(response2.getErrors()).thenReturn(Collections.emptySet());

            when(response3.getData()).thenReturn(caseData);
            when(response3.getErrors()).thenReturn(Collections.emptySet());

            when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler1.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(false);
            when(handler1.handle(eq(callbackStage), any(Callback.class))).thenReturn(response1);

            when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
            when(handler2.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(false);
            when(handler2.handle(eq(callbackStage), any(Callback.class))).thenReturn(response2);

            when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler3.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler3.handle(eq(callbackStage), any(Callback.class))).thenReturn(response3);

            PreSubmitCallbackResponse<CaseData> callbackResponse =
                preSubmitCallbackDispatcher.handle(callbackStage, callback);

            assertNotNull(callbackResponse);
            assertEquals(caseData, callbackResponse.getData());
            assertTrue(callbackResponse.getErrors().isEmpty());

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.BUILD_CASE);

            verify(handler1, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            verify(handler1, times(0)).handle(eq(callbackStage), any(Callback.class));

            verify(handler2, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            verify(handler2, times(0)).handle(eq(callbackStage), any(Callback.class));

            verify(handler3, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            verify(handler3, times(1)).handle(eq(callbackStage), any(Callback.class));

            reset(ccdEventAuthorizor, handler1, handler2, handler3);
        }
    }

    @Test
    public void should_not_dispatch_to_handlers_if_user_not_authorized_for_event() {

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(callback.getEvent()).thenReturn(Event.BUILD_CASE);

            doThrow(AccessDeniedException.class)
                .when(ccdEventAuthorizor)
                .throwIfNotAuthorized(Event.BUILD_CASE);

            assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(callbackStage, callback))
                .isExactlyInstanceOf(AccessDeniedException.class);

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.BUILD_CASE);

            verify(handler1, never()).canHandle(any(), any());
            verify(handler1, never()).handle(any(), any());
            verify(handler2, never()).canHandle(any(), any());
            verify(handler2, never()).handle(any(), any());
            verify(handler3, never()).canHandle(any(), any());
            verify(handler3, never()).handle(any(), any());

            reset(ccdEventAuthorizor, handler1, handler2, handler3);
        }
    }

    @Test
    public void should_not_error_if_no_handlers_are_provided() {

        PreSubmitCallbackDispatcher<CaseData> preSubmitCallbackDispatcher =
            new PreSubmitCallbackDispatcher<>(ccdEventAuthorizor, Collections.emptyList());

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            try {

                when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(caseData);

                PreSubmitCallbackResponse<CaseData> callbackResponse =
                    preSubmitCallbackDispatcher
                        .handle(callbackStage, callback);

                assertNotNull(callbackResponse);
                assertEquals(caseData, callbackResponse.getData());
                assertTrue(callbackResponse.getErrors().isEmpty());

                verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.BUILD_CASE);

                reset(ccdEventAuthorizor);

            } catch (Exception e) {
                fail("Should not have thrown any exception");
            }
        }
    }

    @Test
    public void should_not_allow_null_ccd_event_authorizor() {

        assertThatThrownBy(() -> new PreSubmitCallbackDispatcher<>(null, Collections.emptyList()))
            .hasMessage("ccdEventAuthorizor must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_not_allow_null_handlers() {

        assertThatThrownBy(() -> new PreSubmitCallbackDispatcher<>(ccdEventAuthorizor, null))
            .hasMessage("callbackHandlers must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_sort_handlers_by_name() {
        PreSubmitCallbackHandler<CaseData> h1 = new AaaNameTestHandler();
        PreSubmitCallbackHandler<CaseData> h2 = new BbbNameTestHandler();
        PreSubmitCallbackHandler<CaseData> h3 = new CccNameTestHandler();

        preSubmitCallbackDispatcher = new PreSubmitCallbackDispatcher<>(
            ccdEventAuthorizor,
            Arrays.asList(
                h2,
                h3,
                h1
            )
        );

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
            when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(response1.getData()).thenReturn(caseData);
            when(response1.getErrors()).thenReturn(Collections.emptySet());

            when(response2.getData()).thenReturn(caseData);
            when(response2.getErrors()).thenReturn(Collections.emptySet());

            when(response3.getData()).thenReturn(caseData);
            when(response3.getErrors()).thenReturn(Collections.emptySet());

            preSubmitCallbackDispatcher.handle(callbackStage, callback);

            InOrder inOrder = inOrder(response3, response1, response2);

            inOrder.verify(response3, times(1)).getData();
            inOrder.verify(response3, times(1)).getErrors();

            inOrder.verify(response1, times(1)).getData();
            inOrder.verify(response1, times(1)).getErrors();

            inOrder.verify(response2, times(1)).getData();
            inOrder.verify(response2, times(1)).getErrors();
        }

        reset(ccdEventAuthorizor);
    }


    // created real handler classes, because you cannot mock getClass final method in mockito - use powermock?
    class AaaNameTestHandler implements PreSubmitCallbackHandler<CaseData> {
        @Override
        public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<CaseData> callback) {
            return true;
        }

        @Override
        public PreSubmitCallbackResponse<CaseData> handle(PreSubmitCallbackStage callbackStage, Callback<CaseData> callback) {
            return response1;
        }
    }

    class BbbNameTestHandler implements PreSubmitCallbackHandler<CaseData> {
        @Override
        public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<CaseData> callback) {
            return true;
        }

        @Override
        public PreSubmitCallbackResponse<CaseData> handle(PreSubmitCallbackStage callbackStage, Callback<CaseData> callback) {
            return response2;
        }
    }

    class CccNameTestHandler implements PreSubmitCallbackHandler<CaseData> {
        @Override
        public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<CaseData> callback) {
            return true;
        }

        @Override
        public PreSubmitCallbackResponse<CaseData> handle(PreSubmitCallbackStage callbackStage, Callback<CaseData> callback) {
            return response3;
        }

        @Override
        public DispatchPriority getDispatchPriority() {
            return DispatchPriority.EARLY;
        }
    }

}
