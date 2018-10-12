package uk.gov.hmcts.reform.iacaseapi.forms.infrastructure.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.iacaseapi.forms.domain.service.DocumentExchanger;
import uk.gov.hmcts.reform.iacaseapi.forms.infrastructure.BadRequestException;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.Document;

@RestController
public class UploadDocumentController {

    private final DocumentExchanger documentExchanger;

    public UploadDocumentController(
        @Autowired DocumentExchanger documentExchanger
    ) {
        this.documentExchanger = documentExchanger;
    }

    @PostMapping(
        path = "/IA/Asylum/{caseId}/upload-document",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public ResponseEntity<Document> upload(
        @PathVariable("caseId") final String caseId,
        @RequestParam("file") MultipartFile file
    ) {
        if (null == file || file.isEmpty()) {
            throw new BadRequestException("file cannot be empty");
        }

        Document document = documentExchanger.exchange(file);
        return ResponseEntity.ok(document);
    }
}
