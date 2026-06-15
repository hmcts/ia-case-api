package uk.gov.hmcts.reform.bailcaseapi.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

class PreSubmitCallbackHandlerTest implements PreSubmitCallbackHandler {

    @Test
    void default_dispatch_priority_is_late() {
        assertEquals(DispatchPriority.LATE, this.getDispatchPriority());
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback callback) {
        return false;
    }

    public PreSubmitCallbackResponse handle(PreSubmitCallbackStage callbackStage, Callback callback) {
        return null;
    }
}
