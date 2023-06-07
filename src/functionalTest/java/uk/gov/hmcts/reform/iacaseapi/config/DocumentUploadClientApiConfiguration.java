package uk.gov.hmcts.reform.iacaseapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;

/**
 * Superseded. Will need to be removed as soon as the "use-ccd-document-am" feature flag is permanently on
 */
@Configuration
@Deprecated
public class DocumentUploadClientApiConfiguration {

    @Bean
    @Primary
    public DocumentUploadClientApi documentUploadClientApi(
        @Value("${ccdGatewayUrl}") final String ccdGatewayUrl
    ) {
        return new DocumentUploadClientApi(
            ccdGatewayUrl,
            new RestTemplate(),
            new ObjectMapper()
        );
    }
}
