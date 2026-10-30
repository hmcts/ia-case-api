package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTO_HEARING_REQUEST_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAttendeesAndDurationPreparer.NO_HEARINGS_ERROR_MESSAGE;

import java.util.Collections;
import java.util.List;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordAttendeesAndDurationPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private AsylumCase updatedAsylumCase;
    @Mock
    private IaHearingsApiService iaHearingsApiService;
    @Mock
    private LocationBasedFeatureToggler locationBasedFeatureToggler;

    private RecordAttendeesAndDurationPreparer recordAttendeesAndDurationPreparer;

    @BeforeEach
    public void setUp() {
        when(callback.getEvent()).thenReturn(RECORD_ATTENDEES_AND_DURATION);
        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(updatedAsylumCase);

        recordAttendeesAndDurationPreparer = new RecordAttendeesAndDurationPreparer(
            iaHearingsApiService, locationBasedFeatureToggler);
    }

    @Test
    void should_delegate_call_to_ia_hearings_api_and_return_its_case_data() {
        givenHearings(List.of(new Value("hearing1", "Hearing 1")));

        PreSubmitCallbackResponse<AsylumCase> response =
            recordAttendeesAndDurationPreparer.handle(ABOUT_TO_START, callback);

        verify(iaHearingsApiService, times(1)).aboutToStart(callback);
        assertEquals(updatedAsylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void should_return_error_if_hearings_list_is_empty() {
        givenHearings(Collections.emptyList());

        PreSubmitCallbackResponse<AsylumCase> response =
            recordAttendeesAndDurationPreparer.handle(ABOUT_TO_START, callback);

        assertEquals(1, response.getErrors().size());
        assertEquals(NO_HEARINGS_ERROR_MESSAGE, response.getErrors().iterator().next());
    }

    @Test
    void should_return_error_if_hearings_list_is_missing() {
        when(updatedAsylumCase.read(ATTENDEES_AND_DURATION_HEARING, DynamicList.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response =
            recordAttendeesAndDurationPreparer.handle(ABOUT_TO_START, callback);

        assertEquals(1, response.getErrors().size());
        assertEquals(NO_HEARINGS_ERROR_MESSAGE, response.getErrors().iterator().next());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"NO", "YES"})
    void should_set_auto_hearing_request_enabled_field(YesOrNo value) {
        givenHearings(List.of(new Value("hearing1", "Hearing 1")));
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(updatedAsylumCase)).thenReturn(value);

        recordAttendeesAndDurationPreparer.handle(ABOUT_TO_START, callback);

        verify(updatedAsylumCase, times(1)).write(AUTO_HEARING_REQUEST_ENABLED, value);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordAttendeesAndDurationPreparer.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_START && event == RECORD_ATTENDEES_AND_DURATION) {
                    assertTrue(canHandle, "Can handle event " + event);
                } else {
                    assertFalse(canHandle, "Cannot handle event " + event);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordAttendeesAndDurationPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAttendeesAndDurationPreparer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAttendeesAndDurationPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAttendeesAndDurationPreparer.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordAttendeesAndDurationPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> recordAttendeesAndDurationPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private void givenHearings(List<Value> hearings) {
        DynamicList hearingsList = new DynamicList(new Value("", ""), hearings);
        when(updatedAsylumCase.read(ATTENDEES_AND_DURATION_HEARING, DynamicList.class))
            .thenReturn(Optional.of(hearingsList));
    }
}
