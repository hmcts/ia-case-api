package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Documents;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WrittenLegalArgument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.CcdEventPreSubmitHandler;

@Component
public class BuildCaseArgumentUpdater implements CcdEventPreSubmitHandler<AsylumCase> {

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.BUILD_CASE_ARGUMENT;
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

        Document writtenLegalArgumentDocument =
            asylumCase
                .getWrittenLegalArgumentDocument()
                .orElseThrow(() -> new IllegalStateException("writtenLegalArgumentDocument not present"));

        String writtenLegalArgumentDescription =
            asylumCase
                .getWrittenLegalArgumentDescription()
                .orElseThrow(() -> new IllegalStateException("writtenLegalArgumentDescription not present"));

        Documents writtenLegalArgumentEvidence =
            asylumCase
                .getWrittenLegalArgumentEvidence()
                .orElseThrow(() -> new IllegalStateException("writtenLegalArgumentEvidence not present"));

        asylumCase
            .getCaseArgument()
            .orElseThrow(() -> new IllegalStateException("caseArgument not present"))
            .setWrittenLegalArgument(
                new WrittenLegalArgument(
                    writtenLegalArgumentDocument,
                    writtenLegalArgumentDescription,
                    writtenLegalArgumentEvidence
                )
            );

        // add any new documents to the documents tab ...

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

        asylumCase
            .getWrittenLegalArgumentEvidence()
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
