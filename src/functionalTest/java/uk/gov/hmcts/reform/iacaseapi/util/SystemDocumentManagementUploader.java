package uk.gov.hmcts.reform.iacaseapi.util;


import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;

@Slf4j
@Service
@ComponentScan("uk.gov.hmcts.reform.ccd.document.am.feign")
public class SystemDocumentManagementUploader {

    private final CaseDocumentClient caseDocumentClient;

    private final AuthorizationHeadersProvider authorizationHeadersProvider;

    public SystemDocumentManagementUploader(
            CaseDocumentClient caseDocumentClient,
            AuthorizationHeadersProvider authorizationHeadersProvider
    ) {
        this.caseDocumentClient = caseDocumentClient;
        this.authorizationHeadersProvider = authorizationHeadersProvider;
    }

    public uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document upload(
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
                    uploadResponse
                            .getDocuments()
                            .get(0);

            return new uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document(
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
