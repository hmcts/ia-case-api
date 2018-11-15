package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.*;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

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

    private AsylumCase asylumCase = new AsylumCase(
        homeOfficeReferenceNumber,
        homeOfficeDecisionDate,
        appellantTitle,
        appellantGivenNames,
        appellantLastName,
        appellantDateOfBirth,
        appellantNationalities,
        appellantHasFixedAddress,
        appellantAddress,
        appealType,
        hasNewMatters,
        newMatters,
        hasOtherAppeals,
        otherAppeals,
        legalRepReferenceNumber
    );

    @Test
    public void should_hold_onto_values() {

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

        assertEquals(homeOfficeReferenceNumber, asylumCase.getHomeOfficeReferenceNumber());

        asylumCase.setHomeOfficeReferenceNumber("HO123");
        assertEquals(Optional.of("HO123"), asylumCase.getHomeOfficeReferenceNumber());

        asylumCase.setHomeOfficeReferenceNumber(Optional.of("HO123"));
        assertEquals(Optional.of("HO123"), asylumCase.getHomeOfficeReferenceNumber());
    }
}
