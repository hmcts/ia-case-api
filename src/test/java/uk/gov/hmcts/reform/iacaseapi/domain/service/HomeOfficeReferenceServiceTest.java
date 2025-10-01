package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeReferenceData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HomeOfficeReferenceServiceTest {

    @InjectMocks
    private HomeOfficeReferenceService homeOfficeReferenceService;

    @Test
    void should_return_empty_when_valid_reference_provided() {
        String validReference = "1234567890";

        Optional<HomeOfficeReferenceData> result = homeOfficeReferenceService.getHomeOfficeReferenceData(validReference);

        assertThat(result).isEmpty();
    }

    @Test
    void should_return_empty_when_null_reference_provided() {
        Optional<HomeOfficeReferenceData> result = homeOfficeReferenceService.getHomeOfficeReferenceData(null);

        assertThat(result).isEmpty();
    }

    @Test
    void should_return_empty_when_empty_reference_provided() {
        Optional<HomeOfficeReferenceData> result = homeOfficeReferenceService.getHomeOfficeReferenceData("");

        assertThat(result).isEmpty();
    }

    @Test
    void should_return_empty_when_whitespace_only_reference_provided() {
        Optional<HomeOfficeReferenceData> result = homeOfficeReferenceService.getHomeOfficeReferenceData("   ");

        assertThat(result).isEmpty();
    }
}
