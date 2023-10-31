package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Arrays;
import java.util.Objects;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RecordAdjournmentDetailsPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private IaHearingsApiService iaHearingsApiService;

    private RecordAdjournmentDetailsPreparer recordAdjournmentDetailsPreparer;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        recordAdjournmentDetailsPreparer = new RecordAdjournmentDetailsPreparer(iaHearingsApiService);
    }

    @Test
    void should_delegate_call_to_ia_hearings_api() {
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(asylumCase);

        recordAdjournmentDetailsPreparer.handle(ABOUT_TO_START, callback);

        verify(iaHearingsApiService, times(1)).aboutToStart(callback);
    }

    @Test
    void should_clear_all_fields() {

        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(iaHearingsApiService.aboutToStart(callback)).thenReturn(asylumCase);

        recordAdjournmentDetailsPreparer.handle(ABOUT_TO_START, callback);

        Arrays.asList(
            ADJOURNMENT_DETAILS_HEARING,
            HEARING_ADJOURNMENT_WHEN,
            RELIST_CASE_IMMEDIATELY,
            NEXT_HEARING_LOCATION,
            NEXT_HEARING_DURATION,
            HEARING_ADJOURNMENT_DECISION_PARTY,
            HEARING_ADJOURNMENT_DECISION_PARTY_NAME,
            HEARING_ADJOURNMENT_REQUESTING_PARTY,
            ANY_ADDITIONAL_ADJOURNMENT_INFO,
            ADDITIONAL_ADJOURNMENT_INFO,
            NEXT_HEARING_DATE,
            NEXT_HEARING_DATE_FIXED,
            NEXT_HEARING_DATE_RANGE_EARLIEST,
            NEXT_HEARING_DATE_RANGE_LATEST,
            SHOULD_RESERVE_OR_EXCLUDE_JUDGE,
            RESERVE_OR_EXCLUDE_JUDGE,
            NEXT_HEARING_FORMAT).forEach(field -> verify(asylumCase).clear(field));
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = recordAdjournmentDetailsPreparer.canHandle(callbackStage, callback);

                if (Objects.equals(callbackStage, ABOUT_TO_START)
                    && Objects.equals(RECORD_ADJOURNMENT_DETAILS, callback.getEvent())) {

                    assertTrue(canHandle, "Can handle event " + event);
                } else {
                    assertFalse(canHandle, "Cannot handle event " + event);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> recordAdjournmentDetailsPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAdjournmentDetailsPreparer.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAdjournmentDetailsPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> recordAdjournmentDetailsPreparer.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> recordAdjournmentDetailsPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> recordAdjournmentDetailsPreparer.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
