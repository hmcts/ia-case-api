package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Getter
@EqualsAndHashCode
@ToString
public class RemissionDetails {

    private String feeRemissionType;
    private String asylumSupportReference;
    private Document asylumSupportDocument;
    private String legalAidAccountNumber;
    private Document section17Document;
    private Document section20Document;
    private Document homeOfficeWaiverDocument;
    private String helpWithFeesReferenceNumber;
    private String exceptionalCircumstances;
    private List<IdValue<Document>> remissionEcEvidenceDocuments;
    private String remissionDecision;
    private String feeAmount;
    private String amountRemitted;
    private String amountLeftToPay;
    private String remissionDecisionReason;

    private RemissionDetails() {

    }

    public RemissionDetails(
            String feeRemissionType,
            String asylumSupportReference,
            Document asylumSupportDocument
    ) {
        this.feeRemissionType = feeRemissionType;
        this.asylumSupportReference = asylumSupportReference;
        this.asylumSupportDocument = asylumSupportDocument;
    }

    public RemissionDetails(
            String feeRemissionType,
            String legalAidAccountNumber,
            String helpWithFeesReferenceNumber
    ) {
        this.feeRemissionType = feeRemissionType;
        this.legalAidAccountNumber = legalAidAccountNumber;
        this.helpWithFeesReferenceNumber = helpWithFeesReferenceNumber;
    }

    public RemissionDetails(
            String feeRemissionType,
            Document section17Document,
            Document section20Document,
            Document homeOfficeWaiverDocument
    ) {
        this.feeRemissionType = feeRemissionType;
        this.section17Document = section17Document;
        this.section20Document = section20Document;
        this.homeOfficeWaiverDocument = homeOfficeWaiverDocument;
    }

    public RemissionDetails(
            String feeRemissionType,
            String exceptionalCircumstances,
            List<IdValue<Document>> remissionEcEvidenceDocuments
    ) {
        this.feeRemissionType = feeRemissionType;
        this.exceptionalCircumstances = exceptionalCircumstances;
        this.remissionEcEvidenceDocuments = remissionEcEvidenceDocuments;
    }

    public void setRemissionDecision(String remissionDecision) {
        this.remissionDecision = remissionDecision;
    }

    public void setFeeAmount(String feeAmount) {
        this.feeAmount = feeAmount;
    }

    public void setAmountRemitted(String amountRemitted) {
        this.amountRemitted = amountRemitted;
    }

    public void setAmountLeftToPay(String amountLeftToPay) {
        this.amountLeftToPay = amountLeftToPay;
    }

    public void setRemissionDecisionReason(String remissionDecisionReason) {
        this.remissionDecisionReason = remissionDecisionReason;
    }
}
