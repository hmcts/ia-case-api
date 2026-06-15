package uk.gov.hmcts.reform.bailcaseapi.util;

import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;

@Service
@RequiredArgsConstructor
public class SystemDocumentManagementUploader {

    private final UserDetailsProvider userDetailsProvider;

    private final LDClientInterface ldClient;

    private final CdamSystemDocumentManagementUploader cdamSystemDocumentManagementUploader;

    public Document upload(Resource resource, String contentType) {
        return cdamSystemDocumentManagementUploader.upload(resource, contentType);

    }
}
