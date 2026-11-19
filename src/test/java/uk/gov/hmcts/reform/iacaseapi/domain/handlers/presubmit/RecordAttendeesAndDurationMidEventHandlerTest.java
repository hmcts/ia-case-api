package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CommonRefDataDynamicListProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordAttendeesAndDurationMidEventHandlerTest {

    private static final String SELECT_HEARING_PAGE_ID = "selectHearingRecordAttendeesAndDuration";
    private static final String TIMES_PAGE_ID = "timesRecordAttendeesAndDuration";

    private final List<Value> cancellationReasons = List.of(
        new Value("cancelled", "Cancelled"),
        new Value("withdrawn", "Withdrawn"));
    private final List<Value> subChannels = List.of(
        new Value("INTER", "In Person"),
        new Value("TELCVP", "Telephone - CVP"),
        new Value("VIDCVP", "Video - CVP"));
    private final List<Value> roles = List.of(
        new Value("APEL", "Appellant"),
        new Value("INTP", "Interpreter"),
        new Value("LGRP", "Legal Representative"));
    private final List<Value> partHeardReasons = List.of(
        new Value("timeRanOut", "Time ran out"),
        new Value("judgeUnwell", "Judge unwell"));

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase updatedAsylumCase;
    @Mock
    private CommonRefDataDynamicListProvider provider;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private IaHearingsApiService iaHearingsApiService;

    private RecordAttendeesAndDurationMidEventHandler handler;

    @BeforeEach
    void setUp() {
        when(callback.getEvent()).thenReturn(RECORD_ATTENDEES_AND_DURATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(iaHearingsApiService.midEvent(callback)).thenReturn(updatedAsylumCase);
        when(provider.provideActualCancellationReasonCodes())
            .thenReturn(new DynamicList(new Value("", ""), cancellationReasons));
        when(provider.provideActualPartHeardReasonCodes())
            .thenReturn(new DynamicList(new Value("", ""), partHeardReasons));

        when(provider.provideHearingSubChannels()).thenReturn(new DynamicList(new Value("", ""), subChannels));
        when(provider.provideEntityRoleCodes()).thenReturn(new DynamicList(new Value("", ""), roles));

        handler = new RecordAttendeesAndDurationMidEventHandler(provider, locationRefDataService, iaHearingsApiService);
    }

    @Test
    void should_delegate_to_ia_hearings_api_on_select_hearing_page() {
        when(callback.getPageId()).thenReturn(SELECT_HEARING_PAGE_ID);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(MID_EVENT, callback);

        verify(iaHearingsApiService, times(1)).midEvent(callback);
        assertEquals(updatedAsylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void should_delegate_to_ia_hearings_api_on_times_page() {
        when(callback.getPageId()).thenReturn(TIMES_PAGE_ID);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(MID_EVENT, callback);

        verify(iaHearingsApiService, times(1)).midEvent(callback);
        assertEquals(updatedAsylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());
        verifyNoInteractions(provider);
    }

    @Test
    void should_populate_reason_lists_when_not_already_present() {
        when(callback.getPageId()).thenReturn(SELECT_HEARING_PAGE_ID);
        when(updatedAsylumCase.read(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, DynamicList.class))
            .thenReturn(Optional.empty());
        when(updatedAsylumCase.read(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, DynamicList.class))
            .thenReturn(Optional.empty());

        handler.handle(MID_EVENT, callback);

        verify(updatedAsylumCase).write(
            ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON,
            new DynamicList(new Value("", ""), cancellationReasons));
        verify(updatedAsylumCase).write(
            ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON,
            new DynamicList(new Value("", ""), partHeardReasons));
    }

    @Test
    void should_keep_existing_selections_and_refresh_reason_list_items() {
        when(callback.getPageId()).thenReturn(SELECT_HEARING_PAGE_ID);
        Value selectedCancellationReason = new Value("withdrawn", "Withdrawn");
        Value selectedPartHeardReason = new Value("timeRanOut", "Time ran out");
        when(updatedAsylumCase.read(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(selectedCancellationReason, List.of(selectedCancellationReason))));
        when(updatedAsylumCase.read(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(selectedPartHeardReason, List.of(selectedPartHeardReason))));

        handler.handle(MID_EVENT, callback);

        verify(updatedAsylumCase).write(
            ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON,
            new DynamicList(selectedCancellationReason, cancellationReasons));
        verify(updatedAsylumCase).write(
            ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON,
            new DynamicList(selectedPartHeardReason, partHeardReasons));
    }

    @Test
    void should_add_ref_data_lists_to_participants_on_times_page() {
        when(callback.getPageId()).thenReturn(TIMES_PAGE_ID);
        Map<String, Object> fromHmc = row("1", Map.of("prefilled", "Yes", "attendanceType", hmcValue("TELCVP"),
            "role", hmcValue("INTP"), "roleLabel", "INTP"));
        Map<String, Object> alreadyChosen = row("2", Map.of("prefilled", "Yes",
            "attendanceType", Map.of("value", Map.of("code", "VIDCVP", "label", "Video - CVP")),
            "role", hmcValue("APEL"), "roleLabel", "Appellant"));
        Map<String, Object> unknownValues = row("3", Map.of("prefilled", "Yes", "attendanceType", hmcValue("TELBTM"),
            "role", hmcValue("XXXX"), "roleLabel", "XXXX"));
        Map<String, Object> added = row("4", Map.of("firstName", "Ann",
            "role", Map.of("value", Map.of("code", "LGRP", "label", "Legal Representative")),
            "representedParty", Map.of("value", Map.of("code", "APEL", "label", "Appellant"))));
        when(updatedAsylumCase.read(ATTENDEES_AND_DURATION_PARTICIPANTS))
            .thenReturn(Optional.of(List.of(fromHmc, alreadyChosen, unknownValues, added)));

        handler.handle(MID_EVENT, callback);

        List<Object> participants = writtenParticipants();
        assertEquals(new DynamicList(new Value("TELCVP", "Telephone - CVP"), subChannels),
            field(participants.get(0), "attendanceType"));
        assertEquals(new DynamicList(new Value("INTP", "Interpreter"), roles), field(participants.get(0), "role"));
        assertEquals("Interpreter", field(participants.get(0), "roleLabel"));
        DynamicList noRole = new DynamicList(new Value("", ""), roles);
        assertEquals(noRole, field(participants.get(0), "representedParty"));

        assertEquals(new DynamicList(new Value("VIDCVP", "Video - CVP"), subChannels),
            field(participants.get(1), "attendanceType"));
        assertEquals("Appellant", field(participants.get(1), "roleLabel"));

        assertEquals(new DynamicList(new Value("", ""), subChannels), field(participants.get(2), "attendanceType"));
        assertEquals(noRole, field(participants.get(2), "role"));
        assertEquals("XXXX", field(participants.get(2), "roleLabel"));

        assertEquals(new DynamicList(new Value("", ""), subChannels), field(participants.get(3), "attendanceType"));
        assertEquals(new DynamicList(new Value("LGRP", "Legal Representative"), roles),
            field(participants.get(3), "role"));
        assertEquals(new DynamicList(new Value("APEL", "Appellant"), roles),
            field(participants.get(3), "representedParty"));
        assertEquals(null, field(participants.get(3), "roleLabel"));
        assertEquals("Ann", field(participants.get(3), "firstName"));
        assertEquals("1", ((Map<?, ?>) participants.get(0)).get("id"));
    }

    @SuppressWarnings("unchecked")
    private List<Object> writtenParticipants() {
        ArgumentCaptor<List<Object>> captor = ArgumentCaptor.forClass(List.class);
        verify(updatedAsylumCase).write(eq(ATTENDEES_AND_DURATION_PARTICIPANTS), captor.capture());
        return captor.getValue();
    }

    @Test
    void should_not_call_ref_data_when_there_are_no_participants() {
        when(callback.getPageId()).thenReturn(TIMES_PAGE_ID);
        when(updatedAsylumCase.read(ATTENDEES_AND_DURATION_PARTICIPANTS)).thenReturn(Optional.of(List.of()));

        handler.handle(MID_EVENT, callback);

        verifyNoInteractions(provider);
        verify(updatedAsylumCase, never()).write(eq(ATTENDEES_AND_DURATION_PARTICIPANTS), any());
    }

    private static Map<String, Object> hmcValue(String code) {
        return Map.of("value", Map.of("code", code, "label", code), "list_items", List.of());
    }

    private static Map<String, Object> row(String id, Map<String, Object> value) {
        return Map.of("id", id, "value", value);
    }

    @SuppressWarnings("unchecked")
    private static Object field(Object row, String name) {
        return ((Map<String, Object>) ((Map<String, Object>) row).get("value")).get(name);
    }

    @Test
    void should_return_case_data_unchanged_on_other_pages() {
        when(callback.getPageId()).thenReturn("someOtherPage");

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(MID_EVENT, callback);

        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());
        verifyNoInteractions(provider, locationRefDataService, iaHearingsApiService);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (callbackStage == MID_EVENT && event == RECORD_ATTENDEES_AND_DURATION) {
                    assertTrue(canHandle, "Can handle event " + event);
                } else {
                    assertFalse(canHandle, "Cannot handle event " + event);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> handler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
