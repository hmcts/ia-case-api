package uk.gov.hmcts.reform.iacaseapi.util;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Collections;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;


@Service
public class SystemDocumentManagementUploader {

    private final CaseDocumentClient documentUploadClient;
    private final AuthorizationHeadersProvider authorizationHeadersProvider;

    public SystemDocumentManagementUploader(
            CaseDocumentClient documentUploadClient,
        AuthorizationHeadersProvider authorizationHeadersProvider
    ) {
        this.documentUploadClient = documentUploadClient;
        this.authorizationHeadersProvider = authorizationHeadersProvider;
    }

    public Document upload(
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

            UploadResponse uploadResponse =
                documentUploadClient
                        .uploadDocuments(
                        accessToken,
                        serviceAuthorizationToken,
                        "Asylum",
                        "IA",
                        Collections.singletonList(file)
                    );

            uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument =
                uploadResponse
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
