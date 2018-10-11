package uk.gov.hmcts.reform.iacaseapi.forms.infrastructure.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.service.GroundsOfAppealFetcher;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.service.GroundsOfAppealSubmitter;

@RestController
@RequestMapping(
    path = "/IA/Asylum/{caseId}/grounds-of-appeal",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class GroundsOfAppealController {

    private final GroundsOfAppealFetcher groundsOfAppealFetcher;
    private final GroundsOfAppealSubmitter groundsOfAppealSubmitter;

    public GroundsOfAppealController(
        @Autowired GroundsOfAppealFetcher groundsOfAppealFetcher,
        @Autowired GroundsOfAppealSubmitter groundsOfAppealSubmitter
    ) {
        this.groundsOfAppealFetcher = groundsOfAppealFetcher;
        this.groundsOfAppealSubmitter = groundsOfAppealSubmitter;
    }

    @GetMapping
    public ResponseEntity<List<String>> get(
        @PathVariable("caseId") final String caseId
    ) {
        List<String> groundsOfAppeal = groundsOfAppealFetcher.fetch(caseId);
        return ResponseEntity.ok(groundsOfAppeal);
    }

    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity<Void> post(
        @PathVariable("caseId") final String caseId,
        @RequestBody List<String> groundsOfAppeal
    ) {
        groundsOfAppealSubmitter.submit(caseId, groundsOfAppeal);
        return ResponseEntity.noContent().build();
    }
}
