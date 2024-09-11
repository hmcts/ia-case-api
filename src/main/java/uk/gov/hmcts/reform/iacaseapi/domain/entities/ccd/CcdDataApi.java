package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.FeignConfiguration;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

@FeignClient(
    name = "core-case-data-api",
    url = "${core_case_data_api_url}",
    configuration = FeignConfiguration.class
)
public interface CcdDataApi {
    String EXPERIMENTAL = "experimental=true";
    String CONTENT_TYPE = "content-type=application/json";

    @GetMapping(
        value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases/{cid}/event-triggers/{etid}"
            + "/token?ignore-warning=true",
        headers = CONTENT_TYPE
    )
    StartEventDetails startEvent(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable("uid") String userId,
        @PathVariable("jid") String jurisdiction,
        @PathVariable("ctid") String caseType,
        @PathVariable("cid") String id,
        @PathVariable("etid") String eventId
        );

    @PostMapping(
        value = "/cases/{cid}/events",
        headers = { CONTENT_TYPE, EXPERIMENTAL })
    SubmitEventDetails submitEvent(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable("cid") String id,
        @RequestBody CaseDataContent requestBody
    );

    @GetMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/event-triggers/{etid}"
        + "/token?ignore-warning=true", produces = "application/json", consumes = "application/json")
    StartEventDetails startCaseCreation(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable("uid") String userId,
        @PathVariable("jid") String jurisdiction,
        @PathVariable("ctid") String caseType,
        @PathVariable("etid") String eventId
    );

    @PostMapping(value = "/caseworkers/{uid}/jurisdictions/{jid}/case-types/{ctid}/cases",
        produces = {"application/json; charset=UTF-8"}, consumes = "application/json")
    CaseDetails<AsylumCase> submitCaseCreation(
        @RequestHeader(AUTHORIZATION) String userToken,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable("uid") String userId,
        @PathVariable("jid") String jurisdiction,
        @PathVariable("ctid") String caseType,
        @RequestBody CaseDataContent content
    );
}
