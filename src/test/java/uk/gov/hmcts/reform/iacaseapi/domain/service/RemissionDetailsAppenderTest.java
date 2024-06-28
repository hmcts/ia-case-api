package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import static java.util.Collections.singletonList;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class RemissionDetailsAppenderTest {

    @Mock
    private IdValue<RemissionDetails> remissionDetailsById1;
    private RemissionDetailsAppender remissionDetailsAppender;

    private final String feeRemissionType = "feeRemissionType";

    @BeforeEach
    void setUp() {

        remissionDetailsAppender = new RemissionDetailsAppender();
    }

    @Test
    void append_asylum_support_remission_details() {

        String asylumSupportReference = "asylumSupportReference";
        Document asylumSupportDocument = mock(Document.class);

        RemissionDetails remissionDetails1 = mock(RemissionDetails.class);
        when(remissionDetailsById1.getValue()).thenReturn(remissionDetails1);

        List<IdValue<RemissionDetails>> existingRemissionDetails = singletonList(remissionDetailsById1);

        List<IdValue<RemissionDetails>> remissionDetails =
            remissionDetailsAppender
                .appendAsylumSupportRemissionDetails(existingRemissionDetails, feeRemissionType, asylumSupportReference, asylumSupportDocument);

        assertNotNull(remissionDetails);
        assertEquals(2, remissionDetails.size());
    }

    @Test
    void append_legal_aid_remission_details() {

        String legalAidAccountNumber = "legalAidAccountNumber";

        RemissionDetails remissionDetails1 = mock(RemissionDetails.class);
        when(remissionDetailsById1.getValue()).thenReturn(remissionDetails1);

        List<IdValue<RemissionDetails>> existingRemissionDetails = singletonList(remissionDetailsById1);

        List<IdValue<RemissionDetails>> remissionDetails =
            remissionDetailsAppender.appendLegalAidRemissionDetails(existingRemissionDetails, feeRemissionType, legalAidAccountNumber);

        assertNotNull(remissionDetails);
        assertEquals(2, remissionDetails.size());
    }

    @Test
    void append_help_with_fees_remission_details() {

        String helpWithFeeReference = "helpWithFeeReference";

        RemissionDetails remissionDetails1 = mock(RemissionDetails.class);
        when(remissionDetailsById1.getValue()).thenReturn(remissionDetails1);

        List<IdValue<RemissionDetails>> existingRemissionDetails = singletonList(remissionDetailsById1);

        List<IdValue<RemissionDetails>> remissionDetails =
            remissionDetailsAppender.appendHelpWithFeeReferenceRemissionDetails(existingRemissionDetails, feeRemissionType, helpWithFeeReference);

        assertNotNull(remissionDetails);
        assertEquals(2, remissionDetails.size());
    }

    @Test
    void append_section17_remission_details() {

        Document section17Document = mock(Document.class);

        RemissionDetails remissionDetails1 = mock(RemissionDetails.class);
        when(remissionDetailsById1.getValue()).thenReturn(remissionDetails1);

        List<IdValue<RemissionDetails>> existingRemissionDetails = singletonList(remissionDetailsById1);

        List<IdValue<RemissionDetails>> remissionDetails =
            remissionDetailsAppender.appendSection17RemissionDetails(existingRemissionDetails, feeRemissionType, section17Document);

        assertNotNull(remissionDetails);
        assertEquals(2, remissionDetails.size());
    }

    @Test
    void append_section20_remission_details() {

        Document section20Document = mock(Document.class);

        RemissionDetails remissionDetails1 = mock(RemissionDetails.class);
        when(remissionDetailsById1.getValue()).thenReturn(remissionDetails1);

        List<IdValue<RemissionDetails>> existingRemissionDetails = singletonList(remissionDetailsById1);

        List<IdValue<RemissionDetails>> remissionDetails =
            remissionDetailsAppender.appendSection17RemissionDetails(existingRemissionDetails, feeRemissionType, section20Document);

        assertNotNull(remissionDetails);
        assertEquals(2, remissionDetails.size());
    }

    @Test
    void append_home_office_waiver_remission_details() {

        Document homeOfficeWaiver = mock(Document.class);

        RemissionDetails remissionDetails1 = mock(RemissionDetails.class);
        when(remissionDetailsById1.getValue()).thenReturn(remissionDetails1);

        List<IdValue<RemissionDetails>> existingRemissionDetails = singletonList(remissionDetailsById1);

        List<IdValue<RemissionDetails>> remissionDetails =
            remissionDetailsAppender.appendSection17RemissionDetails(existingRemissionDetails, feeRemissionType, homeOfficeWaiver);

        assertNotNull(remissionDetails);
        assertEquals(2, remissionDetails.size());
    }

    @Test
    void append_exceptional_circumstances_remission_details() {

        String exceptionalCircumstances = "exceptionalCircumstances";
        List<IdValue<Document>> ecDocuments = Collections.emptyList();

        RemissionDetails remissionDetails1 = mock(RemissionDetails.class);
        when(remissionDetailsById1.getValue()).thenReturn(remissionDetails1);

        List<IdValue<RemissionDetails>> existingRemissionDetails = singletonList(remissionDetailsById1);

        List<IdValue<RemissionDetails>> remissionDetails =
            remissionDetailsAppender
                .appendExceptionalCircumstancesRemissionDetails(existingRemissionDetails, feeRemissionType, exceptionalCircumstances, ecDocuments);

        assertNotNull(remissionDetails);
        assertEquals(2, remissionDetails.size());
    }

    @Test
    void append_asylumSupportRefNumberRemissionDetails_remission_details() {

        String asylumSupportReference = "asylumSupportReference";
        RemissionDetails remissionDetails1 = mock(RemissionDetails.class);
        when(remissionDetailsById1.getValue()).thenReturn(remissionDetails1);

        List<IdValue<RemissionDetails>> existingRemissionDetails = List.of(remissionDetailsById1);

        List<IdValue<RemissionDetails>> remissionDetails =
            remissionDetailsAppender
                .appendAsylumSupportRefNumberRemissionDetails(existingRemissionDetails, feeRemissionType, asylumSupportReference);

        assertNotNull(remissionDetails);
        assertEquals(2, remissionDetails.size());
    }

    @Test
    void append_remissionOptionDetails_remission_details() {

        String helpWithFeesOption = "wantToApply";
        String helpWithFeesRefNumber = "HWF123";
        RemissionDetails remissionDetails1 = mock(RemissionDetails.class);
        when(remissionDetailsById1.getValue()).thenReturn(remissionDetails1);

        List<IdValue<RemissionDetails>> existingRemissionDetails = List.of(remissionDetailsById1);

        List<IdValue<RemissionDetails>> remissionDetails =
            remissionDetailsAppender
                .appendRemissionOptionDetails(existingRemissionDetails, feeRemissionType, helpWithFeesOption, helpWithFeesRefNumber);

        assertNotNull(remissionDetails);
        assertEquals(2, remissionDetails.size());
    }

    @Test
    void append_localAuthorityRemissionDetails_remission_details() {

        List<IdValue<DocumentWithMetadata>> localAuthorityLetterList = Mockito.mock(List.class);

        IdValue<DocumentWithMetadata> idValue = new IdValue<>("1", Mockito.mock(DocumentWithMetadata.class));

        when(localAuthorityLetterList.get(0)).thenReturn(idValue);

        when(localAuthorityLetterList.size()).thenReturn(2);

        RemissionDetails remissionDetails1 = mock(RemissionDetails.class);
        when(remissionDetailsById1.getValue()).thenReturn(remissionDetails1);

        List<IdValue<RemissionDetails>> existingRemissionDetails = List.of(remissionDetailsById1);

        List<IdValue<RemissionDetails>> remissionDetails =
            remissionDetailsAppender
                .appendLocalAuthorityRemissionDetails(existingRemissionDetails, feeRemissionType, localAuthorityLetterList);

        assertNotNull(remissionDetails);
        assertEquals(2, remissionDetails.size());
    }
}
