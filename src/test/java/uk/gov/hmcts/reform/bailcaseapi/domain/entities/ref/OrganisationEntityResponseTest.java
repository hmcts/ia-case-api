package uk.gov.hmcts.reform.iacaseapi.domain.entities.ref;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


class OrganisationEntityResponseTest {

    private OrganisationEntityResponse testOrganisationEntityResponse;

    @BeforeEach
    public void setUp() throws Exception {

        String jsonResult = "{\n"
                            + "    \"organisationIdentifier\": \"0UFUG4Z123\",\n"
                            + "    \"name\": \"TestOrg1\",\n"
                            + "    \"status\": \"ACTIVE\",\n"
                            + "    \"sraRegulated\": true,\n"
                            + "    \"superUser\": {\n"
                            + "      \"firstName\": \"John\",\n"
                            + "      \"lastName\": \"Doe\",\n"
                            + "      \"email\": \"john.doe@example.com\"\n"
                            + "    },\n"
                            + "    \"paymentAccount\": [\n"
                            + "      \"NUM1\",\n"
                            + "      \"NUM2\"\n"
                            + "    ],\n"
                            + "\"contactInformation\" : []"
                            + "}";

        ObjectMapper mapper = new ObjectMapper();
        testOrganisationEntityResponse = mapper.readValue(jsonResult, OrganisationEntityResponse.class);
    }

    @Test
    void should_successfully_get_organisation_entity_response() {
        assertThat(testOrganisationEntityResponse.getOrganisationIdentifier()).isNotNull();
        OrganisationEntityResponse organisationEntityResponse = testOrganisationEntityResponse;
        assertEquals("0UFUG4Z123", organisationEntityResponse.getOrganisationIdentifier());
        assertEquals("TestOrg1", organisationEntityResponse.getName());
        assertEquals("ACTIVE", organisationEntityResponse.getStatus());
        assertThat(organisationEntityResponse.isSraRegulated()).isTrue();
        assertThat(organisationEntityResponse.getSuperUser()).isNotNull();
        assertEquals("John", organisationEntityResponse.getSuperUser().getFirstName());
        assertEquals("Doe", organisationEntityResponse.getSuperUser().getLastName());
        assertEquals("john.doe@example.com", organisationEntityResponse.getSuperUser().getEmail());
        assertEquals("NUM1", organisationEntityResponse.getPaymentAccount().get(0));
        assertEquals("NUM2", organisationEntityResponse.getPaymentAccount().get(1));
    }

}
