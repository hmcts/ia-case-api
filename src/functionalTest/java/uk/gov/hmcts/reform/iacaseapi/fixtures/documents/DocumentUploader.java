package uk.gov.hmcts.reform.iacaseapi.fixtures.documents;

import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;

public interface DocumentUploader {

    Document upload(
        Resource resource,
        String contentType
    );
}
