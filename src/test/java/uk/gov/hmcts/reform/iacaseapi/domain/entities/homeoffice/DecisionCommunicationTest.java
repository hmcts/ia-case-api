package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class DecisionCommunicationTest {
    private DecisionCommunication decisionCommunication;

    @Before
    public void setUp() {

        decisionCommunication = new DecisionCommunication(
            "some-text", "some-date", "some-date", "some-type");
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(decisionCommunication);
        assertEquals("some-text", decisionCommunication.getDescription());
        assertEquals("some-date", decisionCommunication.getDispatchDate());
        assertEquals("some-date", decisionCommunication.getSentDate());
        assertEquals("some-type", decisionCommunication.getType());
    }


}
