package uk.gov.hmcts.reform.iacaseapi.forms.infrastructure.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.service.CaseSubmitter;

@RestController
@RequestMapping(
    path = "/IA/Asylum/{caseId}/submit-case",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class SubmitCaseController {

    private final CaseSubmitter caseSubmitter;

    public SubmitCaseController(
        @Autowired CaseSubmitter caseSubmitter
    ) {
        this.caseSubmitter = caseSubmitter;
    }

    @PostMapping
    public ResponseEntity<Void> submit(
        @PathVariable("caseId") final String caseId
    ) {
        caseSubmitter.submit(caseId);
        return ResponseEntity.noContent().build();
    }
}
