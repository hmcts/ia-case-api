package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeignConfiguration;

@FeignClient(
        name = "ccd-case-document-am-client",
        url = "${idam.baseUrl}", // replace this with the
        configuration = FeignConfiguration.class
)
public interface CcdCaseDocumentAmClient {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    String DOCUMENT_ID = "documentId";


    @PostMapping(produces = APPLICATION_JSON_VALUE,  consumes = MULTIPART_FORM_DATA_VALUE)
    UploadResponse uploadDocuments(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                   @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
                                   @RequestBody DocumentUploadRequest uploadRequest);

    @GetMapping(value = "/{documentId}/binary")
    ResponseEntity<Resource> getDocumentBinary(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                               @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
                                               @PathVariable(DOCUMENT_ID) UUID documentId);
}
