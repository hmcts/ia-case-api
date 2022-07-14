package uk.gov.hmcts.reform.bailcaseapi.fixtures.documents;

import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;

public interface DocumentUploader {

    Document upload(
        Resource resource,
        String contentType
    );
}
