package uk.gov.hmcts.reform.iacaseapi.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Service
@RequiredArgsConstructor
public class SystemDocumentManagementUploader {
    @Autowired
    private final FeatureToggler featureToggler;

    private final CDAMSystemDocumentManagementUploader cdamSystemDocumentManagementUploader;

    private final DMSystemDocumentManagementUploader dmSystemDocumentManagementUploader;


    public Document upload(Resource resource, String contentType) {
        if (featureToggler.getValue("use-ccd-document-am", false)) {
            return cdamSystemDocumentManagementUploader.upload(resource, contentType);
        } else {
            return dmSystemDocumentManagementUploader.upload(resource, contentType);
        }

    }
}
