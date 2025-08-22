package uk.gov.hmcts.reform.iacaseapi.domain.entities;

public final class FeeRemissionType {
    public static final String ASYLUM_SUPPORT = "Asylum support";
    public static final String LEGAL_AID = "Legal Aid";
    public static final String SECTION_17 = "Section 17";
    public static final String SECTION_20 = "Section 20";
    public static final String HO_WAIVER = "Home Office fee waiver";
    public static final String HELP_WITH_FEES = "Help with Fees";
    public static final String EXCEPTIONAL_CIRCUMSTANCES = "Exceptional circumstances";
    public static final String LOCAL_AUTHORITY_SUPPORT = "Local Authority Support";

    private FeeRemissionType() {
        // Prevent instantiation
    }
}