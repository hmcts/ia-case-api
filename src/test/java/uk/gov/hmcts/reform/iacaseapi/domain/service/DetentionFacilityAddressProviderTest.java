package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.config.DetentionFacilityAddressLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.DetentionFacilityAddressProvider.*;

class DetentionFacilityAddressProviderTest {

    private DetentionFacilityAddressLoader mockLoader;

    private DetentionFacilityAddressProvider addressProvider;

    @BeforeEach
    void setUp() {
        mockLoader = mock(DetentionFacilityAddressLoader.class);
    }

    @Test
    void shouldReturnAddressWhenFacilityExists() {
        List<String> addresses = Arrays.asList(
                "Addiewell ,\"HMP Addiewell, 9 Station Road, Addiewell, West Lothian, EH55 8QA\"",
                "Altcourse ,\"HMP Altcourse, Brookfield Dr, Fazakerley, Liverpool, L9 7LH\""
        );
        when(mockLoader.loadAddress()).thenReturn(addresses);
        addressProvider = new DetentionFacilityAddressProvider(mockLoader);

        Optional<DetentionAddress> result1 =
                addressProvider.getAddressFor("Addiewell");
        Optional<DetentionAddress> result2 =
                addressProvider.getAddressFor("Altcourse");

        assertThat(result1.get().building()).isEqualTo("HMP Addiewell");
        assertThat(result1.get().addressLines()).isEqualTo("9 Station Road, Addiewell, West Lothian");
        assertThat(result1.get().postcode()).isEqualTo("EH55 8QA");

        assertThat(result2.get().building()).isEqualTo("HMP Altcourse");
        assertThat(result2.get().addressLines()).isEqualTo("Brookfield Dr, Fazakerley, Liverpool");
        assertThat(result2.get().postcode()).isEqualTo("L9 7LH");
    }

    @Test
    void shouldReturnEmptyWhenFacilityDoesNotExist() {
        when(mockLoader.loadAddress()).thenReturn(List.of());
        addressProvider = new DetentionFacilityAddressProvider(mockLoader);

        Optional<DetentionAddress> result = addressProvider.getAddressFor("Unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowExceptionOnMalformedAddress() {
        List<String> addresses = List.of("Faulty ,\"JustOnePart\"");
        when(mockLoader.loadAddress()).thenReturn(addresses);
        addressProvider = new DetentionFacilityAddressProvider(mockLoader);

        assertThatThrownBy(() -> addressProvider.getAddressFor("Faulty"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid address");
    }
}