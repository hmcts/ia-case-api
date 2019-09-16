package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class HearingCentreFinderTest {

    private final HearingCentre defaultHearingCentre = HearingCentre.TAYLOR_HOUSE;

    private final Map<HearingCentre, List<String>> hearingCentreCatchmentAreas =
        ImmutableMap
            .<HearingCentre, List<String>>builder()
            .put(
                HearingCentre.BRADFORD,
                Arrays.asList("BD", "DN", "HD", "HG", "HU",
                    "HX", "LS", "S", "WF", "YO")
            )
            .put(
                HearingCentre.MANCHESTER,
                Arrays.asList("BB", "BL", "CH", "CW", "FY",
                    "L", "LA", "LL", "M", "OL",
                    "PR", "SK", "ST", "WA", "WN")
            )
            .put(
                HearingCentre.NEWPORT,
                Arrays.asList("BA", "BS", "CF", "DT", "EX",
                    "HR", "LD", "NP", "PL", "SA",
                    "SN", "SP", "TA", "TQ", "TR")
            )
            .put(
                HearingCentre.TAYLOR_HOUSE,
                Arrays.asList("AL", "BR", "CO", "CR", "CT",
                    "DA", "E", "EC", "EN", "IG",
                    "NW", "RM", "SG", "SS", "TN",
                    "W", "WC", "X", "XB")
            )
            .build();

    private final Map<HearingCentre, String> hearingCentreActivationDates =
        ImmutableMap
            .<HearingCentre, String>builder()
            .put(HearingCentre.BRADFORD, "2019-01-01")
            .put(HearingCentre.MANCHESTER, "2019-01-01")
            .put(HearingCentre.NEWPORT, "2019-01-01")
            .put(HearingCentre.TAYLOR_HOUSE, "2019-01-01")
            .build();

    private final HearingCentreFinder hearingCentreFinder =
        new HearingCentreFinder(
            defaultHearingCentre,
            hearingCentreCatchmentAreas,
            hearingCentreActivationDates
        );

    @Test
    public void should_find_phoenix_house_hearing_centre_for_bradford_postcodes() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("BD1 2AB", HearingCentre.BRADFORD)     // Bradford
                .put("DN1 2QA", HearingCentre.BRADFORD)     // Doncaster
                .put("HD1 2BQ", HearingCentre.BRADFORD)     // Huddersfield
                .put("HG1 3BG", HearingCentre.BRADFORD)     // Harrogate
                .put("HU8 8HP", HearingCentre.BRADFORD)     // Hull
                .put("HX1 1PB", HearingCentre.BRADFORD)     // Halifax
                .put("LS2 8LP", HearingCentre.BRADFORD)     // Leeds
                .put("S1 2HU", HearingCentre.BRADFORD)      // Sheffield
                .put("WF2 9PY", HearingCentre.BRADFORD)     // Wakefield
                .put("YO1 9QL", HearingCentre.BRADFORD)     // York
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String postcode = inputOutput.getKey();
                final HearingCentre expectedHearingCentre = inputOutput.getValue();

                HearingCentre actualHearingCentre = hearingCentreFinder.find(postcode);

                assertEquals(expectedHearingCentre, actualHearingCentre);
            });
    }

    @Test
    public void should_find_manchester_hearing_centre_for_manchester_postcodes() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("BB1 6AT", HearingCentre.MANCHESTER)   // Blackburn
                .put("BL3 6AB", HearingCentre.MANCHESTER)   // Bolton
                .put("CH1 1HH", HearingCentre.MANCHESTER)   // Chester
                .put("CW1 3HR", HearingCentre.MANCHESTER)   // Crewe
                .put("FY3 9AA", HearingCentre.MANCHESTER)   // Blackpool
                .put("L3 6LG", HearingCentre.MANCHESTER)    // Liverpool
                .put("LA1 1HZ", HearingCentre.MANCHESTER)   // Lancaster
                .put("LL30 2NY", HearingCentre.MANCHESTER)  // Llandudno
                .put("M4 4AA", HearingCentre.MANCHESTER)    // Manchester
                .put("OL1 3HP", HearingCentre.MANCHESTER)   // Oldham
                .put("PR1 1TS", HearingCentre.MANCHESTER)   // Preston
                .put("SK2 5TL", HearingCentre.MANCHESTER)   // Stockport
                .put("ST4 1AA", HearingCentre.MANCHESTER)   // Stoke-on-Trent
                .put("WA5 1PH", HearingCentre.MANCHESTER)   // Warrington
                .put("WN1 2LF", HearingCentre.MANCHESTER)   // Wigan
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String postcode = inputOutput.getKey();
                final HearingCentre expectedHearingCentre = inputOutput.getValue();

                HearingCentre actualHearingCentre = hearingCentreFinder.find(postcode);

                assertEquals(expectedHearingCentre, actualHearingCentre);
            });
    }

    @Test
    public void should_find_columbus_house_hearing_centre_for_newport_postcodes() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("BA1 1RT", HearingCentre.NEWPORT)      // Bath
                .put("BS5 ORB", HearingCentre.NEWPORT)      // Bristol
                .put("CF10 2NX", HearingCentre.NEWPORT)     // Cardiff
                .put("DT1 2LN", HearingCentre.NEWPORT)      // Dorchester
                .put("EX1 1GJ", HearingCentre.NEWPORT)      // Exeter
                .put("HR1 2AB", HearingCentre.NEWPORT)      // Hereford
                .put("LD1 5BH", HearingCentre.NEWPORT)      // Llandrindod Wells
                .put("NP20 4GA", HearingCentre.NEWPORT)     // Newport
                .put("PL1 1RP", HearingCentre.NEWPORT)      // Plymouth
                .put("SA1 6JZ", HearingCentre.NEWPORT)      // Swansea
                .put("SN1 3BL", HearingCentre.NEWPORT)      // Swindon
                .put("SP1 2PF", HearingCentre.NEWPORT)      // Salisbury
                .put("TA1 1ND", HearingCentre.NEWPORT)      // Taunton
                .put("TQ1 1EB", HearingCentre.NEWPORT)      // Torquay
                .put("TR1 2AX", HearingCentre.NEWPORT)      // Truro
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String postcode = inputOutput.getKey();
                final HearingCentre expectedHearingCentre = inputOutput.getValue();

                HearingCentre actualHearingCentre = hearingCentreFinder.find(postcode);

                assertEquals(expectedHearingCentre, actualHearingCentre);
            });
    }

    @Test
    public void should_find_taylor_house_hearing_centre_for_taylor_house_postcodes() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("SG1 1EA", HearingCentre.TAYLOR_HOUSE)
                .put("X1 2ZZ", HearingCentre.TAYLOR_HOUSE)
                .put("XB1 2ZZ", HearingCentre.TAYLOR_HOUSE)
                .put("AL1 3AA", HearingCentre.TAYLOR_HOUSE)     // St Albans
                .put("BN2 1RF", HearingCentre.TAYLOR_HOUSE)     // Brighton
                .put("BR1 1EZ", HearingCentre.TAYLOR_HOUSE)     // Bromley
                .put("CB1 1LH", HearingCentre.TAYLOR_HOUSE)     // Cambridge
                .put("CM1 1EJ", HearingCentre.TAYLOR_HOUSE)     // Chelmsford
                .put("CO1 1DF", HearingCentre.TAYLOR_HOUSE)     // Colchester
                .put("CR9 1HT", HearingCentre.TAYLOR_HOUSE)     // Croydon
                .put("CT1 2LB", HearingCentre.TAYLOR_HOUSE)     // Canterbury
                .put("DA1 1DT", HearingCentre.TAYLOR_HOUSE)     // Dartford
                .put("EN2 7HW", HearingCentre.TAYLOR_HOUSE)     // Enfield
                .put("IG1 4LZ", HearingCentre.TAYLOR_HOUSE)     // Ilford
                .put("IP4 1JL", HearingCentre.TAYLOR_HOUSE)     // Ipswitch
                .put("E13 8RY", HearingCentre.TAYLOR_HOUSE)     // London
                .put("EC1V 9QN", HearingCentre.TAYLOR_HOUSE)    // London
                .put("N3 3HP", HearingCentre.TAYLOR_HOUSE)      // London
                .put("SE10 9EQ", HearingCentre.TAYLOR_HOUSE)    // London
                .put("W1K 4PA", HearingCentre.TAYLOR_HOUSE)     // London
                .put("WC1V 7RL", HearingCentre.TAYLOR_HOUSE)    // London
                .put("NW1 0LU", HearingCentre.TAYLOR_HOUSE)     // London
                .put("ME2 4LA", HearingCentre.TAYLOR_HOUSE)     // Medway
                .put("NR3 1AA", HearingCentre.TAYLOR_HOUSE)     // Norwich
                .put("RH1 1BD", HearingCentre.TAYLOR_HOUSE)     // Redhill
                .put("RM1 2LH", HearingCentre.TAYLOR_HOUSE)     // Romford
                .put("SG2 9XH", HearingCentre.TAYLOR_HOUSE)     // Stevenage
                .put("SS2 9BQ", HearingCentre.TAYLOR_HOUSE)     // Southend-on-Sea
                .put("TN9 1SQ", HearingCentre.TAYLOR_HOUSE)     // Tonbridge
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String postcode = inputOutput.getKey();
                final HearingCentre expectedHearingCentre = inputOutput.getValue();

                HearingCentre actualHearingCentre = hearingCentreFinder.find(postcode);

                assertEquals(expectedHearingCentre, actualHearingCentre);
            });
    }

    @Test
    public void should_use_default_hearing_centre_if_not_in_any_catchment() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("A123 4ZZ", defaultHearingCentre)
                .put("W1 2AB", defaultHearingCentre)
                .put("YY", defaultHearingCentre)
                .put("SW15 6SG", defaultHearingCentre)
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String postcode = inputOutput.getKey();
                final HearingCentre expectedHearingCentre = inputOutput.getValue();

                HearingCentre actualHearingCentre = hearingCentreFinder.find(postcode);

                assertEquals(expectedHearingCentre, actualHearingCentre);
            });
    }

    @Test
    public void should_use_default_hearing_centre_if_hearing_centre_not_active() {

        Map<HearingCentre, String> hearingCentreActivationDates =
            ImmutableMap
                .<HearingCentre, String>builder()
                .put(HearingCentre.BRADFORD, LocalDate.now().plusDays(5).toString())
                .put(HearingCentre.MANCHESTER, "2019-01-01")
                .put(HearingCentre.NEWPORT, LocalDate.now().plusDays(5).toString())
                .put(HearingCentre.TAYLOR_HOUSE, "2019-01-01")
                .build();

        HearingCentreFinder hearingCentreFinder =
            new HearingCentreFinder(
                defaultHearingCentre,
                hearingCentreCatchmentAreas,
                hearingCentreActivationDates
            );

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("BA1 1RT", defaultHearingCentre)   // Bath (Newport)
                .put("BS5 ORB", defaultHearingCentre)   // Bristol (Newport)
                .put("DN1 2QA", defaultHearingCentre)   // Doncaster (Bradford)
                .put("HD1 2BQ", defaultHearingCentre)   // Huddersfield (Bradford)
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String postcode = inputOutput.getKey();
                final HearingCentre expectedHearingCentre = inputOutput.getValue();

                HearingCentre actualHearingCentre = hearingCentreFinder.find(postcode);

                assertEquals(expectedHearingCentre, actualHearingCentre);
            });
    }

    @Test
    public void should_return_true_for_past_and_present_activation_dates() {

        Map<LocalDate, Boolean> exampleInputOutputs =
            ImmutableMap
                .<LocalDate, Boolean>builder()
                .put(LocalDate.now().minusYears(5), true)
                .put(LocalDate.now().minusMonths(5), true)
                .put(LocalDate.now().minusDays(5), true)
                .put(LocalDate.now(), true)
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String activationDate = inputOutput.getKey().toString();
                final boolean expectedHearingCentreIsActive = inputOutput.getValue();
                final boolean actualHearingCentreIsActive = hearingCentreFinder.hearingCentreIsActive(activationDate);

                assertEquals(expectedHearingCentreIsActive, actualHearingCentreIsActive);
            });
    }

    @Test
    public void should_return_false_for_future_activation_dates() {

        Map<LocalDate, Boolean> exampleInputOutputs =
            ImmutableMap
                .<LocalDate, Boolean>builder()
                .put(LocalDate.now().plusYears(5), false)
                .put(LocalDate.now().plusMonths(5), false)
                .put(LocalDate.now().plusDays(5), false)
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String activationDate = inputOutput.getKey().toString();
                final boolean expectedHearingCentreIsActive = inputOutput.getValue();
                final boolean actualHearingCentreIsActive = hearingCentreFinder.hearingCentreIsActive(activationDate);

                assertEquals(expectedHearingCentreIsActive, actualHearingCentreIsActive);
            });
    }

    @Test
    public void should_throw_for_invalid_activation_dates() {

        Map<Integer, String> exampleInputOutputs =
            ImmutableMap
                .<Integer, String>builder()
                .put(1, "2019-12-xx")
                .put(2, "2019-xx-12")
                .put(3, "xxxx-12-12")
                .build();

        exampleInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final String activationDate = inputOutput.getValue();

                assertThatThrownBy(() -> hearingCentreFinder.hearingCentreIsActive(activationDate))
                    .isExactlyInstanceOf(DateTimeParseException.class);
            });
    }

    @Test
    public void should_return_default_hearing_centre() {

        assertEquals(defaultHearingCentre, hearingCentreFinder.getDefaultHearingCentre());
    }
}
