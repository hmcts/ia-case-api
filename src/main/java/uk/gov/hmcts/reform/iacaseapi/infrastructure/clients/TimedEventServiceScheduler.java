package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

@Slf4j
@Service
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
        log.info(serviceAuthorizationToken);
        log.info(accessToken);
        log.info(timedEvent.toString());
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
}
