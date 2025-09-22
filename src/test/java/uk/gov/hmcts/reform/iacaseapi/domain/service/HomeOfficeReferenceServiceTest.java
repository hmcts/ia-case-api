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
    void should_return_dummy_data_when_valid_reference_provided() {
        String validReference = "1234567890";

        Optional<HomeOfficeReferenceData> result = homeOfficeReferenceService.getHomeOfficeReferenceData(validReference);

        assertThat(result).isPresent();
        HomeOfficeReferenceData data = result.get();
        assertThat(data.getUan()).isEqualTo("1234-5678-9012-3456");
        assertThat(data.getAppellants()).hasSize(3);

        // Verify Homer Simpson data
        HomeOfficeReferenceData.Appellant homer = data.getAppellants().get(0);
        assertThat(homer.getFamilyName()).isEqualTo("Simpson");
        assertThat(homer.getGivenNames()).isEqualTo("Homer Jay");
        assertThat(homer.getDateOfBirth()).isEqualTo("1956-05-12");
        assertThat(homer.getNationality()).isEqualTo("USA");
        assertThat(homer.isRoa()).isFalse();

        // Verify Marge Simpson data
        HomeOfficeReferenceData.Appellant marge = data.getAppellants().get(1);
        assertThat(marge.getFamilyName()).isEqualTo("Simpson");
        assertThat(marge.getGivenNames()).isEqualTo("Marjorie Jacqueline");
        assertThat(marge.getDateOfBirth()).isEqualTo("1956-10-01");
        assertThat(marge.getNationality()).isEqualTo("USA");
        assertThat(marge.isRoa()).isFalse();

        // Verify Bart Simpson data
        HomeOfficeReferenceData.Appellant bart = data.getAppellants().get(2);
        assertThat(bart.getFamilyName()).isEqualTo("Simpson");
        assertThat(bart.getGivenNames()).isEqualTo("Bartholomew JoJo");
        assertThat(bart.getDateOfBirth()).isEqualTo("2007-04-01");
        assertThat(bart.getNationality()).isEqualTo("USA");
        assertThat(bart.isRoa()).isTrue();
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

    @Test
    void should_return_dummy_data_for_any_non_empty_reference() {
        String anotherReference = "9876543210";

        Optional<HomeOfficeReferenceData> result = homeOfficeReferenceService.getHomeOfficeReferenceData(anotherReference);

        assertThat(result).isPresent();
        assertThat(result.get().getUan()).isEqualTo("1234-5678-9012-3456");
        assertThat(result.get().getAppellants()).hasSize(3);
    }
}
