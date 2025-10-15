package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

@Service
@Slf4j
public class TimedEventServiceScheduler implements Scheduler {

    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final TimedEventServiceApi timedEventServiceApi;

    public TimedEventServiceScheduler(
        AuthTokenGenerator serviceAuthTokenGenerator,
        @Qualifier("requestUser") AccessTokenProvider accessTokenProvider,
        TimedEventServiceApi timedEventServiceApi
    ) {
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.accessTokenProvider = accessTokenProvider;
        this.timedEventServiceApi = timedEventServiceApi;
    }

    @Override
    public TimedEvent schedule(TimedEvent timedEvent) {

        String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        String accessToken = accessTokenProvider.getAccessToken();

        try {

            return timedEventServiceApi.submitTimedEvent(
                accessToken,
                serviceAuthorizationToken,
                timedEvent
            );

        } catch (FeignException e) {

            throw new AsylumCaseServiceResponseException(
                String.format(
                    "Couldn't schedule timed event for caseId: %d, event: %s",
                    timedEvent.getCaseId(),
                    timedEvent.getEvent()
                ),
                e
            );
        }
    }

    public boolean deleteSchedule(String timedEventId) {
        String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        String accessToken = accessTokenProvider.getAccessToken();

        try {
            timedEventServiceApi.deleteTimedEvent(
                accessToken,
                serviceAuthorizationToken,
                timedEventId
            );
        } catch (FeignException ex) {
            log.warn("The schedule with id {} could not be deleted. This should not happen and may cause problems. " +
                    "Check that the schedule ID is correct.",
                timedEventId);
            return false;
        }

        return true;
    }

    @Override
    public void scheduleTimedEvent(String caseId, ZonedDateTime scheduledDate, Event event, String timedEventId) {
        try {
            schedule(
                    new TimedEvent(
                            timedEventId,
                            event,
                            scheduledDate,
                            "IA",
                            "Asylum",
                            Long.parseLong(caseId)
                    )
            );
            log.info("Scheduled event " + event + " for case ID " + caseId);
        } catch (AsylumCaseServiceResponseException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void scheduleTimedEventNow(String caseId, Event event) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault()); ;
        scheduleTimedEvent(caseId, now, event, "");
    }

    
}
