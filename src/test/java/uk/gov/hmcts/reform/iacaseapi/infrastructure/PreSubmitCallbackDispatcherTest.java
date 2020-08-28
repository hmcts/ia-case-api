package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import com.google.common.collect.ImmutableSet;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.*;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation.EventValid;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation.EventValidCheckers;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.CcdEventAuthorizor;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PreSubmitCallbackDispatcherTest {

    @Mock private CcdEventAuthorizor ccdEventAuthorizor;
    @Mock private PreSubmitCallbackHandler<CaseData> handler1;
    @Mock private PreSubmitCallbackHandler<CaseData> handler2;
    @Mock private PreSubmitCallbackHandler<CaseData> handler3;
    @Mock private PreSubmitCallbackStateHandler<CaseData> stateHandler;
    @Mock private EventValidCheckers<AsylumCase> eventValidChecker;
    @Mock private Callback<CaseData> callback;
    @Mock private CaseDetails<CaseData> caseDetails;
    @Mock private CaseData caseData;
    @Mock private CaseData caseDataMutation1;
    @Mock private CaseData caseDataMutation2;
    @Mock private CaseData caseDataMutation3;
    @Mock private PreSubmitCallbackResponse<CaseData> response1;
    @Mock private PreSubmitCallbackResponse<CaseData> response2;
    @Mock private PreSubmitCallbackResponse<CaseData> response3;

    PreSubmitCallbackDispatcher<CaseData> preSubmitCallbackDispatcher;

    @BeforeEach
    void setUp() {

        preSubmitCallbackDispatcher = new PreSubmitCallbackDispatcher(
            ccdEventAuthorizor,
            Arrays.asList(
                handler1,
                handler2,
                handler3
            ),
            eventValidChecker,
            Arrays.asList(stateHandler)
        );
    }

    @Test
    void should_add_errors_if_events_invalid_for_journey_type() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(caseData);
        when(eventValidChecker.check(any(Callback.class))).thenReturn(new EventValid("Invalid reason"));

        PreSubmitCallbackResponse<CaseData> callbackResponse = preSubmitCallbackDispatcher.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals(callbackResponse.getErrors(), ImmutableSet.of("Invalid reason"));
    }

    @Test
    void should_dispatch_callback_to_handlers_according_to_priority_collecting_any_error_messages() {

        Set<String> expectedErrors =
            ImmutableSet.of("error1", "error2", "error3", "error4");

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(caseDetails.getCaseData()).thenReturn(caseData);
            //when(caseDetails.getState()).thenReturn(State.DECISION);

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

            when(eventValidChecker.check(any(Callback.class))).thenReturn(new EventValid());

            PreSubmitCallbackResponse<CaseData> callbackResponse =
                preSubmitCallbackDispatcher.handle(callbackStage, callback);

            assertNotNull(callbackResponse);
            assertEquals(caseDataMutation2, callbackResponse.getData());
            assertEquals(callbackResponse.getErrors(), expectedErrors);

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
    void should_only_dispatch_callback_to_handlers_that_can_handle_it() {

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(callback.getEvent()).thenReturn(Event.BUILD_CASE);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(caseData);

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

            when(eventValidChecker.check(any(Callback.class))).thenReturn(new EventValid());

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
    void should_not_dispatch_to_handlers_if_user_not_authorized_for_event() {

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
    void should_not_error_if_no_handlers_are_provided() {

        PreSubmitCallbackDispatcher<CaseData> preSubmitCallbackDispatcher =
            new PreSubmitCallbackDispatcher(ccdEventAuthorizor, Collections.emptyList(), eventValidChecker, Collections.emptyList());
        when(eventValidChecker.check(any(Callback.class))).thenReturn(new EventValid());

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
    void should_not_allow_null_ccd_event_authorizor() {

        assertThatThrownBy(() -> new PreSubmitCallbackDispatcher<>(null, Collections.emptyList(), eventValidChecker, Collections.emptyList()))
            .hasMessage("ccdEventAuthorizor must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_handlers() {

        assertThatThrownBy(() -> new PreSubmitCallbackDispatcher<>(ccdEventAuthorizor, null, eventValidChecker, null))
            .hasMessage("callbackHandlers must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_sort_handlers_by_name() {
        PreSubmitCallbackHandler<AsylumCase> h1 = new AppealGroundsForDisplayFormatter();
        PreSubmitCallbackHandler<AsylumCase> h2 = new BuildCaseHandler(mock(DocumentReceiver.class), mock(DocumentsAppender.class));
        PreSubmitCallbackHandler<AsylumCase> h3 = new LegalRepresentativeDetailsHandler(mock(UserDetailsProvider.class));
        PreSubmitCallbackHandler<AsylumCase> h4 = new RequestCaseEditPreparer();
        PreSubmitCallbackHandler<AsylumCase> h5 = new RespondentReviewAppealResponseAddedUpdater();
        PreSubmitCallbackHandler<AsylumCase> h6 = new SendNotificationHandler(mock(NotificationSender.class));

        PreSubmitCallbackDispatcher<AsylumCase> dispatcher = new PreSubmitCallbackDispatcher<>(
            ccdEventAuthorizor,
            Arrays.asList(
                h6,
                h5,
                h2,
                h3,
                h1,
                h4
            ),
                eventValidChecker,
            Collections.emptyList()
        );

        List<PreSubmitCallbackHandler<AsylumCase>> sortedDispatcher =
            (List<PreSubmitCallbackHandler<AsylumCase>>) ReflectionTestUtils.getField(dispatcher, "sortedCallbackHandlers");

        assertEquals(6, sortedDispatcher.size());
        assertEquals(h1, sortedDispatcher.get(0));
        assertEquals(h2, sortedDispatcher.get(1));
        assertEquals(h3, sortedDispatcher.get(2));
        assertEquals(h4, sortedDispatcher.get(3));
        assertEquals(h5, sortedDispatcher.get(4));
        assertEquals(h6, sortedDispatcher.get(5));
    }
}
