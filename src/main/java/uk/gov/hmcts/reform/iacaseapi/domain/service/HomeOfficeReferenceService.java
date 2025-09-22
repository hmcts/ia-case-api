package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeReferenceData;

import java.util.Arrays;
import java.util.Optional;

@Service
public class HomeOfficeReferenceService {

    public Optional<HomeOfficeReferenceData> getHomeOfficeReferenceData(String homeOfficeReferenceString) {
        if (homeOfficeReferenceString == null || homeOfficeReferenceString.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(createDummyHomeOfficeData());
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
        data.setUan("1234-5678-9012-3456");
        data.setAppellants(Arrays.asList(homer, marge, bart));
        
        return data;
    }
}
