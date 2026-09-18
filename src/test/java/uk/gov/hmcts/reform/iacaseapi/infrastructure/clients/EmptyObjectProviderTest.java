package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import tools.jackson.databind.json.JsonMapper;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.http.converter.HttpMessageConverter;

@ExtendWith(MockitoExtension.class)
class EmptyObjectProviderTest {

    ObjectProvider<HttpMessageConverterCustomizer>  emptyObjectProvider = new EmptyObjectProvider<>();

    @Test
    void should_return_message_converter_unchanged() {
        JsonMapper objectMapper = new JsonMapper();
        HttpMessageConverter jacksonConverter1 = new JacksonJsonHttpMessageConverter(objectMapper);
        HttpMessageConverter jacksonConverter2 = new JacksonJsonHttpMessageConverter(objectMapper);
        emptyObjectProvider.forEach(t -> t.accept(Arrays.asList(jacksonConverter1)));

        assertEquals(jacksonConverter1.getSupportedMediaTypes(),jacksonConverter2.getSupportedMediaTypes());
        assertNull(emptyObjectProvider.getObject());
        assertNull(emptyObjectProvider.getIfAvailable());
        assertNull(emptyObjectProvider.getObject(new Object()));
        assertNull(emptyObjectProvider.getIfUnique());

    }

}
