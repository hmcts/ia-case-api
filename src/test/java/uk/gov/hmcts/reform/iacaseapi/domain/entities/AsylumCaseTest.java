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
    private final String appellantLastName = "Smith";
    private final String appellantDateOfBirth = "F";
    private final List<IdValue<Map<String, String>>> appellantNationalities = mock(List.class);
    private final YesOrNo appellantHasFixedAddress = YesOrNo.YES;
    private final AddressUk appellantAddress = mock(AddressUk.class);
    private final String appealType = "I";
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

    // -----------------------------------------------------------------------------
    // case officer directions ...
    // -----------------------------------------------------------------------------

    private final String sendDirectionExplanation = "Do the thing";
    private final Parties sendDirectionParties = Parties.LEGAL_REPRESENTATIVE;
    private final String sendDirectionDateDue = "2022-01-01";
    private final List<IdValue<Direction>> directions = mock(List.class);

    // -----------------------------------------------------------------------------
    // case documents ...
    // -----------------------------------------------------------------------------

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
    // internal API managed fields ...
    // -----------------------------------------------------------------------------

    private final String legalRepresentativeName = "Q";
    private final String legalRepresentativeEmailAddress = "R";
    private final List<IdValue<String>> notificationsSent = mock(List.class);
    private final YesOrNo sendDirectionActionAvailable = YesOrNo.YES;
    private final YesOrNo caseBuildingReadyForSubmission = YesOrNo.YES;
    private final State currentCaseStateVisibleToCaseOfficer = State.APPEAL_SUBMITTED;
    private final State currentCaseStateVisibleToLegalRepresentative = State.APPEAL_SUBMITTED;
    private final YesOrNo caseArgumentAvailable = YesOrNo.YES;
    private final YesOrNo appealResponseAvailable = YesOrNo.NO;

    @Mock AsylumCaseBuilder asylumCaseBuilder;

    @Before
    public void setUp() {

        when(asylumCaseBuilder.getHomeOfficeReferenceNumber()).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCaseBuilder.getHomeOfficeDecisionDate()).thenReturn(Optional.of(homeOfficeDecisionDate));
        when(asylumCaseBuilder.getAppellantTitle()).thenReturn(Optional.of(appellantTitle));
        when(asylumCaseBuilder.getAppellantGivenNames()).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCaseBuilder.getAppellantLastName()).thenReturn(Optional.of(appellantLastName));
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
        when(asylumCaseBuilder.getSendDirectionExplanation()).thenReturn(Optional.of(sendDirectionExplanation));
        when(asylumCaseBuilder.getSendDirectionParties()).thenReturn(Optional.of(sendDirectionParties));
        when(asylumCaseBuilder.getSendDirectionDateDue()).thenReturn(Optional.of(sendDirectionDateDue));
        when(asylumCaseBuilder.getDirections()).thenReturn(Optional.of(directions));
        when(asylumCaseBuilder.getLegalRepresentativeDocuments()).thenReturn(Optional.of(legalRepresentativeDocuments));
        when(asylumCaseBuilder.getRespondentDocuments()).thenReturn(Optional.of(respondentDocuments));
        when(asylumCaseBuilder.getRespondentEvidence()).thenReturn(Optional.of(respondentEvidence));
        when(asylumCaseBuilder.getCaseArgumentDocument()).thenReturn(Optional.of(caseArgumentDocument));
        when(asylumCaseBuilder.getCaseArgumentDescription()).thenReturn(Optional.of(caseArgumentDescription));
        when(asylumCaseBuilder.getCaseArgumentEvidence()).thenReturn(Optional.of(caseArgumentEvidence));
        when(asylumCaseBuilder.getAppealResponseDocument()).thenReturn(Optional.of(appealResponseDocument));
        when(asylumCaseBuilder.getAppealResponseDescription()).thenReturn(Optional.of(appealResponseDescription));
        when(asylumCaseBuilder.getAppealResponseEvidence()).thenReturn(Optional.of(appealResponseEvidence));
        when(asylumCaseBuilder.getLegalRepresentativeName()).thenReturn(Optional.of(legalRepresentativeName));
        when(asylumCaseBuilder.getLegalRepresentativeEmailAddress()).thenReturn(Optional.of(legalRepresentativeEmailAddress));
        when(asylumCaseBuilder.getNotificationsSent()).thenReturn(Optional.of(notificationsSent));
        when(asylumCaseBuilder.getSendDirectionActionAvailable()).thenReturn(Optional.of(sendDirectionActionAvailable));
        when(asylumCaseBuilder.getCaseBuildingReadyForSubmission()).thenReturn(Optional.of(caseBuildingReadyForSubmission));
        when(asylumCaseBuilder.getCurrentCaseStateVisibleToCaseOfficer()).thenReturn(Optional.of(currentCaseStateVisibleToCaseOfficer));
        when(asylumCaseBuilder.getCurrentCaseStateVisibleToLegalRepresentative()).thenReturn(Optional.of(currentCaseStateVisibleToLegalRepresentative));
        when(asylumCaseBuilder.getCaseArgumentAvailable()).thenReturn(Optional.of(caseArgumentAvailable));
        when(asylumCaseBuilder.getAppealResponseAvailable()).thenReturn(Optional.of(appealResponseAvailable));
    }

    @Test
    public void should_hold_onto_values() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        assertEquals(Optional.of(homeOfficeReferenceNumber), asylumCase.getHomeOfficeReferenceNumber());
        assertEquals(Optional.of(homeOfficeDecisionDate), asylumCase.getHomeOfficeDecisionDate());
        assertEquals(Optional.of(appellantTitle), asylumCase.getAppellantTitle());
        assertEquals(Optional.of(appellantGivenNames), asylumCase.getAppellantGivenNames());
        assertEquals(Optional.of(appellantLastName), asylumCase.getAppellantLastName());
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
        assertEquals(Optional.of(sendDirectionExplanation), asylumCase.getSendDirectionExplanation());
        assertEquals(Optional.of(sendDirectionParties), asylumCase.getSendDirectionParties());
        assertEquals(Optional.of(sendDirectionDateDue), asylumCase.getSendDirectionDateDue());
        assertEquals(Optional.of(directions), asylumCase.getDirections());
        assertEquals(Optional.of(legalRepresentativeDocuments), asylumCase.getLegalRepresentativeDocuments());
        assertEquals(Optional.of(respondentDocuments), asylumCase.getRespondentDocuments());
        assertEquals(Optional.of(respondentEvidence), asylumCase.getRespondentEvidence());
        assertEquals(Optional.of(caseArgumentDocument), asylumCase.getCaseArgumentDocument());
        assertEquals(Optional.of(caseArgumentDescription), asylumCase.getCaseArgumentDescription());
        assertEquals(Optional.of(caseArgumentEvidence), asylumCase.getCaseArgumentEvidence());
        assertEquals(Optional.of(appealResponseDocument), asylumCase.getAppealResponseDocument());
        assertEquals(Optional.of(appealResponseDescription), asylumCase.getAppealResponseDescription());
        assertEquals(Optional.of(appealResponseEvidence), asylumCase.getAppealResponseEvidence());
        assertEquals(Optional.of(legalRepresentativeName), asylumCase.getLegalRepresentativeName());
        assertEquals(Optional.of(legalRepresentativeEmailAddress), asylumCase.getLegalRepresentativeEmailAddress());
        assertEquals(Optional.of(notificationsSent), asylumCase.getNotificationsSent());
        assertEquals(Optional.of(sendDirectionActionAvailable), asylumCase.getSendDirectionActionAvailable());
        assertEquals(Optional.of(caseBuildingReadyForSubmission), asylumCase.getCaseBuildingReadyForSubmission());
        assertEquals(Optional.of(currentCaseStateVisibleToCaseOfficer), asylumCase.getCurrentCaseStateVisibleToCaseOfficer());
        assertEquals(Optional.of(currentCaseStateVisibleToLegalRepresentative), asylumCase.getCurrentCaseStateVisibleToLegalRepresentative());
        assertEquals(Optional.of(caseArgumentAvailable), asylumCase.getCaseArgumentAvailable());
        assertEquals(Optional.of(appealResponseAvailable), asylumCase.getAppealResponseAvailable());
    }

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

    @Test
    public void respondent_evidence_is_clearable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.clearRespondentEvidence();
        assertEquals(Optional.empty(), asylumCase.getRespondentEvidence());
    }

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
    public void send_direction_action_available_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setSendDirectionActionAvailable(YesOrNo.NO);
        assertEquals(Optional.of(YesOrNo.NO), asylumCase.getSendDirectionActionAvailable());

        asylumCase.setSendDirectionActionAvailable(null);
        assertEquals(Optional.empty(), asylumCase.getSendDirectionActionAvailable());
    }

    @Test
    public void case_building_ready_for_submission_flag_is_clearable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.clearCaseBuildingReadyForSubmission();
        assertEquals(Optional.empty(), asylumCase.getCaseBuildingReadyForSubmission());
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
}
