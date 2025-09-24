package uk.gov.hmcts.reform.iacaseapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeReferenceData;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
public class HomeOfficeReferenceService {

    @Cacheable(value = "homeOfficeReferenceDataCache")
    public Optional<HomeOfficeReferenceData> getHomeOfficeReferenceData(String homeOfficeReferenceString) {
        log.info("Getting Home Office reference data for: {}", homeOfficeReferenceString);
        
        if (homeOfficeReferenceString == null || homeOfficeReferenceString.trim().isEmpty()) {
            log.warn("Home Office reference string is null or empty");
            return Optional.empty();
        }

        // Use dummy data for now. Do not deploy that to production.
        HomeOfficeReferenceData data = createDummyHomeOfficeData();
        if (!data.getUan().equalsIgnoreCase(homeOfficeReferenceString)) {
            log.warn("The homeOfficeReferenceString provided {} do not match the home office uan from the dummy data.", homeOfficeReferenceString);
        }
        
        return Optional.of(data);
    }

    private HomeOfficeReferenceData createDummyHomeOfficeData() {
        // Return dummy Homer Simpson family data
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

        HomeOfficeReferenceData data = new HomeOfficeReferenceData();
        data.setUan("123456789");
        data.setAppellants(Arrays.asList(homer, marge, bart));
        
        log.info("Using dummy Home Office data: {}", data);

        return data;
    }
}
