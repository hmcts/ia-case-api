package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_CANCEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_UPDATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DURATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_FORMAT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAdjournmentDetailsMidEventHandler.CASE_MANAGEMENT_CANCELLATION_REASONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAdjournmentDetailsMidEventHandler.CHANGE_REASONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAdjournmentDetailsMidEventHandler.CHECK_HEARING_DATE_PAGE_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAdjournmentDetailsMidEventHandler.INITIALIZE_FIELDS_PAGE_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAdjournmentDetailsMidEventHandler.IS_CHILD_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAdjournmentDetailsMidEventHandler.NEXT_HEARING_DATE_CHOOSE_DATE_RANGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAdjournmentDetailsMidEventHandler.NEXT_HEARING_DATE_RANGE_ERROR_MESSAGE;

import java.util.List;
import java.util.Optional;
import java.util.Set;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class RecordAdjournmentDetailsMidEventHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private RefDataUserService refDataUserService;

    @Mock
    private CommonDataResponse commonDataResponse;
    @Mock
    private CategoryValues categoryValues;
    private RecordAdjournmentDetailsMidEventHandler handler;

    private final DynamicList hearingChannel = new DynamicList(new Value("INTER", "In Person"),
            List.of(new Value("INTER",
                    "In Person")));

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(commonDataResponse.getCategoryValues()).thenReturn(List.of(categoryValues));
        when(categoryValues.getKey()).thenReturn("cancellationKey1");
        when(categoryValues.getValueEn()).thenReturn("cancellationKey1");
        when(refDataUserService.retrieveCategoryValues(CASE_MANAGEMENT_CANCELLATION_REASONS, IS_CHILD_REQUIRED))
                .thenReturn(commonDataResponse);

        handler = new RecordAdjournmentDetailsMidEventHandler(refDataUserService);

    }

    @Test
    void should_populate_hearing_values() {

        initializeCommonHearingValues();
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(NEXT_HEARING_FORMAT, hearingChannel);
        verify(asylumCase, times(1)).write(NEXT_HEARING_DURATION, "60");
        verify(asylumCase, times(1))
                .write(NEXT_HEARING_LOCATION, HearingCentre.GLASGOW_TRIBUNALS_CENTRE);
    }

    @Test
    void should_create_an_error_if_hearing_range_values_are_not_set() {
        initializeCommonHearingValues();

        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(callback.getPageId()).thenReturn(CHECK_HEARING_DATE_PAGE_ID);

        when(asylumCase.read(NEXT_HEARING_DATE, String.class))
                .thenReturn(Optional.of(NEXT_HEARING_DATE_CHOOSE_DATE_RANGE));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals(Set.of(NEXT_HEARING_DATE_RANGE_ERROR_MESSAGE),
                callbackResponse.getErrors());
    }


    @Test
    void should_not_populate_hearing_values() {
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(NEXT_HEARING_FORMAT), any());
        verify(asylumCase, never()).write(eq(NEXT_HEARING_DURATION), any());
        verify(asylumCase, never()).write(eq(NEXT_HEARING_LOCATION), any());
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
                () -> handler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(SEND_DIRECTION);
        assertThatThrownBy(
                () -> handler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (event.equals(RECORD_ADJOURNMENT_DETAILS)
                        && callbackStage == MID_EVENT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_populate_cancellation_reasons_values() {
        initializeCommonHearingValues();
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);
        when(refDataUserService.retrieveCategoryValues(CHANGE_REASONS, IS_CHILD_REQUIRED))
                .thenReturn(commonDataResponse);


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        DynamicList cancellationList = new DynamicList(new Value("", ""),
                List.of(new Value(categoryValues.getKey(), categoryValues.getValueEn())));
        verify(asylumCase, times(1)).write(HEARING_REASON_TO_CANCEL, cancellationList);
    }

    @Test
    void should_populate_update_reasons_values() {
        initializeCommonHearingValues();
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);
        when(refDataUserService.retrieveCategoryValues(CHANGE_REASONS, IS_CHILD_REQUIRED))
                .thenReturn(commonDataResponse);
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        DynamicList cancellationList = new DynamicList(new Value("", ""),
                List.of(new Value(categoryValues.getKey(), categoryValues.getValueEn())));
        verify(asylumCase, times(1)).write(HEARING_REASON_TO_UPDATE, cancellationList);
    }

    private void initializeCommonHearingValues() {
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class))
                .thenReturn(Optional.of(hearingChannel));
        when(asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)).thenReturn(Optional.of("60"));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.GLASGOW_TRIBUNALS_CENTRE));
        when(asylumCase.read(NEXT_HEARING_FORMAT, DynamicList.class))
                .thenReturn(Optional.of(
                        new DynamicList(
                                new Value("",
                                        ""),
                                List.of(new Value("INTER",
                                        "In Person")))));
    }
}
