package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;

public final class BailCaseUtils {

    private BailCaseUtils() {
    }

    // helper method to check if the feature IMA is enabled
    public static boolean isImaEnabled(BailCase bailCase) {
        return bailCase.read(BailCaseFieldDefinition.IS_IMA_ENABLED, YesOrNo.class).orElse(YesOrNo.NO)
            .equals(YesOrNo.YES);
    }

}
