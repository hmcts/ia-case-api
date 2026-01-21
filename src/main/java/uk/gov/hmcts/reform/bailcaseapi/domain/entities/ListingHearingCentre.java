package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum ListingHearingCentre {

    BIRMINGHAM("birmingham", "Birmingham", "231596"),
    BRADFORD("bradford", "Bradford", "698118"),
    COVENTRY("coventry", "Coventry Magistrates Court", "787030"),
    GLASGOW_TRIBUNALS_CENTRE("glasgowTribunalsCentre", "Glasgow", "366559"),
    HATTON_CROSS("hattonCross", "Hatton Cross", "386417"),
    MANCHESTER("manchester", "Manchester", "512401"),
    NEWCASTLE("newcastle", "Newcastle Civil & Family Courts and Tribunals Centre", "366796"),
    NEWPORT("newport", "Newport", "227101"),
    NOTTINGHAM("nottingham", "Nottingham Justice Centre", "618632"),
    TAYLOR_HOUSE("taylorHouse", "Taylor House", "765324"),
    BELFAST("belfast", "Belfast", "999973"),
    HARMONDSWORTH("harmondsworth", "Harmondsworth", "28837"),
    HENDON("hendon", "Hendon", "745389"),
    YARLS_WOOD("yarlsWood", "Yarl's Wood", "649000"),
    BRADFORD_KEIGHLEY("bradfordKeighley", "Bradford & Keighley", "580554"),
    MCC_MINSHULL("mccMinshull", "MCC Minshull st", "326944"),
    MCC_CROWN_SQUARE("mccCrownSquare", "MCC Crown Square", "144641"),
    MANCHESTER_MAGS("manchesterMags", "Manchester Mags", "783803"),
    NTH_TYNE_MAGS("nthTyneMags", "NTH Tyne Mags", "443257"),
    LEEDS_MAGS("leedsMags", "Leeds Mags", "569737"),
    ALLOA_SHERRIF("alloaSherrif", "Alloa Sherrif Court", "999971"),
    REMOTE_HEARING("remoteHearing", "Remote", ""),
    DECISION_WITHOUT_HEARING("decisionWithoutHearing", "Decision without hearing", "");

    @JsonValue
    private final String value;
    private final String label;
    private final String epimsId;

    private static final Map<String, ListingHearingCentre> hearingVenueIdMapping = new HashMap<>();
    public static final Map<String, ListingHearingCentre> epimsIdMapping = new HashMap<>();

    static {
        for (ListingHearingCentre centre : ListingHearingCentre.values()) {
            hearingVenueIdMapping.put(centre.getValue(), centre);
            epimsIdMapping.put(centre.getEpimsId(), centre);
        }
    }


    ListingHearingCentre(String value, String label, String epimsId) {
        this.value = value;
        this.label = label;
        this.epimsId = epimsId;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public String getEpimsId() {
        return epimsId;
    }

    public static Map<String, ListingHearingCentre> getEpimsIdMapping() {
        return epimsIdMapping;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
