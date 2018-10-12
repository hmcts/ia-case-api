package uk.gov.hmcts.reform.iacaseapi.forms.infrastructure.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.service.AppealSubmitter;

@RestController
@RequestMapping(
    path = "/IA/Asylum/{caseId}/submit-appeal",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class SubmitAppealController {

    private final AppealSubmitter appealSubmitter;

    public SubmitAppealController(
        @Autowired AppealSubmitter appealSubmitter
    ) {
        this.appealSubmitter = appealSubmitter;
    }

    @PostMapping
    public ResponseEntity<Void> submit(
        @PathVariable("caseId") final String caseId
    ) {
        appealSubmitter.submit(caseId);
        return ResponseEntity.noContent().build();
    }
}
