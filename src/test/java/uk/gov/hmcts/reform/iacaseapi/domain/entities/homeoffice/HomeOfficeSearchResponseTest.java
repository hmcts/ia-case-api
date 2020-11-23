package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class HomeOfficeSearchResponseTest {
    @Mock
    MessageHeader messageHeader;
    @Mock
    HomeOfficeCaseStatus homeOfficeCaseStatus;

    private HomeOfficeSearchResponse response;

    @BeforeEach
    public void setUp() {
        response = new HomeOfficeSearchResponse(
            messageHeader,
            "some-message-type",
            Collections.singletonList(homeOfficeCaseStatus),
            Mockito.mock(HomeOfficeError.class)
        );
    }

    @Test
    public void has_correct_values_after_setting() {
        assertNotNull(response);
        assertNotNull(response.getMessageHeader());
        assertNotNull(response.getStatus());
        assertThat(response.getStatus()).isNotEmpty();
        assertEquals("some-message-type", response.getMessageType());
        assertNotNull(response.getErrorDetail());
    }
}
