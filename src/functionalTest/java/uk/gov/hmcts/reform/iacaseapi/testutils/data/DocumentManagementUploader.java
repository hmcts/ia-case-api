package uk.gov.hmcts.reform.iacaseapi.testutils.data;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Collections;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.util.IdamAuthProvider;

public class DocumentManagementUploader {

    private final CaseDocumentClient caseDocumentClient;
    private final IdamAuthProvider idamAuthProvider;
    private final AuthTokenGenerator s2sAuthTokenGenerator;

    public DocumentManagementUploader(
        CaseDocumentClient caseDocumentClient,
        IdamAuthProvider idamAuthProvider,
        AuthTokenGenerator s2sAuthTokenGenerator
    ) {
        this.caseDocumentClient = caseDocumentClient;
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
                caseDocumentClient
                    .uploadDocuments(
                        accessToken,
                        serviceAuthorizationToken,
                        "Asylum",
                        "IA",
                        Collections.singletonList(file)
                    );

            uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument =
                uploadResponse.getDocuments().get(0);

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
