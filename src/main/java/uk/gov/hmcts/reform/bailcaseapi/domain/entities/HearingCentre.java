package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum HearingCentre {


    BIRMINGHAM("birmingham", "231596"),
    BRADFORD("bradford", "698118"),
    GLASGOW("glasgow", "366559"),
    HATTON_CROSS("hattonCross", "386417"),
    MANCHESTER("manchester", "512401"),
    NEWCASTLE("newcastle", "366796"),
    NEWPORT("newport", "227101"),
    TAYLOR_HOUSE("taylorHouse", "765324"),
    YARLS_WOOD("yarlsWood", "649000");

    @JsonValue
    private final String value;
    private final String epimsId;

    private static final Map<String, HearingCentre> hearingVenueIdMapping = new HashMap<>();
    public static final Map<String, HearingCentre> epimsIdMapping = new HashMap<>();

    static {
        for (HearingCentre centre : HearingCentre.values()) {
            hearingVenueIdMapping.put(centre.getValue(), centre);
            epimsIdMapping.put(centre.getEpimsId(), centre);
        }
    }

    HearingCentre(String value, String epimsId) {
        this.value = value;
        this.epimsId = epimsId;
    }

    public static Optional<HearingCentre> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }

    public String getEpimsId() {
        return epimsId;
    }

    public static String getEpimsIdByValue(String value) {
        return hearingVenueIdMapping.get(value).getEpimsId();
    }

    public static String getValueByEpimsId(String epimsId) {
        return epimsIdMapping.get(epimsId).getValue();
    }

    @Override
    public String toString() {
        return getValue();
    }

}
