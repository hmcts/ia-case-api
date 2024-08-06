package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isCaseUsingLocationRefData;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Slf4j
@Service
public class LocationBasedFeatureToggler {

    private static final String LIST_ASSIST_INTEGRATED_LOCATIONS = "list-assist-integrated-locations";
    private static final String AUTO_HEARING_REQUEST_LOCATIONS_LIST = "auto-hearing-request-locations-list";
    private static final LDValue DEFAULT_VALUE = LDValue.parse("{\"epimsIds\":[]}");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private FeatureToggler featureToggler;

    public LocationBasedFeatureToggler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }


    public YesOrNo isListAssistEnabled(AsylumCase asylumCase) {

        return isTargetLocation(asylumCase, LIST_ASSIST_INTEGRATED_LOCATIONS);
    }

    public YesOrNo isAutoHearingRequestEnabled(AsylumCase asylumCase) {

        return isTargetLocation(asylumCase, AUTO_HEARING_REQUEST_LOCATIONS_LIST);
    }

    private YesOrNo isTargetLocation(AsylumCase asylumCase, String featureKey) {
        String flagValueJsonString = featureToggler.getJsonValue(featureKey, DEFAULT_VALUE)
            .toJsonString();

        Set<Long> epimsIds = extractEpimsIds(flagValueJsonString);

        if (isCaseUsingLocationRefData(asylumCase)) {
            return asylumCase
                .read(CASE_MANAGEMENT_LOCATION_REF_DATA, CaseManagementLocationRefData.class)
                .map(location -> epimsIds.contains(
                    Long.parseLong(location.getBaseLocation().getValue().getCode())) ? YES : NO)
                .orElse(NO);
        }

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
            log.error("Error parsing location EPIMS IDs from LaunchDarkly: {}",
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
