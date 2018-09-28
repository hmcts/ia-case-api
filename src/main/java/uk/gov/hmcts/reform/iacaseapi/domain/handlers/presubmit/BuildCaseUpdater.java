package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Documents;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.LegalArgument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class BuildCaseUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.BUILD_CASE;
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

        Document legalArgumentDocument =
            asylumCase
                .getLegalArgumentDocument()
                .orElseThrow(() -> new IllegalStateException("legalArgumentDocument not present"));

        String legalArgumentDescription =
            asylumCase
                .getLegalArgumentDescription()
                .orElseThrow(() -> new IllegalStateException("legalArgumentDescription not present"));

        Documents legalArgumentEvidence =
            asylumCase
                .getLegalArgumentEvidence()
                .orElseThrow(() -> new IllegalStateException("legalArgumentEvidence not present"));

        asylumCase
            .getCaseArgument()
            .orElseThrow(() -> new IllegalStateException("caseArgument not present"))
            .setLegalArgument(
                new LegalArgument(
                    legalArgumentDocument,
                    legalArgumentDescription,
                    legalArgumentEvidence
                )
            );

        // add any new documents to the documents tab ...

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

        asylumCase
            .getLegalArgumentEvidence()
            .get()
            .getDocuments()
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(document ->
                allDocuments
                    .stream()
                    .map(IdValue::getValue)
                    .noneMatch(existingDocument ->
                        existingDocument
                            .getDocument()
                            .get()
                            .getDocumentUrl()
                            .equals(
                                document
                                    .getDocument()
                                    .get()
                                    .getDocumentUrl()
                            )
                    )
            )
            .forEachOrdered(document -> {

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
            });

        documents.setDocuments(allDocuments);

        asylumCase.setDocuments(documents);

        return preSubmitResponse;
    }
}
