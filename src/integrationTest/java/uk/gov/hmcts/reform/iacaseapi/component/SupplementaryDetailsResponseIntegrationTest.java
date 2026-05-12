package uk.gov.hmcts.reform.iacaseapi.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithCcdStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;

public class SupplementaryDetailsResponseIntegrationTest
    extends SpringBootIntegrationTest
    implements WithServiceAuthStub, WithCcdStub, WithIdamStub {

    @Value("classpath:ccd-search-result-response.json")
    private Resource resourceFile;

    @Value("classpath:ccd-search-result-empty-surname-response.json")
    private Resource emptySurnameResourceFile;

    @Value("classpath:ccd-search-result-missing-surname-response.json")
    private Resource missingSurnameResourceFile;

    @Value("classpath:ccd-search-result-empty-response.json")
    private Resource emptyResponseResourceFile;

    @Value("classpath:ccd-search-result-error-response.json")
    private Resource errorResponseResourceFile;

    private final String fullResponseRequest = "{\"ccd_case_numbers\":["
                                               + "\"1619513189387098\"]}";

    private final String partialResponseRequest = "{\"ccd_case_numbers\":["
                                                  + "\"1619513189387098\","
                                                  + "\"22222222222222\","
                                                  + "\"99999999999999\"]}";

    private final String emptyResponseRequest = "{\"ccd_case_numbers\":["
                                                + "\"1619513189387090\","
                                                + "\"22222222222222\","
                                                + "\"99999999999999\"]}";

    private final String emptySurnameResponseRequest = "{\"ccd_case_numbers\":["
                                                         + "\"1619513189387099\"]}";

    private final String missingSurnameResponseRequest = "{\"ccd_case_numbers\":["
                                                         + "\"1619513189387099\"]}";

    private final String fullResponse = "{\"supplementary_info\":["
                                        + "{\"ccd_case_number\":\"1619513189387098\","
                                        + "\"supplementary_details\":{\"surname\":\"Johnson\",\"case_reference_number\":\"PA/12345/2024\"}}]}";

    private final String partialResponse = "{\"supplementary_info\":["
                                           + "{\"ccd_case_number\":\"1619513189387098\","
                                           + "\"supplementary_details\":{\"surname\":\"Johnson\",\"case_reference_number\":\"PA/12345/2024\"}}],"
                                           + "\"missing_supplementary_info\":"
                                           + "{\"ccd_case_numbers\":[\"22222222222222\",\"99999999999999\"]}}";

    private final String emptyResponse =
        "{\"supplementary_info\":[],"
            + "\"missing_supplementary_info\":"
            + "{\"ccd_case_numbers\":[\"1619513189387090\",\"22222222222222\",\"99999999999999\"]}}";

    private final String emptySurnameResponse = "{\"supplementary_info\":["
                                                + "{\"ccd_case_number\":\"1619513189387099\","
                                                + "\"supplementary_details\":{\"surname\":\"\",\"case_reference_number\":\"\"}}]}";

    private final String missingSurnameResponse = "{\"supplementary_info\":["
                                                  + "{\"ccd_case_number\":\"1619513189387099\","
                                                  + "\"supplementary_details\":{\"surname\":\"null\",\"case_reference_number\":\"null\"}}]}";

    @ParameterizedTest
    @MethodSource("responseMethodSource")
    public void should_return_status_codes_for_responses() throws Exception {
        addUserInfoStub(server);
        addIdamTokenStub(server);
        addServiceAuthStub(server);
        addSearchStub(server, resourceFile);
        String response = iaCaseApiClient.supplementaryResponseRequest(fullResponseRequest, status().isOk());
        assertEquals(fullResponse, response);
    }

    private Stream<Arguments> responseMethodSource() {
        return Stream.of(
            Arguments.of(resourceFile, fullResponseRequest, status().isOk(), fullResponse),
            Arguments.of(resourceFile, partialResponseRequest, status().isPartialContent(), partialResponse),
            Arguments.of(emptyResponseResourceFile, emptyResponseRequest, status().isNotFound(), emptyResponse),
            Arguments.of(errorResponseResourceFile, emptyResponseRequest, status().is4xxClientError(), emptyResponse),
            Arguments.of(emptySurnameResourceFile, emptySurnameResponseRequest, status().isOk(), emptySurnameResponse),
            Arguments.of(missingSurnameResourceFile, missingSurnameResponseRequest, status().isOk(), missingSurnameResponse)
        );
    }
}
