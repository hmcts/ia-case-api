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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_CANCEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_UPDATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DURATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_FORMAT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_VENUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAdjournmentDetailsMidEventHandler.CHECK_HEARING_DATE_PAGE_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAdjournmentDetailsMidEventHandler.INITIALIZE_FIELDS_PAGE_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAdjournmentDetailsMidEventHandler.NEXT_HEARING_DATE_CHOOSE_DATE_RANGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.RecordAdjournmentDetailsMidEventHandler.NEXT_HEARING_DATE_RANGE_ERROR_MESSAGE;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CommonRefDataDynamicListProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

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
    private CommonRefDataDynamicListProvider provider;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private IaHearingsApiService iaHearingsApiService;
    @Captor
    private ArgumentCaptor<DynamicList> hearingVenueCaptor;
    @Captor
    private ArgumentCaptor<DynamicList> hearingChannelCaptor;
    private RecordAdjournmentDetailsMidEventHandler handler;

    private final DynamicList hearingChannel = new DynamicList(new Value("INTER", "In Person"),
            List.of(new Value("INTER", "In Person")));
    private final DynamicList hearingChannelFromRefData = new DynamicList(new Value("", ""),
        List.of(
            new Value("TEL", "Telephone"),
            new Value("INTER", "In Person"),
            new Value("VID", "Video")
        )
    );
    private final DynamicList nextHearingVenue = new DynamicList(new Value("366559", "Glasgow"),
            List.of(new Value("366559",
                    "Glasgow")));

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(locationRefDataService.getHearingLocationsDynamicList()).thenReturn(nextHearingVenue);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.GLASGOW_TRIBUNALS_CENTRE));
        when(provider.provideCaseManagementCancellationReasons()).thenReturn(new DynamicList(""));
        when(provider.provideChangeReasons()).thenReturn(new DynamicList(""));

        handler = new RecordAdjournmentDetailsMidEventHandler(
            provider, locationRefDataService, iaHearingsApiService);

    }

    @Test
    void should_populate_next_hearing_format() {

        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class))
            .thenReturn(Optional.of(hearingChannel));
        when(provider.provideHearingChannels()).thenReturn(hearingChannelFromRefData);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(provider, times(1)).provideHearingChannels();
        verify(asylumCase, times(1))
            .write(eq(NEXT_HEARING_FORMAT), hearingChannelCaptor.capture());
        assertEquals(hearingChannel.getValue(), hearingChannelCaptor.getValue().getValue());
        assertEquals(hearingChannelFromRefData.getListItems(), hearingChannelCaptor.getValue().getListItems());
    }

    @Test
    void should_populate_next_hearing_format_when_hearing_channel_is_not_set() {

        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);
        when(provider.provideHearingChannels()).thenReturn(hearingChannelFromRefData);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(MID_EVENT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(provider, times(1)).provideHearingChannels();
        verify(asylumCase, times(1))
            .write(eq(NEXT_HEARING_FORMAT), hearingChannelCaptor.capture());
        assertEquals(hearingChannelFromRefData.getValue(), hearingChannelCaptor.getValue().getValue());
        assertEquals(hearingChannelFromRefData.getListItems(), hearingChannelCaptor.getValue().getListItems());
    }

    @Test
    void should_populate_next_hearing_length() {

        when(asylumCase.read(LISTING_LENGTH, HoursMinutes.class)).thenReturn(Optional.of(
            new HoursMinutes(1,0)
        ));

        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(NEXT_HEARING_DURATION, "60");
    }

    @Test
    void should_populate_next_hearing_venue_from_list_case_hearing_centre() {

        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(locationRefDataService, times(1)).getHearingLocationsDynamicList();
        verify(asylumCase, times(1))
            .write(eq(NEXT_HEARING_VENUE), hearingVenueCaptor.capture());
        assertEquals(nextHearingVenue.getValue(), hearingVenueCaptor.getValue().getValue());
    }

    static Stream<Arguments> listCaseHearingCentre() {
        return Stream.of(
            Arguments.of(Optional.empty()),
            Arguments.of(Optional.of(HearingCentre.REMOTE_HEARING)),
            Arguments.of(Optional.of(HearingCentre.DECISION_WITHOUT_HEARING))
            );
    }

    @ParameterizedTest
    @MethodSource("listCaseHearingCentre")
    void should_attempt_to_get_location_from_hmc_if_epims_id_empty(Optional<HearingCentre> hearingCentre) {

        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(hearingCentre);
        when(iaHearingsApiService.midEvent(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(locationRefDataService, times(1)).getHearingLocationsDynamicList();
        verify(iaHearingsApiService, times(1)).midEvent(callback);
    }

    @Test
    void should_create_an_error_if_hearing_range_values_are_not_set() {

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
    void should_not_populate_next_hearing_duration() {
        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, never()).write(eq(NEXT_HEARING_DURATION), any());
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
        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);
        List<Value> values = List.of(new Value("keyA", "valueA"));
        DynamicList reasons = new DynamicList(new Value("", ""), values);
        when(provider.provideCaseManagementCancellationReasons()).thenReturn(reasons);


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(HEARING_REASON_TO_CANCEL, reasons);
    }

    @Test
    void should_populate_update_reasons_values() {
        when(callback.getPageId()).thenReturn(INITIALIZE_FIELDS_PAGE_ID);
        List<Value> values = List.of(new Value("keyA", "valueA"));
        DynamicList reasons = new DynamicList(new Value("", ""), values);
        when(provider.provideChangeReasons()).thenReturn(reasons);
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                handler.handle(MID_EVENT, callback);
        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(HEARING_REASON_TO_UPDATE, reasons);
    }
}
