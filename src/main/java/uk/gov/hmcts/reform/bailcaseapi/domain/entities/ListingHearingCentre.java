package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ListingHearingCentre {

    BIRMINGHAM("birmingham", "Birmingham"),
    BRADFORD("bradford", "Bradford"),
    COVENTRY("coventry", "Coventry Magistrates Court"),
    GLASGOW_TRIBUNALS_CENTRE("glasgowTribunalsCentre", "Glasgow"),
    HATTON_CROSS("hattonCross", "Hatton Cross"),
    MANCHESTER("manchester", "Manchester"),
    NEWCASTLE("newcastle", "Newcastle Civil & Family Courts and Tribunals Centre"),
    NEWPORT("newport", "Newport"),
    NOTTINGHAM("nottingham", "Nottingham Justice Centre"),
    TAYLOR_HOUSE("taylorHouse", "Taylor House"),
    BELFAST("belfast", "Belfast"),
    HARMONDSWORTH("harmondsworth", "Harmondsworth"),
    HENDON("hendon", "Hendon"),
    YARLS_WOOD("yarlsWood", "Yarl's Wood"),
    BRADFORD_KEIGHLEY("bradfordKeighley", "Bradford & Keighley"),
    MCC_MINSHULL("mccMinshull", "MCC Minshull st"),
    MCC_CROWN_SQUARE("mccCrownSquare", "MCC Crown Square"),
    MANCHESTER_MAGS("manchesterMags", "Manchester Mags"),
    NTH_TYNE_MAGS("nthTyneMags", "NTH Tyne Mags"),
    LEEDS_MAGS("leedsMags", "Leeds Mags"),
    ALLOA_SHERRIF("alloaSherrif", "Alloa Sherrif Court"),
    REMOTE_HEARING("remoteHearing", "Remote"),
    DECISION_WITHOUT_HEARING("decisionWithoutHearing", "Decision without hearing");

    @JsonValue
    private final String value;
    private final String label;


    ListingHearingCentre(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
