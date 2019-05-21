package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AsylumCaseTest {

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
    private final AppealType appealType = AppealType.RP;
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
    private final String sendDirectionDateDue = "2022-01-01";
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

    private List<IdValue<DocumentWithDescription>> additionalEvidence = mock(List.class);

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
    // start decision and reasons ...
    // -----------------------------------------------------------------------------

    private String caseIntroductionDescription = "a";
    private String appellantCaseSummaryDescription = "a";
    private YesOrNo immigrationHistoryAgreement = YesOrNo.YES;
    private String agreedImmigrationHistoryDescription = "a";
    private String respondentsImmigrationHistoryDescription = "a";
    private String immigrationHistoryDisagreementDescription = "a";
    private YesOrNo scheduleOfIssuesAgreement = YesOrNo.YES;
    private String respondentsAgreedScheduleOfIssuesDescription = "a";
    private String respondentsScheduleOfIssuesDescription = "a";
    private String respondentsDisputedScheduleOfIssuesDescription = "a";
    private String scheduleOfIssuesDisagreementDescription = "a";

    // -----------------------------------------------------------------------------

    @Mock AsylumCaseBuilder asylumCaseBuilder;

    @Before
    public void setUp() {

        when(asylumCaseBuilder.getHomeOfficeReferenceNumber()).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCaseBuilder.getHomeOfficeDecisionDate()).thenReturn(Optional.of(homeOfficeDecisionDate));
        when(asylumCaseBuilder.getAppellantTitle()).thenReturn(Optional.of(appellantTitle));
        when(asylumCaseBuilder.getAppellantGivenNames()).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCaseBuilder.getAppellantFamilyName()).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCaseBuilder.getAppellantDateOfBirth()).thenReturn(Optional.of(appellantDateOfBirth));
        when(asylumCaseBuilder.getAppellantNationalities()).thenReturn(Optional.of(appellantNationalities));
        when(asylumCaseBuilder.getAppellantHasFixedAddress()).thenReturn(Optional.of(appellantHasFixedAddress));
        when(asylumCaseBuilder.getAppellantAddress()).thenReturn(Optional.of(appellantAddress));
        when(asylumCaseBuilder.getAppealType()).thenReturn(Optional.of(appealType));
        when(asylumCaseBuilder.getAppealGroundsProtection()).thenReturn(Optional.of(appealGroundsProtection));
        when(asylumCaseBuilder.getAppealGroundsHumanRights()).thenReturn(Optional.of(appealGroundsHumanRights));
        when(asylumCaseBuilder.getAppealGroundsRevocation()).thenReturn(Optional.of(appealGroundsRevocation));
        when(asylumCaseBuilder.getHasNewMatters()).thenReturn(Optional.of(hasNewMatters));
        when(asylumCaseBuilder.getNewMatters()).thenReturn(Optional.of(newMatters));
        when(asylumCaseBuilder.getHasOtherAppeals()).thenReturn(Optional.of(hasOtherAppeals));
        when(asylumCaseBuilder.getOtherAppeals()).thenReturn(Optional.of(otherAppeals));
        when(asylumCaseBuilder.getLegalRepReferenceNumber()).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCaseBuilder.getAppealReferenceNumber()).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCaseBuilder.getAppellantNameForDisplay()).thenReturn(Optional.of(appellantNameForDisplay));
        when(asylumCaseBuilder.getAppealGroundsForDisplay()).thenReturn(Optional.of(appealGroundsForDisplay));
        when(asylumCaseBuilder.getHearingCentre()).thenReturn(Optional.of(hearingCentre));

        when(asylumCaseBuilder.getSendDirectionExplanation()).thenReturn(Optional.of(sendDirectionExplanation));
        when(asylumCaseBuilder.getSendDirectionParties()).thenReturn(Optional.of(sendDirectionParties));
        when(asylumCaseBuilder.getSendDirectionDateDue()).thenReturn(Optional.of(sendDirectionDateDue));
        when(asylumCaseBuilder.getDirections()).thenReturn(Optional.of(directions));

        when(asylumCaseBuilder.getEditableDirections()).thenReturn(Optional.of(editableDirections));

        when(asylumCaseBuilder.getAdditionalEvidenceDocuments()).thenReturn(Optional.of(additionalEvidenceDocuments));
        when(asylumCaseBuilder.getHearingDocuments()).thenReturn(Optional.of(hearingDocuments));
        when(asylumCaseBuilder.getLegalRepresentativeDocuments()).thenReturn(Optional.of(legalRepresentativeDocuments));
        when(asylumCaseBuilder.getRespondentDocuments()).thenReturn(Optional.of(respondentDocuments));

        when(asylumCaseBuilder.getRespondentEvidence()).thenReturn(Optional.of(respondentEvidence));

        when(asylumCaseBuilder.getCaseArgumentDocument()).thenReturn(Optional.of(caseArgumentDocument));
        when(asylumCaseBuilder.getCaseArgumentDescription()).thenReturn(Optional.of(caseArgumentDescription));
        when(asylumCaseBuilder.getCaseArgumentEvidence()).thenReturn(Optional.of(caseArgumentEvidence));

        when(asylumCaseBuilder.getAppealResponseDocument()).thenReturn(Optional.of(appealResponseDocument));
        when(asylumCaseBuilder.getAppealResponseDescription()).thenReturn(Optional.of(appealResponseDescription));
        when(asylumCaseBuilder.getAppealResponseEvidence()).thenReturn(Optional.of(appealResponseEvidence));

        when(asylumCaseBuilder.getApplicationOutOfTimeExplanation()).thenReturn(Optional.of(applicationOutOfTimeExplanation));
        when(asylumCaseBuilder.getApplicationOutOfTimeDocument()).thenReturn(Optional.of(applicationOutOfTimeDocument));

        when(asylumCaseBuilder.getAdditionalEvidence()).thenReturn(Optional.of(additionalEvidence));

        when(asylumCaseBuilder.getListCaseHearingCentre()).thenReturn(Optional.of(listCaseHearingCentre));
        when(asylumCaseBuilder.getListCaseHearingLength()).thenReturn(Optional.of(listCaseHearingLength));
        when(asylumCaseBuilder.getListCaseHearingDate()).thenReturn(Optional.of(listCaseHearingDate));
        when(asylumCaseBuilder.getListCaseRequirementsVulnerabilities()).thenReturn(Optional.of(listCaseRequirementsVulnerabilities));
        when(asylumCaseBuilder.getListCaseRequirementsMultimedia()).thenReturn(Optional.of(listCaseRequirementsMultimedia));
        when(asylumCaseBuilder.getListCaseRequirementsSingleSexCourt()).thenReturn(Optional.of(listCaseRequirementsSingleSexCourt));
        when(asylumCaseBuilder.getListCaseRequirementsInCameraCourt()).thenReturn(Optional.of(listCaseRequirementsInCameraCourt));
        when(asylumCaseBuilder.getListCaseRequirementsOther()).thenReturn(Optional.of(listCaseRequirementsOther));

        when(asylumCaseBuilder.getCaseSummaryDocument()).thenReturn(Optional.of(caseSummaryDocument));
        when(asylumCaseBuilder.getCaseSummaryDescription()).thenReturn(Optional.of(caseSummaryDescription));

        when(asylumCaseBuilder.getLegalRepresentativeName()).thenReturn(Optional.of(legalRepresentativeName));
        when(asylumCaseBuilder.getLegalRepresentativeEmailAddress()).thenReturn(Optional.of(legalRepresentativeEmailAddress));
        when(asylumCaseBuilder.getNotificationsSent()).thenReturn(Optional.of(notificationsSent));
        when(asylumCaseBuilder.getChangeDirectionDueDateActionAvailable()).thenReturn(Optional.of(changeDirectionDueDateActionAvailable));
        when(asylumCaseBuilder.getSendDirectionActionAvailable()).thenReturn(Optional.of(sendDirectionActionAvailable));
        when(asylumCaseBuilder.getUploadAdditionalEvidenceActionAvailable()).thenReturn(Optional.of(uploadAdditionalEvidenceActionAvailable));
        when(asylumCaseBuilder.getCurrentCaseStateVisibleToCaseOfficer()).thenReturn(Optional.of(currentCaseStateVisibleToCaseOfficer));
        when(asylumCaseBuilder.getCurrentCaseStateVisibleToLegalRepresentative()).thenReturn(Optional.of(currentCaseStateVisibleToLegalRepresentative));
        when(asylumCaseBuilder.getCaseArgumentAvailable()).thenReturn(Optional.of(caseArgumentAvailable));
        when(asylumCaseBuilder.getAppealResponseAvailable()).thenReturn(Optional.of(appealResponseAvailable));
        when(asylumCaseBuilder.getSubmissionOutOfTime()).thenReturn(Optional.of(submissionOutOfTime));

        when(asylumCaseBuilder.getCaseBuildingReadyForSubmission()).thenReturn(Optional.of(caseBuildingReadyForSubmission));
        when(asylumCaseBuilder.getRespondentReviewAppealResponseAdded()).thenReturn(Optional.of(respondentReviewAppealResponseAdded));

        when(asylumCaseBuilder.getCaseIntroductionDescription()).thenReturn(Optional.of(caseIntroductionDescription));
        when(asylumCaseBuilder.getAppellantCaseSummaryDescription()).thenReturn(Optional.of(appellantCaseSummaryDescription));
        when(asylumCaseBuilder.getImmigrationHistoryAgreement()).thenReturn(Optional.of(immigrationHistoryAgreement));
        when(asylumCaseBuilder.getAgreedImmigrationHistoryDescription()).thenReturn(Optional.of(agreedImmigrationHistoryDescription));
        when(asylumCaseBuilder.getRespondentsImmigrationHistoryDescription()).thenReturn(Optional.of(respondentsImmigrationHistoryDescription));
        when(asylumCaseBuilder.getImmigrationHistoryDisagreementDescription()).thenReturn(Optional.of(immigrationHistoryDisagreementDescription));
        when(asylumCaseBuilder.getScheduleOfIssuesAgreement()).thenReturn(Optional.of(scheduleOfIssuesAgreement));
        when(asylumCaseBuilder.getRespondentsAgreedScheduleOfIssuesDescription()).thenReturn(Optional.of(respondentsAgreedScheduleOfIssuesDescription));
        when(asylumCaseBuilder.getRespondentsScheduleOfIssuesDescription()).thenReturn(Optional.of(respondentsScheduleOfIssuesDescription));
        when(asylumCaseBuilder.getRespondentsDisputedScheduleOfIssuesDescription()).thenReturn(Optional.of(respondentsDisputedScheduleOfIssuesDescription));
        when(asylumCaseBuilder.getScheduleOfIssuesDisagreementDescription()).thenReturn(Optional.of(scheduleOfIssuesDisagreementDescription));
    }

    @Test
    public void should_hold_onto_values() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

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

        assertEquals(Optional.of(caseIntroductionDescription), asylumCase.getCaseIntroductionDescription());
        assertEquals(Optional.of(appellantCaseSummaryDescription), asylumCase.getAppellantCaseSummaryDescription());
        assertEquals(Optional.of(immigrationHistoryAgreement), asylumCase.getImmigrationHistoryAgreement());
        assertEquals(Optional.of(agreedImmigrationHistoryDescription), asylumCase.getAgreedImmigrationHistoryDescription());
        assertEquals(Optional.of(respondentsImmigrationHistoryDescription), asylumCase.getRespondentsImmigrationHistoryDescription());
        assertEquals(Optional.of(immigrationHistoryDisagreementDescription), asylumCase.getImmigrationHistoryDisagreementDescription());
        assertEquals(Optional.of(scheduleOfIssuesAgreement), asylumCase.getScheduleOfIssuesAgreement());
        assertEquals(Optional.of(respondentsAgreedScheduleOfIssuesDescription), asylumCase.getRespondentsAgreedScheduleOfIssuesDescription());
        assertEquals(Optional.of(respondentsScheduleOfIssuesDescription), asylumCase.getRespondentsScheduleOfIssuesDescription());
        assertEquals(Optional.of(respondentsDisputedScheduleOfIssuesDescription), asylumCase.getRespondentsDisputedScheduleOfIssuesDescription());
        assertEquals(Optional.of(scheduleOfIssuesDisagreementDescription), asylumCase.getScheduleOfIssuesDisagreementDescription());

        assertEquals(Optional.of(caseBuildingReadyForSubmission), asylumCase.getCaseBuildingReadyForSubmission());
        assertEquals(Optional.of(respondentReviewAppealResponseAdded), asylumCase.getRespondentReviewAppealResponseAdded());
    }

    // -----------------------------------------------------------------------------
    // legal rep appeal ...
    // -----------------------------------------------------------------------------

    @Test
    public void appeal_reference_number_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setAppealReferenceNumber("PR/12345/2019");
        assertEquals(Optional.of("PR/12345/2019"), asylumCase.getAppealReferenceNumber());

        asylumCase.setAppealReferenceNumber(null);
        assertEquals(Optional.empty(), asylumCase.getAppealReferenceNumber());
    }

    @Test
    public void appeal_grounds_for_display_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setAppealGroundsForDisplay(Arrays.asList("Human Rights"));
        assertEquals(Optional.of(Arrays.asList("Human Rights")), asylumCase.getAppealGroundsForDisplay());

        asylumCase.setAppealGroundsForDisplay(null);
        assertEquals(Optional.empty(), asylumCase.getAppealGroundsForDisplay());
    }

    @Test
    public void hearing_centre_for_display_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setHearingCentre(HearingCentre.MANCHESTER);
        assertEquals(Optional.of(HearingCentre.MANCHESTER), asylumCase.getHearingCentre());

        asylumCase.setHearingCentre(null);
        assertEquals(Optional.empty(), asylumCase.getHearingCentre());
    }

    @Test
    public void appellant_name_for_display_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setAppellantNameForDisplay("John Doe");
        assertEquals(Optional.of("John Doe"), asylumCase.getAppellantNameForDisplay());

        asylumCase.setAppellantNameForDisplay(null);
        assertEquals(Optional.empty(), asylumCase.getAppellantNameForDisplay());
    }

    @Test
    public void home_office_reference_number_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setHomeOfficeReferenceNumber("HO123");
        assertEquals(Optional.of("HO123"), asylumCase.getHomeOfficeReferenceNumber());

        asylumCase.setHomeOfficeReferenceNumber(null);
        assertEquals(Optional.empty(), asylumCase.getHomeOfficeReferenceNumber());
    }

    // -----------------------------------------------------------------------------
    // case officer directions ...
    // -----------------------------------------------------------------------------

    @Test
    public void send_direction_explanation_is_clearable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.clearSendDirectionExplanation();
        assertEquals(Optional.empty(), asylumCase.getSendDirectionExplanation());
    }

    @Test
    public void send_direction_parties_is_clearable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.clearSendDirectionParties();
        assertEquals(Optional.empty(), asylumCase.getSendDirectionParties());
    }

    @Test
    public void send_direction_date_due_is_clearable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.clearSendDirectionDateDue();
        assertEquals(Optional.empty(), asylumCase.getSendDirectionDateDue());
    }

    @Test
    public void send_direction_explanation_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setSendDirectionExplanation("explanation");
        assertEquals(Optional.of("explanation"), asylumCase.getSendDirectionExplanation());

        asylumCase.setSendDirectionExplanation(null);
        assertEquals(Optional.empty(), asylumCase.getSendDirectionExplanation());
    }

    @Test
    public void send_direction_parties_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setSendDirectionParties(Parties.LEGAL_REPRESENTATIVE);
        assertEquals(Optional.of(Parties.LEGAL_REPRESENTATIVE), asylumCase.getSendDirectionParties());

        asylumCase.setSendDirectionParties(null);
        assertEquals(Optional.empty(), asylumCase.getSendDirectionParties());
    }

    @Test
    public void send_direction_date_due_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setSendDirectionDateDue("2018-12-25");
        assertEquals(Optional.of("2018-12-25"), asylumCase.getSendDirectionDateDue());

        asylumCase.setSendDirectionDateDue(null);
        assertEquals(Optional.empty(), asylumCase.getSendDirectionDateDue());
    }

    @Test
    public void directions_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        List<IdValue<Direction>> newDirections =
            Arrays.asList(new IdValue<>("ABC", mock(Direction.class)));

        asylumCase.setDirections(newDirections);
        assertEquals(Optional.of(newDirections), asylumCase.getDirections());

        asylumCase.setDirections(Collections.emptyList());
        assertEquals(Optional.of(Collections.emptyList()), asylumCase.getDirections());

        asylumCase.setDirections(null);
        assertEquals(Optional.empty(), asylumCase.getDirections());
    }

    // -----------------------------------------------------------------------------
    // change direction due date ...
    // -----------------------------------------------------------------------------

    @Test
    public void editable_directions_are_clearable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        List<IdValue<EditableDirection>> editableDirections = Arrays.asList(new IdValue<>("1", mock(EditableDirection.class)));

        asylumCase.setEditableDirections(editableDirections);
        assertEquals(Optional.of(editableDirections), asylumCase.getEditableDirections());

        asylumCase.clearEditableDirections();
        assertEquals(Optional.empty(), asylumCase.getEditableDirections());
    }

    @Test
    public void editable_directions_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        List<IdValue<EditableDirection>> newEditableDirections =
            Arrays.asList(new IdValue<>("ABC", mock(EditableDirection.class)));

        asylumCase.setEditableDirections(newEditableDirections);
        assertEquals(Optional.of(newEditableDirections), asylumCase.getEditableDirections());

        asylumCase.setEditableDirections(Collections.emptyList());
        assertEquals(Optional.of(Collections.emptyList()), asylumCase.getEditableDirections());

        asylumCase.setEditableDirections(null);
        assertEquals(Optional.empty(), asylumCase.getEditableDirections());
    }

    // -----------------------------------------------------------------------------
    // case documents ...
    // -----------------------------------------------------------------------------

    @Test
    public void additional_evidence_documents_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        List<IdValue<DocumentWithMetadata>> newAdditionalEvidenceDocuments =
            Arrays.asList(new IdValue<>("ABC", mock(DocumentWithMetadata.class)));

        asylumCase.setAdditionalEvidenceDocuments(newAdditionalEvidenceDocuments);
        assertEquals(Optional.of(newAdditionalEvidenceDocuments), asylumCase.getAdditionalEvidenceDocuments());

        asylumCase.setAdditionalEvidenceDocuments(Collections.emptyList());
        assertEquals(Optional.of(Collections.emptyList()), asylumCase.getAdditionalEvidenceDocuments());

        asylumCase.setAdditionalEvidenceDocuments(null);
        assertEquals(Optional.empty(), asylumCase.getAdditionalEvidenceDocuments());
    }

    @Test
    public void hearing_documents_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        List<IdValue<DocumentWithMetadata>> newHearingDocuments =
            Arrays.asList(new IdValue<>("ABC", mock(DocumentWithMetadata.class)));

        asylumCase.setHearingDocuments(newHearingDocuments);
        assertEquals(Optional.of(newHearingDocuments), asylumCase.getHearingDocuments());

        asylumCase.setHearingDocuments(Collections.emptyList());
        assertEquals(Optional.of(Collections.emptyList()), asylumCase.getHearingDocuments());

        asylumCase.setHearingDocuments(null);
        assertEquals(Optional.empty(), asylumCase.getHearingDocuments());
    }

    @Test
    public void legal_representative_documents_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        List<IdValue<DocumentWithMetadata>> newLegalRepresentativeDocuments =
            Arrays.asList(new IdValue<>("ABC", mock(DocumentWithMetadata.class)));

        asylumCase.setLegalRepresentativeDocuments(newLegalRepresentativeDocuments);
        assertEquals(Optional.of(newLegalRepresentativeDocuments), asylumCase.getLegalRepresentativeDocuments());

        asylumCase.setLegalRepresentativeDocuments(Collections.emptyList());
        assertEquals(Optional.of(Collections.emptyList()), asylumCase.getLegalRepresentativeDocuments());

        asylumCase.setLegalRepresentativeDocuments(null);
        assertEquals(Optional.empty(), asylumCase.getLegalRepresentativeDocuments());
    }

    @Test
    public void respondent_documents_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        List<IdValue<DocumentWithMetadata>> newRespondentDocuments =
            Arrays.asList(new IdValue<>("ABC", mock(DocumentWithMetadata.class)));

        asylumCase.setRespondentDocuments(newRespondentDocuments);
        assertEquals(Optional.of(newRespondentDocuments), asylumCase.getRespondentDocuments());

        asylumCase.setRespondentDocuments(Collections.emptyList());
        assertEquals(Optional.of(Collections.emptyList()), asylumCase.getRespondentDocuments());

        asylumCase.setRespondentDocuments(null);
        assertEquals(Optional.empty(), asylumCase.getRespondentDocuments());
    }

    // -----------------------------------------------------------------------------
    // upload respondent evidence ...
    // -----------------------------------------------------------------------------

    @Test
    public void respondent_evidence_is_clearable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.clearRespondentEvidence();
        assertEquals(Optional.empty(), asylumCase.getRespondentEvidence());
    }

    // -----------------------------------------------------------------------------
    // upload additional evidence ...
    // -----------------------------------------------------------------------------

    @Test
    public void additional_evidence_is_clearable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.clearAdditionalEvidence();
        assertEquals(Optional.empty(), asylumCase.getAdditionalEvidence());
    }

    // -----------------------------------------------------------------------------
    // list case ...
    // -----------------------------------------------------------------------------

    @Test
    public void list_case_hearing_centre_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setListCaseHearingCentre(HearingCentre.TAYLOR_HOUSE);
        assertEquals(Optional.of(HearingCentre.TAYLOR_HOUSE), asylumCase.getListCaseHearingCentre());

        asylumCase.setListCaseHearingCentre(null);
        assertEquals(Optional.empty(), asylumCase.getListCaseHearingCentre());
    }

    // -----------------------------------------------------------------------------
    // internal API managed fields ...
    // -----------------------------------------------------------------------------

    @Test
    public void legal_representative_name_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setLegalRepresentativeName("ABC");
        assertEquals(Optional.of("ABC"), asylumCase.getLegalRepresentativeName());

        asylumCase.setLegalRepresentativeName(null);
        assertEquals(Optional.empty(), asylumCase.getLegalRepresentativeName());
    }

    @Test
    public void legal_representative_email_address_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setLegalRepresentativeEmailAddress("ABC");
        assertEquals(Optional.of("ABC"), asylumCase.getLegalRepresentativeEmailAddress());

        asylumCase.setLegalRepresentativeEmailAddress(null);
        assertEquals(Optional.empty(), asylumCase.getLegalRepresentativeEmailAddress());
    }

    @Test
    public void notifications_sent_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        List<IdValue<String>> notificationsSent = Arrays.asList(new IdValue<>("1", "ABC"));

        asylumCase.setNotificationsSent(notificationsSent);
        assertEquals(Optional.of(notificationsSent), asylumCase.getNotificationsSent());

        asylumCase.setNotificationsSent(Collections.emptyList());
        assertEquals(Optional.of(Collections.emptyList()), asylumCase.getNotificationsSent());

        asylumCase.setNotificationsSent(null);
        assertEquals(Optional.empty(), asylumCase.getNotificationsSent());
    }

    @Test
    public void change_direction_due_date_action_available_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setChangeDirectionDueDateActionAvailable(YesOrNo.NO);
        assertEquals(Optional.of(YesOrNo.NO), asylumCase.getChangeDirectionDueDateActionAvailable());

        asylumCase.setChangeDirectionDueDateActionAvailable(null);
        assertEquals(Optional.empty(), asylumCase.getChangeDirectionDueDateActionAvailable());
    }

    @Test
    public void send_direction_action_available_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setSendDirectionActionAvailable(YesOrNo.NO);
        assertEquals(Optional.of(YesOrNo.NO), asylumCase.getSendDirectionActionAvailable());

        asylumCase.setSendDirectionActionAvailable(null);
        assertEquals(Optional.empty(), asylumCase.getSendDirectionActionAvailable());
    }

    @Test
    public void upload_additional_evidence_action_available_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setUploadAdditionalEvidenceActionAvailable(YesOrNo.NO);
        assertEquals(Optional.of(YesOrNo.NO), asylumCase.getUploadAdditionalEvidenceActionAvailable());

        asylumCase.setUploadAdditionalEvidenceActionAvailable(null);
        assertEquals(Optional.empty(), asylumCase.getUploadAdditionalEvidenceActionAvailable());
    }

    @Test
    public void current_case_state_visible_to_case_officer_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setCurrentCaseStateVisibleToCaseOfficer(State.AWAITING_RESPONDENT_EVIDENCE);
        assertEquals(Optional.of(State.AWAITING_RESPONDENT_EVIDENCE), asylumCase.getCurrentCaseStateVisibleToCaseOfficer());

        asylumCase.setCurrentCaseStateVisibleToCaseOfficer(null);
        assertEquals(Optional.empty(), asylumCase.getCurrentCaseStateVisibleToCaseOfficer());
    }

    @Test
    public void current_case_state_visible_to_legal_representative_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setCurrentCaseStateVisibleToLegalRepresentative(State.CASE_UNDER_REVIEW);
        assertEquals(Optional.of(State.CASE_UNDER_REVIEW), asylumCase.getCurrentCaseStateVisibleToLegalRepresentative());

        asylumCase.setCurrentCaseStateVisibleToLegalRepresentative(null);
        assertEquals(Optional.empty(), asylumCase.getCurrentCaseStateVisibleToLegalRepresentative());
    }

    @Test
    public void case_argument_available_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setCaseArgumentAvailable(YesOrNo.NO);
        assertEquals(Optional.of(YesOrNo.NO), asylumCase.getCaseArgumentAvailable());

        asylumCase.setCaseArgumentAvailable(null);
        assertEquals(Optional.empty(), asylumCase.getCaseArgumentAvailable());
    }

    @Test
    public void appeal_response_available_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setAppealResponseAvailable(YesOrNo.YES);
        assertEquals(Optional.of(YesOrNo.YES), asylumCase.getAppealResponseAvailable());

        asylumCase.setAppealResponseAvailable(null);
        assertEquals(Optional.empty(), asylumCase.getAppealResponseAvailable());
    }

    @Test
    public void submission_out_of_time_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setSubmissionOutOfTime(YesOrNo.YES);
        assertEquals(Optional.of(YesOrNo.YES), asylumCase.getSubmissionOutOfTime());

        asylumCase.setSubmissionOutOfTime(null);
        assertEquals(Optional.empty(), asylumCase.getSubmissionOutOfTime());
    }

    // -----------------------------------------------------------------------------
    // sub-state flags ...
    // -----------------------------------------------------------------------------

    @Test
    public void case_building_ready_for_submission_flag_is_clearable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.clearCaseBuildingReadyForSubmission();
        assertEquals(Optional.empty(), asylumCase.getCaseBuildingReadyForSubmission());
    }

    @Test
    public void respondent_review_appeal_response_added_flag_is_clearable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.clearRespondentReviewAppealResponseAdded();
        assertEquals(Optional.empty(), asylumCase.getRespondentReviewAppealResponseAdded());
    }

    @Test
    public void case_building_ready_for_submission_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setCaseBuildingReadyForSubmission(YesOrNo.NO);
        assertEquals(Optional.of(YesOrNo.NO), asylumCase.getCaseBuildingReadyForSubmission());

        asylumCase.setCaseBuildingReadyForSubmission(null);
        assertEquals(Optional.empty(), asylumCase.getCaseBuildingReadyForSubmission());
    }

    @Test
    public void respondent_review_appeal_response_added_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setRespondentReviewAppealResponseAdded(YesOrNo.NO);
        assertEquals(Optional.of(YesOrNo.NO), asylumCase.getRespondentReviewAppealResponseAdded());

        asylumCase.setRespondentReviewAppealResponseAdded(null);
        assertEquals(Optional.empty(), asylumCase.getRespondentReviewAppealResponseAdded());
    }
}
