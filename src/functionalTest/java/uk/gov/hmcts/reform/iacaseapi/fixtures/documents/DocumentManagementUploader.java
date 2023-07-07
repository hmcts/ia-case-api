package uk.gov.hmcts.reform.iacaseapi.fixtures.documents;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

@Service
public class DocumentManagementUploader implements DocumentUploader {

    private final DocumentUploadClientApi documentUploadClientApi;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;

    public DocumentManagementUploader(
        DocumentUploadClientApi documentUploadClientApi,
        AuthTokenGenerator serviceAuthorizationTokenGenerator,
        @Qualifier("requestUser") UserDetailsProvider userDetailsProvider
    ) {
        this.documentUploadClientApi = documentUploadClientApi;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
    }

    public Document upload(
        Resource resource,
        String contentType
    ) {
        final String serviceAuthorizationToken = serviceAuthorizationTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();
        final String userId = userDetails.getId();

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
                    .originalDocumentName,
                    null
            );

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
