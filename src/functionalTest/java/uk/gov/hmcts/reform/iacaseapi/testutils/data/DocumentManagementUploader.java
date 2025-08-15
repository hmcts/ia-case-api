package uk.gov.hmcts.reform.iacaseapi.testutils.data;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Collections;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

public class DocumentManagementUploader {

    private final DocumentUploadClientApi documentUploadClientApi;
    private final IdamAuthProvider idamAuthProvider;
    private final AuthTokenGenerator s2sAuthTokenGenerator;

    public DocumentManagementUploader(
        DocumentUploadClientApi documentUploadClientApi,
        IdamAuthProvider idamAuthProvider,
        AuthTokenGenerator s2sAuthTokenGenerator
    ) {
        this.documentUploadClientApi = documentUploadClientApi;
        this.idamAuthProvider = idamAuthProvider;
        this.s2sAuthTokenGenerator = s2sAuthTokenGenerator;
    }

    public Document upload(
        Resource resource,
        String contentType
    ) {
        final String serviceAuthorizationToken = s2sAuthTokenGenerator.generate();

        final String accessToken = idamAuthProvider.getLegalRepToken();

        final String userId = "1";

        try {

            MultipartFile file = new InMemoryMultipartFile(
                resource.getFilename(),
                resource.getFilename(),
                contentType,
                ByteStreams.toByteArray(resource.getInputStream())
            );

            UploadResponse uploadResponse =
                documentUploadClientApi
                    .upload(
                        accessToken,
                        serviceAuthorizationToken,
                        userId,
                        Collections.singletonList(file)
                    );

            uk.gov.hmcts.reform.document.domain.Document uploadedDocument =
                uploadResponse
                    .getEmbedded()
                    .getDocuments()
                    .get(0);

            return new Document(
                uploadedDocument
                    .links
                    .self
                    .href,
                uploadedDocument
                    .links
                    .binary
                    .href,
                uploadedDocument
                    .originalDocumentName
            );

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
