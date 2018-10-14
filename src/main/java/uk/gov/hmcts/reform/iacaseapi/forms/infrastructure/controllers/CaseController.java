package uk.gov.hmcts.reform.iacaseapi.forms.infrastructure.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.service.CaseCreator;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.service.CaseFetcher;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.CaseDetails;

@RestController
@RequestMapping(
    produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)
public class CaseController {

    private final CaseCreator caseCreator;
    private final CaseFetcher caseFetcher;

    public CaseController(
        @Autowired CaseCreator caseCreator,
        @Autowired CaseFetcher caseFetcher
    ) {
        this.caseCreator = caseCreator;
        this.caseFetcher = caseFetcher;
    }

    @GetMapping(
        path = "/IA/Asylum/{caseId}"
    )
    public ResponseEntity<AsylumCase> get(
        @PathVariable("caseId") final String caseId
    ) {
        AsylumCase asylumCase = caseFetcher.fetch(caseId);
        return ResponseEntity.ok(asylumCase);
    }

    @PostMapping(
        path = "/IA/Asylum",
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity<CaseDetails<AsylumCase>> post(
        @RequestBody AsylumCase asylumCase
    ) {
        CaseDetails<AsylumCase> asylumCaseCaseDetails =
            caseCreator.create(asylumCase);

        return ResponseEntity.ok(asylumCaseCaseDetails);
    }
}
