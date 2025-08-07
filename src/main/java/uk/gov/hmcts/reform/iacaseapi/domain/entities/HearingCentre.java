package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public enum HearingCentre {

    BIRMINGHAM("birmingham", "231596"),
    BRADFORD("bradford", "698118"),
    COVENTRY("coventry", "787030"),
    GLASGOW_TRIBUNALS_CENTRE("glasgowTribunalsCentre", "366559"),
    GLASGOW("glasgow", "366559"),
    HATTON_CROSS("hattonCross", "386417"),
    MANCHESTER("manchester", "512401"),
    NEWCASTLE("newcastle", "366796"),
    NEWPORT("newport", "227101"),
    NORTH_SHIELDS("northShields", ""),
    NOTTINGHAM("nottingham", "618632"),
    TAYLOR_HOUSE("taylorHouse", "765324"),
    BELFAST("belfast", "999973"),
    HARMONDSWORTH("harmondsworth", "28837"),
    HENDON("hendon", "745389"),
    YARLS_WOOD("yarlsWood", "649000"),
    BRADFORD_KEIGHLEY("bradfordKeighley", "580554"),
    MCC_MINSHULL("mccMinshull", "326944"),
    MCC_CROWN_SQUARE("mccCrownSquare", "144641"),
    MANCHESTER_MAGS("manchesterMags", "783803"),
    NTH_TYNE_MAGS("nthTyneMags", "443257"),
    LEEDS_MAGS("leedsMags", "569737"),
    ALLOA_SHERRIF("alloaSherrif", "999971"),
    ARNHEM_HOUSE("arnhemHouse", "324339"),
    CROWN_HOUSE("crownHouse", "420587"),
    IAC_NATIONAL_VIRTUAL("iacNationalVirtual", "999970"),
    REMOTE_HEARING("remoteHearing", ""),
    DECISION_WITHOUT_HEARING("decisionWithoutHearing", "");

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

    public static Optional<HearingCentre> fromEpimsId(String epimsId, boolean isListCaseHearingCentre) {
        if (Objects.equals(GLASGOW.epimsId, epimsId)) {
            if (isListCaseHearingCentre) {
                return Optional.of(GLASGOW_TRIBUNALS_CENTRE);
            } else {
                return Optional.of(GLASGOW);
            }
        }

        return epimsIdMapping.containsKey(epimsId)
            ? Optional.of(epimsIdMapping.get(epimsId))
            : Optional.empty();
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

    @Override
    public String toString() {
        return getValue();
    }
}
