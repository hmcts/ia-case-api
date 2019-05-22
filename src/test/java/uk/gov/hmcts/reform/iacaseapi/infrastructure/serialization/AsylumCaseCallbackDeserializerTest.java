package uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

@RunWith(MockitoJUnitRunner.class)
public class AsylumCaseCallbackDeserializerTest {

    @Mock private ObjectMapper mapper;
    @Mock private Callback<CaseDataMap> expectedAsylumCaseCallback;

    private AsylumCaseCallbackDeserializer asylumCaseCallbackDeserializer;

    @Before
    public void setUp() {
        asylumCaseCallbackDeserializer = new AsylumCaseCallbackDeserializer(mapper);
    }

    @Test
    public void should_deserialize_callback_source_to_asylum_case_callback() throws IOException {

        String source = "callback";

        doReturn(expectedAsylumCaseCallback)
            .when(mapper)
            .readValue(eq(source), isA(TypeReference.class));

        Callback<CaseDataMap> actualAsylumCaseCallback = asylumCaseCallbackDeserializer.deserialize(source);

        assertEquals(expectedAsylumCaseCallback, actualAsylumCaseCallback);
    }

    @Test
    public void should_convert_checked_exception_to_runtime_on_error() throws IOException {

        String source = "callback";

        doThrow(mock(JsonProcessingException.class))
            .when(mapper)
            .readValue(eq(source), isA(TypeReference.class));

        assertThatThrownBy(() -> asylumCaseCallbackDeserializer.deserialize(source))
            .hasMessage("Could not deserialize callback")
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
