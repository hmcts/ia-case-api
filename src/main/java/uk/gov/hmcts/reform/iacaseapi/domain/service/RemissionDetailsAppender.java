package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
@Slf4j
public class RemissionDetailsAppender {

    private List<IdValue<RemissionDetails>> remissions;

    public List<IdValue<RemissionDetails>> appendAsylumSupportRemissionDetails(
        List<IdValue<RemissionDetails>> existingRemissionDetails,
        String feeRemissionType,
        String asylumSupportReference,
        Document asylumSupportDocument
    ) {
        final RemissionDetails newRemissionDetails =
                new RemissionDetails(feeRemissionType, asylumSupportReference, asylumSupportDocument);

        return append(existingRemissionDetails, newRemissionDetails);
    }

    public List<IdValue<RemissionDetails>> appendLegalAidRemissionDetails(
        List<IdValue<RemissionDetails>> existingRemissionDetails,
        String feeRemissionType,
        String legalAidAccountNumber
    ) {
        RemissionDetails newRemissionDetails = new RemissionDetails(
            feeRemissionType,
            legalAidAccountNumber,
            ""
        );

        return append(existingRemissionDetails, newRemissionDetails);
    }

    public List<IdValue<RemissionDetails>> appendHelpWithFeeReferenceRemissionDetails(
        List<IdValue<RemissionDetails>> existingRemissionDetails,
        String feeRemissionType,
        String helpWithFeesReferenceNumber
    ) {
        RemissionDetails newRemissionDetails = new RemissionDetails(
            feeRemissionType,
            null,
            helpWithFeesReferenceNumber
        );

        return append(existingRemissionDetails, newRemissionDetails);
    }

    public List<IdValue<RemissionDetails>> appendSection17RemissionDetails(
        List<IdValue<RemissionDetails>> existingRemissionDetails,
        String feeRemissionType,
        Document section17Document
    ) {
        RemissionDetails newRemissionDetails = new RemissionDetails(
            feeRemissionType,
            section17Document,
            null,
            null
        );

        return append(existingRemissionDetails, newRemissionDetails);
    }

    public List<IdValue<RemissionDetails>> appendSection20RemissionDetails(
        List<IdValue<RemissionDetails>> existingRemissionDetails,
        String feeRemissionType,
        Document section20Document
    ) {
        RemissionDetails newRemissionDetails = new RemissionDetails(
            feeRemissionType,
            null,
            section20Document,
            null
        );

        return append(existingRemissionDetails, newRemissionDetails);
    }

    public List<IdValue<RemissionDetails>> appendHomeOfficeWaiverRemissionDetails(
        List<IdValue<RemissionDetails>> existingRemissionDetails,
        String feeRemissionType,
        Document homeOfficeWaiverDocument
    ) {
        RemissionDetails newRemissionDetails = new RemissionDetails(
            feeRemissionType,
            null,
            null,
            homeOfficeWaiverDocument
        );

        return append(existingRemissionDetails, newRemissionDetails);
    }

    public List<IdValue<RemissionDetails>> appendExceptionalCircumstancesRemissionDetails(
        List<IdValue<RemissionDetails>> existingRemissionDetails,
        String feeRemissionType,
        String exceptionalCircumstances,
        List<IdValue<Document>> remissionEcEvidenceDocuments
    ) {
        RemissionDetails newRemissionDetails = new RemissionDetails(
            feeRemissionType,
            exceptionalCircumstances,
            remissionEcEvidenceDocuments
        );

        return append(existingRemissionDetails, newRemissionDetails);
    }

    public List<IdValue<RemissionDetails>> appendAsylumSupportRefNumberRemissionDetails(
        List<IdValue<RemissionDetails>> existingRemissionDetails,
        String feeRemissionType,
        String asylumSupportReference
    ) {
        final RemissionDetails newRemissionDetails = new RemissionDetails(feeRemissionType, asylumSupportReference);

        return append(existingRemissionDetails, newRemissionDetails);
    }

    public List<IdValue<RemissionDetails>> appendRemissionOptionDetails(
        List<IdValue<RemissionDetails>> existingRemissionDetails,
        String feeRemissionType,
        String helpWithFeesOption,
        String helpWithFeesRefNumber
    ) {
        RemissionDetails newRemissionDetails = new RemissionDetails(feeRemissionType);
        newRemissionDetails.setHelpWithFeesOption(helpWithFeesOption);
        newRemissionDetails.setHelpWithFeesReferenceNumber(helpWithFeesRefNumber);
        return append(existingRemissionDetails, newRemissionDetails);
    }

    public List<IdValue<RemissionDetails>> appendLocalAuthorityRemissionDetails(
        List<IdValue<RemissionDetails>> existingRemissionDetails,
        String feeRemissionType,
        List<IdValue<DocumentWithMetadata>> localAuthorityLetter
    ) {
        final RemissionDetails newRemissionDetails = new RemissionDetails(feeRemissionType, localAuthorityLetter);

        return append(existingRemissionDetails, newRemissionDetails);
    }

    private List<IdValue<RemissionDetails>> append(
        List<IdValue<RemissionDetails>> existingRemissionDetails,
        RemissionDetails newRemissionDetails) {

        final List<IdValue<RemissionDetails>> allRemissionDetails = new ArrayList<>();

        int index = existingRemissionDetails.size() + 1;

        allRemissionDetails.add(new IdValue<>(String.valueOf(index--), newRemissionDetails));

        for (IdValue<RemissionDetails> existingRemission : existingRemissionDetails) {
            allRemissionDetails.add(new IdValue<>(String.valueOf(index--), existingRemission.getValue()));
        }

        return allRemissionDetails;
    }

    public synchronized void setRemissions(List<IdValue<RemissionDetails>> remissions) {
        log.info("SETTING REMISSION: " + remissions);
        log.info("TO: " + this.remissions);
        this.remissions = remissions;
    }

    public synchronized List<IdValue<RemissionDetails>> getRemissions() {
        log.info("GETTING REMISSIONS: " + remissions);
        return remissions;
    }
}
