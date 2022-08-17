package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@ExtendWith(MockitoExtension.class)
public class TimedEventMessageConverterCustomizerTest {

    ObjectProvider<HttpMessageConverterCustomizer>  timedEventMessageConverterCustomizer = new TimedEventMessageConverterCustomizer<>();

    @Test
    void should_return_message_converter_unchanged() {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpMessageConverter jacksonConverter1 = new MappingJackson2HttpMessageConverter(objectMapper);
        HttpMessageConverter jacksonConverter2 = new MappingJackson2HttpMessageConverter(objectMapper);

        timedEventMessageConverterCustomizer.forEach(t -> t.accept(Arrays.asList(jacksonConverter1)));

        assertNull(timedEventMessageConverterCustomizer.getObject());
        assertNull(timedEventMessageConverterCustomizer.getIfAvailable());
        assertNull(timedEventMessageConverterCustomizer.getObject(new Object()));
        assertNull(timedEventMessageConverterCustomizer.getIfUnique());
        assertEquals(jacksonConverter1.getSupportedMediaTypes(),jacksonConverter2.getSupportedMediaTypes());
    }

}
