package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MessageHeaderTest {
    @Mock
    private CodeWithDescription consumerType;
    private MessageHeader messageHeader;

    @BeforeEach
    public void setUp() {
        messageHeader = new MessageHeader(
            consumerType,
            "1234-tttt-wwwwwww",
            "2020-06-15T17:35:38Z");
    }

    @Test
    void has_correct_values() {
        assertEquals(consumerType, messageHeader.getConsumer());
        assertEquals("1234-tttt-wwwwwww", messageHeader.getCorrelationId());
        assertEquals("2020-06-15T17:35:38Z", messageHeader.getEventDateTime());
    }

}
