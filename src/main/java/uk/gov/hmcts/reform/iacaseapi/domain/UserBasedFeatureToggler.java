package uk.gov.hmcts.reform.iacaseapi.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchdarkly.sdk.LDValue;
import java.util.Collections;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserBasedFeatureToggler {

    private static final String APPEALS_LOCATION_REF_DATA_BY_USER_PROFILE = "appeals-location-ref-data-by-user-profile";
    private static final LDValue DEFAULT_VALUE = LDValue.parse("{\"profiles\":[]}");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FeatureToggler featureToggler;
    private final UserDetails userDetails;

    public boolean isAppealsRefDataEnabled() {
        String flagValueJsonString = featureToggler.getJsonValue(APPEALS_LOCATION_REF_DATA_BY_USER_PROFILE, DEFAULT_VALUE)
            .toJsonString();

        Set<String> roles = extractRoles(flagValueJsonString);

        return userDetails.getRoles()
            .stream()
            .anyMatch(roles::contains);
    }

    private Set<String> extractRoles(String flagValueJsonString) {

        Set<String> roles = Collections.emptySet();

        try {
            roles = OBJECT_MAPPER.readValue(flagValueJsonString, EnabledProfiles.class)
                .getProfiles();
        } catch (JsonProcessingException e) {
            log.error("Error parsing list of profiles from LaunchDarkly: {}",
                flagValueJsonString);
        }

        return roles;
    }

    private static class EnabledProfiles {

        public Set<String> profiles;

        EnabledProfiles(@JsonProperty("profiles") Set<String> profiles) {
            this.profiles = profiles;
        }

        public Set<String> getProfiles() {
            return profiles;
        }
    }

}
