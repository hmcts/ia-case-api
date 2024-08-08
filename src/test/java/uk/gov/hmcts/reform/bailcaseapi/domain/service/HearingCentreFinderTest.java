package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;

import static java.util.Collections.singletonList;

@ExtendWith(MockitoExtension.class)
class HearingCentreFinderTest {

    private final Map<HearingCentre, List<String>> hearingCentreMappings = ImmutableMap.<HearingCentre,
            List<String>>builder()
        .put(HearingCentre.BIRMINGHAM, Arrays.asList(
            "Ashwell",
            "Aylesbury",
            "Bedford",
            "Birmingham",
            "Blakenhurst",
            "Oakwood"
        )).put(HearingCentre.BRADFORD, Arrays.asList(
            "Acklington",
            "Askham Grange",
            "Castington",
            "Deerbolt",
            "Derwentside",
            "Hatfield",
            "Humber"
        )).put(HearingCentre.GLASGOW, Arrays.asList(
            "Addiewell",
            "Barlinnie",
            "Castle Huntly",
            "Cornton Vale",
            "Larne House",
            "Stirling"
        )).put(HearingCentre.HATTON_CROSS, Arrays.asList(
            "Albany",
            "Brixton",
            "Bronzefield",
            "Camp Hill",
            "Colnbrook",
            "Isle of Wight"
        )).put(HearingCentre.MANCHESTER, Arrays.asList(
            "Altcourse",
            "Buckley Hall",
            "Berwyn",
            "Dovegate",
            "Drake Hall",
            "Forest Bank"
        )).put(HearingCentre.NEWCASTLE, singletonList(
            "Northumberland"
        )).put(HearingCentre.NEWPORT, Arrays.asList(
            "Ashfield",
            "Bristol",
            "Cardiff",
            "Channings Wood",
            "Dartmoor",
            "The Verne"
        )).put(HearingCentre.TAYLOR_HOUSE, Arrays.asList(
            "Belmarsh",
            "Blantyre House",
            "Blundeston",
            "Brookhouse",
            "Bullwood Hall",
            "Bure"
        )).put(HearingCentre.YARLS_WOOD, List.of(
            "Yarlswood"
        ))
        .build();

    private final HearingCentreFinder hearingCentreFinder = new HearingCentreFinder(hearingCentreMappings);

    @Test
    void should_find_birmingham_hearing_centre_with_valid_prison_or_irc() {

        List<String> validInputs = Arrays.asList("Ashwell", "Aylesbury", "Bedford", "Birmingham", "Blakenhurst");

        for (String valid : validInputs) {
            HearingCentre actualHearingCentre = hearingCentreFinder.find(valid);
            assertEquals(HearingCentre.BIRMINGHAM, actualHearingCentre);
        }
    }

    @Test
    void should_find_bradford_hearing_centre_with_valid_prison_or_irc() {

        List<String> validInputs = Arrays.asList(
            "Acklington", "Askham Grange", "Castington", "Deerbolt", "Derwentside"
        );

        for (String valid : validInputs) {
            HearingCentre actualHearingCentre = hearingCentreFinder.find(valid);
            assertEquals(HearingCentre.BRADFORD, actualHearingCentre);
        }
    }

    @Test
    void should_find_glasgow_hearing_centre_with_valid_prison_or_irc() {

        List<String> validInputs = Arrays.asList(
            "Addiewell", "Barlinnie", "Castle Huntly", "Cornton Vale", "Larne House"
        );

        for (String valid : validInputs) {
            HearingCentre actualHearingCentre = hearingCentreFinder.find(valid);
            assertEquals(HearingCentre.GLASGOW, actualHearingCentre);
        }
    }

    @Test
    void should_find_hatton_cross_hearing_centre_with_valid_prison_or_irc() {

        List<String> validInputs = Arrays.asList("Albany", "Brixton", "Bronzefield", "Camp Hill", "Colnbrook");

        for (String valid : validInputs) {
            HearingCentre actualHearingCentre = hearingCentreFinder.find(valid);
            assertEquals(HearingCentre.HATTON_CROSS, actualHearingCentre);
        }
    }

    @Test
    void should_find_manchester_hearing_centre_with_valid_prison_or_irc() {

        List<String> validInputs = Arrays.asList("Altcourse", "Buckley Hall", "Dovegate", "Drake Hall", "Forest Bank");

        for (String valid : validInputs) {
            HearingCentre actualHearingCentre = hearingCentreFinder.find(valid);
            assertEquals(HearingCentre.MANCHESTER, actualHearingCentre);
        }
    }

    @Test
    void should_find_newport_hearing_centre_with_valid_prison_or_irc() {

        List<String> validInputs = Arrays.asList("Ashfield", "Bristol", "Cardiff", "Channings Wood", "Dartmoor");

        for (String valid : validInputs) {
            HearingCentre actualHearingCentre = hearingCentreFinder.find(valid);
            assertEquals(HearingCentre.NEWPORT, actualHearingCentre);
        }
    }

    @Test
    void should_find_taylor_house_hearing_centre_with_valid_prison_or_irc() {

        List<String> validInputs = Arrays.asList(
            "Belmarsh", "Blantyre House", "Blundeston", "Brookhouse", "Bullwood Hall"
        );

        for (String valid : validInputs) {
            HearingCentre actualHearingCentre = hearingCentreFinder.find(valid);
            assertEquals(HearingCentre.TAYLOR_HOUSE, actualHearingCentre);
        }
    }

    @Test
    void should_find_yarlswood_hearing_centre_with_valid_prison_or_irc() {

        String validInput = "Yarlswood";

        HearingCentre actualHearingCentre = hearingCentreFinder.find(validInput);
        assertEquals(HearingCentre.YARLS_WOOD, actualHearingCentre);
    }

    @Test
    void should_throw_for_invalid_prison_or_irc() {

        List<String> invalidInputs = Arrays.asList("Invalid", "", "Standford Hill");

        for (String invalid : invalidInputs) {
            assertThatThrownBy(() -> hearingCentreFinder.find(invalid))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Prison or Immigration Removal Centre is not mapped to a Hearing Centre");

        }
    }

    @Test
    void should_create_object_with_empty_mapping() {

        final Map<HearingCentre, List<String>> emptyHearingCentreMappings = ImmutableMap.<HearingCentre,
                List<String>>builder()
            .build();

        final HearingCentreFinder hearingCentreFinderEmptyMap = new HearingCentreFinder(emptyHearingCentreMappings);

        assertNotNull(hearingCentreFinderEmptyMap);
    }

}
