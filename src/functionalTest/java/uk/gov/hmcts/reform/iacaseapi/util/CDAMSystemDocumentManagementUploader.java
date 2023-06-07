package uk.gov.hmcts.reform.iacaseapi.util;

import java.io.IOException;
import java.util.Collections;

import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

/**
 * This class supersedes DMDocumentManagementUploader. Its usage is driven by a feature flag.
 */
@Component
@ComponentScan("uk.gov.hmcts.reform.ccd.document.am.feign")
public class CDAMSystemDocumentManagementUploader {

    private final CaseDocumentClient caseDocumentClient;
    private final AuthorizationHeadersProvider authorizationHeadersProvider;

    public CDAMSystemDocumentManagementUploader(
        CaseDocumentClient caseDocumentClient,
        AuthorizationHeadersProvider authorizationHeadersProvider
    ) {
        this.caseDocumentClient = caseDocumentClient;
        this.authorizationHeadersProvider = authorizationHeadersProvider;
    }

    @SneakyThrows
    public Document upload(Resource resource, String contentType) {
        final String serviceAuthorizationToken = authorizationHeadersProvider
            .getLegalRepresentativeAuthorization()
            .getValue("ServiceAuthorization");

        final String accessToken = authorizationHeadersProvider
            .getLegalRepresentativeAuthorization()
            .getValue("Authorization");

        MultipartFile file = new InMemoryMultipartFile(
            resource.getFilename(),
            resource.getFilename(),
            contentType,
            ByteStreams.toByteArray(resource.getInputStream())
        );

        UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                accessToken,
                serviceAuthorizationToken,
                "Asylum",
                "IA",
                Collections.singletonList(file)
            );

        uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument = uploadResponse.getDocuments().get(0);

        return new Document(
            uploadedDocument.links.self.href,
            uploadedDocument.links.binary.href,
            uploadedDocument.originalDocumentName
        );
    }
}
