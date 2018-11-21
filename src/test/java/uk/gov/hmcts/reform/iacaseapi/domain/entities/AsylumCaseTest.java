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

@RunWith(MockitoJUnitRunner.class)
public class AsylumCaseTest {

    private final Optional<String> homeOfficeReferenceNumber = Optional.of("A");
    private final Optional<String> homeOfficeDecisionDate = Optional.of("B");
    private final Optional<String> appellantTitle = Optional.of("C");
    private final Optional<String> appellantGivenNames = Optional.of("D");
    private final Optional<String> appellantLastName = Optional.of("E");
    private final Optional<String> appellantDateOfBirth = Optional.of("F");
    private final Optional<List<IdValue<Map<String, String>>>> appellantNationalities =
        Optional.of(Arrays.asList(new IdValue<>("1", Collections.emptyMap())));
    private final Optional<String> appellantHasFixedAddress = Optional.of("H");
    private final Optional<AddressUk> appellantAddress = Optional.of(mock(AddressUk.class));
    private final Optional<String> appealType = Optional.of("I");
    private final Optional<String> hasNewMatters = Optional.of("J");
    private final Optional<List<String>> newMatters = Optional.of(Arrays.asList("K"));
    private final Optional<String> hasOtherAppeals = Optional.of("L");
    private final Optional<List<IdValue<String>>> otherAppeals = Optional.of(Arrays.asList(new IdValue<>("1", "M")));
    private final Optional<String> legalRepReferenceNumber = Optional.of("N");

    @Mock AsylumCaseBuilder asylumCaseBuilder;

    @Test
    public void should_hold_onto_values() {

        when(asylumCaseBuilder.getHomeOfficeReferenceNumber()).thenReturn(this.homeOfficeReferenceNumber);
        when(asylumCaseBuilder.getHomeOfficeDecisionDate()).thenReturn(this.homeOfficeDecisionDate);
        when(asylumCaseBuilder.getAppellantTitle()).thenReturn(this.appellantTitle);
        when(asylumCaseBuilder.getAppellantGivenNames()).thenReturn(this.appellantGivenNames);
        when(asylumCaseBuilder.getAppellantLastName()).thenReturn(this.appellantLastName);
        when(asylumCaseBuilder.getAppellantDateOfBirth()).thenReturn(this.appellantDateOfBirth);
        when(asylumCaseBuilder.getAppellantNationalities()).thenReturn(this.appellantNationalities);
        when(asylumCaseBuilder.getAppellantHasFixedAddress()).thenReturn(this.appellantHasFixedAddress);
        when(asylumCaseBuilder.getAppellantAddress()).thenReturn(this.appellantAddress);
        when(asylumCaseBuilder.getAppealType()).thenReturn(this.appealType);
        when(asylumCaseBuilder.getHasNewMatters()).thenReturn(this.hasNewMatters);
        when(asylumCaseBuilder.getNewMatters()).thenReturn(this.newMatters);
        when(asylumCaseBuilder.getHasOtherAppeals()).thenReturn(this.hasOtherAppeals);
        when(asylumCaseBuilder.getOtherAppeals()).thenReturn(this.otherAppeals);
        when(asylumCaseBuilder.getLegalRepReferenceNumber()).thenReturn(this.legalRepReferenceNumber);

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        assertEquals(homeOfficeReferenceNumber, asylumCase.getHomeOfficeReferenceNumber());
        assertEquals(homeOfficeDecisionDate, asylumCase.getHomeOfficeDecisionDate());
        assertEquals(appellantTitle, asylumCase.getAppellantTitle());
        assertEquals(appellantGivenNames, asylumCase.getAppellantGivenNames());
        assertEquals(appellantLastName, asylumCase.getAppellantLastName());
        assertEquals(appellantDateOfBirth, asylumCase.getAppellantDateOfBirth());
        assertEquals(appellantNationalities, asylumCase.getAppellantNationalities());
        assertEquals(appellantHasFixedAddress, asylumCase.getAppellantHasFixedAddress());
        assertEquals(appellantAddress, asylumCase.getAppellantAddress());
        assertEquals(appealType, asylumCase.getAppealType());
        assertEquals(hasNewMatters, asylumCase.getHasNewMatters());
        assertEquals(newMatters, asylumCase.getNewMatters());
        assertEquals(hasOtherAppeals, asylumCase.getHasOtherAppeals());
        assertEquals(otherAppeals, asylumCase.getOtherAppeals());
        assertEquals(legalRepReferenceNumber, asylumCase.getLegalRepReferenceNumber());
    }

    @Test
    public void home_office_reference_number_is_mutable() {

        AsylumCase asylumCase = new AsylumCase(asylumCaseBuilder);

        asylumCase.setHomeOfficeReferenceNumber("HO123");
        assertEquals(Optional.of("HO123"), asylumCase.getHomeOfficeReferenceNumber());

        asylumCase.setHomeOfficeReferenceNumber(Optional.of("HO123"));
        assertEquals(Optional.of("HO123"), asylumCase.getHomeOfficeReferenceNumber());
    }
}
