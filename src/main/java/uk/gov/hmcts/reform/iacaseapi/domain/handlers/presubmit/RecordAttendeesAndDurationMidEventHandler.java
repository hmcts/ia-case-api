package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CommonRefDataDynamicListProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IaHearingsApiService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordAttendeesAndDurationMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private static final String SELECT_ATTENDEES_AND_DURATION_HEARING_LIST_PAGE_ID =
            "selectHearingRecordAttendeesAndDuration";
    private static final String TIMES_ATTENDEES_AND_DURATION_PAGE_ID =
            "timesRecordAttendeesAndDuration";

    private static final String ATTENDANCE_TYPE = "attendanceType";

    private final CommonRefDataDynamicListProvider provider;
    private final LocationRefDataService locationRefDataService;
    private final IaHearingsApiService iaHearingsApiService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        log.info("handling event {}, callbackStage {}", callback.getEvent(), callbackStage);

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && callback.getEvent() == Event.RECORD_ATTENDEES_AND_DURATION;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        log.info("Handling callback for RecordAttendeesAndDurationMidEvent, page {}", callback.getPageId());

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        switch (callback.getPageId()) {
            case SELECT_ATTENDEES_AND_DURATION_HEARING_LIST_PAGE_ID -> {
                log.info("selecting attendees and duration hearing");
                asylumCase = iaHearingsApiService.midEvent(callback);

                prePopulateActualHearingCancellationReason(asylumCase);
                prePopulateActualHearingPartHeardReason(asylumCase);
            }
            case TIMES_ATTENDEES_AND_DURATION_PAGE_ID -> {
                log.info("populating attendees and duration participants");
                asylumCase = iaHearingsApiService.midEvent(callback);

                prePopulateAttendanceTypeLists(asylumCase);
            }
            default -> { }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void prePopulateActualHearingCancellationReason(AsylumCase asylumCase) {
        DynamicList hearingCancellationReason =
            asylumCase.read(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, DynamicList.class)
                .orElse(null);
        if (hearingCancellationReason == null) {
            hearingCancellationReason = provider.provideActualCancellationReasonCodes();
        } else {
            hearingCancellationReason = new DynamicList(
                hearingCancellationReason.getValue(),
                provider.provideActualCancellationReasonCodes().getListItems()
            );
        }

        log.info("hearingCancellationReason = {}", hearingCancellationReason);
        asylumCase.write(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, hearingCancellationReason);
    }

    private void prePopulateActualHearingPartHeardReason(AsylumCase asylumCase) {
        DynamicList hearingPartHeardReason =
            asylumCase.read(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, DynamicList.class)
                .orElse(null);
        if (hearingPartHeardReason == null) {
            hearingPartHeardReason = provider.provideActualPartHeardReasonCodes();
        } else {
            hearingPartHeardReason = new DynamicList(
                hearingPartHeardReason.getValue(),
                provider.provideActualPartHeardReasonCodes().getListItems()
            );
        }

        log.info("hearingPartHeardReason = {}", hearingPartHeardReason);
        asylumCase.write(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, hearingPartHeardReason);
    }

    /**
     * Every attendee chooses their attendance type from HearingSubChannel ref data, with HMC's sub-channel
     * preselected. ExUI shows no options for this list in rows added with "Add new".
     */
    @SuppressWarnings("unchecked")
    private void prePopulateAttendanceTypeLists(AsylumCase asylumCase) {
        List<Object> participants = asylumCase.<List<Object>>read(ATTENDEES_AND_DURATION_PARTICIPANTS)
            .orElse(List.of());
        if (participants.isEmpty()) {
            return;
        }

        List<Value> subChannels = provider.provideHearingSubChannels().getListItems();
        List<Object> updated = new ArrayList<>();
        for (Object item : participants) {
            if (!(item instanceof Map<?, ?> original) || !(original.get("value") instanceof Map<?, ?> value)) {
                updated.add(item);
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>((Map<String, Object>) original);
            Map<String, Object> participant = new LinkedHashMap<>((Map<String, Object>) value);
            String selectedCode = selectedCode(participant.get(ATTENDANCE_TYPE)).orElse("");
            Value selected = subChannels.stream()
                .filter(subChannel -> selectedCode.equals(subChannel.getCode()))
                .findFirst()
                .orElse(new Value("", ""));
            participant.put(ATTENDANCE_TYPE, new DynamicList(selected, subChannels));
            row.put("value", participant);
            updated.add(row);
        }
        asylumCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS, updated);
    }

    private Optional<String> selectedCode(Object attendanceType) {
        // HMC's sub-channel from ia-hearings-api, or the caseworker's choice when they come back to the times page
        if (attendanceType instanceof DynamicList list) {
            return Optional.ofNullable(list.getValue()).map(Value::getCode).filter(code -> !code.isBlank());
        }
        if (attendanceType instanceof Map<?, ?> list && list.get("value") instanceof Map<?, ?> value) {
            return Optional.ofNullable(value.get("code")).map(Object::toString).filter(code -> !code.isBlank());
        }
        return Optional.empty();
    }
}
