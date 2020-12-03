package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HomeOfficeCaseStatusTest {
    @Mock
    Person person;
    @Mock
    ApplicationStatus applicationStatus;

    private HomeOfficeCaseStatus homeOfficeCaseStatus;

    @BeforeEach
    public void setUp() {
        homeOfficeCaseStatus = new HomeOfficeCaseStatus(
            person, applicationStatus);
    }

    @Test
    void has_correct_values_after_setting() {
        assertNotNull(homeOfficeCaseStatus);
        assertNotNull(homeOfficeCaseStatus.getPerson());
        assertNotNull(homeOfficeCaseStatus.getApplicationStatus());
        assertEquals(person, homeOfficeCaseStatus.getPerson());
        assertEquals(applicationStatus, homeOfficeCaseStatus.getApplicationStatus());
    }

    @Test
    void create_new_object_has_correct_values_for_display() {
        homeOfficeCaseStatus = new HomeOfficeCaseStatus(
            person, applicationStatus, "some-text", "some-text", "some-text", "some-text", "some-text", "some-text",
            "some-text", "some-text"
        );
        assertNotNull(homeOfficeCaseStatus);
        assertEquals(person, homeOfficeCaseStatus.getPerson());
        assertEquals(applicationStatus, homeOfficeCaseStatus.getApplicationStatus());
        assertEquals("some-text", homeOfficeCaseStatus.getDisplayDateOfBirth());
        assertEquals("some-text", homeOfficeCaseStatus.getDisplayDecisionDate());
        assertEquals("some-text", homeOfficeCaseStatus.getDisplayDecisionSentDate());
        assertEquals("some-text", homeOfficeCaseStatus.getDisplayMetadataValueBoolean());
        assertEquals("some-text", homeOfficeCaseStatus.getDisplayMetadataValueDateTime());
        assertEquals("some-text", homeOfficeCaseStatus.getDisplayRejectionReasons());
        assertEquals("some-text", homeOfficeCaseStatus.getDisplayAppellantDetailsTitle());
        assertEquals("some-text", homeOfficeCaseStatus.getDisplayApplicationDetailsTitle());
    }
}
