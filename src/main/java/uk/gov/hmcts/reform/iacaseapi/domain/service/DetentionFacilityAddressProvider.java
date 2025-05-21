package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.DetentionFacilityAddressLoader;


@Component
public class DetentionFacilityAddressProvider {

    private final DetentionFacilityAddressLoader addressLoader;

    public DetentionFacilityAddressProvider(DetentionFacilityAddressLoader addressLoader) {
      this.addressLoader = addressLoader;
    }

    public Optional<DetentionAddress> getAddressFor(String detentionFacility) {
        Optional<String> maybeDetentionAddress = addressLoader.getAddressStringFor(detentionFacility);

        if (maybeDetentionAddress.isEmpty()) {
            return Optional.empty();
        }

        String addressStr = maybeDetentionAddress.get();

        String[] parts = addressStr.split(",");

        if (parts.length < 2) {
            throw new RuntimeException("Invalid address: " + addressStr);
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

    public record DetentionAddress(String building, String addressLines, String postcode) {}
}
