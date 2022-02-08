package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HearingCentreFinderTest {

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
                Arrays.asList("AL", "BN", "BR", "CB", "CM",
                    "CO", "CR", "CT", "DA", "E",
                    "EC", "EN", "IG", "IP", "ME",
                    "N", "NR", "NW", "RH", "RM",
                    "SE", "SG", "SS", "TN", "W",
                    "WC"
                )
            )
            .put(
                HearingCentre.NORTH_SHIELDS,
                Arrays.asList("CA", "DH", "DL", "NE", "SR",
                    "TS")
            )
            .put(
                HearingCentre.BIRMINGHAM,
                Arrays.asList("B", "CV", "DE", "DY", "GL",
                    "HP", "LE", "LN", "LU", "MK",
                    "NG", "NN", "OX", "PE", "RG",
                    "SY", "TF", "WD", "WR", "WS",
                    "WV")
            )
            .put(
                HearingCentre.HATTON_CROSS,
                Arrays.asList("BH", "GU", "HA", "KT", "PO",
                    "SL", "SM", "SO", "SW", "TW", "UB")
            )
            .put(
                HearingCentre.GLASGOW,
                Arrays.asList("AB", "DD", "DG", "EH", "FK",
                    "G", "HS", "IV", "KA", "KW", "KY",
                    "ML", "PA", "PH", "TD", "ZE")
            )
            .build();

    private final Map<HearingCentre, String> hearingCentreActivationDates =
        ImmutableMap
            .<HearingCentre, String>builder()
            .put(HearingCentre.BRADFORD, "2019-01-01")
            .put(HearingCentre.MANCHESTER, "2019-01-01")
            .put(HearingCentre.NEWPORT, "2019-01-01")
            .put(HearingCentre.TAYLOR_HOUSE, "2019-01-01")
            .put(HearingCentre.NORTH_SHIELDS, "2019-01-01")
            .put(HearingCentre.BIRMINGHAM, "2019-01-01")
            .put(HearingCentre.HATTON_CROSS, "2019-01-01")
            .put(HearingCentre.GLASGOW, "2019-01-01")
            .build();

    private final HearingCentreFinder hearingCentreFinder =
        new HearingCentreFinder(
            defaultHearingCentre,
            hearingCentreCatchmentAreas,
            hearingCentreActivationDates
        );

    @Test
    void should_find_bradford_hearing_centre_for_bradford_postcodes() {

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
    void should_find_manchester_hearing_centre_for_manchester_postcodes() {

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
    void should_find_newport_hearing_centre_for_newport_postcodes() {

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
    void should_find_taylor_house_hearing_centre_for_taylor_house_postcodes() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("AL1 3AA", HearingCentre.TAYLOR_HOUSE)     // St Albans
                .put("BN2 1RF", HearingCentre.TAYLOR_HOUSE)     // Brighton
                .put("BR1 1EZ", HearingCentre.TAYLOR_HOUSE)     // Bromley
                .put("CB1 1LH", HearingCentre.TAYLOR_HOUSE)     // Cambridge
                .put("CM1 1EJ", HearingCentre.TAYLOR_HOUSE)     // Chelmsford
                .put("CO1 1DF", HearingCentre.TAYLOR_HOUSE)     // Colchester
                .put("CR9 1HT", HearingCentre.TAYLOR_HOUSE)     // Croydon
                .put("CT1 2LB", HearingCentre.TAYLOR_HOUSE)     // Canterbury
                .put("DA1 1DT", HearingCentre.TAYLOR_HOUSE)     // Dartford
                .put("E13 8RY", HearingCentre.TAYLOR_HOUSE)     // London
                .put("EC1V 9QN", HearingCentre.TAYLOR_HOUSE)    // London
                .put("EN2 7HW", HearingCentre.TAYLOR_HOUSE)     // Enfield
                .put("IG1 4LZ", HearingCentre.TAYLOR_HOUSE)     // Ilford
                .put("IP4 1JL", HearingCentre.TAYLOR_HOUSE)     // Ipswitch
                .put("ME2 4LA", HearingCentre.TAYLOR_HOUSE)     // Medway
                .put("N3 3HP", HearingCentre.TAYLOR_HOUSE)      // London
                .put("NR3 1AA", HearingCentre.TAYLOR_HOUSE)     // Norwich
                .put("NW1 0LU", HearingCentre.TAYLOR_HOUSE)     // London
                .put("RH1 1BD", HearingCentre.TAYLOR_HOUSE)     // Redhill
                .put("RM1 2LH", HearingCentre.TAYLOR_HOUSE)     // Romford
                .put("SE10 9EQ", HearingCentre.TAYLOR_HOUSE)    // London
                .put("SG2 9XH", HearingCentre.TAYLOR_HOUSE)     // Stevenage
                .put("SS2 9BQ", HearingCentre.TAYLOR_HOUSE)     // Southend-on-Sea
                .put("TN9 1SQ", HearingCentre.TAYLOR_HOUSE)     // Tonbridge
                .put("W1K 4PA", HearingCentre.TAYLOR_HOUSE)     // London
                .put("WC1V 7RL", HearingCentre.TAYLOR_HOUSE)    // London
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
    void should_find_north_shields_hearing_centre_for_north_shields_postcodes() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("CA3 8JY", HearingCentre.NORTH_SHIELDS)      // Carlisle
                .put("DH1 3NJ", HearingCentre.NORTH_SHIELDS)      // Durham
                .put("DL1 2EQ", HearingCentre.NORTH_SHIELDS)      // Darlington
                .put("NE1 7DQ", HearingCentre.NORTH_SHIELDS)      // Newcastle
                .put("SR1 1RR", HearingCentre.NORTH_SHIELDS)      // Sunderland
                .put("TS1 2NR", HearingCentre.NORTH_SHIELDS)      // Cleveland
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
    void should_find_birmingham_hearing_centre_for_birmingham_postcodes() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("B2 4AA", HearingCentre.BIRMINGHAM)       // Birmingham
                .put("CV1 4ET", HearingCentre.BIRMINGHAM)      // Coventry
                .put("DE1 1SD", HearingCentre.BIRMINGHAM)      // Derby
                .put("DY1 1PY", HearingCentre.BIRMINGHAM)      // Dudley
                .put("GL1 4SS", HearingCentre.BIRMINGHAM)      // Gloucester
                .put("HP3 8EW", HearingCentre.BIRMINGHAM)      // Hemel Hempstead
                .put("LE3 9AR", HearingCentre.BIRMINGHAM)      // Leicester
                .put("LN1 3LJ", HearingCentre.BIRMINGHAM)      // Lincoln
                .put("LU1 1EX", HearingCentre.BIRMINGHAM)      // Luton
                .put("MK6 5BZ", HearingCentre.BIRMINGHAM)      // Milton Keynes
                .put("NN1 1AF", HearingCentre.BIRMINGHAM)      // Northampton
                .put("NG7 3HE", HearingCentre.BIRMINGHAM)      // Nottingham
                .put("OX2 6HA", HearingCentre.BIRMINGHAM)      // Oxford
                .put("PE1 1DP", HearingCentre.BIRMINGHAM)      // Peterborough
                .put("RG1 2AD", HearingCentre.BIRMINGHAM)      // Reading
                .put("SY1 2LE", HearingCentre.BIRMINGHAM)      // Shrewsbury
                .put("TF4 2DZ", HearingCentre.BIRMINGHAM)      // Telford
                .put("WD17 2QN", HearingCentre.BIRMINGHAM)     // Watford
                .put("WR1 2EH", HearingCentre.BIRMINGHAM)      // Worcester
                .put("WS1 1NW", HearingCentre.BIRMINGHAM)      // Walsall
                .put("WV1 4HW", HearingCentre.BIRMINGHAM)      // Wolverhampton
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
    void should_find_hatton_cross_hearing_centre_for_hatton_cross_postcodes() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("BH1 1DY", HearingCentre.HATTON_CROSS)      // Bournemouth
                .put("GU1 3ES", HearingCentre.HATTON_CROSS)      // Guildford
                .put("HA3 5QL", HearingCentre.HATTON_CROSS)      // Harrow
                .put("KT1 1BL", HearingCentre.HATTON_CROSS)      // Kingston upon Thames
                .put("PO2 8HS", HearingCentre.HATTON_CROSS)      // Portsmouth
                .put("SL1 1JN", HearingCentre.HATTON_CROSS)      // Slough
                .put("SM1 1LZ", HearingCentre.HATTON_CROSS)      // Sutton
                .put("SO15 2WW", HearingCentre.HATTON_CROSS)     // Southampton
                .put("SW15 6SG", HearingCentre.HATTON_CROSS)     // London
                .put("TW1 3SZ", HearingCentre.HATTON_CROSS)      // Twickenham
                .put("UB1 2LF", HearingCentre.HATTON_CROSS)      // Southall
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
    void should_find_glasgow_hearing_centre_for_glasgow_postcodes() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("AB11 5BB", HearingCentre.GLASGOW)     // Aberdeen
                .put("DD1 4AF", HearingCentre.GLASGOW)      // Dundee
                .put("DG1 2QF", HearingCentre.GLASGOW)      // Dumfries
                .put("EH1 1SX", HearingCentre.GLASGOW)      // Edinburgh
                .put("FK8 2EE", HearingCentre.GLASGOW)      // Stirling
                .put("G1 2RD", HearingCentre.GLASGOW)       // Glasgow
                .put("HS1 2SF", HearingCentre.GLASGOW)      // Hebrides
                .put("IV2 3JT", HearingCentre.GLASGOW)      // Inverness
                .put("KA1 1NP", HearingCentre.GLASGOW)      // Kilmarnock
                .put("KW15 1DD", HearingCentre.GLASGOW)     // Kirkwall
                .put("KY1 1JT", HearingCentre.GLASGOW)      // Kirklady
                .put("ML1 1BN", HearingCentre.GLASGOW)      // Motherwell
                .put("PA15 1UL", HearingCentre.GLASGOW)     // Gateside
                .put("PH1 5TJ", HearingCentre.GLASGOW)      // Perth
                .put("TD1 1BJ", HearingCentre.GLASGOW)      // Galashiels
                .put("ZE1 0EH", HearingCentre.GLASGOW)      // Lerwick
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
    void should_use_default_hearing_centre_if_not_in_any_catchment() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("PP 1AB", defaultHearingCentre)
                .put("TT 1AB", defaultHearingCentre)
                .put("YY 1AB", defaultHearingCentre)
                .put("ZZ 1AB", defaultHearingCentre)
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
    void should_use_default_hearing_centre_if_hearing_centre_not_active() {

        Map<HearingCentre, String> hearingCentreActivationDates =
            ImmutableMap
                .<HearingCentre, String>builder()
                .put(HearingCentre.BRADFORD, "2019-01-01")
                .put(HearingCentre.MANCHESTER, "2019-01-01")
                .put(HearingCentre.NEWPORT, "2019-01-01")
                .put(HearingCentre.TAYLOR_HOUSE, "2019-01-01")
                .put(HearingCentre.NORTH_SHIELDS, LocalDate.now().plusDays(5).toString())
                .put(HearingCentre.BIRMINGHAM, LocalDate.now().plusDays(5).toString())
                .put(HearingCentre.HATTON_CROSS, LocalDate.now().plusDays(5).toString())
                .put(HearingCentre.GLASGOW, LocalDate.now().plusDays(5).toString())
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
                .put("DL1 2EQ", defaultHearingCentre)   // Darlington (North Shields)
                .put("RG1 2AD", defaultHearingCentre)   // Reading (Birmingham)
                .put("PO2 8HS", defaultHearingCentre)   // Portsmouth (Hatton Cross)
                .put("IV2 3JT", defaultHearingCentre)   // Inverness (Glasgow)
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
    void should_return_true_for_past_and_present_activation_dates() {

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
    void should_return_false_for_future_activation_dates() {

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
    void should_return_true_for_hearing_centres_with_past_activation_dates() {

        final Map<HearingCentre, String> hearingCentreActivationDates =
            ImmutableMap
                .<HearingCentre, String>builder()
                .put(HearingCentre.BRADFORD, "2019-01-01")
                .put(HearingCentre.MANCHESTER, "2019-01-01")
                .put(HearingCentre.NEWPORT, "2019-01-01")
                .put(HearingCentre.TAYLOR_HOUSE, "2019-01-01")
                .build();

        HearingCentreFinder hearingCentreFinder =
            new HearingCentreFinder(
                defaultHearingCentre,
                hearingCentreCatchmentAreas,
                hearingCentreActivationDates
            );

        Map<HearingCentre, Boolean> exampleHearingCentreInputOutputs =
            ImmutableMap
                .<HearingCentre, Boolean>builder()
                .put(HearingCentre.BRADFORD, true)
                .put(HearingCentre.MANCHESTER, true)
                .put(HearingCentre.NEWPORT, true)
                .put(HearingCentre.TAYLOR_HOUSE, true)
                .build();

        exampleHearingCentreInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final HearingCentre hearingCentre = inputOutput.getKey();
                final boolean expectedHearingCentreIsActive = inputOutput.getValue();
                final boolean actualHearingCentreIsActive = hearingCentreFinder.hearingCentreIsActive(hearingCentre);

                assertEquals(expectedHearingCentreIsActive, actualHearingCentreIsActive);
            });
    }


    @Test
    void should_return_true_or_false_when_checking_for_listing_only_hearing_centres() {

        Set<HearingCentre> allHearingCentres = EnumSet.allOf(HearingCentre.class);

        List<HearingCentre> listingOnlyHearingCentres = Arrays.asList(
            HearingCentre.COVENTRY,
            HearingCentre.GLASGOW_TRIBUNALS_CENTRE,
            HearingCentre.NEWCASTLE,
            HearingCentre.BELFAST,
            HearingCentre.NOTTINGHAM);

        allHearingCentres.forEach(
            hearingCentre -> {
                if (listingOnlyHearingCentres.contains(hearingCentre)) {
                    assertTrue(hearingCentreFinder.isListingOnlyHearingCentre(hearingCentre));
                } else {
                    assertFalse(hearingCentreFinder.isListingOnlyHearingCentre(hearingCentre));
                }
            }
        );
    }


    @Test
    void should_return_false_for_hearing_centres_with_future_activation_dates() {

        final Map<HearingCentre, String> hearingCentreActivationDates =
            ImmutableMap
                .<HearingCentre, String>builder()
                .put(HearingCentre.NORTH_SHIELDS, LocalDate.now().plusDays(5).toString())
                .put(HearingCentre.BIRMINGHAM, LocalDate.now().plusDays(5).toString())
                .put(HearingCentre.HATTON_CROSS, LocalDate.now().plusDays(5).toString())
                .put(HearingCentre.GLASGOW, LocalDate.now().plusDays(5).toString())
                .build();

        HearingCentreFinder hearingCentreFinder =
            new HearingCentreFinder(
                defaultHearingCentre,
                hearingCentreCatchmentAreas,
                hearingCentreActivationDates
            );

        Map<HearingCentre, Boolean> exampleHearingCentreInputOutputs =
            ImmutableMap
                .<HearingCentre, Boolean>builder()
                .put(HearingCentre.NORTH_SHIELDS, false)
                .put(HearingCentre.BIRMINGHAM, false)
                .put(HearingCentre.HATTON_CROSS, false)
                .put(HearingCentre.GLASGOW, false)
                .build();

        exampleHearingCentreInputOutputs
            .entrySet()
            .forEach(inputOutput -> {

                final HearingCentre hearingCentre = inputOutput.getKey();
                final boolean expectedHearingCentreIsActive = inputOutput.getValue();
                final boolean actualHearingCentreIsActive = hearingCentreFinder.hearingCentreIsActive(hearingCentre);

                assertEquals(expectedHearingCentreIsActive, actualHearingCentreIsActive);
            });
    }

    @Test
    void should_throw_for_invalid_activation_dates() {

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
    void should_return_default_hearing_centre() {

        assertEquals(defaultHearingCentre, hearingCentreFinder.getDefaultHearingCentre());
    }
}
