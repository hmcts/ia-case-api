package uk.gov.hmcts.reform.bailcaseapi.util;

import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;

@Service
@RequiredArgsConstructor
public class SystemDocumentManagementUploader {

    private final UserDetailsProvider userDetailsProvider;

    private final LDClientInterface ldClient;

    private final CdamSystemDocumentManagementUploader cdamSystemDocumentManagementUploader;

    private final DMSystemDocumentManagementUploader dmSystemDocumentManagementUploader;


    public Document upload(Resource resource, String contentType) {
        if (getValue("use-ccd-document-am", false)) {
            return cdamSystemDocumentManagementUploader.upload(resource, contentType);
        } else {
            return dmSystemDocumentManagementUploader.upload(resource, contentType);
        }

    }

    public boolean getValue(String key, boolean defaultValue) {

        UserDetails userDetails = userDetailsProvider.getUserDetails();

        LDUser ldUser =  new LDUser.Builder(userDetails.getId())
            .firstName(userDetails.getForename())
            .lastName(userDetails.getSurname())
            .email(userDetails.getEmailAddress())
            .build();

        return ldClient.boolVariation(key, ldUser, defaultValue);
    }
}
