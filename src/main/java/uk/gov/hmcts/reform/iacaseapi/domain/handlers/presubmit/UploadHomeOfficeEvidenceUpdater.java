package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Documents;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class UploadHomeOfficeEvidenceUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.UPLOAD_HOME_OFFICE_EVIDENCE;
    }

    public CcdEventPreSubmitResponse<AsylumCase> handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventPreSubmitResponse<AsylumCase> preSubmitResponse =
            new CcdEventPreSubmitResponse<>(asylumCase);

        DocumentWithMetadata homeOfficeEvidence =
            asylumCase
                .getHomeOfficeEvidence()
                .orElseThrow(() -> new IllegalStateException("homeOfficeEvidence not present"));

        List<IdValue<DocumentWithMetadata>> allDocuments = new ArrayList<>();

        Documents documents =
            asylumCase
                .getDocuments()
                .orElse(new Documents());

        if (documents.getDocuments().isPresent()) {
            allDocuments.addAll(
                documents.getDocuments().get()
            );
        }

        homeOfficeEvidence.setStored(Optional.of("Yes"));
        homeOfficeEvidence.setDateUploaded(LocalDate.now().toString());

        allDocuments.add(
            new IdValue<>(
                homeOfficeEvidence
                    .getDocument()
                    .get()
                    .getDocumentUrl(),
                homeOfficeEvidence
            )
        );

        documents.setDocuments(allDocuments);

        asylumCase.setDocuments(documents);

        asylumCase.clearDocument();

        return preSubmitResponse;
    }
}
