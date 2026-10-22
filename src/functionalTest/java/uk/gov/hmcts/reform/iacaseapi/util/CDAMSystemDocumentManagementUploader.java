package uk.gov.hmcts.reform.iacaseapi.util;

import java.util.Collections;

import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

/**
 * This class supersedes DMDocumentManagementUploader. Its usage is driven by a feature flag.
 */
@Slf4j
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

        log.info("sb4 response: " + uploadResponse);
        log.info("sb4 string: " + uploadResponse.toString());
        log.info("sb4 size: " + uploadResponse.getDocuments().size());
        log.info("sb4 first size: " + uploadResponse.getDocuments().getFirst().size);
        log.info("sb4 links: " + uploadResponse.getDocuments().getFirst().links);
        log.info("sb4 created: " + uploadResponse.getDocuments().getFirst().createdOn);
        log.info("sb4 doc name: " + uploadResponse.getDocuments().getFirst().originalDocumentName);

        uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument = uploadResponse.getDocuments().getFirst();

        return new Document(
            uploadedDocument.links.self.href,
            uploadedDocument.links.binary.href,
            uploadedDocument.originalDocumentName
        );
    }
}
