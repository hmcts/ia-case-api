package uk.gov.hmcts.reform.bailcaseapi.infrastructure.controllers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.bailcaseapi.controllers.PreSubmitCallbackController;

public class PreSubmitCallbackControllerTest {

    @Test
    void should_fail_for_null_constructor_args() {
        assertThatThrownBy(() -> new PreSubmitCallbackController(null))
            .hasMessage("callbackDispatcher can not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
