package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Documents;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class UploadDocumentUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.UPLOAD_DOCUMENT;
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

        DocumentWithType document =
            asylumCase
                .getDocument()
                .orElseThrow(() -> new IllegalStateException("document not present"));

        List<IdValue<DocumentWithType>> allDocuments = new ArrayList<>();

        Documents documents =
            asylumCase
                .getDocuments()
                .orElse(new Documents());

        if (documents.getDocuments().isPresent()) {
            allDocuments.addAll(
                documents.getDocuments().get()
            );
        }

        document.setDateUploaded(LocalDate.now().toString());

        allDocuments.add(
            new IdValue<>(
                document
                    .getDocument()
                    .get()
                    .getDocumentUrl(),
                document
            )
        );

        documents.setDocuments(allDocuments);

        asylumCase.setDocuments(documents);

        return preSubmitResponse;
    }
}
