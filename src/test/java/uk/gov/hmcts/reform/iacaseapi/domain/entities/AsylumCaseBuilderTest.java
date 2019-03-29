package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@SuppressWarnings("unchecked")
public class AsylumCaseBuilderTest {

    // -----------------------------------------------------------------------------
    // legal rep appeal ...
    // -----------------------------------------------------------------------------

    private final String homeOfficeReferenceNumber = "A";
    private final String homeOfficeDecisionDate = "B";
    private final String appellantTitle = "C";
    private final String appellantGivenNames = "Jane Mary";
    private final String appellantFamilyName = "Smith";
    private final String appellantDateOfBirth = "F";
    private final List<IdValue<Map<String, String>>> appellantNationalities = mock(List.class);
    private final YesOrNo appellantHasFixedAddress = YesOrNo.YES;
    private final AddressUk appellantAddress = mock(AddressUk.class);
    private final AppealType appealType = AppealType.PA;
    private final CheckValues<String> appealGroundsProtection = mock(CheckValues.class);
    private final CheckValues<String> appealGroundsHumanRights = mock(CheckValues.class);
    private final CheckValues<String> appealGroundsRevocation = mock(CheckValues.class);
    private final YesOrNo hasNewMatters = YesOrNo.YES;
    private final String newMatters = "K";
    private final String hasOtherAppeals = "NotSure";
    private final List<IdValue<Map<String, String>>> otherAppeals = mock(List.class);
    private final String legalRepReferenceNumber = "N";
    private final String appealReferenceNumber = "PA/00001/2018";
    private final String appellantNameForDisplay = "Jane Mary Smith";
    private final List<String> appealGroundsForDisplay = mock(List.class);
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;

    // -----------------------------------------------------------------------------
    // case officer directions ...
    // -----------------------------------------------------------------------------

    private final String sendDirectionExplanation = "Do the thing";
    private final Parties sendDirectionParties = Parties.LEGAL_REPRESENTATIVE;
    private final String sendDirectionDateDue = "2022-01-01 00:00:00";
    private final List<IdValue<Direction>> directions = mock(List.class);

    // -----------------------------------------------------------------------------
    // change direction due date ...
    // -----------------------------------------------------------------------------

    private final List<IdValue<EditableDirection>> editableDirections = mock(List.class);

    // -----------------------------------------------------------------------------
    // case documents ...
    // -----------------------------------------------------------------------------

    private final List<IdValue<DocumentWithMetadata>> additionalEvidenceDocuments = mock(List.class);
    private final List<IdValue<DocumentWithMetadata>> hearingDocuments = mock(List.class);
    private final List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments = mock(List.class);
    private final List<IdValue<DocumentWithMetadata>> respondentDocuments = mock(List.class);

    // -----------------------------------------------------------------------------
    // upload respondent evidence ...
    // -----------------------------------------------------------------------------

    private final List<IdValue<DocumentWithDescription>> respondentEvidence = mock(List.class);

    // -----------------------------------------------------------------------------
    // case argument ...
    // -----------------------------------------------------------------------------

    private final Document caseArgumentDocument = mock(Document.class);
    private final String caseArgumentDescription = "O";
    private final List<IdValue<DocumentWithDescription>> caseArgumentEvidence = mock(List.class);

    // -----------------------------------------------------------------------------
    // appeal response ...
    // -----------------------------------------------------------------------------

    private final Document appealResponseDocument = mock(Document.class);
    private final String appealResponseDescription = "P";
    private final List<IdValue<DocumentWithDescription>> appealResponseEvidence = mock(List.class);

    // -----------------------------------------------------------------------------
    // out of time reason ...
    // -----------------------------------------------------------------------------

    private final String applicationOutOfTimeExplanation = "Delayed in the post";
    private final Document applicationOutOfTimeDocument = mock(Document.class);

    // -----------------------------------------------------------------------------
    // upload additional evidence ...
    // -----------------------------------------------------------------------------

    private final List<IdValue<DocumentWithDescription>> additionalEvidence = mock(List.class);

    // -----------------------------------------------------------------------------
    // list case ...
    // -----------------------------------------------------------------------------

