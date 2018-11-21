package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;

@RunWith(MockitoJUnitRunner.class)
public class PreSubmitCallbackResponseTest {

    @Mock private CaseData caseData;

    private PreSubmitCallbackResponse<CaseData> preSubmitCallbackResponse;

    @Before
    public void setUp() {
        preSubmitCallbackResponse = new PreSubmitCallbackResponse<>(caseData);
    }

    @Test
    public void should_hold_onto_values() {

        assertEquals(caseData, preSubmitCallbackResponse.getData());
    }

    @Test
    public void should_store_distinct_errors() {

        List<String> someErrors = Arrays.asList("error3", "error4");
        List<String> someMoreErrors = Arrays.asList("error4", "error1");

        assertTrue(preSubmitCallbackResponse.getErrors().isEmpty());

        preSubmitCallbackResponse.addErrors(someErrors);
        preSubmitCallbackResponse.addErrors(someMoreErrors);

        String[] storedErrors =
            preSubmitCallbackResponse
                .getErrors()
                .toArray(new String[0]);

        assertEquals(3, storedErrors.length);
        assertEquals("error3", storedErrors[0]);
        assertEquals("error4", storedErrors[1]);
        assertEquals("error1", storedErrors[2]);
    }
}
