package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ProfessionalUsersResponse;

@RunWith(MockitoJUnitRunner.class)
public class ProfessionalUsersRetrieverTest {


    private ObjectMapper objectMapper = new ObjectMapper();


    private String userData =
        "{\n"
        + "  \"users\": "
        + "[\n"
        + "    {\n"
        + "      \"userIdentifier\": \"146bd2bd-0e26-4b26-b83a-e4fae21cf04b\",\n"
        + "      \"firstName\": \"fname1\",\n"
        + "      \"lastName\": \"lname1\",\n"
        + "      \"email\": \"someone1@email.com\",\n"
        + "      \"userStatus\": \"ACTIVE\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"userIdentifier\": \"146bd2bd-0e26-4b26-b83a-e4fae21cf04c\",\n"
        + "      \"firstName\": \"fname2\",\n"
        + "      \"lastName\": \"lname2\",\n"
        + "      \"email\": \"someone2@email.com\",\n"
        + "      \"userStatus\": \"ACTIVE\"\n"
        + "    }\n"
        + "  ]\n"
        + "}\n"
        + "\n";

    private String userData2 =
        "[\n"
        + "    {\n"
        + "      \"userIdentifier\": \"146bd2bd-0e26-4b26-b83a-e4fae21cf04b\",\n"
        + "      \"firstName\": \"fname1\",\n"
        + "      \"lastName\": \"lname1\",\n"
        + "      \"email\": \"someone1@email.com\",\n"
        + "      \"userStatus\": \"ACTIVE\"\n"
        + "    },\n"
        + "    {\n"
        + "      \"userIdentifier\": \"146bd2bd-0e26-4b26-b83a-e4fae21cf04c\",\n"
        + "      \"firstName\": \"fname2\",\n"
        + "      \"lastName\": \"lname2\",\n"
        + "      \"email\": \"someone2@email.com\",\n"
        + "      \"userStatus\": \"ACTIVE\"\n"
        + "    }\n"
        + "  ]\n"
        + "\n";


    @Test
    public void should_deserialize() throws JsonProcessingException {

        //new ParameterizedTypeReference<PreSubmitCallbackResponse<BundleCaseData>>() {
        //}

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);

        ProfessionalUsersResponse response =
            objectMapper.readValue(userData, new TypeReference<ProfessionalUsersResponse>() {
                });

        List<ProfessionalUser> users = response.getProfessionalUsers();
        assertThat(users.size()).isEqualTo(2);

        users.forEach(professionalUser ->  System.out.println(professionalUser.getEmail()));

        System.out.println(response);

    }

}
