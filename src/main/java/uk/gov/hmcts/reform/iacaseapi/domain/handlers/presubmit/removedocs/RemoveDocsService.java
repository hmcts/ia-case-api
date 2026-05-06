package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.removedocs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.editdocs.EditDocsService;

@Service
public class RemoveDocsService {

    private final EditDocsService editDocsService;

    @Autowired
    public RemoveDocsService(EditDocsService editDocsService) {
        this.editDocsService = editDocsService;
    }

    public void handleRemoval(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {

        if (asylumCaseBefore == null) {
            return;
        }

        editDocsService.cleanUpOverviewTabDocs(asylumCase, asylumCaseBefore);
    }
}