package uk.gov.hmcts.reform.iacaseapi.infrastructure.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.security.AccessTokenProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.security.UserAndRolesProvider;

@Service
public class DocumentDataFetcher {

    private final DocumentManagementDownloadApi documentManagementDownloadApi;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final UserAndRolesProvider userAndRolesProvider;

    public DocumentDataFetcher(
        @Autowired DocumentManagementDownloadApi documentManagementDownloadApi,
        @Autowired AuthTokenGenerator serviceAuthorizationTokenGenerator,
        @Autowired AccessTokenProvider accessTokenProvider,
        @Autowired UserAndRolesProvider userAndRolesProvider
    ) {
        this.documentManagementDownloadApi = documentManagementDownloadApi;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.accessTokenProvider = accessTokenProvider;
        this.userAndRolesProvider = userAndRolesProvider;
    }

    public Resource fetch(
        Document document
    ) {
        final String accessToken = accessTokenProvider.getAccessToken();
        final String serviceAuthorizationToken = serviceAuthorizationTokenGenerator.generate();
        final String userId = userAndRolesProvider.getUserId();
        final List<String> userRoles = userAndRolesProvider.getUserRoles();

        final String documentFilename = document.getDocumentFilename();

        try {

            URI documentBinaryUrl = new URI(document.getDocumentBinaryUrl());
            String documentBinaryPath = documentBinaryUrl.getPath().replaceFirst("/", "");

            ResponseEntity<Resource> response =
                documentManagementDownloadApi.downloadResource(
                    accessToken,
                    serviceAuthorizationToken,
                    userId,
                    String.join(",", userRoles),
                    documentBinaryPath
                );

            if (response.getBody() == null) {
                throw new IllegalStateException("No data returned from document management");
            }

            try {

                return new ByteArrayResource(
                    StreamUtils.copyToByteArray(response.getBody().getInputStream())
                ) {
                    public String getFilename() {
                        return documentFilename;
                    }
                };

            } catch (IOException e) {
                throw new IllegalArgumentException("Document binary URL could not be read", e);
            }

        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Document binary URL could not be parsed", e);
        }
    }
}
