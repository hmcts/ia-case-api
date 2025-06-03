package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.DetentionFacilityAddressLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
public class DetentionFacilityAddressProvider {

    private final Map<String, String> addresses = new HashMap<>();

    public DetentionFacilityAddressProvider(DetentionFacilityAddressLoader addressLoader) {
        extractAddressesToMap(addressLoader);
    }

    public Optional<DetentionAddress> getAddressFor(String detentionFacility) {
        String maybeDetentionAddress = this.addresses.get(detentionFacility);

        if (maybeDetentionAddress == null) {
            return Optional.empty();
        }

        String[] parts = maybeDetentionAddress.split(",");

        if (parts.length < 2) {
            throw new IllegalStateException("Invalid address: " + maybeDetentionAddress);
        }

        String name = parts[0].trim();
        String postcode = parts[parts.length - 1].trim();

        StringBuilder addressLines = new StringBuilder();
        for (int i = 1; i < parts.length - 1; i++) {
            addressLines.append(parts[i].trim());
            if (i < parts.length - 2) {
                addressLines.append(", ");
            }
        }

        return Optional.of(new DetentionAddress(name, addressLines.toString(), postcode));
    }

    private void extractAddressesToMap(DetentionFacilityAddressLoader addressLoader) {
        List<String> addressLines = addressLoader.loadAddress();
        for (String line : addressLines) {
            String[] parts = line.split(",", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim().replaceAll("^\"(.*)\"$", "$1");
                addresses.put(key, value);
            }
        }
    }

    public record DetentionAddress(String building, String addressLines, String postcode) {
    }
}
