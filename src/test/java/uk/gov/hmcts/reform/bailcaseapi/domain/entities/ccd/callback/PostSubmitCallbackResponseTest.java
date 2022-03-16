package uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class PostSubmitCallbackResponseTest {

    PostSubmitCallbackResponse respose = new PostSubmitCallbackResponse();

    @Test
    void should_hold_values() {
        assertFalse(respose.getConfirmationBody().isPresent());
        assertFalse(respose.getConfirmationHeader().isPresent());

        String expectedHeader = "Expected Header";
        String expectedBody = "Expected Body";

        respose.setConfirmationBody(expectedBody);
        respose.setConfirmationHeader(expectedHeader);

        assertEquals(Optional.of(expectedBody), respose.getConfirmationBody());
        assertEquals(Optional.of(expectedHeader), respose.getConfirmationHeader());
    }

    @Test
    void should_convert_null_to_empty_optional() {
        assertFalse(respose.getConfirmationHeader().isPresent());
        assertFalse(respose.getConfirmationBody().isPresent());

        respose.setConfirmationHeader(null);
        respose.setConfirmationBody(null);

        assertEquals(Optional.empty(), respose.getConfirmationHeader());
        assertEquals(Optional.empty(), respose.getConfirmationBody());
    }
}
