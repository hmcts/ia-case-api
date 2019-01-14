package uk.gov.hmcts.reform.iacaseapi.integration.stubs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.singletonMap;
import static java.util.stream.IntStream.range;
import static uk.gov.hmcts.reform.iacaseapi.integration.util.TestFixtures.anEmptyListOfCaseDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Serializer;

public class CcdMock {

    public static final String retrieveCasesUrl = "/caseworkers/1/jurisdictions/IA/case-types/Asylum/cases";
    public static final String paginationMetadataUrl = "/caseworkers/1/jurisdictions/IA/case-types/Asylum/cases/pagination_metadata";
    public static final int PAGE_SIZE = 25;

    private final Serializer<List> ccdCaseListSerializer;
    private ObjectMapper objectMapper = new ObjectMapper();

    public CcdMock(Serializer<List> ccdCaseListSerializer) {
        this.ccdCaseListSerializer = ccdCaseListSerializer;
    }

    public void doesntHaveAnyExistingAppealCases() throws JsonProcessingException {

        stubFor(get(urlEqualTo(paginationMetadataUrl))
                .withHeader("Accept", equalTo("application/json;charset=UTF-8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                objectMapper.writeValueAsString(
                                        singletonMap("total_pages_count", 0)))));

        stubFor(get(urlEqualTo(retrieveCasesUrl))
                .withHeader("Accept", equalTo("application/json;charset=UTF-8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ccdCaseListSerializer.serialize(anEmptyListOfCaseDetails()))));
    }

    public void returns(List<CaseDetails<AsylumCase>> caseDetails) throws JsonProcessingException {

        int numberOfPages = caseDetails.size() == 0
                ? 0 : 1 + (caseDetails.size() / PAGE_SIZE);

        stubFor(get(urlEqualTo(paginationMetadataUrl))
                .withHeader("Accept", equalTo("application/json;charset=UTF-8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                objectMapper.writeValueAsString(
                                        singletonMap("total_pages_count", String.valueOf(numberOfPages))))));


        range(0, numberOfPages)
                .forEach(i -> {
                    int fromIndex = i * PAGE_SIZE;
                    int toIndex = (fromIndex) + PAGE_SIZE;

                    if (toIndex > caseDetails.size()) {
                        toIndex = caseDetails.size();
                    }

                    stubCasesPageRequest(caseDetails.subList(fromIndex, toIndex), i + 1);
                });

    }

    private void stubCasesPageRequest(List<CaseDetails<AsylumCase>> caseDetails, int page) {
        stubFor(get(urlEqualTo(retrieveCasesUrl + "?page=" + page))
                .withHeader("Accept", equalTo("application/json;charset=UTF-8"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(ccdCaseListSerializer.serialize(caseDetails))));
    }

    public void hadAnInternalError() {

        stubFor(get(urlEqualTo(paginationMetadataUrl))
                .withHeader("Accept", equalTo("application/json;charset=UTF-8"))
                .willReturn(aResponse()
                        .withStatus(500)));

        stubFor(get(urlEqualTo(retrieveCasesUrl))
                .withHeader("Accept", equalTo("application/json;charset=UTF-8"))
                .willReturn(aResponse()
                        .withStatus(500)));
    }
}
