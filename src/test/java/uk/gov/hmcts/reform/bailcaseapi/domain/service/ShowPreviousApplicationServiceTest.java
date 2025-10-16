package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.AGREES_TO_BOUND_BY_FINANCIAL_COND;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_ARRIVAL_IN_UK;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DETENTION_LOCATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DISABILITY_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GENDER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_HAS_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_NATIONALITIES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_PRISON_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICATION_SUBMITTED_BY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_ACTIVITIES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_APPEARANCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_ELECTRONIC_MONITORING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_OTHER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_REPORTING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.CONDITION_RESIDENCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_DETAILS_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DISABILITY_YESNO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.END_APPLICATION_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.END_APPLICATION_OUTCOME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.END_APPLICATION_REASONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS1_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS1_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS1_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS_INTERPRETER_YESNO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_COND_AMOUNT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.GROUNDS_FOR_BAIL_REASONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_APPEAL_HEARING_PENDING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_APPEAL_HEARING_PENDING_UT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_PROBATION_OFFENDER_MANAGER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.INTERPRETER_LANGUAGES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.INTERPRETER_YESNO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_IMA_ENABLED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_FAMILY_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_PHONE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_REFERENCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.NO_TRANSFER_BAIL_MANAGEMENT_REASONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.OBJECTED_TRANSFER_BAIL_MANAGEMENT_REASONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PRISON_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PROBATION_OFFENDER_MANAGER_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PROBATION_OFFENDER_MANAGER_FAMILY_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PROBATION_OFFENDER_MANAGER_GIVEN_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PROBATION_OFFENDER_MANAGER_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PROBATION_OFFENDER_MANAGER_TELEPHONE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.REASONS_JUDGE_IS_MINDED_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_THE_DECISION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SECRETARY_OF_STATE_REFUSAL_REASONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SIGNED_DECISION_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_ADDRESS_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_FAMILY_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_HAS_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_IMMIGRATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_NATIONALITY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_OCCUPATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_RELATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_TELEPHONE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TRANSFER_BAIL_MANAGEMENT_OBJECTION_OPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TRANSFER_BAIL_MANAGEMENT_OPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UT_APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.VIDEO_HEARING_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.VIDEO_HEARING_YESNO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingHearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.AddressUK;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ShowPreviousApplicationServiceTest {

    private ShowPreviousApplicationService showPreviousApplicationService;
    @Mock
    private BailCase bailCase = new BailCase();

    @Mock
    private DocumentWithMetadata document1WithMetadata;
    @Mock
    private DocumentWithMetadata document2WithMetadata;
    @Mock
    private CaseNote caseNote;
    @Mock
    private CaseNote caseNoteWithoutDocument;
    @Mock
    InterpreterLanguageRefData interpreterLanguageRefDataSpoken1;
    @Mock
    DynamicList dynamicListSpoken1;
    @Mock
    Value valueSpoken1;
    @Mock
    InterpreterLanguageRefData interpreterLanguageRefDataSign1;

    @BeforeEach
    void setUp() {
        showPreviousApplicationService = new ShowPreviousApplicationService();
        setBailCaseDefinitions();
    }

    void setBailCaseDefinitions() {
        List<IdValue<DocumentWithMetadata>> existingApplicantDocuments = List.of(
            new IdValue<>("1", document1WithMetadata)
        );
        List<IdValue<DocumentWithMetadata>> existingTribunalDocuments = List.of(
            new IdValue<>("1", document2WithMetadata)
        );
        List<IdValue<DocumentWithMetadata>> existingHODocuments = List.of(
            new IdValue<>("1", document2WithMetadata)
        );
        List<IdValue<DocumentWithMetadata>> existingDecisionDocuments = List.of(
            new IdValue<>("1", document1WithMetadata)
        );
        List<IdValue<DocumentWithMetadata>> existingHearingDocuments = List.of(
            new IdValue<>("1", document2WithMetadata)
        );


        List<IdValue<Direction>> existingDirections =
            Arrays.asList(
                new IdValue<>("1", new Direction(
                    "explanation-1",
                    "Applicant",
                    "2020-12-01",
                    "2019-12-01",
                    "",
                    "",
                    Collections.emptyList()
                )),
                new IdValue<>("2", new Direction(
                    "explanation-2",
                    "Home Office",
                    "2020-11-01",
                    "2019-11-01",
                    "",
                    "",
                    Collections.emptyList()
                ))
            );

        List<IdValue<InterpreterLanguage>> interpreterLanguages =
            Arrays.asList(
                new IdValue<>("1", new InterpreterLanguage("English", "NA")),
                new IdValue<>("2", new InterpreterLanguage("African", "NA"))
            );

        List<IdValue<CaseNote>> existingCaseNotes =
            List.of(new IdValue<>("1", caseNote), new IdValue<>("2", caseNoteWithoutDocument));

        List<IdValue<NationalityFieldValue>> nationalities =
            List.of(new IdValue<>("1", new NationalityFieldValue("American")));

        when(bailCase.read(END_APPLICATION_OUTCOME, String.class))
            .thenReturn(Optional.of("Withdrawn"));
        when(bailCase.read(END_APPLICATION_DATE, String.class))
            .thenReturn(Optional.of("2022-06-20"));
        when(bailCase.read(END_APPLICATION_REASONS))
            .thenReturn(Optional.of("Withdraw Reasons"));
        when(bailCase.read(CONDITION_APPEARANCE))
            .thenReturn(Optional.of("Appearance Conditions"));
        when(bailCase.read(CONDITION_ACTIVITIES))
            .thenReturn(Optional.of("Activities Conditions"));
        when(bailCase.read(CONDITION_RESIDENCE))
            .thenReturn(Optional.of("Residence Conditions"));
        when(bailCase.read(CONDITION_REPORTING))
            .thenReturn(Optional.of("Reporting Conditions"));
        when(bailCase.read(CONDITION_ELECTRONIC_MONITORING))
            .thenReturn(Optional.of("Electronic Monitoring Conditions"));
        when(bailCase.read(CONDITION_OTHER))
            .thenReturn(Optional.of("Other Conditions"));
        when(bailCase.read(RECORD_DECISION_TYPE))
            .thenReturn(Optional.of("Refused"));
        when(bailCase.read(DECISION_DETAILS_DATE, String.class))
            .thenReturn(Optional.of("2022-06-20"));
        when(bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA))
            .thenReturn(Optional.of(existingApplicantDocuments));
        when(bailCase.read(TRIBUNAL_DOCUMENTS_WITH_METADATA))
            .thenReturn(Optional.of(existingTribunalDocuments));
        when(bailCase.read(HEARING_DOCUMENTS))
            .thenReturn(Optional.of(existingHearingDocuments));
        when(bailCase.read(HOME_OFFICE_DOCUMENTS_WITH_METADATA))
            .thenReturn(Optional.of(existingHODocuments));
        when(bailCase.read(SIGNED_DECISION_DOCUMENTS_WITH_METADATA))
            .thenReturn(Optional.of(existingDecisionDocuments));
        when(document1WithMetadata.getDocument())
            .thenReturn(new Document("document1Url", "/hostname/documents/document1BinaryUrl",
                                     "document1FileName", "document1Hash"
            ));
        when(document1WithMetadata.getDateUploaded()).thenReturn("2022-05-25");
        when(document2WithMetadata.getDocument())
            .thenReturn(new Document("document2Url", "/hostname/documents/document2BinaryUrl",
                                     "document2FileName", "document2Hash"
            ));
        when(document2WithMetadata.getDateUploaded()).thenReturn("2022-05-27");
        when(bailCase.read(DIRECTIONS)).thenReturn(Optional.of(existingDirections));
        when(bailCase.read(CASE_NOTES)).thenReturn(Optional.of(existingCaseNotes));
        when(caseNote.getCaseNoteDocument()).thenReturn(new Document(
            "document1Url", "/hostname/documents/document1BinaryUrl",
            "document1FileName", "document1Hash"
        ));
        when(caseNote.getCaseNoteSubject()).thenReturn("subject");
        when(caseNote.getCaseNoteDescription()).thenReturn("description");
        when(caseNote.getUser()).thenReturn("admin-user");
        when(caseNote.getDateAdded()).thenReturn("2022-06-23");
        when(caseNoteWithoutDocument.getCaseNoteSubject()).thenReturn("subject2");
        when(caseNoteWithoutDocument.getCaseNoteDescription()).thenReturn("description2");
        when(caseNoteWithoutDocument.getUser()).thenReturn("admin-user2");
        when(caseNoteWithoutDocument.getDateAdded()).thenReturn("2022-06-22");

        when(bailCase.read(INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(INTERPRETER_LANGUAGES)).thenReturn(Optional.of(interpreterLanguages));
        when(bailCase.read(DISABILITY_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(APPLICANT_DISABILITY_DETAILS)).thenReturn(Optional.of("Disability details"));
        when(bailCase.read(VIDEO_HEARING_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(VIDEO_HEARING_DETAILS)).thenReturn(Optional.of("Video hearing details"));

        when(bailCase.read(APPLICATION_SUBMITTED_BY)).thenReturn(Optional.of("Applicant"));
        when(bailCase.read(APPLICANT_GIVEN_NAMES)).thenReturn(Optional.of("John"));
        when(bailCase.read(APPLICANT_FAMILY_NAME)).thenReturn(Optional.of("Smith"));
        when(bailCase.read(APPLICANT_DOB)).thenReturn(Optional.of("1999-06-22"));
        when(bailCase.read(APPLICANT_GENDER)).thenReturn(Optional.of("Male"));
        when(bailCase.read(APPLICANT_NATIONALITIES)).thenReturn(Optional.of(nationalities));

        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER)).thenReturn(Optional.of("1122334455"));
        when(bailCase.read(APPLICANT_DETENTION_LOCATION, String.class)).thenReturn(Optional.of("prison"));
        when(bailCase.read(PRISON_NAME)).thenReturn(Optional.of("Milton Keynes"));
        when(bailCase.read(APPLICANT_PRISON_DETAILS)).thenReturn(Optional.of("11112222"));
        when(bailCase.read(IRC_NAME)).thenReturn(Optional.of("IRC Name"));
        when(bailCase.read(APPLICANT_ARRIVAL_IN_UK))
            .thenReturn(Optional.ofNullable(Optional.of("2020-03-02").toString()));
        when(bailCase.read(APPLICANT_ARRIVAL_IN_UK, String.class)).thenReturn(Optional.of("2020-03-02"));
        when(bailCase.read(HAS_APPEAL_HEARING_PENDING)).thenReturn(Optional.of(YesOrNo.YES.toString()));
        when(bailCase.read(APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.of("REF12345"));

        when(bailCase.read(HAS_APPEAL_HEARING_PENDING_UT)).thenReturn(Optional.of(YesOrNo.YES.toString()));
        when(bailCase.read(UT_APPEAL_REFERENCE_NUMBER)).thenReturn(Optional.of("REF12345"));

        when(bailCase.read(APPLICANT_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(APPLICANT_ADDRESS, AddressUK.class)).thenReturn(Optional.of(
            new AddressUK("Line 1", "Line 2", null, null,
                          "PostCode", "County", "Country"
            )));
        when(bailCase.read(AGREES_TO_BOUND_BY_FINANCIAL_COND, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(FINANCIAL_COND_AMOUNT)).thenReturn(Optional.of("2000"));

        when(bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_GIVEN_NAMES)).thenReturn(Optional.of("Jane"));
        when(bailCase.read(SUPPORTER_FAMILY_NAMES)).thenReturn(Optional.of("Smith"));
        when(bailCase.read(SUPPORTER_TELEPHONE_NUMBER, String.class)).thenReturn(Optional.of("7799885544"));
        when(bailCase.read(SUPPORTER_MOBILE_NUMBER, String.class)).thenReturn(Optional.of("1122336655"));
        when(bailCase.read(SUPPORTER_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of("jane.smith@test.com"));
        when(bailCase.read(SUPPORTER_DOB, String.class)).thenReturn(Optional.of("1988-04-05"));
        when(bailCase.read(SUPPORTER_RELATION)).thenReturn(Optional.of("Wife"));
        when(bailCase.read(SUPPORTER_OCCUPATION)).thenReturn(Optional.of("Doctor"));
        when(bailCase.read(SUPPORTER_IMMIGRATION)).thenReturn(Optional.of("Resident"));
        when(bailCase.read(SUPPORTER_NATIONALITY)).thenReturn(Optional.of(nationalities));
        when(bailCase.read(SUPPORTER_HAS_PASSPORT, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SUPPORTER_PASSPORT)).thenReturn(Optional.of("P12345"));
        when(bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES)).thenReturn(Optional.of("3000"));

        when(bailCase.read(GROUNDS_FOR_BAIL_REASONS, String.class)).thenReturn(Optional.of("Grounds for bail reasons"));
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(NO_TRANSFER_BAIL_MANAGEMENT_REASONS, String.class))
            .thenReturn(Optional.of("Transfer bail management reasons"));
        when(bailCase.read(OBJECTED_TRANSFER_BAIL_MANAGEMENT_REASONS, String.class))
            .thenReturn(Optional.of("Objected transfer bail management reasons"));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(LEGAL_REP_COMPANY)).thenReturn(Optional.of("Legal Rep Company"));
        when(bailCase.read(LEGAL_REP_NAME)).thenReturn(Optional.of("LR ABC"));
        when(bailCase.read(LEGAL_REP_FAMILY_NAME)).thenReturn(Optional.of("Jones"));
        when(bailCase.read(LEGAL_REP_EMAIL_ADDRESS)).thenReturn(Optional.of("lr_abc@test.com"));
        when(bailCase.read(LEGAL_REP_PHONE)).thenReturn(Optional.of("1122334455"));
        when(bailCase.read(LEGAL_REP_REFERENCE)).thenReturn(Optional.of("Ref78965"));


        when(interpreterLanguageRefDataSpoken1.getLanguageRefData()).thenReturn(dynamicListSpoken1);
        when(interpreterLanguageRefDataSign1.getLanguageRefData()).thenReturn(null);
        when(interpreterLanguageRefDataSign1.getLanguageManualEntry()).thenReturn("Yes");
        when(bailCase.read(APPLICANT_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(interpreterLanguageRefDataSpoken1));
        when(bailCase.read(APPLICANT_INTERPRETER_SIGN_LANGUAGE)).thenReturn(Optional.of(interpreterLanguageRefDataSign1));
        when(dynamicListSpoken1.getValue()).thenReturn(valueSpoken1);
        when(valueSpoken1.getLabel()).thenReturn("lang 1");
        when(interpreterLanguageRefDataSign1.getLanguageManualEntryDescription()).thenReturn("lang sign 1");

        when(bailCase.read(FCS_INTERPRETER_YESNO, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(FCS1_INTERPRETER_LANGUAGE_CATEGORY)).thenReturn(Optional.of(List.of("spokenLanguageInterpreter")));
        when(bailCase.read(FCS1_INTERPRETER_SPOKEN_LANGUAGE)).thenReturn(Optional.of(interpreterLanguageRefDataSpoken1));

        when(bailCase.read(LISTING_LOCATION, ListingHearingCentre.class)).thenReturn(Optional.of(ListingHearingCentre.BIRMINGHAM));
        when(bailCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2024-04-04T08:00:00.000"));
        when(bailCase.read(HAS_PROBATION_OFFENDER_MANAGER, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(PROBATION_OFFENDER_MANAGER_GIVEN_NAME)).thenReturn(Optional.of("Jane"));
        when(bailCase.read(PROBATION_OFFENDER_MANAGER_FAMILY_NAME)).thenReturn(Optional.of("Smith"));
        when(bailCase.read(PROBATION_OFFENDER_MANAGER_TELEPHONE_NUMBER, String.class)).thenReturn(Optional.of("7799885544"));
        when(bailCase.read(PROBATION_OFFENDER_MANAGER_MOBILE_NUMBER, String.class)).thenReturn(Optional.of("1122336655"));
        when(bailCase.read(PROBATION_OFFENDER_MANAGER_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of("jane.smith@test.com"));

    }

    @Test
    void check_decision_label_for_Ended_Application() {
        Value selectedApplicationValue = new Value("1", "Bail Application 1 Ended 20/06/2022");
        String label = showPreviousApplicationService
            .getDecisionLabel(bailCase, selectedApplicationValue);
        assertNotNull(label);
        assertTrue(label.contains("|Withdrawn|"));
        assertTrue(label.contains("|\n|End reasons|Withdraw Reasons|"));
        assertTrue(label.contains("|\n|End date|20 Jun 2022|"));
    }

    @Test
    void check_decision_label_for_Decided_Application() {
        Value selectedApplicationValue = new Value("1", "Bail Application 1 Decided 20/06/2022");
        String label = showPreviousApplicationService
            .getDecisionLabel(bailCase, selectedApplicationValue);
        assertNotNull(label);
        assertTrue(label.contains("|Decision details||"));
        assertTrue(label.contains("|\n|Decision date|20 Jun 2022|"));
        assertTrue(label.contains("|\n|Conditions|*Appearance*<br>Appearance Conditions"));
        assertTrue(label.contains("<br><br> *Activities*<br>Activities Conditions"));
        assertTrue(label.contains("<br><br> *Residence*<br>Residence Conditions"));
        assertTrue(label.contains("<br><br> *Reporting*<br>Reporting Conditions"));
        assertTrue(label.contains("<br><br> *Electronic monitoring*<br>Electronic Monitoring Conditions"));
        assertTrue(label.contains("<br><br>*Other*<br>Other Conditions|"));
    }

    @Test
    void check_decision_label_for_Decided_Application_with_minded_to_grant() {
        Value selectedApplicationValue = new Value("1", "Bail Application 1 Decided 20/06/2022");
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of("refused"));
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of("mindedToGrant"));
        when(bailCase.read(REASONS_JUDGE_IS_MINDED_DETAILS, String.class)).thenReturn(Optional.of("Reasons for minded to Grant"));
        when(bailCase.read(SECRETARY_OF_STATE_REFUSAL_REASONS, String.class)).thenReturn(Optional.of("Reason 123"));

        String label = showPreviousApplicationService
            .getDecisionLabel(bailCase, selectedApplicationValue);

        assertNotNull(label);
        assertTrue(label.contains("|Decision details||"));
        assertTrue(label.contains("|\n|Decision date|20 Jun 2022|"));
        assertTrue(label.contains("|\n|Reasons judge minded to grant bail|Reasons for minded to Grant|"));
        assertTrue(label.contains("|\n|Reasons for refusal|Reason 123"));
    }

    @Test
    void check_documents_label() {
        String label = showPreviousApplicationService.getDocumentsLabel(bailCase);
        assertTrue(label.contains(
            "|Applicant document 1<br>*Document:* <a href=\"/documents/document1BinaryUrl\""
                + " target=\"_blank\">document1FileName</a>"));
        assertTrue(label.contains(
            "|Tribunal document 1<br>*Document:* <a href=\"/documents/document2BinaryUrl\" "
                + "target=\"_blank\">document2FileName</a>"));
        assertTrue(label.contains(
            "|Home Office document 1<br>*Document:* <a href=\"/documents/document2BinaryUrl\" "
                + "target=\"_blank\">document2FileName</a>"));
        assertTrue(label.contains(
            "|Decision document 1<br>*Document:* <a href=\"/documents/document1BinaryUrl\" "
                + "target=\"_blank\">document1FileName</a>"));
        assertTrue(label.contains(
            "|Hearing document 1<br>*Document:* <a href=\"/documents/document2BinaryUrl\" "
            + "target=\"_blank\">document2FileName</a>"));
    }

    @Test
    void check_directions_label() {
        String label = showPreviousApplicationService.getDirectionLabel(bailCase);
        assertTrue(label.contains(
            "|Directions 1<br>*Explanation:* explanation-1<br>*Parties:* Applicant<br>"
                + "*Date due:* 1 Dec 2020<br>*Date sent:* 1 Dec 2019<br><br>"));
        assertTrue(label.contains(
            "Directions 2<br>*Explanation:* explanation-2<br>*Parties:* Home Office<br>"
                + "*Date due:* 1 Nov 2020<br>*Date sent:* 1 Nov 2019<br>|"));
    }

    //Test to make sure methods cope with absence of data
    @Test
    void check_labels_if_no_data_present() {
        reset(bailCase);
        assertNull(showPreviousApplicationService.getDocumentsLabel(bailCase));
        assertNull(showPreviousApplicationService.getDirectionLabel(bailCase));
        String label = showPreviousApplicationService.getHearingReqDetails(bailCase);
        assertTrue(label.contains("|Interpreter|No|\n|Disability|No|\n|Video hearing|No|"));
        assertThatThrownBy(() -> showPreviousApplicationService.getSubmissionDetails(bailCase))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Missing field: applicationSubmittedBy");
        assertThatThrownBy(() -> showPreviousApplicationService.getSubmissionDetails(bailCase))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Missing field: applicationSubmittedBy");
        assertThatThrownBy(() -> showPreviousApplicationService.getPersonalInfoLabel(bailCase))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Missing field: applicantGivenNames");
        when(bailCase.read(APPLICANT_GIVEN_NAMES)).thenReturn(Optional.of("John"));
        assertThatThrownBy(() -> showPreviousApplicationService.getPersonalInfoLabel(bailCase))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Missing field: applicantFamilyName");
        label = showPreviousApplicationService.getFinancialCondCommitment(bailCase);
        assertTrue(label.contains("|Financial condition"));

        assertNull(showPreviousApplicationService.getFinancialConditionSupporterLabel(
            bailCase,
             HAS_FINANCIAL_COND_SUPPORTER,
             SUPPORTER_GIVEN_NAMES,
             SUPPORTER_FAMILY_NAMES,
             SUPPORTER_ADDRESS_DETAILS,
             SUPPORTER_TELEPHONE_NUMBER,
             SUPPORTER_MOBILE_NUMBER,
             SUPPORTER_EMAIL_ADDRESS,
             SUPPORTER_DOB,
             SUPPORTER_RELATION,
             SUPPORTER_OCCUPATION,
             SUPPORTER_IMMIGRATION,
             SUPPORTER_NATIONALITY,
             SUPPORTER_HAS_PASSPORT,
             SUPPORTER_PASSPORT,
             FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES,
             FCS1_INTERPRETER_SPOKEN_LANGUAGE,
             FCS1_INTERPRETER_SIGN_LANGUAGE));
        assertTrue(showPreviousApplicationService.getLegalRepDetails(bailCase).isEmpty());
    }

    @Test
    void check_hearing_details_labels() {
        String label = showPreviousApplicationService.getHearingDetails(bailCase);
        assertTrue(label.contains(
            "|Location|Birmingham|\n"
                + "|Date and time|04 Apr 2024, 08:00|\n"), "Label mismatch, expected label: " + label);
    }

    @Test
    void check_hearing_details_labels_when_hearing_locations_are_from_ref_data() {
        Value value = new Value("231596", "Birmingham Civil And Family Justice Centre");
        when(bailCase.read(REF_DATA_LISTING_LOCATION, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(value, List.of(value))));

        String label = showPreviousApplicationService.getHearingDetails(bailCase);
        assertTrue(label.contains(
            "|Location|Birmingham Civil And Family Justice Centre|\n"
            + "|Date and time|04 Apr 2024, 08:00|\n"), "Label mismatch, expected label: " + label);
    }

    @Test
    void check_hearing_info_labels() {
        String label = showPreviousApplicationService.getHearingReqDetails(bailCase);
        assertTrue(label.contains(
            "|Interpreter|Yes|\n"
                + "|Language|English (NA)<br>African (NA)|\n"
                + "|Disability|Yes|\n"
                + "|Explain any special <br>arrangements needed for the <br>hearing|Disability details|\n"
                + "|Video hearing|No|\n"
                + "|Explain why the applicant <br>would not be able to join the <br>hearing by video link"
                + "|Video hearing details|"));
    }

    @Test
    void check_submission_label() {
        String label = showPreviousApplicationService.getSubmissionDetails(bailCase);
        assertTrue(label.contains("|Application submitted by|Applicant|/n"));
    }

    @Test
    void check_personal_info_label() {
        String label = showPreviousApplicationService.getPersonalInfoLabel(bailCase);
        assertTrue(label.contains(
            "|Given names|John|\n|Family name|Smith|\n|Date of birth"
        ));
    }

    @Test
    void check_applicant_info() {
        when(bailCase.read(IS_IMA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        String label = showPreviousApplicationService.getApplicantInfo(bailCase);
        assertTrue(label.contains(
            "|Prison|Yes|\n"
                + "|NOMS number|11112222|\n"
                + "|Name of prison|HM PrisonMilton Keynes|\n"
                + "|Arrival date into the UK|2 Mar 2020|\n"
                + "|Pending appeal hearing|Yes|\n"
                + "|Pending appeal reference|REF12345|\n"
                + "|Pending appeal hearing in UT|Yes|\n"
                + "|Pending appeal reference number in UT|REF12345|\n"
                + "|Address if bail granted|Yes|\n"
                + "|Address|Line 1<br>Line 2<br>PostCode<br>County<br>Country<br>|\n"
        ));
    }

    @Test
    void check_financial_cond_commitment_label() {
        String label = showPreviousApplicationService.getFinancialCondCommitment(bailCase);
        assertTrue(label.contains(
            "|Financial condition amount|2000|\n"));
    }

    @Test
    void check_financial_condition_supporter() {
        String label = showPreviousApplicationService.getFinancialConditionSupporterLabel(
            bailCase,
            HAS_FINANCIAL_COND_SUPPORTER,
            SUPPORTER_GIVEN_NAMES,
            SUPPORTER_FAMILY_NAMES,
            SUPPORTER_ADDRESS_DETAILS,
            SUPPORTER_TELEPHONE_NUMBER,
            SUPPORTER_MOBILE_NUMBER,
            SUPPORTER_EMAIL_ADDRESS,
            SUPPORTER_DOB,
            SUPPORTER_RELATION,
            SUPPORTER_OCCUPATION,
            SUPPORTER_IMMIGRATION,
            SUPPORTER_NATIONALITY,
            SUPPORTER_HAS_PASSPORT,
            SUPPORTER_PASSPORT,
            FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES,
            FCS1_INTERPRETER_SPOKEN_LANGUAGE,
            FCS1_INTERPRETER_SIGN_LANGUAGE
        );
        assertTrue(label.contains(
            "|Financial condition supporter|Yes|\n"
                + "|Given names|Jane|\n"
                + "|Family name|Smith|\n"
                + "|Telephone number|7799885544|\n"
                + "|Mobile number|1122336655|\n"
                + "|Email address|jane.smith@test.com|\n"
                + "|Date of birth|5 Apr 1988|\n"
                + "|Relationship to the applicant|Wife|\n"
                + "|Occupation|Doctor|\n"
                + "|Immigration status|Resident|\n"
                + "|Nationalities|American|\n"
                + "|Passport number|Yes|\n"
                + "|Passport number|P12345|\n"
                + "|Spoken language Interpreter|lang 1|\n"
                + "|Financial condition amount (£)|3000|"
        ));
    }

    @Test
    void check_probation_offender_manager() {
        String label = showPreviousApplicationService.getProbationOffenderManager(
            bailCase
        );
        assertTrue(label.contains(
            "|Probation offender manager|Yes|\n"
                + "|Given names|Jane|\n"
                + "|Family name|Smith|\n"
                + "|Telephone number|7799885544|\n"
                + "|Mobile number|1122336655|\n"
                + "|Email address|jane.smith@test.com|"
        ));
    }


    @Test
    void check_grounds_for_bails_label_when_transfer_not_acceptable() {
        String label = showPreviousApplicationService.getGroundsForBail(bailCase);
        assertTrue(label.contains(
            "|Bail Grounds|Grounds for bail reasons|\n"
                + "|Transfer bail management|No|\n"
                + "|Reasons applicant does not consent to bail transfer|Transfer bail management reasons|"
        ));
    }

    @Test
    void check_grounds_for_bails_label_when_old_transfer_acceptable() {
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        String label = showPreviousApplicationService.getGroundsForBail(bailCase);
        assertTrue(label.contains(
            "|Bail Grounds|Grounds for bail reasons|\n"
                + "|Transfer bail management|Yes|\n"
        ));
    }

    @Test
    void check_grounds_for_bails_label_when_old_transfer_not_acceptable() {
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        String label = showPreviousApplicationService.getGroundsForBail(bailCase);
        assertTrue(label.contains(
            "|Bail Grounds|Grounds for bail reasons|\n"
                + "|Transfer bail management|No|\n"
                + "|Reasons applicant does not consent to bail transfer|Transfer bail management reasons|\n"
        ));
    }

    @Test
    void check_grounds_for_bails_label_when_transfer_not_objected() {
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OBJECTION_OPTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        String label = showPreviousApplicationService.getGroundsForBail(bailCase);
        assertTrue(label.contains(
            "|Bail Grounds|Grounds for bail reasons|\n"
                + "|Transfer of management to the Home Office|The applicant consents to the management being transferred|\n"
        ));
    }

    @Test
    void check_grounds_for_bails_label_when_transfer_objected() {
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OPTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(TRANSFER_BAIL_MANAGEMENT_OBJECTION_OPTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        String label = showPreviousApplicationService.getGroundsForBail(bailCase);
        assertTrue(label.contains(
            "|Bail Grounds|Grounds for bail reasons|\n"
                + "|Transfer of management to the Home Office|The applicant objects to the management being transferred|\n"
                + "|Reasons why the applicant objects to the management of bail being transferred to the Home Office|Objected transfer bail management reasons|\n"
        ));
    }

    @Test
    void check_legal_rep_label() {
        String label = showPreviousApplicationService.getLegalRepDetails(bailCase);
        assertTrue(label.contains(
            "|Company|Legal Rep Company|\n"
                + "|Name|LR ABC|\n"
                + "|Family name|Jones|\n"
                + "|Email address|lr_abc@test.com|\n"
                + "|Phone number|1122334455|\n"
                + "|Reference|Ref78965|"
        ));
    }

    @Test
    void test_interpreter_details_label_after_list_assist() {
        when(bailCase.read(INTERPRETER_LANGUAGES)).thenReturn(Optional.empty());
        String label = showPreviousApplicationService.getHearingReqDetails(bailCase);
        assertTrue(label.contains(
            "|Interpreter|Yes|\n"
                + "|Spoken language Interpreter|lang 1|\n|Sign language Interpreter|lang sign 1|\n"
                + "|Disability|Yes|\n"
                + "|Explain any special <br>arrangements needed for the <br>hearing|Disability details|\n"
                + "|Video hearing|No|\n"
                + "|Explain why the applicant <br>would not be able to join the <br>hearing by video link"
                + "|Video hearing details|"));
    }
}
