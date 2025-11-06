package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_ID_LIST;

@Slf4j
@Component
public class RetriggerWaTasksForFixedCaseIdHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean timedEventServiceEnabled;
    private final DateProvider dateProvider;
    private final Scheduler scheduler;


    public RetriggerWaTasksForFixedCaseIdHandler(
        @Value("${featureFlag.timedEventServiceEnabled}") boolean timedEventServiceEnabled,
        DateProvider dateProvider,
        Scheduler scheduler
    ) {
        this.timedEventServiceEnabled = timedEventServiceEnabled;
        this.dateProvider = dateProvider;
        this.scheduler = scheduler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Event qualifyingEvent = Event.RE_TRIGGER_WA_BULK_TASKS;

        return timedEventServiceEnabled
            && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == qualifyingEvent;

    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        int scheduleDelayInMinutes = 5;
        ZonedDateTime scheduledDate = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(scheduleDelayInMinutes);

        String caseIdList = asylumCase.read(CASE_ID_LIST, String.class)
            .orElse("");

        if (!caseIdList.contains(",")) {
            log.info("No Case Ids found to re-trigger WA tasks");
            return new PreSubmitCallbackResponse<>(asylumCase);
        }
        String[] caseIdListList = caseIdList.split(",");

        Arrays.stream(caseIdListList)
            .forEach(caseId -> {
                    if (caseId.length() != 16) {
                        log.info("Invalid Case Id found to re-trigger WA tasks: {}", caseId);
                        return;
                    }
                    scheduler.scheduleTimedEvent(caseId, scheduledDate, Event.RE_TRIGGER_WA_TASKS, "");
                }
            );
        asylumCase.clear(CASE_ID_LIST);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
