package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalUserTaskWorker {
    private final static Logger LOGGER = Logger.getLogger(CompleteTaskWorker.class.getName());
    private final static String CAMUNDA_URL = "http://localhost:8080/engine-rest";

    private final RestTemplate restTemplate;
    private final TaskLog taskLog;

    public ExternalUserTaskWorker(RestTemplate restTemplate, TaskLog taskLog) {
        this.restTemplate = restTemplate;
        this.taskLog = taskLog;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        LOGGER.info("Registering for Camunda events external task");

        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")
//                .asyncResponseTimeout(10000) // long polling timeout
                .maxTasks(1)
                .asyncResponseTimeout(30000)
                .backoffStrategy(new ExponentialBackoffStrategy(0, 0, 0)) // prevents long waits after Camunda hasn't been used for a while
                .build();

        client.subscribe("send_reminder")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {
                    String ccdReference = (String) externalTask.getVariable("id");
                    LOGGER.info("Send reminder for [" + ccdReference + "]");

                    externalTaskService.complete(externalTask);
                })
                .open();

        client.subscribe("out_of_time")
                .lockDuration(1000)
                .handler((externalTask, externalTaskService) -> {
                    String ccdReference = (String) externalTask.getVariable("id");
                    LOGGER.info("Out of time for [" + ccdReference + "]");

                    externalTaskService.complete(externalTask);
                })
                .open();

        client.subscribe("complete-task-external")
                .lockDuration(1000) // the default lock duration is 20 seconds, but you can override this
                .handler((externalTask, externalTaskService) -> {
                    String ccdReference = (String) externalTask.getVariable("id");
                    String directionToComplete = (String)((Map)externalTask.getVariable("task")).get("directionToComplete");

                    LOGGER.info("Completing task for direction [" + directionToComplete + "] for [" + ccdReference + "]");

                    String processVariablesForDirectionToComplete = "id_eq_" + ccdReference;
                    if (!directionToComplete.equalsIgnoreCase("All")) {
                        processVariablesForDirectionToComplete += ",direction_eq_" + directionToComplete;
                    }
                    ResponseEntity<List<Task>> exchange = restTemplate.exchange(
                            CAMUNDA_URL + "/task?processDefinitionKey=workAllocationExternal&processVariables=" + processVariablesForDirectionToComplete,
                            HttpMethod.GET, null, new ParameterizedTypeReference<List<Task>>() {}
                    );
                    List<Task> tasks = exchange.getBody();

                    for (Task task : tasks) {
                        LOGGER.info("Completing external task [" + task.getId() + "]");

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<String> request = new HttpEntity<>("{}", headers);
                        ResponseEntity<String> res = restTemplate.postForEntity(CAMUNDA_URL + "/task/" + task.getId() + "/complete", request, String.class);

                        if (res.getStatusCode().is2xxSuccessful()) {
                            LOGGER.info("Completed task [" + task.getId() + "]");
                        } else {
                            LOGGER.info("Failed to complete task [" + task.getId() + "] " + res.getStatusCode() + "\n" + res.getBody());
                        }
                    }
                    // Complete the task
                    externalTaskService.complete(externalTask);
                })
                .open();

        LOGGER.info("Registering for external user Camunda events - done");
    }
}
