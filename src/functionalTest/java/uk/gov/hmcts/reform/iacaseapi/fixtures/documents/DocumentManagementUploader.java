package uk.gov.hmcts.reform.iacaseapi.fixtures.documents;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Service
@RequiredArgsConstructor
public class DocumentManagementUploader implements DocumentUploader {

    private final FeatureToggler featureToggler;
    private final CDAMDocumentManagementUploader cdamDocumentManagementUploader;

    @Override
    public Document upload(Resource resource, String contentType) {
        return cdamDocumentManagementUploader.upload(resource, contentType);
    }
}
