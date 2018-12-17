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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AsylumCaseTest {

    private final String homeOfficeReferenceNumber = "A";
    private final String homeOfficeDecisionDate = "B";
    private final String appellantTitle = "C";
    private final String appellantGivenNames = "D";
    private final String appellantLastName = "E";
    private final String appellantDateOfBirth = "F";
    private final List<IdValue<Map<String, String>>> appellantNationalities = Arrays.asList(new IdValue<>("1", Collections.emptyMap()));
    private final YesOrNo appellantHasFixedAddress = YesOrNo.YES;
    private final AddressUk appellantAddress = mock(AddressUk.class);
    private final String appealType = "I";
    private final CheckValues<String> appealGroundsProtection = mock(CheckValues.class);
    private final CheckValues<String> appealGroundsHumanRights = mock(CheckValues.class);
    private final CheckValues<String> appealGroundsRevocation = mock(CheckValues.class);
    private final YesOrNo hasNewMatters = YesOrNo.YES;
    private final String newMatters = "K";
    private final String hasOtherAppeals = "NotSure";
    private final List<IdValue<Map<String, String>>> otherAppeals = Arrays.asList(new IdValue<>("1", Collections.emptyMap()));
    private final String legalRepReferenceNumber = "N";

    private final YesOrNo sendDirectionActionAvailable = YesOrNo.YES;
    private final String sendDirectionExplanation = "Do the thing";
    private final Parties sendDirectionParties = Parties.LEGAL_REPRESENTATIVE;
    private final String sendDirectionDateDue = "2022-01-01";
    private final List<IdValue<Direction>> directions = Arrays.asList(new IdValue<>("1", mock(Direction.class)));

    private final List<IdValue<DocumentWithMetadata>> respondentDocuments = Arrays.asList(new IdValue<>("1", mock(DocumentWithMetadata.class)));
    private final List<IdValue<DocumentWithDescription>> respondentEvidence = Arrays.asList(new IdValue<>("1", mock(DocumentWithDescription.class)));

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
        when(asylumCaseBuilder.getSendDirectionActionAvailable()).thenReturn(Optional.of(sendDirectionActionAvailable));
        when(asylumCaseBuilder.getSendDirectionExplanation()).thenReturn(Optional.of(sendDirectionExplanation));
        when(asylumCaseBuilder.getSendDirectionParties()).thenReturn(Optional.of(sendDirectionParties));
        when(asylumCaseBuilder.getSendDirectionDateDue()).thenReturn(Optional.of(sendDirectionDateDue));
        when(asylumCaseBuilder.getDirections()).thenReturn(Optional.of(directions));
        when(asylumCaseBuilder.getRespondentDocuments()).thenReturn(Optional.of(respondentDocuments));
        when(asylumCaseBuilder.getRespondentEvidence()).thenReturn(Optional.of(respondentEvidence));
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
        assertEquals(Optional.of(sendDirectionActionAvailable), asylumCase.getSendDirectionActionAvailable());
        assertEquals(Optional.of(sendDirectionExplanation), asylumCase.getSendDirectionExplanation());
        assertEquals(Optional.of(sendDirectionParties), asylumCase.getSendDirectionParties());
        assertEquals(Optional.of(sendDirectionDateDue), asylumCase.getSendDirectionDateDue());
        assertEquals(Optional.of(directions), asylumCase.getDirections());
        assertEquals(Optional.of(respondentDocuments), asylumCase.getRespondentDocuments());
        assertEquals(Optional.of(respondentEvidence), asylumCase.getRespondentEvidence());
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
    public void send_direction_action_available_flag_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setSendDirectionActionAvailable(YesOrNo.YES);
        assertEquals(Optional.of(YesOrNo.YES), asylumCase.getSendDirectionActionAvailable());

        asylumCase.setSendDirectionActionAvailable(null);
        assertEquals(Optional.empty(), asylumCase.getSendDirectionActionAvailable());
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
}
