package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@SuppressWarnings("unchecked")
public class AsylumCaseBuilderTest {

    private final String homeOfficeReferenceNumber = "A";
    private final String homeOfficeDecisionDate = "B";
    private final String appellantTitle = "C";
    private final String appellantGivenNames = "D";
    private final String appellantLastName = "E";
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

    private final YesOrNo sendDirectionActionAvailable = YesOrNo.YES;
    private final String sendDirectionExplanation = "Do the thing";
    private final Parties sendDirectionParties = Parties.LEGAL_REPRESENTATIVE;
    private final String sendDirectionDateDue = "2022-01-01 00:00:00";
    private final List<IdValue<Direction>> directions = mock(List.class);

    private final List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments = mock(List.class);
    private final List<IdValue<DocumentWithMetadata>> respondentDocuments = mock(List.class);

    private final List<IdValue<DocumentWithDescription>> respondentEvidence = mock(List.class);

    private final Document caseArgumentDocument = mock(Document.class);
    private final String caseArgumentDescription = "O";
    private final List<IdValue<DocumentWithDescription>> caseArgumentEvidence = mock(List.class);

    private final Document appealResponseDocument = mock(Document.class);
    private final String appealResponseDescription = "P";
    private final List<IdValue<DocumentWithDescription>> appealResponseEvidence = mock(List.class);

    private AsylumCaseBuilder asylumCaseBuilder = new AsylumCaseBuilder();

    @Test
    public void should_build_asylum_case() {

        asylumCaseBuilder.setHomeOfficeReferenceNumber(Optional.of(homeOfficeReferenceNumber));
        asylumCaseBuilder.setHomeOfficeDecisionDate(Optional.of(homeOfficeDecisionDate));
        asylumCaseBuilder.setAppellantTitle(Optional.of(appellantTitle));
        asylumCaseBuilder.setAppellantGivenNames(Optional.of(appellantGivenNames));
        asylumCaseBuilder.setAppellantLastName(Optional.of(appellantLastName));
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
        asylumCaseBuilder.setSendDirectionActionAvailable(Optional.of(sendDirectionActionAvailable));
        asylumCaseBuilder.setSendDirectionExplanation(Optional.of(sendDirectionExplanation));
        asylumCaseBuilder.setSendDirectionParties(Optional.of(sendDirectionParties));
        asylumCaseBuilder.setSendDirectionDateDue(Optional.of(sendDirectionDateDue));
        asylumCaseBuilder.setDirections(Optional.of(directions));
        asylumCaseBuilder.setLegalRepresentativeDocuments(Optional.of(legalRepresentativeDocuments));
        asylumCaseBuilder.setRespondentDocuments(Optional.of(respondentDocuments));
        asylumCaseBuilder.setRespondentEvidence(Optional.of(respondentEvidence));
        asylumCaseBuilder.setCaseArgumentDocument(Optional.of(caseArgumentDocument));
        asylumCaseBuilder.setCaseArgumentDescription(Optional.of(caseArgumentDescription));
        asylumCaseBuilder.setCaseArgumentEvidence(Optional.of(caseArgumentEvidence));

        AsylumCase asylumCase = asylumCaseBuilder.build();

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
        assertEquals(Optional.of(legalRepresentativeDocuments), asylumCase.getLegalRepresentativeDocuments());
        assertEquals(Optional.of(respondentDocuments), asylumCase.getRespondentDocuments());
        assertEquals(Optional.of(respondentEvidence), asylumCase.getRespondentEvidence());
        assertEquals(Optional.of(caseArgumentDocument), asylumCase.getCaseArgumentDocument());
        assertEquals(Optional.of(caseArgumentDescription), asylumCase.getCaseArgumentDescription());
        assertEquals(Optional.of(caseArgumentEvidence), asylumCase.getCaseArgumentEvidence());
    }
}
