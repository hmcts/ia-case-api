package uk.gov.hmcts.reform.iacaseapi.forms.infrastructure.controllers;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.datasource.GroundsOfAppealFetcher;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.datasource.GroundsOfAppealSubmitter;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.GroundOfAppeal;

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
    public ResponseEntity<List<GroundOfAppeal>> get(
        @PathVariable("caseId") final String caseId
    ) {
        List<GroundOfAppeal> groundsOfAppeal = groundsOfAppealFetcher.fetch(caseId);
        return ResponseEntity.ok(groundsOfAppeal);
    }

    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity<Void> put(
        @PathVariable("caseId") final String caseId,
        @RequestBody List<GroundOfAppeal> groundsOfAppeal
    ) {
        groundsOfAppealSubmitter.subhmit(caseId, groundsOfAppeal);
        return ResponseEntity.noContent().build();
    }
}
