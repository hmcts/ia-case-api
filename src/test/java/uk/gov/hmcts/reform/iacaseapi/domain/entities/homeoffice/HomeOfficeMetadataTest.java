package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class HomeOfficeMetadataTest {

    private HomeOfficeMetadata metadata;

    @Before
    public void setUp() {

        metadata = new HomeOfficeMetadata(
            "some-text", "true", "some-date", "some-text");
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(metadata);
        assertEquals("some-text", metadata.getCode());
        assertEquals("true", metadata.getValueBoolean());
        assertEquals("some-date", metadata.getValueDateTime());
        assertEquals("some-text", metadata.getValueString());

    }

}
