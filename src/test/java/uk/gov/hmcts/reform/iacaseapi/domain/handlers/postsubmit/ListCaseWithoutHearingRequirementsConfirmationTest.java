package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AutoRequestHearingService;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class ListCaseWithoutHearingRequirementsConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AutoRequestHearingService autoRequestHearingService;

    private ListCaseWithoutHearingRequirementsConfirmation handler;
    private final long caseId = 1L;
    private PostSubmitCallbackResponse expectedResponse = new PostSubmitCallbackResponse();

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(callback.getEvent()).thenReturn(Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);
        when(autoRequestHearingService.shouldAutoRequestHearing(asylumCase, true)).thenReturn(true);

        handler =
            new ListCaseWithoutHearingRequirementsConfirmation(autoRequestHearingService);
    }

    @Test
    void should_return_successful_confirmation_when_for_auto_request_hearing() {
        String header = "# Hearing listed";
        expectedResponse.setConfirmationHeader(header);
        expectedResponse.setConfirmationBody("""
            #### What happens next

            The hearing request has been created and is visible on the [Hearings tab](/cases/case-details/1/hearings)""");

        when(asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(autoRequestHearingService.buildAutoHearingRequestConfirmation(asylumCase, header, caseId))
            .thenReturn(expectedResponse);

        PostSubmitCallbackResponse callbackResponse = handler.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertEquals(expectedResponse.getConfirmationHeader().get(), callbackResponse.getConfirmationHeader().get());
        assertEquals(expectedResponse.getConfirmationBody().get(), callbackResponse.getConfirmationBody().get());
    }

    @Test
    void should_return_confirmation_when_panel_required() {
        String header = "# List without requirements complete";
        expectedResponse.setConfirmationHeader(header);
        expectedResponse.setConfirmationBody("""
            #### What happens next

            The listing team will now list the case. All parties will be notified when the Hearing Notice is available to view""");

        when(asylumCase.read(IS_PANEL_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(autoRequestHearingService.shouldAutoRequestHearing(asylumCase, false)).thenReturn(false);

        PostSubmitCallbackResponse callbackResponse =
            handler.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertEquals(expectedResponse.getConfirmationHeader().get(), callbackResponse.getConfirmationHeader().get());
        assertEquals(expectedResponse.getConfirmationBody().get(), callbackResponse.getConfirmationBody().get());
    }

    @Test
    void should_return_failed_confirmation_for_auto_request_hearing() {
        expectedResponse.setConfirmationBody("![Hearing could not be listed](https://raw.githubusercontent.com/hmcts/"
                                 + "ia-appeal-frontend/master/app/assets/images/hearingCouldNotBeListed.png)"
                                 + "\n\n"
                                 + "#### What happens next\n\n"
                                 + "The hearing could not be auto-requested. Please manually request the "
                                 + "hearing via the [Hearings tab](/cases/case-details/1/hearings)");
        String header = "# Hearing listed";
        expectedResponse.setConfirmationHeader(header);

        when(asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(autoRequestHearingService.buildAutoHearingRequestConfirmation(asylumCase, header, caseId))
            .thenReturn(expectedResponse);

        PostSubmitCallbackResponse callbackResponse =
            handler.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertEquals(expectedResponse.getConfirmationBody().get(), callbackResponse.getConfirmationBody().get());
        assertEquals(expectedResponse.getConfirmationHeader().get(), callbackResponse.getConfirmationHeader().get());
    }

    @Test
    void should_return_confirmation_when_panel_not_required() {
        final String header = "# You've recorded the agreed hearing adjustments";
        final String body = "#### What happens next\n\n"
                                 + "The listing team will now list the case."
                                 + " All parties will be notified when the Hearing Notice is available to view.<br><br>";

        when(autoRequestHearingService.shouldAutoRequestHearing(asylumCase, true)).thenReturn(false);

        PostSubmitCallbackResponse callbackResponse =
            handler.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertEquals(header, callbackResponse.getConfirmationHeader().get());
        assertEquals(body, callbackResponse.getConfirmationBody().get());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() -> handler.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = handler.canHandle(callback);

            if (event == LIST_CASE_WITHOUT_HEARING_REQUIREMENTS) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "NO", "YES" })
    void should_return_confirmation_ada_journey(YesOrNo yesOrNo) {

        when(asylumCase.read(AUTO_REQUEST_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(autoRequestHearingService.shouldAutoRequestHearing(asylumCase, true)).thenReturn(false);
        when(asylumCase.read(AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));


        PostSubmitCallbackResponse callbackResponse =
                handler.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());


        if (yesOrNo.equals(YesOrNo.NO)) {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("# You've recorded the agreed hearing adjustments");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains(
                            "#### What happens next\n\n"
                                    + "The listing team will now list the case. All parties will be notified when "
                                    + "the Hearing Notice is available to view");

        } else {
            assertThat(
                    callbackResponse.getConfirmationHeader().get())
                    .contains("You've recorded the agreed hearing adjustments");

            assertThat(
                    callbackResponse.getConfirmationBody().get())
                    .contains(
                            "All parties will be notified of the agreed adjustments.");
        }
    }

}
