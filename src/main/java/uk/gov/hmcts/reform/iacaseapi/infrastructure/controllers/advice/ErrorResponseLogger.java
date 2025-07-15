package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
public class ErrorResponseLogger {

    public ErrorResponseLogger() {
        //no args
    }

    public void maybeLogException(Throwable ex) {

        if (ex instanceof RestClientResponseException) {

            RestClientResponseException cause = (RestClientResponseException) ex;
            String responseBody = cause.getResponseBodyAsString();

            log.error("Error returned with status: {}. \nWith response body: {}",
                cause.getStatusCode().value(),
                responseBody.contains("{\"data\":") ? "" : responseBody);
        }

    }

}
