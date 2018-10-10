package uk.gov.hmcts.reform.iacaseapi.events.domain.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.Documents;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.Document;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.IdValue;
import uk.gov.hmcts.reform.iacaseapi.events.infrastructure.api.DocumentDataFetcher;
import uk.gov.hmcts.reform.iacaseapi.events.infrastructure.api.DocumentToPdfConverter;

@Service
public class DocumentBundler {

    private static final String BUNDLE_FILENAME = "Hearing_Ready_Bundle.pdf";

    private final DocumentDataFetcher documentDataFetcher;
    private final DocumentToPdfConverter documentToPdfConverter;
    private final DocumentUploader documentUploader;
    private final DocumentAppender documentAppender;

    public DocumentBundler(
        @Autowired DocumentDataFetcher documentDataFetcher,
        @Autowired DocumentToPdfConverter documentToPdfConverter,
        @Autowired DocumentUploader documentUploader,
        @Autowired DocumentAppender documentAppender
    ) {
        this.documentDataFetcher = documentDataFetcher;
        this.documentToPdfConverter = documentToPdfConverter;
        this.documentUploader = documentUploader;
        this.documentAppender = documentAppender;
    }

    public synchronized void createAndUploadBundle(
        AsylumCase asylumCase
    ) {
        List<Document> allDocuments =
            asylumCase
                .getDocuments()
                .orElse(new Documents())
                .getDocuments()
                .get()
                .stream()
                .map(IdValue::getValue)
                .map(DocumentWithMetadata::getDocument)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (allDocuments.isEmpty()) {
            return;
        }

        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();

        ByteArrayOutputStream bundleDocumentDataOutputStream = new ByteArrayOutputStream();
        pdfMergerUtility.setDestinationStream(bundleDocumentDataOutputStream);

        allDocuments
            .stream()
            .filter(document -> !document.getDocumentFilename().equals(BUNDLE_FILENAME))
            .forEach(document -> {

                Resource documentData = documentDataFetcher.fetch(document);
                Resource documentDataAsPdf = documentToPdfConverter.convert(documentData);

                try {
                    pdfMergerUtility.addSource(documentDataAsPdf.getInputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        try {
            pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Resource bundleDocumentData = new ByteArrayResource(
            bundleDocumentDataOutputStream.toByteArray()
        ) {
            public String getFilename() {
                return BUNDLE_FILENAME;
            }
        };

        String sampleDocumentUrl =
            allDocuments
                .stream()
                .findFirst()
                .get()
                .getDocumentUrl();

        try {

            URI sampleDocumentUrlParts = new URI(sampleDocumentUrl);
            String documentPath = sampleDocumentUrlParts.getPath();
            String documentManagmentWebBaseUrl = sampleDocumentUrl.replaceFirst(documentPath, "");

            Document bundleDocument = documentUploader.upload(
                bundleDocumentData,
                "application/pdf",
                documentManagmentWebBaseUrl
            );

            documentAppender.append(asylumCase, bundleDocument);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
