package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
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
                HearingCentre.MANCHESTER,
                Arrays.asList("AB", "MAN", "XA", "MM")
            )
            .put(
                HearingCentre.TAYLOR_HOUSE,
                Arrays.asList("X", "XB")
            )
            .build();

    private final HearingCentreFinder hearingCentreFinder =
        new HearingCentreFinder(
            defaultHearingCentre,
            hearingCentreCatchmentAreas
        );

    @Test
    public void should_find_hearing_centre_from_postcode() {

        Map<String, HearingCentre> exampleInputOutputs =
            ImmutableMap
                .<String, HearingCentre>builder()
                .put("AB4 1XB", HearingCentre.MANCHESTER)
                .put("MAN3 4ZZ", HearingCentre.MANCHESTER)
                .put("MM", HearingCentre.MANCHESTER)
                .put("XA1 2ZZ", HearingCentre.MANCHESTER)
                .put("X1 2ZZ", HearingCentre.TAYLOR_HOUSE)
                .put("XB1 2ZZ", HearingCentre.TAYLOR_HOUSE)
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
    public void should_return_default_hearing_centre() {

        assertEquals(defaultHearingCentre, hearingCentreFinder.getDefaultHearingCentre());
    }
}
