package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;


@ExtendWith(MockitoExtension.class)
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

    private ReviewHearingRequirementsConfirmation reviewHearingRequirementsConfirmation;

    private long caseId = 1234;

    @BeforeEach
    public void setUp() {
        reviewHearingRequirementsConfirmation =
            new ReviewHearingRequirementsConfirmation(locationBasedFeatureToggler);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "REVIEW_HEARING_REQUIREMENTS", "UPDATE_HEARING_ADJUSTMENTS"
    })
    void should_return_confirmation_when_review_hearing_requirements(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(callback.getEvent()).thenReturn(event);

        PostSubmitCallbackResponse callbackResponse =
            reviewHearingRequirementsConfirmation.handle(callback);

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
    void should_return_confirmation_when_list_case_without_requirements() {

        when(callback.getEvent()).thenReturn(Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(YesOrNo.NO);

        PostSubmitCallbackResponse callbackResponse =
            reviewHearingRequirementsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(
            callbackResponse.getConfirmationHeader().get())
            .contains("You've recorded the agreed hearing adjustments");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .doesNotContain("You should ensure that the case flags reflect the hearing requests that have been approved. This may require adding new case flags or making active flags inactive.");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .doesNotContain("[Add case flag](/case/IA/Asylum/" + caseId + "/trigger/createFlag)");

        assertThat(
                callbackResponse.getConfirmationBody().get())
                .doesNotContain("[Manage case flags](/case/IA/Asylum/" + caseId + "/trigger/manageFlags)");

        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains(
                "The listing team will now list the case. All parties will be notified when the Hearing Notice is available to view.");

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        assertThatThrownBy(() -> reviewHearingRequirementsConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES","NO"})
    void it_can_handle_callback(YesOrNo isAutoHearingRequestEnabled) {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase))
                .thenReturn(isAutoHearingRequestEnabled);

            boolean canHandle = reviewHearingRequirementsConfirmation.canHandle(callback);

            if (event == Event.REVIEW_HEARING_REQUIREMENTS
                || event == Event.UPDATE_HEARING_ADJUSTMENTS
                || (event == Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS
                    && !isAutoHearingRequestEnabled.equals(YES))) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> reviewHearingRequirementsConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewHearingRequirementsConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}
