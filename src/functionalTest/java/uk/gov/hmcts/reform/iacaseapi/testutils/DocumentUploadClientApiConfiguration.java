package uk.gov.hmcts.reform.iacaseapi.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;

@Configuration
public class DocumentUploadClientApiConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public DocumentUploadClientApi documentUploadClientApi(
        @Value("${ccdGatewayUrl}") final String ccdGatewayUrl,
        RestTemplate restTemplate
    ) {
        return new DocumentUploadClientApi(
            ccdGatewayUrl,
            restTemplate,
            new ObjectMapper()
        );
    }
}
