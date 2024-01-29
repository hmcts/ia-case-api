package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTO_REQUEST_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_PANEL_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class ReviewHearingRequirementsConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private LocationBasedFeatureToggler locationBasedFeatureToggler;

    private ReviewHearingRequirementsConfirmation handler;
    private final long caseId = 1L;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(REVIEW_HEARING_REQUIREMENTS);
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(YES);
        when(asylumCase.read(AUTO_REQUEST_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));

        handler =
            new ReviewHearingRequirementsConfirmation(locationBasedFeatureToggler);
    }

    @Test
    void should_return_successful_confirmation_when_auto_request_enabled_and_panel_not_required() {

        PostSubmitCallbackResponse callbackResponse =
            handler.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("Hearing listed");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The hearing request has been created and is visible on the [Hearings tab]"
                      + "(/cases/case-details/1/hearings)");

    }

    @Test
    void should_return_different_confirmation_when_auto_request_enabled_and_panel_required() {

        when(asylumCase.read(IS_PANEL_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YES));

        PostSubmitCallbackResponse callbackResponse =
            handler.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("Hearing requirements complete");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The listing team will now list the case. All parties will be notified when "
                      + "the Hearing Notice is available to view");

    }

    @Test
    void should_return_failed_confirmation_when_auto_request_enabled() {

        when(asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(YES));

        PostSubmitCallbackResponse callbackResponse =
            handler.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertEquals("", callbackResponse.getConfirmationHeader().get());

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("hearingCouldNotBeListed.png");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("The hearing could not be auto-requested. Please manually request the "
                      + "hearing via the [Hearings tab](/cases/case-details/1/hearings)");
    }

    @Test
    void should_return_confirmation_when_auto_request_not_enabled() {

        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(NO);
        when(asylumCase.read(AUTO_REQUEST_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));

        PostSubmitCallbackResponse callbackResponse =
            handler.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the agreed hearing adjustments");


        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("You should ensure that the case flags reflect the hearing requests that have been approved. This may require adding new case flags or making active flags inactive.");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("[Add case flag](/case/IA/Asylum/" + caseId + "/trigger/createFlag)");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("[Manage case flags](/case/IA/Asylum/" + caseId + "/trigger/manageFlags)");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "The listing team will now list the case. All parties will be notified when the Hearing Notice is available to view.");

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

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

            if (event == REVIEW_HEARING_REQUIREMENTS) {
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

}