    private HearingCentre listCaseHearingCentre = HearingCentre.MANCHESTER;
    private HearingLength listCaseHearingLength = HearingLength.LENGTH_2_HOURS;
    private String listCaseHearingDate = "2030-01-01";
    private String listCaseRequirementsVulnerabilities = "something-about-vulnerabilities";
    private String listCaseRequirementsMultimedia = "something-about-multimedia";
    private String listCaseRequirementsSingleSexCourt = "something-about-single-sex-court";
    private String listCaseRequirementsInCameraCourt = "something-about-in-camera-court";
    private String listCaseRequirementsOther = "some-other";

    // -----------------------------------------------------------------------------
    // create case summary ...
    // -----------------------------------------------------------------------------

    private final Document caseSummaryDocument = mock(Document.class);
    private final String caseSummaryDescription = "Case summary...";

    // -----------------------------------------------------------------------------
    // internal API managed fields ...
    // -----------------------------------------------------------------------------

    private final String legalRepresentativeName = "Q";
    private final String legalRepresentativeEmailAddress = "R";
    private final List<IdValue<String>> notificationsSent = mock(List.class);
    private final YesOrNo changeDirectionDueDateActionAvailable = YesOrNo.YES;
    private final YesOrNo sendDirectionActionAvailable = YesOrNo.YES;
    private final YesOrNo uploadAdditionalEvidenceActionAvailable = YesOrNo.YES;
    private final State currentCaseStateVisibleToCaseOfficer = State.APPEAL_SUBMITTED;
    private final State currentCaseStateVisibleToLegalRepresentative = State.APPEAL_SUBMITTED;
    private final YesOrNo caseArgumentAvailable = YesOrNo.YES;
    private final YesOrNo appealResponseAvailable = YesOrNo.NO;
    private final YesOrNo submissionOutOfTime = YesOrNo.YES;

    // -----------------------------------------------------------------------------
    // sub-state flags ...
    // -----------------------------------------------------------------------------

    private final YesOrNo caseBuildingReadyForSubmission = YesOrNo.YES;
    private final YesOrNo respondentReviewAppealResponseAdded = YesOrNo.YES;

    // -----------------------------------------------------------------------------

    private AsylumCaseBuilder asylumCaseBuilder = new AsylumCaseBuilder();

