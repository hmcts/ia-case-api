package uk.gov.hmcts.reform.iacaseapi.util;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Collections;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
//import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Document

@Service
public class SystemDocumentManagementUploader {

    private final DocumentUploadClientApi documentUploadClientApi;
    private final AuthorizationHeadersProvider authorizationHeadersProvider;

    CaseDocumentClient caseDocumentClient;

    public SystemDocumentManagementUploader(
        DocumentUploadClientApi documentUploadClientApi,
        AuthorizationHeadersProvider authorizationHeadersProvider
    ) {
        this.documentUploadClientApi = documentUploadClientApi;
        this.authorizationHeadersProvider = authorizationHeadersProvider;
    }

    public UploadResponse upload(
        Resource resource,
        String contentType
    ) {
        final String serviceAuthorizationToken =
            authorizationHeadersProvider
                .getLegalRepresentativeAuthorization()
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

            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments( accessToken,
                    serviceAuthorizationToken,
                    "Asylum",
                    "IA",
                    Collections.singletonList(file)
                    );

              return uploadResponse;
               /* documentUploadClientApi
                    .upload(
                        accessToken,
                        serviceAuthorizationToken,
                        userId,
                        Collections.singletonList(file)
                    ); */


             /*uk.gov.hmcts.reform.document.domain.Document uploadedDoc =
                uploadResponse1
                    .getEmbedded()
                    .getDocuments()
                    .get(0);

            return new Document(
                    uploadedDoc
                    .links
                    .self
                    .href,
                    uploadedDoc
                    .links
                    .binary
                    .href,
                    uploadedDoc
                    .originalDocumentName
            );*/

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
