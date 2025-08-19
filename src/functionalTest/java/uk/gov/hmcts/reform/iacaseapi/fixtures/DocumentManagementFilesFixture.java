package uk.gov.hmcts.reform.iacaseapi.fixtures;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.util.BinaryResourceLoader;
import uk.gov.hmcts.reform.iacaseapi.util.SystemDocumentManagementUploader;

@Component
public class DocumentManagementFilesFixture implements Fixture {


    private static Map<String, String> metadata = new HashMap<>();

    @Autowired private SystemDocumentManagementUploader documentManagementUploader;

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
