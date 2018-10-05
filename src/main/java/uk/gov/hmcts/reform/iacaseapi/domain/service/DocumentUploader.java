package uk.gov.hmcts.reform.iacaseapi.domain.service;

import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.security.AccessTokenProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.security.UserAndRolesProvider;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.api.DocumentManagementUploadApi;

@Service
public class DocumentUploader {

    private final String documentManagementApiBaseUrl;
    private final DocumentManagementUploadApi documentManagementUploadApi;
    private final AuthTokenGenerator serviceAuthorizationTokenGenerator;
    private final AccessTokenProvider accessTokenProvider;
    private final UserAndRolesProvider userAndRolesProvider;

    public DocumentUploader(
        @Value("${documentManagementApi.baseUrl}") String documentManagementApiBaseUrl,
        @Autowired DocumentManagementUploadApi documentManagementUploadApi,
        @Autowired AuthTokenGenerator serviceAuthorizationTokenGenerator,
        @Autowired AccessTokenProvider accessTokenProvider,
        @Autowired UserAndRolesProvider userAndRolesProvider
    ) {
        this.documentManagementApiBaseUrl = documentManagementApiBaseUrl;
        this.documentManagementUploadApi = documentManagementUploadApi;
        this.serviceAuthorizationTokenGenerator = serviceAuthorizationTokenGenerator;
        this.accessTokenProvider = accessTokenProvider;
        this.userAndRolesProvider = userAndRolesProvider;
    }

    public Document upload(
        Resource documentData,
        String documentContentType,
        String documentManagmentWebBaseUrl
    ) {
        final String accessToken = accessTokenProvider.getAccessToken();
        final String serviceAuthorizationToken = serviceAuthorizationTokenGenerator.generate();
        final String userId = userAndRolesProvider.getUserId();

        try {

            MultipartFile file = new ByteMultipartFile(
                documentData.getFilename(),
                documentData.getFilename(),
                documentContentType,
                ByteStreams.toByteArray(documentData.getInputStream())
            );

            UploadResponse uploadResponse =
                documentManagementUploadApi
                    .uploadFiles(
                        accessToken,
                        serviceAuthorizationToken,
                        userId,
                        Collections.singletonList(file)
                    );

            return new Document(
                uploadResponse
                    .getEmbedded()
                    .getDocuments()
                    .get(0)
                    .links
                    .self
                    .href
                    .replaceFirst(documentManagementApiBaseUrl, documentManagmentWebBaseUrl),
                uploadResponse
                    .getEmbedded()
                    .getDocuments()
                    .get(0)
                    .links
                    .binary
                    .href
                    .replaceFirst(documentManagementApiBaseUrl, documentManagmentWebBaseUrl),
                uploadResponse
                    .getEmbedded()
                    .getDocuments()
                    .get(0)
                    .originalDocumentName
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class ByteMultipartFile implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] bytes;

        public ByteMultipartFile(
            String name,
            String originalFilename,
            String contentType,
            byte[] bytes
        ) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.bytes = bytes;
        }

        public String getName() {
            return name;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public String getContentType() {
            return contentType;
        }

        public boolean isEmpty() {
            return bytes.length == 0;
        }

        public long getSize() {
            return bytes.length;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        public void transferTo(File dest) throws IllegalStateException {
            throw new IllegalStateException("Method not implemented");
        }
    }
}
