package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import static java.util.stream.Collectors.toList;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import org.camunda.bpm.dmn.engine.*;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;

@Component
public class SendToWorkAllocationImpl implements SendToWorkAllocation<AsylumCase> {
    private final static Logger LOGGER = Logger.getLogger(SendToWorkAllocationImpl.class.getName());
    private final static String CAMUNDA_URL = "http://localhost:8080/engine-rest";

    private final RestTemplate restTemplate;
    private final String endpoint;
    private final TaskLog taskLog;

    public SendToWorkAllocationImpl(RestTemplate restTemplate,
                                    @Value("${workAllocationApi.endpoint}") String endpoint,
                                    TaskLog taskLog) {
        this.restTemplate = restTemplate;
        this.endpoint = endpoint;
        this.taskLog = taskLog;
    }

    public void handle(Callback<AsylumCase> callback) {

        long ccdId = callback.getCaseDetails().getId();
        String event = callback.getEvent().toString();
        String currentState = callback.getCaseDetails().getState().toString();
        String previousStateString = callback.getCaseDetailsBefore().map(previousState -> {
            return previousState.getState().toString();
        }).orElse("");

        AsylumCase caseData = callback.getCaseDetails().getCaseData();
        String hearingCentreString = caseData.<HearingCentre>read(AsylumCaseFieldDefinition.HEARING_CENTRE).map(hearingCentre -> {
            return hearingCentre.getValue();
        }).orElse("");

        String appellantName = caseData.<String>read(AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY).orElse("");

        String assignedTo = caseData.<String>read(AsylumCaseFieldDefinition.ASSIGNED_TO).orElse(null);

        LOGGER.info("Creating task for [" + ccdId + "] [" + event + "] assigned to [" + assignedTo + "]");

        createTask(ccdId, event, currentState, previousStateString, hearingCentreString, appellantName, assignedTo);
    }

//    @EventListener(ApplicationReadyEvent.class)
    public void setUpTestTasks() {
        System.out.println("setting up data");
        for (int i = 0; i < 100; i++) {
            createTask(i, "submitAppeal", "appealSubmitted", "", "hearing centre", "Appellant name", null);
            System.out.println("created task " + i);
        }

        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(60000 * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int ccdId = i;
//            createTask(ccdId, "submitAppeal", "appealSubmitted", "", "hearing centre", "Appellant name");
            createTask(ccdId, "requestRespondentEvidence", "awaitingRespondentEvidence", "appealSubmitted", "hearing centre", "Appellant name", null);
            System.out.println("completed task " + i);

        }
        System.out.println("setting up data - done");
    }

    public void createTask(long ccdId, String event, String currentState, String previousStateString, String hearingCentreString, String appellantName, String assignedTo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HashMap<String, WorkAllocationVariable> variables = new HashMap<>();
        variables.put("id", new WorkAllocationVariable(ccdId + "", "String"));
        variables.put("event", new WorkAllocationVariable(event, "String"));
        variables.put("currentState", new WorkAllocationVariable(currentState, "String"));
        variables.put("previousState", new WorkAllocationVariable(previousStateString, "String"));
        variables.put("hearingCentre", new WorkAllocationVariable(hearingCentreString, "String"));
        variables.put("appellantName", new WorkAllocationVariable(appellantName, "String"));
        variables.put("assignedTo", new WorkAllocationVariable(assignedTo, "String"));
        String correlationId = UUID.randomUUID().toString();
        LOGGER.info("Creating workflow with correlation ID [" + correlationId + "]");
        variables.put("correlationId", new WorkAllocationVariable(correlationId, "String"));

        WorkAllocationRequest workAllocationRequest = new WorkAllocationRequest(variables);

        HttpEntity<WorkAllocationRequest> requestEntity = new HttpEntity<>(workAllocationRequest, headers);

        ArrayList<String> startedProcessIds = new ArrayList<>();

        try {
            ResponseEntity<StartResponse> exchange = restTemplate
                    .exchange(
                            CAMUNDA_URL + "/process-definition/key/workAllocation/start",
                            HttpMethod.POST,
                            requestEntity,
                            StartResponse.class
                    );
            taskLog.record(ccdId + event);

            LOGGER.info("Started instance id\n" + exchange.getBody());

            if (!exchange.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(
                        "Got [" + exchange.getStatusCode() + "] when calling " + CAMUNDA_URL + "/process-definition/workAllocation/start"
                );
            }

            startedProcessIds.add(exchange.getBody().getId());
        } catch (RestClientException e) {
            throw new AsylumCaseServiceResponseException(
                    "Couldn't delegate callback to API: " + CAMUNDA_URL + "/process-definition/workAllocation/start", e
            );
        }

        LOGGER.info("Created internal task for [" + ccdId + "] [" + event + "] assigned to [" + assignedTo + "]");

        try {
            ResponseEntity<StartResponse> exchange = restTemplate
                    .exchange(
                            CAMUNDA_URL + "/process-definition/key/workAllocationExternal/start",
                            HttpMethod.POST,
                            requestEntity,
                            StartResponse.class
                    );

            if (!exchange.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(
                        "Got [" + exchange.getStatusCode() + "] when calling " + CAMUNDA_URL + "/process-definition/workAllocationExternal/start"
                );
            }

            startedProcessIds.add(exchange.getBody().getId());
            LOGGER.info("Created external task for respomnse [" + exchange.getStatusCodeValue() + "]");
        } catch (RestClientException e) {
            throw new AsylumCaseServiceResponseException(
                    "Couldn't delegate callback to API: " + CAMUNDA_URL + "/process-definition/workAllocationExternal/start", e
            );
        }


//        checkTaskCreated(correlationId, startedProcessIds);
        completeInternalTasks(event, previousStateString, ccdId + "");


        LOGGER.info("Created external task for [" + ccdId + "] [" + event + "] assigned to [" + assignedTo + "]");
    }

