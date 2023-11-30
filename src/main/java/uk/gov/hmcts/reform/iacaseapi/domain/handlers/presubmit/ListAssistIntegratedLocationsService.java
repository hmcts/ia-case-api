package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchdarkly.sdk.LDValue;
import java.util.Collections;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Slf4j
@Service
public class ListAssistIntegratedLocationsService {

    private static final String LIST_ASSIST_INTEGRATED_LOCATIONS = "list-assist-integrated-locations";
    private static final LDValue DEFAULT_VALUE = LDValue.parse("{\"epimsIds\":[]}");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private FeatureToggler featureToggler;

    public ListAssistIntegratedLocationsService(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }


    public YesOrNo isListAssistEnabled(AsylumCase asylumCase ) {

        String flagValueJsonString = featureToggler.getJsonValue(LIST_ASSIST_INTEGRATED_LOCATIONS, DEFAULT_VALUE)
            .toJsonString();

        Set<Long> epimsIds = extractEpimsIds(flagValueJsonString);

       return asylumCase
            .read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class)
            .map(location -> epimsIds.contains(Long.parseLong(location.getBaseLocation().getId())) ? YES : NO)
            .orElse(NO);
    }

    private Set<Long> extractEpimsIds(String flagValueJsonString) {

        Set<Long> epimsIds = Collections.emptySet();

        try {
            epimsIds = OBJECT_MAPPER.readValue(flagValueJsonString, ListAssistIntegratedLocations.class)
                .getLocations();
        } catch (JsonProcessingException e) {
            log.error("Error parsing list-assist-integrated-locations LaunchDarkly flag value: {}",
                flagValueJsonString);
        }

        return epimsIds;
    }

    private static class ListAssistIntegratedLocations {

        public Set<Long> epimsIds;

        ListAssistIntegratedLocations(@JsonProperty("epimsIds") Set<Long> epimsIds) {
            this.epimsIds = epimsIds;
        }

        public Set<Long> getLocations() {
            return epimsIds;
        }
    }
}
