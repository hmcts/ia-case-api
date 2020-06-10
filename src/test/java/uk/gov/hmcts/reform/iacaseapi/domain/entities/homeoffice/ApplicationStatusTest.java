package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ApplicationStatusTest {
    @Mock
    CodeWithDescription mockCode;
    @Mock
    DecisionCommunication decisionCommunication;
    private ApplicationStatus applicationStatus;

    @Before
    public void setUp() {
        applicationStatus = new ApplicationStatus(
            mockCode,
            mockCode,
            decisionCommunication,
            "some-date",
            mockCode,
            "some-doc-ref",
            mockCode,
            mockCode,
            new ArrayList<HomeOfficeMetadata>(),
            new ArrayList<RejectionReason>()
        );
    }

    @Test
    public void has_correct_values_for_ccd_list_and_other_values() {
        assertNotNull(applicationStatus.getDecisionType());
        assertEquals(mockCode, applicationStatus.getApplicationType());
        assertEquals(mockCode, applicationStatus.getClaimReasonType());
        assertEquals(decisionCommunication, applicationStatus.getDecisionCommunication());
        assertEquals("some-date", applicationStatus.getDecisionDate());
        assertEquals(mockCode, applicationStatus.getDecisionType());
        assertEquals("some-doc-ref", applicationStatus.getDocumentReference());
        assertEquals(mockCode, applicationStatus.getRoleType());
        assertEquals(mockCode, applicationStatus.getRoleSubType());
        assertNotNull(applicationStatus.getHomeOfficeMetadata());
        assertNotNull(applicationStatus.getRejectionReasons());
        assertThat(applicationStatus.getHomeOfficeMetadata()).isEmpty();
        assertThat(applicationStatus.getRejectionReasons()).isEmpty();
        assertNull(applicationStatus.getCcdHomeOfficeMetadata());
        assertNull(applicationStatus.getCcdRejectionReasons());

    }

    @Test
    public void modify_list_converts_to_valid_ccd_list_id_values() {
        List<HomeOfficeMetadata> metadataList = new ArrayList<>();
        metadataList.add(new HomeOfficeMetadata("", "", "", ""));

        List<RejectionReason> rejectionReasons = new ArrayList<>();
        rejectionReasons.add(new RejectionReason(""));

        applicationStatus = new ApplicationStatus(
            mockCode,
            mockCode,
            decisionCommunication,
            "some-date",
            mockCode,
            "some-doc-ref",
            mockCode,
            mockCode,
            metadataList,
            rejectionReasons
        );

        applicationStatus.modifyListDataForCcd();
        assertNull(applicationStatus.getHomeOfficeMetadata());
        assertNull(applicationStatus.getRejectionReasons());
        assertThat(applicationStatus.getCcdHomeOfficeMetadata()).isNotEmpty();
        assertThat(applicationStatus.getCcdRejectionReasons()).isNotEmpty();
        assertThat(applicationStatus.getCcdHomeOfficeMetadata().size()).isEqualTo(1);
        assertThat(applicationStatus.getCcdRejectionReasons().size()).isEqualTo(1);

    }
}
