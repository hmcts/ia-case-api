package uk.gov.hmcts.reform.bailcaseapi.controllers;

import static java.util.Objects.requireNonNull;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.PreSubmitCallbackDispatcher;

@Slf4j
@Api(
    value = "/bail",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequestMapping(
    path = "/bail",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class PreSubmitCallbackController {

    private final PreSubmitCallbackDispatcher<BailCase> callbackDispatcher;

    public PreSubmitCallbackController(PreSubmitCallbackDispatcher<BailCase> callbackDispatcher) {
        requireNonNull(callbackDispatcher, "callbackDispatcher can not be null");
        this.callbackDispatcher = callbackDispatcher;
    }

    //TODO: add endpoints


}
