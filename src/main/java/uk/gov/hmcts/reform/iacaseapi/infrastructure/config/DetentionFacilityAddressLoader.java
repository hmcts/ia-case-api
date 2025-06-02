package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;


@Component
public class DetentionFacilityAddressLoader {

    private final String addressFile;

    public DetentionFacilityAddressLoader(@Value("${files.detention-facilities-addresses}") String addressesFile) {
        this.addressFile = addressesFile;
    }

    public List<String> loadAddress() {
        InputStream resourceAsStream = requireNonNull(getClass().getResourceAsStream(addressFile));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream, UTF_8))) {
            return reader.lines().collect(toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prison addresses", e);
        }
    }
}
