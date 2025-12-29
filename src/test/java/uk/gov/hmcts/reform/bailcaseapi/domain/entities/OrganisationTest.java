package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrganisationTest {

    private String organisationId = "some organisation id";

    private String organisationName = "some organisation name";

    private Organisation organisation;

    @BeforeEach
    public void setUp() {
        organisation = Organisation.builder()
            .organisationID(organisationId)
            .organisationName(organisationName)
            .build();
    }

    @Test
    void should_hold_onto_values() {
        assertThat(organisation.getOrganisationID()).isEqualTo(organisationId);
        assertThat(organisation.getOrganisationName()).isEqualTo(organisationName);
    }

}
