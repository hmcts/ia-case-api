package uk.gov.hmcts.reform.iacaseapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeReferenceData;

import java.util.Optional;

@Slf4j
@Service
public class HomeOfficeReferenceService {

    @Cacheable(value = "homeOfficeReferenceDataCache")
    public Optional<HomeOfficeReferenceData> getHomeOfficeReferenceData(String homeOfficeReferenceString) {
        log.info("Getting Home Office reference data for: {}", homeOfficeReferenceString);
        
        if (homeOfficeReferenceString == null || homeOfficeReferenceString.trim().isEmpty()) {
            log.warn("Home Office reference string provided is null or empty");
            return Optional.empty();
        }
        
        // Put there the code fetching home office data
        return Optional.empty();
    }

}
