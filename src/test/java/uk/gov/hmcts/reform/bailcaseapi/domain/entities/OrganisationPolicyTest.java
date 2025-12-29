package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class OrganisationPolicyTest {

    private String orgPolicyReference = "some organisation policy reference";

    private String orgPolicyCaseAssignedRole = "some organisation policy case assigned role";

    @Mock private Organisation organisation;

    private OrganisationPolicy organisationPolicy;

    @BeforeEach
    public void setUp() {
        organisationPolicy = OrganisationPolicy.builder()
            .organisation(organisation)
            .orgPolicyReference(orgPolicyReference)
            .orgPolicyCaseAssignedRole(orgPolicyCaseAssignedRole)
            .build();
    }

    @Test
    void should_hold_onto_values() {
        assertThat(organisationPolicy.getOrganisation()).isEqualTo(organisation);
        assertThat(organisationPolicy.getOrgPolicyReference()).isEqualTo(orgPolicyReference);
        assertThat(organisationPolicy.getOrgPolicyCaseAssignedRole()).isEqualTo(orgPolicyCaseAssignedRole);
    }

}
