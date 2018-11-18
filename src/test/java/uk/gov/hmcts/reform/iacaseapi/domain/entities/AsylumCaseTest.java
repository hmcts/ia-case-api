package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
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
    private final YesOrNo hasNewMatters = YesOrNo.YES;
    private final String newMatters = "K";
    private final String hasOtherAppeals = "NotSure";
    private final List<IdValue<Map<String, String>>> otherAppeals = Arrays.asList(new IdValue<>("1", Collections.emptyMap()));
    private final String legalRepReferenceNumber = "N";

    private final YesOrNo sendDirectionActionAvailable = YesOrNo.YES;
    private final String sendDirectionExplanation = "Do the thing";
    private final Parties sendDirectionParties = Parties.LEGAL_REPRESENTATIVE;
    private final String sendDirectionDateDue = "2022-01-01 00:00:00";
    private final List<IdValue<Direction>> directions = Arrays.asList(new IdValue<>("1", mock(Direction.class)));

    @Mock AsylumCaseBuilder asylumCaseBuilder;

    @Test
    public void should_hold_onto_values() {

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
    public void directions_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        List<IdValue<Direction>> newDirections = Arrays.asList(new IdValue<>("ABC", mock(Direction.class)));

        asylumCase.setDirections(newDirections);
        assertEquals(Optional.of(newDirections), asylumCase.getDirections());

        asylumCase.setDirections(Collections.emptyList());
        assertEquals(Optional.of(Collections.emptyList()), asylumCase.getDirections());

        asylumCase.setDirections(null);
        assertEquals(Optional.empty(), asylumCase.getDirections());
    }
}
