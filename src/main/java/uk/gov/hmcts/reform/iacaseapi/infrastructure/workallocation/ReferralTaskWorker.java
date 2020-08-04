package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import java.util.Map;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ReferralTaskWorker {
    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")
                .maxTasks(1)
                .asyncResponseTimeout(30000)
                .backoffStrategy(new ExponentialBackoffStrategy(0, 0, 0)) // prevents long waits after Camunda hasn't been used for a while
                .build();

        client.subscribe("referralTask")
                .handler((externalTask, externalTaskService) -> {
                    System.out.println("Handle referral here");
                })
                .open();
    }

}
