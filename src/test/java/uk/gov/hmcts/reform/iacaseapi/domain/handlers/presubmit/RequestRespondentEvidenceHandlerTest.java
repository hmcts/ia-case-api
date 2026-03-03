package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CALCULATED_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LISTING_AVAILABLE_FOR_ADA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.holidaydates.HolidayService;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RequestRespondentEvidenceHandlerTest {

    private static final List<LocalDate> HOLIDAYS = List.of(
        LocalDate.of(2022, Month.DECEMBER, 25),
        LocalDate.of(2022, Month.JANUARY, 1),
        LocalDate.of(2022, Month.APRIL, 15)
    );

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private HolidayService holidayService;
    private DueDateService dueDateService;

    private RequestRespondentEvidenceHandler requestRespondentEvidenceHandler;

    @BeforeEach
    public void setUp() {
        holidayService = new HolidayService(HOLIDAYS);
        dueDateService = new DueDateService(holidayService);
        requestRespondentEvidenceHandler =
            new RequestRespondentEvidenceHandler(dueDateService);
    }

    @Test
    void should_set_the_bundle_flag_on_valid_case_data() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_EVIDENCE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.empty());

        requestRespondentEvidenceHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(asylumCase).write(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE, YesOrNo.YES);
    }

    @Test
    void should_write_necessary_fields_for_accelerated_detained_appeals() {
        // Testing limited by ZonedDateTime being a final class: it can't be involved in mocking/spying

        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONDENT_EVIDENCE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        requestRespondentEvidenceHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(asylumCase).write(LISTING_AVAILABLE_FOR_ADA, YesOrNo.YES);

        verify(asylumCase).write(eq(CALCULATED_HEARING_DATE), anyString());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> requestRespondentEvidenceHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(
            () -> requestRespondentEvidenceHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
        verify(asylumCase, never()).write(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE, YesOrNo.YES);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestRespondentEvidenceHandler.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_RESPONDENT_EVIDENCE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> requestRespondentEvidenceHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> requestRespondentEvidenceHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestRespondentEvidenceHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestRespondentEvidenceHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
