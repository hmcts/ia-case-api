package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_ARGUMENT_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RESPONDENT_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEND_DIRECTION_PARTIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUBMISSION_OUT_OF_TIME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.MANCHESTER;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@SuppressWarnings("OperatorWrap")
class AsylumCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void reads_string() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<String> maybeAppealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER);

        assertThat(maybeAppealReferenceNumber.get()).isEqualTo("PA/50222/2019");
    }

    @Test
    void reads_document_with_description() throws IOException {

        String caseData = "{\n" +
            "  \"respondentEvidence\": [\n" +
            "    {\n" +
            "      \"id\": \"d019091d-806c-49cf-af64-669fb3d21361\",\n" +
            "      \"value\": {\n" +
            "        \"document\": {\n" +
            "          \"document_url\": \"http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2\",\n" +
            "          \"document_filename\": \"test.doc\",\n" +
            "          \"document_binary_url\": \"http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2/binary\"\n" +
            "        },\n" +
            "        \"description\": \"desc\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<List<IdValue<DocumentWithDescription>>> maybeRespondentEvidence = asylumCase.read(RESPONDENT_EVIDENCE);

        IdValue<DocumentWithDescription> documentWithDescriptionIdValue = maybeRespondentEvidence.get().get(0);

        assertThat(documentWithDescriptionIdValue.getId())
            .isEqualTo("d019091d-806c-49cf-af64-669fb3d21361");
        assertThat(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentUrl())
            .isEqualTo("http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2");
        assertThat(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentBinaryUrl())
            .isEqualTo("http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2/binary");
        assertThat(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentFilename())
            .isEqualTo("test.doc");
        assertThat(documentWithDescriptionIdValue.getValue().getDescription().get())
            .isEqualTo("desc");
    }

    @Test
    void reads_yes_or_no() throws IOException {

        String caseData = "{\"appealType\": \"revocationOfProtection\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<AppealType> appealType = asylumCase.read(APPEAL_TYPE);

        assertThat(appealType.get()).isEqualTo(AppealType.RP);
    }

    @Test
    void reads_address_with_target_type_generics() throws IOException {

        String caseData = "{\n" +
            "  \"appellantAddress\": {\n" +
            "    \"AddressLine1\": \"a1\",\n" +
            "    \"AddressLine2\": \"a2\",\n" +
            "    \"PostTown\": \"some-post-town\",\n" +
            "    \"PostCode\": \"some-post-code\",\n" +
            "    \"County\": \"some-county\",\n" +
            "    \"AddressLine3\": \"a3\",\n" +
            "    \"Country\": \"some-country\"\n" +
            "  }\n" +
            "}";

        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<AddressUk> maybeAddress = asylumCase.read(APPELLANT_ADDRESS);

        assertThat(maybeAddress.get().getAddressLine1().get()).isEqualTo("a1");
        assertThat(maybeAddress.get().getAddressLine2().get()).isEqualTo("a2");
        assertThat(maybeAddress.get().getAddressLine3().get()).isEqualTo("a3");
        assertThat(maybeAddress.get().getPostTown().get()).isEqualTo("some-post-town");
        assertThat(maybeAddress.get().getPostCode().get()).isEqualTo("some-post-code");
        assertThat(maybeAddress.get().getCounty().get()).isEqualTo("some-county");
        assertThat(maybeAddress.get().getCountry().get()).isEqualTo("some-country");
    }

    @Test
    void reads_appeal_type() throws IOException {

        String caseData = "{\"submissionOutOfTime\": \"Yes\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<YesOrNo> maybeYesNo = asylumCase.read(SUBMISSION_OUT_OF_TIME);

        assertThat(maybeYesNo.get()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void reads_hearing_centre() throws IOException {

        String caseData = "{\"hearingCentre\": \"manchester\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<HearingCentre> maybeHearingCentre = asylumCase.read(HEARING_CENTRE, HearingCentre.class);

        assertThat(maybeHearingCentre.get()).isEqualTo(MANCHESTER);
    }

    @Test
    void reads_parties() throws IOException {

        String caseData = "{\"sendDirectionParties\": \"both\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<Parties> maybeParties = asylumCase.read(SEND_DIRECTION_PARTIES, Parties.class);

        assertThat(maybeParties.get()).isEqualTo(Parties.BOTH);
    }

    @Test
    void reads_id_value_list() throws IOException {

        String caseData = "{\"respondentDocuments\": [\n" +
            "    {\n" +
            "      \"id\": \"3\",\n" +
            "      \"value\": {\n" +
            "        \"tag\": \"appealResponse\",\n" +
            "        \"document\": {\n" +
            "          \"document_url\": \"http://dm-store:8080/documents/c3792783-970b-4994-a361-78bd2ec843a1\",\n" +
            "          \"document_filename\": \"AppealResponse.pdf\",\n" +
            "          \"document_binary_url\": \"http://dm-store:8080/documents/c3792783-970b-4994-a361-78bd2ec843a1/binary\"\n" +
            "        },\n" +
            "        \"description\": \"This is the appeal response\",\n" +
            "        \"dateUploaded\": \"2019-05-16\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"2\",\n" +
            "      \"value\": {\n" +
            "        \"tag\": \"appealResponse\",\n" +
            "        \"document\": {\n" +
            "          \"document_url\": \"http://dm-store:8080/documents/ab3acaa1-0b6c-4d9b-843e-1d3d338d550b\",\n" +
            "          \"document_filename\": \"AppealResponseEvidence.pdf\",\n" +
            "          \"document_binary_url\": \"http://dm-store:8080/documents/ab3acaa1-0b6c-4d9b-843e-1d3d338d550b/binary\"\n" +
            "        },\n" +
            "        \"description\": \"This is the appeal response evidence\",\n" +
            "        \"dateUploaded\": \"2019-05-16\"\n" +
            "      }\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"1\",\n" +
            "      \"value\": {\n" +
            "        \"tag\": \"respondentEvidence\",\n" +
            "        \"document\": {\n" +
            "          \"document_url\": \"http://dm-store:8080/documents/25546e97-d71e-4e85-8141-b954e9aadb75\",\n" +
            "          \"document_filename\": \"RespondentEvidence.pdf\",\n" +
            "          \"document_binary_url\": \"http://dm-store:8080/documents/25546e97-d71e-4e85-8141-b954e9aadb75/binary\"\n" +
            "        },\n" +
            "        \"description\": \"This is the respondent evidence\",\n" +
            "        \"dateUploaded\": \"2019-05-16\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]}";

        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<List<IdValue<DocumentWithMetadata>>> maybeRespondentDocuments = asylumCase.read(RESPONDENT_DOCUMENTS);

        List<IdValue<DocumentWithMetadata>> idValues = maybeRespondentDocuments.get();

        assertThat(idValues.get(0).getId()).isEqualTo("3");
        assertThat(idValues.get(0).getValue()).isInstanceOf(DocumentWithMetadata.class);

        assertThat(idValues.get(1).getId()).isEqualTo("2");
        assertThat(idValues.get(1).getValue()).isInstanceOf(DocumentWithMetadata.class);

        assertThat(idValues.get(2).getId()).isEqualTo("1");
        assertThat(idValues.get(2).getValue()).isInstanceOf(DocumentWithMetadata.class);
    }

    @Test
    void reads_document() throws IOException {

        String caseData = "{\"caseArgumentDocument\": {\n" +
            "    \"document_url\": \"http://dm-store:8080/documents/81e61012-52cd-44b3-9570-873c538ecc00\",\n" +
            "    \"document_filename\": \"CaseArgument.pdf\",\n" +
            "    \"document_binary_url\": \"http://dm-store:8080/documents/81e61012-52cd-44b3-9570-873c538ecc00/binary\"\n" +
            "  }}";

        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<Document> maybeDocument = asylumCase.read(CASE_ARGUMENT_DOCUMENT);

        assertThat(maybeDocument.get().getDocumentFilename()).isEqualTo("CaseArgument.pdf");
        assertThat(maybeDocument.get().getDocumentUrl())
            .isEqualTo("http://dm-store:8080/documents/81e61012-52cd-44b3-9570-873c538ecc00");
        assertThat(maybeDocument.get().getDocumentBinaryUrl())
            .isEqualTo("http://dm-store:8080/documents/81e61012-52cd-44b3-9570-873c538ecc00/binary");
    }

    @Test
    void reads_simple_type_with_parameter_type_generics() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);


        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get())
            .isEqualTo("PA/50222/2019");
    }

    @Test
    void writes_simple_type() {

        AsylumCase asylumCase = new AsylumCase();

        asylumCase.write(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number");

        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get())
            .isEqualTo("some-appeal-reference-number");
    }

    @Test
    void writes_complex_type() {

        AsylumCase asylumCase = new AsylumCase();

        IdValue<DocumentWithDescription> idValue = new IdValue<>(
            "some-id",
            new DocumentWithDescription(
                new Document(
                    "some-doc-url",
                    "some-doc-binary-url",
                    "some-doc-filename"),
                "some-description"));

        asylumCase.write(RESPONDENT_EVIDENCE, asList(idValue));


        Optional<List<IdValue<DocumentWithDescription>>> maybeRespondentEvidence = asylumCase.read(RESPONDENT_EVIDENCE);

        IdValue<DocumentWithDescription> documentWithDescriptionIdValue = maybeRespondentEvidence.get().get(0);


        assertThat(maybeRespondentEvidence.get().size())
            .isEqualTo(1);
        assertThat(documentWithDescriptionIdValue.getId())
            .isEqualTo("some-id");
        assertThat(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentUrl())
            .isEqualTo("some-doc-url");
        assertThat(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentBinaryUrl())
            .isEqualTo("some-doc-binary-url");
        assertThat(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentFilename())
            .isEqualTo("some-doc-filename");
        assertThat(documentWithDescriptionIdValue.getValue().getDescription().get())
            .isEqualTo("some-description");
    }

    @Test
    void clears_value() throws IOException {

        String caseData = "{\"appealReferenceNumber\": \"PA/50222/2019\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        asylumCase.clear(APPEAL_REFERENCE_NUMBER);

        assertThat(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).isEmpty();
    }
}
