package uk.gov.hmcts.reform.iacaseapi.infrastructure.utils;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;

public class CaseTypeHelper {
    public static final String ASYLUM = "Asylum";
    public static final String BAIL = "Bail";

    private CaseTypeHelper() {
        // Prevent instantiation
    }

    public static boolean isAsylumCase(CaseData caseData) {
        if (caseData instanceof AsylumCase) {
            return true;
        } else if (caseData instanceof BailCase) {
            return false;
        } else {
            throw new IllegalArgumentException("Unsupported case data type: " + caseData.getClass().getName());
        }
    }
}
