package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import static java.util.Objects.requireNonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class DetentionFacilityAddressLoader {

    private final Map<String, String> addresses = new HashMap<>();
    private final String addressesFile;

    public DetentionFacilityAddressLoader(@Value("${files.detention-facilities-addresses}") String addressesFile) {
        this.addressesFile = "/" + addressesFile;
        loadPrisonAddresses();
    }

    public Optional<String> getAddressStringFor(String detentionFacility) {
        String maybeAddress = addresses.get(detentionFacility);
        return Optional.ofNullable(maybeAddress);
    }

    private void loadPrisonAddresses() {
        try (BufferedReader reader = new BufferedReader(
              new InputStreamReader(
                    requireNonNull(getClass().getResourceAsStream(addressesFile)), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim().replaceAll("^\"|\"$", ""); // remove surrounding quotes
                    addresses.put(key, value);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prison addresses", e);
        }
    }
}
