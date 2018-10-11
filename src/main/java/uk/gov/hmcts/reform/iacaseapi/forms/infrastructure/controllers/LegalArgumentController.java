package uk.gov.hmcts.reform.iacaseapi.forms.infrastructure.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.service.LegalArgumentFetcher;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.service.LegalArgumentSubmitter;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.LegalArgument;

@RestController
@RequestMapping(
    path = "/IA/Asylum/{caseId}/legal-argument",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class LegalArgumentController {

    private final LegalArgumentFetcher legalArgumentFetcher;
    private final LegalArgumentSubmitter legalArgumentSubmitter;

    public LegalArgumentController(
        @Autowired LegalArgumentFetcher legalArgumentFetcher,
        @Autowired LegalArgumentSubmitter legalArgumentSubmitter
    ) {
        this.legalArgumentFetcher = legalArgumentFetcher;
        this.legalArgumentSubmitter = legalArgumentSubmitter;
    }

    @GetMapping
    public ResponseEntity<LegalArgument> get(
        @PathVariable("caseId") final String caseId
    ) {
        LegalArgument legalArgument = legalArgumentFetcher.fetch(caseId);
        return ResponseEntity.ok(legalArgument);
    }

    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity<Void> post(
        @PathVariable("caseId") final String caseId,
        @RequestBody LegalArgument legalArgument
    ) {
        legalArgumentSubmitter.submit(caseId, legalArgument);
        return ResponseEntity.noContent().build();
    }
}
