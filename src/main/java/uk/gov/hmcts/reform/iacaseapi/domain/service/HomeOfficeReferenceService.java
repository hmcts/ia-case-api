package uk.gov.hmcts.reform.iacaseapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeReferenceData;

import java.util.Optional;

@Slf4j
@Service
public class HomeOfficeReferenceService {

    private final CcdDataService ccdDataService;

    public HomeOfficeReferenceService(CcdDataService ccdDataService) {
        this.ccdDataService = ccdDataService;
    }

    @Cacheable(value = "homeOfficeReferenceDataCache")
    public Optional<HomeOfficeReferenceData> getHomeOfficeReferenceData(String homeOfficeReferenceString) {
        
        if (homeOfficeReferenceString == null || homeOfficeReferenceString.trim().isEmpty()) {
            log.warn("Home Office reference number is null or empty");
            return Optional.empty();
        }

        log.info("Getting Home Office reference data for: {}", homeOfficeReferenceString);
        
        // Put there the code fetching home office data


        //                 ccdDataService.raiseEvent(trimmedCaseId, Event.RE_TRIGGER_WA_TASKS);
        //        String appellantFamilyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("");


        return Optional.empty();
    }

}
