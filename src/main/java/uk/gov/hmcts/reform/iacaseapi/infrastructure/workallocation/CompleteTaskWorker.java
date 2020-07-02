package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import java.util.HashMap;
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
public class CompleteTaskWorker {
    private final static Logger LOGGER = Logger.getLogger(CompleteTaskWorker.class.getName());
    private final static String CAMUNDA_URL = "http://localhost:8080/engine-rest";

    private final RestTemplate restTemplate;
    private final TaskLog taskLog;

    public CompleteTaskWorker(RestTemplate restTemplate, TaskLog taskLog) {
        this.restTemplate = restTemplate;
        this.taskLog = taskLog;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        LOGGER.info("Registering for Camunda events");

        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl("http://localhost:8080/engine-rest")
//                .asyncResponseTimeout(10000) // long polling timeout
                .maxTasks(1)
                .asyncResponseTimeout(30000)
                .backoffStrategy(new ExponentialBackoffStrategy(0, 0, 0)) // prevents long waits after Camunda hasn't been used for a while
                .build();

        client.subscribe("complete-task")
//                .lockDuration(1000) // the default lock duration is 20 seconds, but you can override this
                .handler((externalTask, externalTaskService) -> {
////                    try {
////                        Thread.sleep(10000);
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
//
//                    String ccdReference = (String) externalTask.getVariable("id");
//                    String taskToComplete = (String)((Map)externalTask.getVariable("completeTask")).get("taskToComplete");
//                    String operation = (String)((Map)externalTask.getVariable("completeTask")).get("operation");
//
//                    LOGGER.info(((Map)externalTask.getVariable("completeTask")).toString());
//                    boolean mapped = (boolean)((Map)externalTask.getVariable("completeTask")).get("mapped");
//
//                    if (mapped) {
//                        LOGGER.info("Completing task [" + taskToComplete + "] for [" + ccdReference + "]");
//
//                        String processVariablesForTaskToComplete = "id_eq_" + ccdReference;
//                        if (!taskToComplete.equalsIgnoreCase("All")) {
//                            processVariablesForTaskToComplete += ",nextTask_eq_" + taskToComplete;
//                        }
//                        ResponseEntity<List<Task>> exchange = restTemplate.exchange(
//                                CAMUNDA_URL + "/task?processDefinitionKey=workAllocation&processVariables=" + processVariablesForTaskToComplete,
//                                HttpMethod.GET, null, new ParameterizedTypeReference<List<Task>>() {
//                                }
//                        );
//                        List<Task> tasks = exchange.getBody();
//
//                        for (Task task : tasks) {
//                            LOGGER.info("Completing task [" + task.getId() + "]");
//
//                            HttpHeaders headers = new HttpHeaders();
//                            headers.setContentType(MediaType.APPLICATION_JSON);
//                            HttpEntity<String> request = new HttpEntity<>("{\"variables\":{\"completionReason\": {\"value\": \"" + operation + "\"}}}", headers);
//                            ResponseEntity<String> res = restTemplate.postForEntity(CAMUNDA_URL + "/task/" + task.getId() + "/complete", request, String.class);
//
//                            if (res.getStatusCode().is2xxSuccessful()) {
//                                LOGGER.info("Completed task [" + task.getId() + "]");
//                            } else {
//                                LOGGER.info("Failed to complete task [" + task.getId() + "] " + res.getStatusCode() + "\n" + res.getBody());
//                            }
//                        }
//
////                    taskLog.end(ccdReference + externalTask.getVariable("event"));
//                    }
                    // Complete the task
                    externalTaskService.complete(externalTask);
                })
                .open();

        LOGGER.info("Registering for Camunda events - done");
    }

    //clear out tasks
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();

        HashMap<String, String> params = new HashMap<>();
        params.put("processDefinitionKey", "workAllocation");
        ResponseEntity<List<Task>> exchange = restTemplate.exchange(
                CAMUNDA_URL + "/task?processDefinitionKey=workAllocation",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Task>>() {}
        );
        List<Task> tasks = exchange.getBody();

        tasks.parallelStream().forEach(task -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>("{}", headers);
            ResponseEntity<String> res = restTemplate.postForEntity(CAMUNDA_URL + "/task/" + task.getId() + "/complete", request, String.class);

            if (res.getStatusCode().is2xxSuccessful()) {
                System.out.println("Completed task [" + task.getId() + "]");
            } else {
                System.out.println("Failed to complete task [" + task.getId() + "] " + res.getStatusCode() + "\n" + res.getBody());
            }
        });
    }

}