    private void completeInternalTasks(String eventValue, String previousState, String ccdReference) {
        // create a default DMN engine
        DmnEngine dmnEngine = DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration()
                .buildEngine();

        File initialFile = new File("/Users/chris/hmcts/ia-case-api/work-allocation-poc/work_allocation_complete_task.dmn");
        try (InputStream inputStream = new FileInputStream(initialFile)) {
            DmnDecision decision = dmnEngine.parseDecision("completeTask", inputStream);

            VariableMap variables = new VariableMapImpl();
            variables.putValue("event", eventValue);
            variables.putValue("previousState", previousState);

            DmnDecisionTableResult dmnDecisionRuleResults = dmnEngine.evaluateDecisionTable(decision, variables);

            DmnDecisionRuleResult singleResult = dmnDecisionRuleResults.getSingleResult();
            LOGGER.info(singleResult.getEntryMap().toString());
            Boolean mapped = singleResult.getEntry("mapped");

            if (mapped) {
                String taskToComplete = singleResult.getEntry("taskToComplete");
                String operation = singleResult.getEntry("operation");
                LOGGER.info("Completing task [" + taskToComplete + "] for [" + ccdReference + "]");

                String processVariablesForTaskToComplete = "id_eq_" + ccdReference;
                if (!taskToComplete.equalsIgnoreCase("All")) {
                    processVariablesForTaskToComplete += ",nextTask_eq_" + taskToComplete;
                }
                ResponseEntity<List<Task>> exchange = restTemplate.exchange(
                        CAMUNDA_URL + "/task?processDefinitionKey=workAllocation&processVariables=" + processVariablesForTaskToComplete,
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<Task>>() {
                        }
                );
                List<Task> tasks = exchange.getBody();

                for (Task task : tasks) {
                    LOGGER.info("Completing task [" + task.getId() + "]");

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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkTaskCreated(String correlationId, ArrayList<String> startedProcessIds) {
//        List<String> processInstanceIds = getProcessInstances(correlationId);
        List<String> processInstanceIds = startedProcessIds;

        List<Boolean> processInstancesCompleted = new ArrayList<>();
        processInstanceIds.forEach(processInstance -> processInstancesCompleted.add(false));

        while (processInstancesCompleted.contains(false)) {
            for (int processInstanceIndex = 0; processInstanceIndex < processInstanceIds.size(); processInstanceIndex++) {
                String instanceId = processInstanceIds.get(processInstanceIndex);
                ResponseEntity<String> completedTasks = restTemplate.exchange(
                        CAMUNDA_URL + "/history/activity-instance?processInstanceId=" + instanceId + "&activityId=recordTaskAsComplete",
                        HttpMethod.GET, null, String.class
                );
                LOGGER.info(completedTasks.getBody());

                ResponseEntity<List<ActivityInstance>> possibleCompletedTasks = restTemplate.exchange(
                        CAMUNDA_URL + "/history/activity-instance?processInstanceId=" + instanceId + "&activityId=recordTaskAsComplete",
                        HttpMethod.GET, null, new ParameterizedTypeReference<List<ActivityInstance>>() {}
                );


                List<ActivityInstance> activeInstances = possibleCompletedTasks.getBody();

                boolean foundTask = activeInstances.size() > 0 && activeInstances.get(0).getDurationInMillis() != null;
                processInstancesCompleted.set(processInstanceIndex, foundTask);

                LOGGER.info(processInstancesCompleted.toString());
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException("Error waiting for task ", e);
            }
        }
    }

    private List<String> getProcessInstances(String correlationId) {
        String processVariables = "correlationId_eq_" + correlationId;
        ResponseEntity<List<ProcessInstance>> exchange = restTemplate.exchange(
                CAMUNDA_URL + "/history/process-instance?variables=" + processVariables,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<ProcessInstance>>() {}
        );

        List<ProcessInstance> processInstances = exchange.getBody();
        return processInstances.stream().map(processInstance -> processInstance.getId()).collect(toList());
    }
}
