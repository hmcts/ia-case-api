package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;

public class CaseDataMapTest {

    private final String caseData = "{\"appealType\": \"protection\", \"directions\": [{\"id\": \"2\", \"value\": {\"tag\": \"buildCase\", \"dateDue\": \"2019-06-06\", \"parties\": \"legalRepresentative\", \"dateSent\": \"2019-05-09\", \"explanation\": \"You must now build your case by uploading your appeal argument and evidence.\\n\\nAdvice on writing an appeal argument\\nYou must write a full argument that references:\\n- all the evidence you have or plan to rely on, including any witness statements\\n- the grounds and issues of the case\\n- any new matters\\n- any legal authorities you plan to rely on and why they are applicable to your case\\n\\nYour argument must explain why you believe the respondent's decision is wrong. You must provide all the information for the Home Office to conduct a thorough review of their decision at this stage.\\n\\nNext steps\\nOnce you have uploaded your appeal argument and all evidence, submit your case. The case officer will then review everything you've added. If your case looks ready, the case officer will send it to the respondent for their review. The respondent then has 14 days to respond.\"}}, {\"id\": \"1\", \"value\": {\"tag\": \"respondentEvidence\", \"dateDue\": \"2019-05-23\", \"parties\": \"respondent\", \"dateSent\": \"2019-05-09\", \"explanation\": \"A notice of appeal has been lodged against this asylum decision.\\n\\nYou must now send all documents to the case officer. The case officer will send them to the other party. You have 14 days to supply these documents.\\n\\nYou must include:\\n- the notice of decision\\n- any other document provided to the appellant giving reasons for that decision\\n- any statements of evidence\\n- the application form\\n- any record of interview with the appellant in relation to the decision being appealed\\n- any other unpublished documents on which you rely\\n- the notice of any other appealable decision made in relation to the appellant\"}}], \"hasNewMatters\": \"No\", \"hearingCentre\": \"taylorHouse\", \"appellantTitle\": \"MR\", \"hasOtherAppeals\": \"No\", \"notificationsSent\": [{\"id\": \"1557411655106767_APPEAL_SUBMITTED_CASE_OFFICER\", \"value\": \"c959bba5-f808-4c84-be35-3c4a59f5e71a\"}, {\"id\": \"1557411655106767_RESPONDENT_EVIDENCE_DIRECTION\", \"value\": \"c1c3c96c-326d-4916-b63c-7d49277121fe\"}, {\"id\": \"1557411655106767_BUILD_CASE_DIRECTION\", \"value\": \"c54a67fd-eede-4ad1-aa39-2c05400bf201\"}], \"respondentEvidence\": [{\"id\": \"d019091d-806c-49cf-af64-669fb3d21361\", \"value\": {\"document\": {\"document_url\": \"http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2\", \"document_filename\": \"test.doc\", \"document_binary_url\": \"http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2/binary\"}, \"description\": \"desc\"}}], \"appellantFamilyName\": \"Mortimer\", \"appellantGivenNames\": \"Antony\", \"respondentDocuments\": [{\"id\": \"1\", \"value\": {\"tag\": \"respondentEvidence\", \"document\": {\"document_url\": \"http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2\", \"document_filename\": \"test.doc\", \"document_binary_url\": \"http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2/binary\"}, \"description\": \"desc\", \"dateUploaded\": \"2019-05-09\"}}], \"submissionOutOfTime\": \"Yes\", \"appellantDateOfBirth\": \"1980-04-12\", \"appealReferenceNumber\": \"PA/50222/2019\", \"appellantNationalities\": [{\"id\": \"44ac652f-4c79-4bf5-95f2-3f670990a900\", \"value\": {\"code\": \"AF\"}}], \"homeOfficeDecisionDate\": \"1980-04-12\", \"appealGroundsForDisplay\": [\"protectionRefugeeConvention\", \"protectionHumanRights\"], \"appealGroundsProtection\": {\"values\": [\"protectionRefugeeConvention\"]}, \"appellantNameForDisplay\": \"Antony Mortimer\", \"legalRepresentativeName\": \"A Legal Rep\", \"appealGroundsHumanRights\": {\"values\": [\"protectionHumanRights\"]}, \"appellantHasFixedAddress\": \"No\", \"homeOfficeReferenceNumber\": \"A1234567\", \"sendDirectionActionAvailable\": \"Yes\", \"caseBuildingReadyForSubmission\": \"No\", \"legalRepresentativeEmailAddress\": \"ia-law-firm-a@fake.hmcts.net\", \"currentCaseStateVisibleToCaseOfficer\": \"caseBuilding\", \"changeDirectionDueDateActionAvailable\": \"Yes\", \"uploadAdditionalEvidenceActionAvailable\": \"No\", \"currentCaseStateVisibleToLegalRepresentative\": \"caseBuilding\"}";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testCaseDataSimpleMap() throws IOException {

        CaseDataMap caseDataMap = objectMapper.readValue(caseData, CaseDataMap.class);

//        assertThat(caseDataMap.getAppellantGivenNames().get()).isEqualTo("José");
    }

    @Test
    public void testCaseDataComplexMap() throws IOException {

        CaseDataMap caseDataMap = objectMapper.readValue(caseData, CaseDataMap.class);

//        Optional<List<IdValue<DocumentWithDescription>>> evidence = caseDataMap.getRespondentEvidence();

        System.out.println();
    }
}