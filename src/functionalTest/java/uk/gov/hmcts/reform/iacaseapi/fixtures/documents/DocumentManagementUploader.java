package uk.gov.hmcts.reform.iacaseapi.fixtures.documents;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;


@Service
@ComponentScan("uk.gov.hmcts.reform.ccd.document.am.feign")
public class DocumentManagementUploader implements DocumentUploader {

    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;

    private final CaseDocumentClientApi caseDocumentClientApi;

    public DocumentManagementUploader(
        AuthTokenGenerator serviceAuthorizationTokenGenerator,
        @Qualifier("requestUser") UserDetailsProvider userDetailsProvider,
        CaseDocumentClientApi caseDocumentClientApi) {
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
        this.caseDocumentClientApi = caseDocumentClientApi;
    }

    public Document upload(
        Resource resource,
        String contentType
    ) {
        final String serviceAuthorizationToken = serviceAuthorizationTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        try {

            final MultipartFile file = new MockMultipartFile(Objects.requireNonNull(resource.getFilename()), resource.getFilename(), contentType, resource.getInputStream());

            DocumentUploadRequest documentUploadRequest = new DocumentUploadRequest(
                    Classification.PUBLIC.toString(),
                    "Asylum",
                    "IA",
                    Collections.singletonList(file)
            );

            UploadResponse response = caseDocumentClientApi.uploadDocuments(
                    accessToken,
                    serviceAuthorizationToken,
                    documentUploadRequest
            );

            var document  = (uk.gov.hmcts.reform.ccd.document.am.model.Document) response.getDocuments().stream()
                    .findFirst()
                    .orElseThrow();

            return new Document(
                    document
                    .links
                    .self
                    .href,
                    document
                    .links
                    .binary
                    .href,
                    document
                    .originalDocumentName
            );

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
