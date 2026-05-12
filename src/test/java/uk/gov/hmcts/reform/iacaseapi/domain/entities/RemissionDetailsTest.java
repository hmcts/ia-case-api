package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

class RemissionDetailsTest {

    private String feeRemissionType = "A remission type";
    private String asylumSupportReference = FeeRemissionType.ASYLUM_SUPPORT;
    private Document asylumSupportDocument = mock(Document.class);
    private String legalAidAccountNumber = "legalAidAccountNumber";
    private Document section17Document = mock(Document.class);
    private Document section20Document = mock(Document.class);
    private Document homeOfficeWaiverDocument = mock(Document.class);
    private String helpWithFeesReferenceNumber = "helpWithFeesReferenceNumber";
    private String exceptionalCircumstances = "exceptionalCircumstances";
    private List<IdValue<Document>> remissionEcEvidenceDocuments = Collections.emptyList();
    private String remissionDecision = "remissionDecision";
    private String feeAmount = "14000";
    private String amountRemitted = "4000";
    private String amountLeftToPay = "4000";
    private String remissionDecisionReason = "remissionDecisionReason";

    @Test
    void should_hold_asylum_support_remission_details() {

        RemissionDetails remissionDetails =
            new RemissionDetails(feeRemissionType, asylumSupportReference, asylumSupportDocument);

        assertEquals(feeRemissionType, remissionDetails.getFeeRemissionType());
        assertEquals(asylumSupportReference, remissionDetails.getAsylumSupportReference());
        assertEquals(asylumSupportDocument, remissionDetails.getAsylumSupportDocument());
    }

    @Test
    void should_hold_legal_aid_remission_details() {

        RemissionDetails remissionDetails =
            new RemissionDetails(feeRemissionType, legalAidAccountNumber, "");

        assertEquals(feeRemissionType, remissionDetails.getFeeRemissionType());
        assertEquals(legalAidAccountNumber, remissionDetails.getLegalAidAccountNumber());
    }

    @Test
    void should_hold_help_with_fees_remission_details() {

        RemissionDetails remissionDetails =
            new RemissionDetails(feeRemissionType, null, helpWithFeesReferenceNumber);

        assertEquals(feeRemissionType, remissionDetails.getFeeRemissionType());
        assertEquals(helpWithFeesReferenceNumber, remissionDetails.getHelpWithFeesReferenceNumber());
    }

    @Test
    void should_hold_section_17_remission_details() {

        RemissionDetails remissionDetails =
            new RemissionDetails(feeRemissionType, section17Document, null, null);

        assertEquals(feeRemissionType, remissionDetails.getFeeRemissionType());
        assertEquals(section17Document, remissionDetails.getSection17Document());
    }

    @Test
    void should_hold_section_20_remission_details() {

        RemissionDetails remissionDetails =
            new RemissionDetails(feeRemissionType, null, section20Document, null);

        assertEquals(feeRemissionType, remissionDetails.getFeeRemissionType());
        assertEquals(section20Document, remissionDetails.getSection20Document());
    }

    @Test
    void should_hold_home_office_waiver_remission_details() {

        RemissionDetails remissionDetails =
            new RemissionDetails(feeRemissionType, null, null, homeOfficeWaiverDocument);

        assertEquals(feeRemissionType, remissionDetails.getFeeRemissionType());
        assertEquals(homeOfficeWaiverDocument, remissionDetails.getHomeOfficeWaiverDocument());
    }

    @Test
    void should_hold_exceptional_circumstances_remission_details() {

        RemissionDetails remissionDetails =
            new RemissionDetails(feeRemissionType, exceptionalCircumstances, remissionEcEvidenceDocuments);

        assertEquals(feeRemissionType, remissionDetails.getFeeRemissionType());
        assertEquals(exceptionalCircumstances, remissionDetails.getExceptionalCircumstances());
        assertEquals(remissionEcEvidenceDocuments, remissionDetails.getRemissionEcEvidenceDocuments());
    }

    @Test
    void should_set_correct_fields_in_remission_details() {

        RemissionDetails remissionDetails =
            new RemissionDetails("", "", "");

        remissionDetails.setAmountLeftToPay(amountLeftToPay);
        remissionDetails.setAmountRemitted(amountRemitted);
        remissionDetails.setFeeAmount(feeAmount);
        remissionDetails.setRemissionDecision(remissionDecision);
        remissionDetails.setRemissionDecisionReason(remissionDecisionReason);

        assertEquals(amountLeftToPay, remissionDetails.getAmountLeftToPay());
        assertEquals(amountRemitted, remissionDetails.getAmountRemitted());
        assertEquals(feeAmount, remissionDetails.getFeeAmount());
        assertEquals(remissionDecision, remissionDetails.getRemissionDecision());
        assertEquals(remissionDecisionReason, remissionDetails.getRemissionDecisionReason());
    }
}
