package uk.gov.hmcts.reform.iacaseapi.forms.infrastructure.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.service.CaseFetcher;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;

@RestController
@RequestMapping(
    path = "/IA/Asylum/{caseId}",
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class CaseController {

    private final CaseFetcher caseFetcher;

    public CaseController(
        @Autowired CaseFetcher caseFetcher
    ) {
        this.caseFetcher = caseFetcher;
    }

    @GetMapping
    public ResponseEntity<AsylumCase> get(
        @PathVariable("caseId") final String caseId
    ) {
        AsylumCase asylumCase = caseFetcher.fetch(caseId);
        return ResponseEntity.ok(asylumCase);
    }
}
