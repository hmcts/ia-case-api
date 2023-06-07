package uk.gov.hmcts.reform.iacaseapi.util;

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Collections;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Service
@RequiredArgsConstructor
public class SystemDocumentManagementUploader {

    private final FeatureToggler featureToggler;

    private final CDAMSystemDocumentManagementUploader cdamSystemDocumentManagementUploader;

    private final DMSystemDocumentManagementUploader dmSystemDocumentManagementUploader;


    public Document upload(Resource resource, String contentType) {
        if (featureToggler.getValue("use-ccd-document-am", false)) {
            return cdamSystemDocumentManagementUploader.upload(resource, contentType);
        }
        else {
            return dmSystemDocumentManagementUploader.upload(resource, contentType);
        }

    }
}
