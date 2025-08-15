package uk.gov.hmcts.reform.iacaseapi.testutils.data;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

public class DocumentManagementFilesFixture {

    private static Map<String, String> metadata = new HashMap<>();

    private final DocumentManagementUploader documentManagementUploader;

    public DocumentManagementFilesFixture(DocumentManagementUploader documentManagementUploader) {
        this.documentManagementUploader = documentManagementUploader;
    }

    public void prepare() throws IOException {

        Collection<Resource> documentResources =
            BinaryResourceLoader
                .load("/documents/*")
                .values();

        documentResources
            .forEach(documentResource -> {

                String filename =
                    documentResource
                        .getFilename()
                        .toUpperCase();

                String contentType;

                if (filename.endsWith(".PDF")) {
                    contentType = "application/pdf";

                } else if (filename.endsWith(".DOC")) {
                    contentType = "application/msword";

                } else if (filename.endsWith(".DOCX")) {
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

                } else {
                    throw new RuntimeException("Missing content type mapping for document: " + filename);
                }

                Document docStoreDocumentMetadata =
                    documentManagementUploader.upload(
                        documentResource,
                        contentType
                    );

                String placeholder = filename.replace(".", "_");

                metadata.put("FIXTURE_" + placeholder + "_URL", docStoreDocumentMetadata.getDocumentUrl());
                metadata.put("FIXTURE_" + placeholder + "_URL_BINARY", docStoreDocumentMetadata.getDocumentBinaryUrl());
                metadata.put("FIXTURE_" + placeholder + "_FILENAME", docStoreDocumentMetadata.getDocumentFilename());
            });
    }

    public Map<String, String> getProperties() {
        return metadata;
    }
}
