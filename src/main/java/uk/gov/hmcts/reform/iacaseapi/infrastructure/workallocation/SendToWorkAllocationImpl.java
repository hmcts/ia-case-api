package uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation;

import java.util.HashMap;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;

@Component
public class SendToWorkAllocationImpl implements SendToWorkAllocation<AsylumCase> {
    private final static Logger LOGGER = Logger.getLogger(SendToWorkAllocationImpl.class.getName());

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

    private void createTask(long ccdId, String event, String currentState, String previousStateString, String hearingCentreString, String appellantName, String assignedTo) {
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

        WorkAllocationRequest workAllocationRequest = new WorkAllocationRequest(variables);

        HttpEntity<WorkAllocationRequest> requestEntity = new HttpEntity<>(workAllocationRequest, headers);

        try {
            ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> exchange = restTemplate
                    .exchange(
                            "http://localhost:8080/engine-rest/process-definition/key/workAllocation/start",
                            HttpMethod.POST,
                            requestEntity,
                            new ParameterizedTypeReference<PreSubmitCallbackResponse<AsylumCase>>() {
                            }
                    );
            taskLog.record(ccdId + event);

            if (!exchange.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException(
                        "Got [" + exchange.getStatusCode() + "] when calling http://localhost:8080/engine-rest/process-definition/workAllocation/start"
                );
            }
        } catch (RestClientException e) {
            throw new AsylumCaseServiceResponseException(
                    "Couldn't delegate callback to API: http://localhost:8080/engine-rest/process-definition/workAllocation/start", e
            );
        }
    }
}
