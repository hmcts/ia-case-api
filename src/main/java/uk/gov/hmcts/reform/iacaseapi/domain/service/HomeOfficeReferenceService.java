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
        HomeOfficeReferenceData.Appellant homer = new HomeOfficeReferenceData.Appellant(
            "Simpson",
            "Homer Jay",
            "1956-05-12",
            "USA",
            false
        );

        HomeOfficeReferenceData.Appellant marge = new HomeOfficeReferenceData.Appellant(
            "Simpson",
            "Marjorie Jacqueline",
            "1956-10-01",
            "USA",
            false
        );

        HomeOfficeReferenceData.Appellant bart = new HomeOfficeReferenceData.Appellant(
            "Simpson",
            "Bartholomew JoJo",
            "2007-04-01",
            "USA",
            true
        );

        return new HomeOfficeReferenceData(
            "1234-5678-9012-3456",
            Arrays.asList(homer, marge, bart)
        );
    }
}
