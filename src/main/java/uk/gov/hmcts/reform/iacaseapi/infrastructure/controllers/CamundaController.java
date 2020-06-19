package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static org.springframework.http.ResponseEntity.ok;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.workallocation.TaskLog;


@RequestMapping(
        path = "/camunda",
        produces = MediaType.TEXT_PLAIN_VALUE
)
@RestController
public class CamundaController {
    private final TaskLog taskLog;

    public CamundaController(TaskLog taskLog) {
        this.taskLog = taskLog;
    }

    @GetMapping(path = "/all")
    public ResponseEntity<String> all() {
        return ok(taskLog.getFullResults());
    }

    @GetMapping(path = "/average")
    public ResponseEntity<String> average() {
        return ok(taskLog.getAverageResults());
    }
}
