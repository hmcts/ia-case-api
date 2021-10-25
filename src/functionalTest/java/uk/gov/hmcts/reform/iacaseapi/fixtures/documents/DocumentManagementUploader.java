package uk.gov.hmcts.reform.iacaseapi.fixtures.documents;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification;

@Service
public class DocumentManagementUploader implements DocumentUploader {

    private final CaseDocumentClientApi caseDocumentClientApi;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;

    public DocumentManagementUploader(
        CaseDocumentClientApi caseDocumentClientApi,
        AuthTokenGenerator serviceAuthorizationTokenGenerator,
        @Qualifier("requestUser") UserDetailsProvider userDetailsProvider
    ) {
        this.caseDocumentClientApi = caseDocumentClientApi;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
    }

    public Document upload(
        Resource resource,
        String jurisdictionId
    ) {
        final String serviceAuthorizationToken = serviceAuthorizationTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        try {
            MultipartFile file = new InMemoryMultipartFile(
                resource.getFilename(),
                ByteStreams.toByteArray(resource.getInputStream())
            );

            UploadResponse uploadResponse = caseDocumentClientApi.uploadDocuments(accessToken,
                serviceAuthorizationToken,
                new DocumentUploadRequest(Classification.PUBLIC.name(), "Asylum", "IA", Collections.singletonList(file)));


            uk.gov.hmcts.reform.ccd.document.am.model.Document uploadedDocument =
                uploadResponse
                    .getDocuments().get(0);

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
                uploadedDocument
                    .hashToken
            );

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
