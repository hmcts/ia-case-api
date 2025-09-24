package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeReferenceData;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.HomeOfficeReferenceHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

@ExtendWith(MockitoExtension.class)
class HomeOfficeReferenceServiceTest {

    @InjectMocks
    private HomeOfficeReferenceService homeOfficeReferenceService;

    @Mock
    private HomeOfficeReferenceService mockHomeOfficeReferenceService;

    @Mock
    private AsylumCase asylumCase;

    @Test
    void should_return_dummy_data_when_valid_reference_provided() {
        String validReference = "1234567890";

        Optional<HomeOfficeReferenceData> result = homeOfficeReferenceService.getHomeOfficeReferenceData(validReference);

        assertThat(result).isPresent();
        HomeOfficeReferenceData data = result.get();
        assertThat(data.getUan()).isEqualTo("123456789");
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
        assertThat(result.get().getUan()).isEqualTo("123456789");
        assertThat(result.get().getAppellants()).hasSize(3);
    }

    @Test
    void isMatchingHomeOfficeCaseDetails_should_return_true_when_details_match_first_appellant() {
        HomeOfficeReferenceHandler handler = new HomeOfficeReferenceHandler(mockHomeOfficeReferenceService);
        String reference = "1234567890";
        
        HomeOfficeReferenceData mockData = createMockHomeOfficeData();
        when(mockHomeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Homer Jay"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Simpson"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1956-05-12"));

        boolean result = handler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);

        assertThat(result).isTrue();
    }

    @Test
    void isMatchingHomeOfficeCaseDetails_should_return_false_when_given_names_do_not_match() {
        HomeOfficeReferenceHandler handler = new HomeOfficeReferenceHandler(mockHomeOfficeReferenceService);
        String reference = "1234567890";
        
        HomeOfficeReferenceData mockData = createMockHomeOfficeData();
        when(mockHomeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("John"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Simpson"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1956-05-12"));

        boolean result = handler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);

        assertThat(result).isFalse();
    }

    @Test
    void isMatchingHomeOfficeCaseDetails_should_return_false_when_family_name_does_not_match() {
        HomeOfficeReferenceHandler handler = new HomeOfficeReferenceHandler(mockHomeOfficeReferenceService);
        String reference = "1234567890";
        
        HomeOfficeReferenceData mockData = createMockHomeOfficeData();
        when(mockHomeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Homer Jay"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Johnson"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1956-05-12"));

        boolean result = handler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);

        assertThat(result).isFalse();
    }

    @Test
    void isMatchingHomeOfficeCaseDetails_should_return_false_when_date_of_birth_does_not_match() {
        HomeOfficeReferenceHandler handler = new HomeOfficeReferenceHandler(mockHomeOfficeReferenceService);
        String reference = "1234567890";
        
        HomeOfficeReferenceData mockData = createMockHomeOfficeData();
        when(mockHomeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("Homer Jay"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Simpson"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1960-01-01"));

        boolean result = handler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);

        assertThat(result).isFalse();
    }

    @Test
    void isMatchingHomeOfficeCaseDetails_should_return_true_when_case_insensitive_match() {
        HomeOfficeReferenceHandler handler = new HomeOfficeReferenceHandler(mockHomeOfficeReferenceService);
        String reference = "1234567890";
        
        HomeOfficeReferenceData mockData = createMockHomeOfficeData();
        when(mockHomeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));
        
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("HOMER JAY"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("SIMPSON"));
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class))
            .thenReturn(Optional.of("1956-05-12"));

        boolean result = handler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);

        assertThat(result).isTrue();
    }

    @Test
    void isMatchingHomeOfficeCaseDetails_should_return_false_when_reference_is_null() {
        HomeOfficeReferenceHandler handler = new HomeOfficeReferenceHandler(mockHomeOfficeReferenceService);

        boolean result = handler.isMatchingHomeOfficeCaseDetails(null, asylumCase);

        assertThat(result).isFalse();
    }

    @Test
    void isMatchingHomeOfficeCaseDetails_should_return_false_when_home_office_data_not_found() {
        HomeOfficeReferenceHandler handler = new HomeOfficeReferenceHandler(mockHomeOfficeReferenceService);
        String reference = "1234567890";
        
        when(mockHomeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.empty());

        boolean result = handler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);

        assertThat(result).isFalse();
    }

    @Test
    void isMatchingHomeOfficeCaseDetails_should_return_false_when_appellants_list_is_empty() {
        String reference = "1234567890";
        
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setUan("123456789");
        mockData.setAppellants(Collections.emptyList());
        
        when(mockHomeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));

        HomeOfficeReferenceHandler handler = new HomeOfficeReferenceHandler(mockHomeOfficeReferenceService);
        boolean result = handler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);

        assertThat(result).isFalse();
    }

    @Test
    void isMatchingHomeOfficeCaseDetails_should_return_false_when_appellants_list_is_null() {
        String reference = "1234567890";
        
        HomeOfficeReferenceData mockData = new HomeOfficeReferenceData();
        mockData.setUan("123456789");
        mockData.setAppellants(null);
        
        when(mockHomeOfficeReferenceService.getHomeOfficeReferenceData(reference))
            .thenReturn(Optional.of(mockData));

        HomeOfficeReferenceHandler handler = new HomeOfficeReferenceHandler(mockHomeOfficeReferenceService);    
        boolean result = handler.isMatchingHomeOfficeCaseDetails(reference, asylumCase);

        assertThat(result).isFalse();
    }

    private HomeOfficeReferenceData createMockHomeOfficeData() {
        HomeOfficeReferenceData data = new HomeOfficeReferenceData();
        data.setUan("123456789");

        HomeOfficeReferenceData.Appellant homer = new HomeOfficeReferenceData.Appellant();
        homer.setFamilyName("Simpson");
        homer.setGivenNames("Homer Jay");
        homer.setDateOfBirth("1956-05-12");
        homer.setNationality("USA");
        homer.setRoa(false);

        HomeOfficeReferenceData.Appellant marge = new HomeOfficeReferenceData.Appellant();
        marge.setFamilyName("Simpson");
        marge.setGivenNames("Marjorie Jacqueline");
        marge.setDateOfBirth("1956-10-01");
        marge.setNationality("USA");
        marge.setRoa(false);

        HomeOfficeReferenceData.Appellant bart = new HomeOfficeReferenceData.Appellant();
        bart.setFamilyName("Simpson");
        bart.setGivenNames("Bartholomew JoJo");
        bart.setDateOfBirth("2007-04-01");
        bart.setNationality("USA");
        bart.setRoa(true);

        data.setAppellants(Arrays.asList(homer, marge, bart));
        return data;
    }
}
