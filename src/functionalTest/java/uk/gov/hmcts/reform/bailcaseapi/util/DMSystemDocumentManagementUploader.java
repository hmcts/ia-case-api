package uk.gov.hmcts.reform.bailcaseapi.util;

import java.io.IOException;
import java.util.Collections;

import com.google.common.io.ByteStreams;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

/**
 * Superseded. Will need to be removed as soon as the "use-ccd-document-am" feature flag is permanently on
 */
@Component
@Deprecated
public class DMSystemDocumentManagementUploader {

    private final DocumentUploadClientApi documentUploadClientApi;
    private final AuthorizationHeadersProvider authorizationHeadersProvider;

    public DMSystemDocumentManagementUploader(
        DocumentUploadClientApi documentUploadClientApi,
        AuthorizationHeadersProvider authorizationHeadersProvider
    ) {
        this.documentUploadClientApi = documentUploadClientApi;
        this.authorizationHeadersProvider = authorizationHeadersProvider;
    }

    public Document upload(
        Resource resource,
        String contentType
    ) {
        final String serviceAuthorizationToken =
            authorizationHeadersProvider
                .getAdminOfficerAuthorization()
                .getValue("ServiceAuthorization");

        final String accessToken =
            authorizationHeadersProvider
                .getLegalRepresentativeAuthorization()
                .getValue("Authorization");

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
                    .originalDocumentName,
                ""
            );

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
