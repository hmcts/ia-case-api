package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class MessageHeaderTest {
    @Mock
    private CodeWithDescription consumerType;
    private MessageHeader messageHeader;

    @Before
    public void setUp() {
        messageHeader = new MessageHeader(
            consumerType,
            "1234-tttt-wwwwwww",
            "2020-06-15T17:35:38Z");
    }

    @Test
    public void has_correct_values() {
        assertEquals(consumerType, messageHeader.getConsumer());
        assertEquals("1234-tttt-wwwwwww", messageHeader.getCorrelationId());
        assertEquals("2020-06-15T17:35:38Z", messageHeader.getEventDateTime());
    }

}
