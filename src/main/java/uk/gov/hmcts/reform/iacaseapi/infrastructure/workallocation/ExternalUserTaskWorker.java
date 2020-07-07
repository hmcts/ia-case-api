package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.camunda.bpm.client.task.ExternalTask;
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
                    String directionToComplete = (String)((Map)externalTask.getVariable("completeTask")).get("directionToComplete");
                    String operation = (String)((Map)externalTask.getVariable("completeTask")).get("operation");
                    boolean mapped = (boolean)((Map)externalTask.getVariable("completeTask")).get("mapped");

                    LOGGER.info("Completing task for direction [" + directionToComplete + "] for [" + ccdReference + "]");

                    if (mapped) {
                        if (operation.equalsIgnoreCase("updateDueDate")) {
                            String directionId = externalTask.getVariable("directionId");
                            LOGGER.info("Updating due date for [" + ccdReference + "] operation [" + operation + "] direction id [" + directionId + "]");

                            String processVariablesForTaskToComplete = "id_eq_" + ccdReference + ",directionId_eq_" + directionId;
                            ResponseEntity<List<Task>> exchange = restTemplate.exchange(
                                    CAMUNDA_URL + "/task?processDefinitionKey=workAllocationExternal&processVariables=" + processVariablesForTaskToComplete,
                                    HttpMethod.GET, null, new ParameterizedTypeReference<List<Task>>() {
                                    }
                            );
                            List<Task> tasks = exchange.getBody();

                            LOGGER.info(CAMUNDA_URL + "/task?processDefinitionKey=workAllocationExternal&processVariables=" + processVariablesForTaskToComplete + " Found [" + tasks.size() + "]");
                            for (Task task : tasks) {
                                updateDueDates(task, externalTask);
                            }
                        } else {
                            String processVariablesForDirectionToComplete = "id_eq_" + ccdReference;
                            if (!directionToComplete.equalsIgnoreCase("All")) {
                                processVariablesForDirectionToComplete += ",direction_eq_" + directionToComplete;
                            }
                            ResponseEntity<List<Task>> exchange = restTemplate.exchange(
                                    CAMUNDA_URL + "/task?processDefinitionKey=workAllocationExternal&processVariables=" + processVariablesForDirectionToComplete,
                                    HttpMethod.GET, null, new ParameterizedTypeReference<List<Task>>() {
                                    }
                            );
                            List<Task> tasks = exchange.getBody();

                            for (Task task : tasks) {
                                LOGGER.info("Completing external task [" + task.getId() + "]");

                                completeTasks(operation, task);
                            }
                        }
                    }
                    // Complete the task
                    externalTaskService.complete(externalTask);
                })
                .open();

        LOGGER.info("Registering for external user Camunda events - done");
    }

    private void updateDueDates(Task task, ExternalTask externalTask) {
        String executionId = task.getExecutionId();

        LOGGER.info(CAMUNDA_URL + "/job?processDefinitionKey=workAllocationExternal&executionId=" + executionId);
        ResponseEntity<List<Job>> jobResult = restTemplate.exchange(
                CAMUNDA_URL + "/job?processDefinitionKey=workAllocationExternal&executionId=" + executionId,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Job>>() {
                }
        );
        List<Job> jobsToUpdate = jobResult.getBody();

        Map<String, Object> newDates = CalculateDatesTaskWorker.calculateDates(externalTask);
        for (Job job : jobsToUpdate) {
            LOGGER.info("Looking up job" + job);
            ResponseEntity<JobDefinition> jobDefinitionResult = restTemplate.exchange(
                    CAMUNDA_URL + "/job-definition/" + job.getJobDefinitionId(),
                    HttpMethod.GET, null, JobDefinition.class);
            Object dueDate;
            String jobId = jobDefinitionResult.getBody().getActivityId();
            if (jobId.equalsIgnoreCase("reminderTimer")) {
                dueDate = newDates.get("reminderDateISO");
            } else if (jobId.equalsIgnoreCase("dueDateTimer")) {
                dueDate = newDates.get("dueDateISO");
            } else if (jobId.equalsIgnoreCase("escalationTimer")) {
                dueDate = newDates.get("escalationDateISO");
            } else {
                LOGGER.info("Job id [" + jobId + "] not mapped to a new date.");
                return;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String body = "{\"duedate\": \"" + dueDate.toString() + ".000+0000\"}";

            LOGGER.info(body);
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            restTemplate.put(CAMUNDA_URL + "/job/" + job.getId() + "/duedate", request);
        }
    }

    private void completeTasks(String operation, Task task) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"variables\":{\"completionReason\": {\"value\": \"" + operation + "\"}}}", headers);
        ResponseEntity<String> res = restTemplate.postForEntity(CAMUNDA_URL + "/task/" + task.getId() + "/complete", request, String.class);

        if (res.getStatusCode().is2xxSuccessful()) {
            LOGGER.info("Completed task [" + task.getId() + "]");
        } else {
            LOGGER.info("Failed to complete task [" + task.getId() + "] " + res.getStatusCode() + "\n" + res.getBody());
        }
    }
}
