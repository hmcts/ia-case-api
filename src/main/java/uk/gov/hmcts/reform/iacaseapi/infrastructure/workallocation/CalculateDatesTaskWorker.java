package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CalculateDatesTaskWorker {
    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")
                .maxTasks(1)
                .asyncResponseTimeout(30000)
                .backoffStrategy(new ExponentialBackoffStrategy(0, 0, 0)) // prevents long waits after Camunda hasn't been used for a while
                .build();

        client.subscribe("calculate-dates")
                .handler((externalTask, externalTaskService) -> {
                    String dueDateString = (String) externalTask.getVariable("dueDate");

                    Map<String, Object> timedDates = new HashMap<>();
                    if (dueDateString != null) {
                        LocalDate dueDate = LocalDate.parse(dueDateString);

                        java.time.LocalDateTime localDateTime = dueDate.minusDays(1).atStartOfDay();
                        timedDates.put("reminderDateISO", localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
                        timedDates.put("dueDateISO", dueDate.atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
                        timedDates.put("escalationDateISO", dueDate.plusDays(1).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
                    } else {
                        int firstDueDate = (int) ((Map)externalTask.getVariable("task")).get("firstDueDate");
                        int secondDueDate = (int) ((Map)externalTask.getVariable("task")).get("secondDueDate");
                        int thirdDueDate = (int) ((Map)externalTask.getVariable("task")).get("thirdDueDate");

                        timedDates.put("reminderDateISO", LocalDate.now().plusDays(firstDueDate).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
                        timedDates.put("dueDateISO", LocalDate.now().plusDays(secondDueDate).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
                        timedDates.put("escalationDateISO", LocalDate.now().plusDays(thirdDueDate).atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME));
                    }

                    externalTaskService.complete(externalTask, timedDates);
                })
                .open();
    }
}
