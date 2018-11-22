package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PostSubmitCallbackResponseTest {

    private PostSubmitCallbackResponse postSubmitCallbackResponse =
        new PostSubmitCallbackResponse();

    @Test
    public void should_store_confirmation() {

        assertFalse(postSubmitCallbackResponse.getConfirmationHeader().isPresent());
        assertFalse(postSubmitCallbackResponse.getConfirmationBody().isPresent());

        String expectedConfirmationHeader = "header";
        String expectedConfirmationBody = "body";

        postSubmitCallbackResponse.setConfirmationHeader(expectedConfirmationHeader);
        postSubmitCallbackResponse.setConfirmationBody(expectedConfirmationBody);

        assertEquals(Optional.of(expectedConfirmationHeader), postSubmitCallbackResponse.getConfirmationHeader());
        assertEquals(Optional.of(expectedConfirmationBody), postSubmitCallbackResponse.getConfirmationBody());
    }

    @Test
    public void should_convert_null_values_to_empty_optional() {

        assertFalse(postSubmitCallbackResponse.getConfirmationHeader().isPresent());
        assertFalse(postSubmitCallbackResponse.getConfirmationBody().isPresent());

        postSubmitCallbackResponse.setConfirmationHeader(null);
        postSubmitCallbackResponse.setConfirmationBody(null);

        assertFalse(postSubmitCallbackResponse.getConfirmationHeader().isPresent());
        assertFalse(postSubmitCallbackResponse.getConfirmationBody().isPresent());
    }
}
