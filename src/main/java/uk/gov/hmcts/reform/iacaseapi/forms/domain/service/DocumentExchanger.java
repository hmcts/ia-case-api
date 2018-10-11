package uk.gov.hmcts.reform.iacaseapi.forms.domain.service;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.iacaseapi.events.infrastructure.api.DocumentManagementUploadApi;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.Document;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.security.AccessTokenProvider;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.security.UserAndRolesProvider;

@Service
public class DocumentExchanger {

    private final String documentManagementApiBaseUrl;
    private final String documentManagementWebBaseUrl;
    private final DocumentManagementUploadApi documentManagementUploadApi;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final UserAndRolesProvider userAndRolesProvider;

    public DocumentExchanger(
        @Value("${documentManagementApi.baseUrl}") String documentManagementApiBaseUrl,
        @Value("${documentManagementWeb.baseUrl}") String documentManagementWebBaseUrl,
        @Autowired DocumentManagementUploadApi documentManagementUploadApi,
        @Autowired AuthTokenGenerator serviceAuthorizationTokenGenerator,
        @Autowired AccessTokenProvider accessTokenProvider,
        @Autowired UserAndRolesProvider userAndRolesProvider
    ) {
        this.documentManagementApiBaseUrl = documentManagementApiBaseUrl;
        this.documentManagementWebBaseUrl = documentManagementWebBaseUrl;
        this.documentManagementUploadApi = documentManagementUploadApi;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.accessTokenProvider = accessTokenProvider;
        this.userAndRolesProvider = userAndRolesProvider;
    }

    public Document exchange(
        MultipartFile file
    ) {
        final String accessToken = accessTokenProvider.getAccessToken();
        final String serviceAuthorizationToken = serviceAuthorizationTokenGenerator.generate();
        final String userId = userAndRolesProvider.getUserId();

        UploadResponse uploadResponse =
            documentManagementUploadApi
                .uploadFiles(
                    accessToken,
                    serviceAuthorizationToken,
                    userId,
                    Collections.singletonList(file)
                );

        if (uploadResponse == null) {
            return new Document(
                "a", "b", "c"
            );
        }

        return new Document(
            uploadResponse
                .getEmbedded()
                .getDocuments()
                .get(0)
                .links
                .self
                .href
                .replaceFirst(documentManagementApiBaseUrl, documentManagementWebBaseUrl),
            uploadResponse
                .getEmbedded()
                .getDocuments()
                .get(0)
                .links
                .binary
                .href
                .replaceFirst(documentManagementApiBaseUrl, documentManagementWebBaseUrl),
            uploadResponse
                .getEmbedded()
                .getDocuments()
                .get(0)
                .originalDocumentName
        );
    }
}