    @Test
    public void should_build_asylum_case() {

        asylumCaseBuilder.setHomeOfficeReferenceNumber(Optional.of(homeOfficeReferenceNumber));
        asylumCaseBuilder.setHomeOfficeDecisionDate(Optional.of(homeOfficeDecisionDate));
        asylumCaseBuilder.setAppellantTitle(Optional.of(appellantTitle));
        asylumCaseBuilder.setAppellantGivenNames(Optional.of(appellantGivenNames));
        asylumCaseBuilder.setAppellantFamilyName(Optional.of(appellantFamilyName));
        asylumCaseBuilder.setAppellantDateOfBirth(Optional.of(appellantDateOfBirth));
        asylumCaseBuilder.setAppellantNationalities(Optional.of(appellantNationalities));
        asylumCaseBuilder.setAppellantHasFixedAddress(Optional.of(appellantHasFixedAddress));
        asylumCaseBuilder.setAppellantAddress(Optional.of(appellantAddress));
        asylumCaseBuilder.setAppealType(Optional.of(appealType));
        asylumCaseBuilder.setAppealGroundsProtection(Optional.of(appealGroundsProtection));
        asylumCaseBuilder.setAppealGroundsHumanRights(Optional.of(appealGroundsHumanRights));
        asylumCaseBuilder.setAppealGroundsRevocation(Optional.of(appealGroundsRevocation));
        asylumCaseBuilder.setHasNewMatters(Optional.of(hasNewMatters));
        asylumCaseBuilder.setNewMatters(Optional.of(newMatters));
        asylumCaseBuilder.setHasOtherAppeals(Optional.of(hasOtherAppeals));
        asylumCaseBuilder.setOtherAppeals(Optional.of(otherAppeals));
        asylumCaseBuilder.setLegalRepReferenceNumber(Optional.of(legalRepReferenceNumber));
        asylumCaseBuilder.setAppealReferenceNumber(Optional.of(appealReferenceNumber));
        asylumCaseBuilder.setAppellantNameForDisplay(Optional.of(appellantNameForDisplay));
        asylumCaseBuilder.setAppealGroundsForDisplay(Optional.of(appealGroundsForDisplay));
        asylumCaseBuilder.setHearingCentre(Optional.of(hearingCentre));

        asylumCaseBuilder.setSendDirectionExplanation(Optional.of(sendDirectionExplanation));
        asylumCaseBuilder.setSendDirectionParties(Optional.of(sendDirectionParties));
        asylumCaseBuilder.setSendDirectionDateDue(Optional.of(sendDirectionDateDue));
        asylumCaseBuilder.setDirections(Optional.of(directions));

        asylumCaseBuilder.setEditableDirections(Optional.of(editableDirections));

        asylumCaseBuilder.setAdditionalEvidenceDocuments(Optional.of(additionalEvidenceDocuments));
        asylumCaseBuilder.setHearingDocuments(Optional.of(hearingDocuments));
        asylumCaseBuilder.setLegalRepresentativeDocuments(Optional.of(legalRepresentativeDocuments));
        asylumCaseBuilder.setRespondentDocuments(Optional.of(respondentDocuments));

        asylumCaseBuilder.setRespondentEvidence(Optional.of(respondentEvidence));

        asylumCaseBuilder.setCaseArgumentDocument(Optional.of(caseArgumentDocument));
        asylumCaseBuilder.setCaseArgumentDescription(Optional.of(caseArgumentDescription));
        asylumCaseBuilder.setCaseArgumentEvidence(Optional.of(caseArgumentEvidence));

        asylumCaseBuilder.setAppealResponseDocument(Optional.of(appealResponseDocument));
        asylumCaseBuilder.setAppealResponseDescription(Optional.of(appealResponseDescription));
        asylumCaseBuilder.setAppealResponseEvidence(Optional.of(appealResponseEvidence));

        asylumCaseBuilder.setApplicationOutOfTimeExplanation(Optional.of(applicationOutOfTimeExplanation));
        asylumCaseBuilder.setApplicationOutOfTimeDocument(Optional.of(applicationOutOfTimeDocument));

        asylumCaseBuilder.setAdditionalEvidence(Optional.of(additionalEvidence));

        asylumCaseBuilder.setListCaseHearingCentre(Optional.of(listCaseHearingCentre));
        asylumCaseBuilder.setListCaseHearingLength(Optional.of(listCaseHearingLength));
        asylumCaseBuilder.setListCaseHearingDate(Optional.of(listCaseHearingDate));
        asylumCaseBuilder.setListCaseRequirementsVulnerabilities(Optional.of(listCaseRequirementsVulnerabilities));
        asylumCaseBuilder.setListCaseRequirementsMultimedia(Optional.of(listCaseRequirementsMultimedia));
        asylumCaseBuilder.setListCaseRequirementsSingleSexCourt(Optional.of(listCaseRequirementsSingleSexCourt));
        asylumCaseBuilder.setListCaseRequirementsInCameraCourt(Optional.of(listCaseRequirementsInCameraCourt));
        asylumCaseBuilder.setListCaseRequirementsOther(Optional.of(listCaseRequirementsOther));

        asylumCaseBuilder.setCaseSummaryDocument(Optional.of(caseSummaryDocument));
        asylumCaseBuilder.setCaseSummaryDescription(Optional.of(caseSummaryDescription));

        asylumCaseBuilder.setLegalRepresentativeName(Optional.of(legalRepresentativeName));
        asylumCaseBuilder.setLegalRepresentativeEmailAddress(Optional.of(legalRepresentativeEmailAddress));
        asylumCaseBuilder.setNotificationsSent(Optional.of(notificationsSent));
        asylumCaseBuilder.setChangeDirectionDueDateActionAvailable(Optional.of(changeDirectionDueDateActionAvailable));
        asylumCaseBuilder.setSendDirectionActionAvailable(Optional.of(sendDirectionActionAvailable));
        asylumCaseBuilder.setUploadAdditionalEvidenceActionAvailable(Optional.of(uploadAdditionalEvidenceActionAvailable));
        asylumCaseBuilder.setCurrentCaseStateVisibleToCaseOfficer(Optional.of(currentCaseStateVisibleToCaseOfficer));
        asylumCaseBuilder.setCurrentCaseStateVisibleToLegalRepresentative(Optional.of(currentCaseStateVisibleToLegalRepresentative));
        asylumCaseBuilder.setCaseArgumentAvailable(Optional.of(caseArgumentAvailable));
        asylumCaseBuilder.setAppealResponseAvailable(Optional.of(appealResponseAvailable));
        asylumCaseBuilder.setSubmissionOutOfTime(Optional.of(submissionOutOfTime));

        asylumCaseBuilder.setCaseBuildingReadyForSubmission(Optional.of(caseBuildingReadyForSubmission));
        asylumCaseBuilder.setRespondentReviewAppealResponseAdded(Optional.of(respondentReviewAppealResponseAdded));

        AsylumCase asylumCase = asylumCaseBuilder.build();

        assertEquals(Optional.of(homeOfficeReferenceNumber), asylumCase.getHomeOfficeReferenceNumber());
        assertEquals(Optional.of(homeOfficeDecisionDate), asylumCase.getHomeOfficeDecisionDate());
        assertEquals(Optional.of(appellantTitle), asylumCase.getAppellantTitle());
        assertEquals(Optional.of(appellantGivenNames), asylumCase.getAppellantGivenNames());
        assertEquals(Optional.of(appellantFamilyName), asylumCase.getAppellantFamilyName());
        assertEquals(Optional.of(appellantDateOfBirth), asylumCase.getAppellantDateOfBirth());
        assertEquals(Optional.of(appellantNationalities), asylumCase.getAppellantNationalities());
        assertEquals(Optional.of(appellantHasFixedAddress), asylumCase.getAppellantHasFixedAddress());
        assertEquals(Optional.of(appellantAddress), asylumCase.getAppellantAddress());
        assertEquals(Optional.of(appealType), asylumCase.getAppealType());
        assertEquals(Optional.of(appealGroundsProtection), asylumCase.getAppealGroundsProtection());
        assertEquals(Optional.of(appealGroundsHumanRights), asylumCase.getAppealGroundsHumanRights());
        assertEquals(Optional.of(appealGroundsRevocation), asylumCase.getAppealGroundsRevocation());
        assertEquals(Optional.of(hasNewMatters), asylumCase.getHasNewMatters());
        assertEquals(Optional.of(newMatters), asylumCase.getNewMatters());
        assertEquals(Optional.of(hasOtherAppeals), asylumCase.getHasOtherAppeals());
        assertEquals(Optional.of(otherAppeals), asylumCase.getOtherAppeals());
        assertEquals(Optional.of(legalRepReferenceNumber), asylumCase.getLegalRepReferenceNumber());
        assertEquals(Optional.of(appealReferenceNumber), asylumCase.getAppealReferenceNumber());
        assertEquals(Optional.of(appellantNameForDisplay), asylumCase.getAppellantNameForDisplay());
        assertEquals(Optional.of(appealGroundsForDisplay), asylumCase.getAppealGroundsForDisplay());
        assertEquals(Optional.of(hearingCentre), asylumCase.getHearingCentre());

        assertEquals(Optional.of(sendDirectionExplanation), asylumCase.getSendDirectionExplanation());
        assertEquals(Optional.of(sendDirectionParties), asylumCase.getSendDirectionParties());
        assertEquals(Optional.of(sendDirectionDateDue), asylumCase.getSendDirectionDateDue());
        assertEquals(Optional.of(directions), asylumCase.getDirections());

        assertEquals(Optional.of(editableDirections), asylumCase.getEditableDirections());

        assertEquals(Optional.of(additionalEvidenceDocuments), asylumCase.getAdditionalEvidenceDocuments());
        assertEquals(Optional.of(hearingDocuments), asylumCase.getHearingDocuments());
        assertEquals(Optional.of(legalRepresentativeDocuments), asylumCase.getLegalRepresentativeDocuments());
        assertEquals(Optional.of(respondentDocuments), asylumCase.getRespondentDocuments());

        assertEquals(Optional.of(respondentEvidence), asylumCase.getRespondentEvidence());

        assertEquals(Optional.of(caseArgumentDocument), asylumCase.getCaseArgumentDocument());
        assertEquals(Optional.of(caseArgumentDescription), asylumCase.getCaseArgumentDescription());
        assertEquals(Optional.of(caseArgumentEvidence), asylumCase.getCaseArgumentEvidence());

        assertEquals(Optional.of(appealResponseDocument), asylumCase.getAppealResponseDocument());
        assertEquals(Optional.of(appealResponseDescription), asylumCase.getAppealResponseDescription());
        assertEquals(Optional.of(appealResponseEvidence), asylumCase.getAppealResponseEvidence());

        assertEquals(Optional.of(applicationOutOfTimeExplanation), asylumCase.getApplicationOutOfTimeExplanation());
        assertEquals(Optional.of(applicationOutOfTimeDocument), asylumCase.getApplicationOutOfTimeDocument());

        assertEquals(Optional.of(additionalEvidence), asylumCase.getAdditionalEvidence());

        assertEquals(Optional.of(listCaseHearingCentre), asylumCase.getListCaseHearingCentre());
        assertEquals(Optional.of(listCaseHearingLength), asylumCase.getListCaseHearingLength());
        assertEquals(Optional.of(listCaseHearingDate), asylumCase.getListCaseHearingDate());
        assertEquals(Optional.of(listCaseRequirementsVulnerabilities), asylumCase.getListCaseRequirementsVulnerabilities());
        assertEquals(Optional.of(listCaseRequirementsMultimedia), asylumCase.getListCaseRequirementsMultimedia());
        assertEquals(Optional.of(listCaseRequirementsSingleSexCourt), asylumCase.getListCaseRequirementsSingleSexCourt());
        assertEquals(Optional.of(listCaseRequirementsInCameraCourt), asylumCase.getListCaseRequirementsInCameraCourt());
        assertEquals(Optional.of(listCaseRequirementsOther), asylumCase.getListCaseRequirementsOther());

        assertEquals(Optional.of(caseSummaryDocument), asylumCase.getCaseSummaryDocument());
        assertEquals(Optional.of(caseSummaryDescription), asylumCase.getCaseSummaryDescription());

        assertEquals(Optional.of(legalRepresentativeName), asylumCase.getLegalRepresentativeName());
        assertEquals(Optional.of(legalRepresentativeEmailAddress), asylumCase.getLegalRepresentativeEmailAddress());
        assertEquals(Optional.of(notificationsSent), asylumCase.getNotificationsSent());
        assertEquals(Optional.of(changeDirectionDueDateActionAvailable), asylumCase.getChangeDirectionDueDateActionAvailable());
        assertEquals(Optional.of(sendDirectionActionAvailable), asylumCase.getSendDirectionActionAvailable());
        assertEquals(Optional.of(uploadAdditionalEvidenceActionAvailable), asylumCase.getUploadAdditionalEvidenceActionAvailable());
        assertEquals(Optional.of(currentCaseStateVisibleToCaseOfficer), asylumCase.getCurrentCaseStateVisibleToCaseOfficer());
        assertEquals(Optional.of(currentCaseStateVisibleToLegalRepresentative), asylumCase.getCurrentCaseStateVisibleToLegalRepresentative());
        assertEquals(Optional.of(caseArgumentAvailable), asylumCase.getCaseArgumentAvailable());
        assertEquals(Optional.of(appealResponseAvailable), asylumCase.getAppealResponseAvailable());
        assertEquals(Optional.of(submissionOutOfTime), asylumCase.getSubmissionOutOfTime());

        assertEquals(Optional.of(caseBuildingReadyForSubmission), asylumCase.getCaseBuildingReadyForSubmission());
        assertEquals(Optional.of(respondentReviewAppealResponseAdded), asylumCase.getRespondentReviewAppealResponseAdded());
    }
}
