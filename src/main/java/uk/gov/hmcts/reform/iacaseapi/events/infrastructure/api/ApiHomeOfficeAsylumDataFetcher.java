package uk.gov.hmcts.reform.iacaseapi.events.infrastructure.api;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacaseapi.events.domain.datasource.HomeOfficeAsylumDataFetcher;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.HomeOfficeAsylumData;

@Service
public class ApiHomeOfficeAsylumDataFetcher implements HomeOfficeAsylumDataFetcher {

    private final String homeOfficeEndpoint;
    private final RestTemplate restTemplate;

    public ApiHomeOfficeAsylumDataFetcher(
        @Value("${homeOffice.asylumData.endpoint}") String homeOfficeEndpoint,
        @Autowired RestTemplate restTemplate
    ) {
        this.homeOfficeEndpoint = homeOfficeEndpoint;
        this.restTemplate = restTemplate;
    }

    @Retryable
    public Optional<HomeOfficeAsylumData> fetch(
        String homeOfficeReferenceNumber
    ) {
        return Optional.ofNullable(
            restTemplate.getForObject(
                homeOfficeEndpoint + "/" + homeOfficeReferenceNumber,
                HomeOfficeAsylumData.class
            )
        );
    }
}
