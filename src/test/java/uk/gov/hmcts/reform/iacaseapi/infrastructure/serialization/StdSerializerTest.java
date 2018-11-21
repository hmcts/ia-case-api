package uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StdSerializerTest {

    @Mock private ObjectMapper mapper;

    private StdSerializer<Integer> stdSerializer;

    @Before
    public void setUp() {
        stdSerializer = new StdSerializer<>(mapper);
    }

    @Test
    public void should_serialize_argument_to_string() throws JsonProcessingException {

        Integer source = 123;
        String expectedSerializedSource = "123";

        doReturn(expectedSerializedSource)
            .when(mapper)
            .writeValueAsString(source);

        String actualSerializedSource = stdSerializer.serialize(source);

        assertEquals(expectedSerializedSource, actualSerializedSource);
    }

    @Test
    public void should_convert_checked_exception_to_runtime_on_error() throws JsonProcessingException {

        Integer source = 123;

        doThrow(mock(JsonProcessingException.class))
            .when(mapper)
            .writeValueAsString(source);

        assertThatThrownBy(() -> stdSerializer.serialize(source))
            .hasMessage("Could not serialize data")
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
