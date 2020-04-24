package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @SneakyThrows
    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
            case 400:
            case 404:
                log.error("StatusCode: {} , methodKey: {}, reason: {}, message: {}",
                    response.status(), methodKey, response.reason(), IOUtils.toString(response.body().asReader()));
                return new ResponseStatusException(HttpStatus.valueOf(response.status()), response.reason());
            default:
                return new Exception(response.reason());
        }
    }

}
