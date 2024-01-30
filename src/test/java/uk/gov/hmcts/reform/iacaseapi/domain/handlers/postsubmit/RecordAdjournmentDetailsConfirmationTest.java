package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ADJOURNMENT_WHEN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HMC_REQUEST_SUCCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RecordAdjournmentDetailsConfirmationTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private LocationBasedFeatureToggler locationBasedFeatureToggler;

    private final Long caseId = 112233L;

    private RecordAdjournmentDetailsConfirmation recordAdjournmentDetailsConfirmation;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getId()).thenReturn(caseId);
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase))
            .thenReturn(YesOrNo.YES);

        recordAdjournmentDetailsConfirmation =
            new RecordAdjournmentDetailsConfirmation(locationBasedFeatureToggler);
    }

    @Test
    void should_return_hearing_update_success_confirmation() {

        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(HearingAdjournmentDay.BEFORE_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(UPDATE_HMC_REQUEST_SUCCESS, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        PostSubmitCallbackResponse callbackResponse =
            recordAdjournmentDetailsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(callbackResponse.getConfirmationHeader().get())
            .contains("# You have recorded the adjournment details");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### Do this next\n\n"
                + "The hearing will be adjourned using the details recorded.\n\n"
                + "The adjournment details are available on the [Hearing requirements tab](/cases/case-details/"
                + caseId + "#Hearing%20and%20appointment).");
    }

    @Test
    void should_return_hearing_update_request_failed_confirmation() {

        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
                .thenReturn(Optional.of(HearingAdjournmentDay.BEFORE_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        PostSubmitCallbackResponse callbackResponse =
                recordAdjournmentDetailsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(callbackResponse.getConfirmationHeader().get())
                .contains("# You have recorded the adjournment details");
        assertThat(
                callbackResponse.getConfirmationBody().get())
                .contains("#### Do this next\n\n"
                        + "The hearing could not be automatically updated. You will need to update the "
                        + "[hearings manually](/cases/case-details/" + caseId + "/hearings )."
                        + "\n\nThe adjournment details are available on the "
                        + "[Hearing requirements](/cases/case-details/" + caseId
                        + "#Hearing%20and%20appointment) tab.");
    }

    @Test
    void should_return_hearing_cancellation_success_confirmation() {

        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(HearingAdjournmentDay.BEFORE_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PostSubmitCallbackResponse callbackResponse =
            recordAdjournmentDetailsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(callbackResponse.getConfirmationHeader().get())
            .contains("# You have recorded the adjournment details");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### Do this next\n\n"
                + "All parties will be informed of the decision to adjourn without a date.\n\n"
                + "The existing hearing will be cancelled.\n\n"
                + "The adjournment details are available on the [Hearing requirements tab](/cases/case-details/"
                + caseId + "#Hearing%20and%20appointment).");
    }

    @Test
    void should_return_hearing_cancellation_failed_confirmation() {

        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(HearingAdjournmentDay.BEFORE_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PostSubmitCallbackResponse callbackResponse =
            recordAdjournmentDetailsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(callbackResponse.getConfirmationHeader().get())
            .contains("# You have recorded the adjournment details");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### Do this next\n\n"
                      + "All parties will be informed of the decision to adjourn without a date.\n\n"
                      + "The hearing could not be automatically cancelled. "
                      + "The hearing can be cancelled on the [Hearings tab](/cases/case-details/" + caseId + "/hearings)\n\n"
                      + "The adjournment details are available on the "
                      + "[Hearing requirements tab](/cases/case-details/" + caseId + "#Hearing%20and%20appointment).");
    }

    @Test
    void should_return_auto_hearing_request_success_confirmation() {

        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(HearingAdjournmentDay.ON_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PostSubmitCallbackResponse callbackResponse =
            recordAdjournmentDetailsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(callbackResponse.getConfirmationHeader().get())
            .contains("# You have recorded the adjournment details");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### Do this next\n\n"
                      + "The hearing request has been created and is visible on the [Hearings tab]"
                      + "(/cases/case-details/" + caseId + "/hearings)");
    }

    @Test
    void should_return_auto_hearing_request_failed_confirmation() {

        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(HearingAdjournmentDay.ON_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        PostSubmitCallbackResponse callbackResponse =
            recordAdjournmentDetailsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(callbackResponse.getConfirmationHeader().get())
            .contains("# You have recorded the adjournment details");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("![Hearing could not be listed](https://raw.githubusercontent.com/hmcts/"
                      + "ia-appeal-frontend/master/app/assets/images/hearingCouldNotBeListed.png)"
                      + "\n\n"
                      + "#### Do this next\n\n"
                      + "The hearing could not be auto-requested. Please manually request the "
                      + "hearing via the [Hearings tab](/cases/case-details/" + caseId + "/hearings)");
    }

    @Test
    void should_return_default_confirmation() {

        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(HearingAdjournmentDay.ON_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        PostSubmitCallbackResponse callbackResponse =
            recordAdjournmentDetailsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(callbackResponse.getConfirmationHeader().get())
            .contains("# You have recorded the adjournment details");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### Do this next\n\n"
                      + "The hearing will be adjourned using the details recorded.\n\n"
                      + "The adjournment details are available on the [Hearing requirements tab](/cases/case-details/"
                      + caseId + "#Hearing%20and%20appointment).\n\n"
                      + "You must now [update the hearing actuals in the hearings tab](/cases/case-details/"
                      + caseId + "/hearings).");
    }

    @Test
    void should_return_default_confirmation_when_auto_request_hearing_is_not_enabled() {

        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(HearingAdjournmentDay.ON_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase))
            .thenReturn(YesOrNo.NO);

        PostSubmitCallbackResponse callbackResponse =
            recordAdjournmentDetailsConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getConfirmationHeader().isPresent());
        assertTrue(callbackResponse.getConfirmationBody().isPresent());

        assertThat(callbackResponse.getConfirmationHeader().get())
            .contains("# You have recorded the adjournment details");
        assertThat(
            callbackResponse.getConfirmationBody().get())
            .contains("#### Do this next\n\n"
                      + "The hearing will be adjourned using the details recorded.\n\n"
                      + "The adjournment details are available on the [Hearing requirements tab](/cases/case-details/"
                      + caseId + "#Hearing%20and%20appointment).\n\n"
                      + "You must now [update the hearing actuals in the hearings tab](/cases/case-details/"
                      + caseId + "/hearings).");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);

        assertThatThrownBy(() -> recordAdjournmentDetailsConfirmation.handle(callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            boolean canHandle = recordAdjournmentDetailsConfirmation.canHandle(callback);

            if (event == RECORD_ADJOURNMENT_DETAILS) {

                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordAdjournmentDetailsConfirmation.canHandle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAdjournmentDetailsConfirmation.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
