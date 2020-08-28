package uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

// suppress warning because of changes in jackson-core library API version: 2.10.0.pr3
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class AsylumCaseCallbackDeserializerTest {

    @Mock private ObjectMapper mapper;
    @Mock private Callback<AsylumCase> expectedAsylumCaseCallback;

    AsylumCaseCallbackDeserializer asylumCaseCallbackDeserializer;

    @BeforeEach
    void setUp() {

        asylumCaseCallbackDeserializer = new AsylumCaseCallbackDeserializer(mapper);
    }

    @Test
    void should_deserialize_callback_source_to_asylum_case_callback() throws IOException {

        String source = "callback";

        doReturn(expectedAsylumCaseCallback)
            .when(mapper)
            .readValue(eq(source), isA(TypeReference.class));

        Callback<AsylumCase> actualAsylumCaseCallback = asylumCaseCallbackDeserializer.deserialize(source);

        assertEquals(expectedAsylumCaseCallback, actualAsylumCaseCallback);
    }

    @Test
    void should_convert_checked_exception_to_runtime_on_error() throws IOException {

        String source = "callback";

        doThrow(mock(JsonProcessingException.class))
            .when(mapper)
            .readValue(eq(source), isA(TypeReference.class));

        assertThatThrownBy(() -> asylumCaseCallbackDeserializer.deserialize(source))
            .hasMessage("Could not deserialize callback")
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
