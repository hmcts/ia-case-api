package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;

public abstract class RefDataIntegrationTest extends SpringBootIntegrationTest {

    @Autowired
    @Qualifier("refDataObjectMapper")
    private ObjectMapper refDataObjectMapper;

    @org.springframework.beans.factory.annotation.Value("classpath:prd-org-users-response.json")
    private Resource resourceFile;

    @org.springframework.beans.factory.annotation.Value("${prof.ref.data.path.org.users}")
    private String refDataPath;

    protected ProfessionalUsersResponse prdSuccessResponse;

    @Before
    public void setupReferenceDataStub() throws IOException {

        String prdResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        assertThat(prdResponseJson).isNotBlank();

        prdSuccessResponse = refDataObjectMapper.readValue(prdResponseJson,
            ProfessionalUsersResponse.class);

        stubFor(get(urlEqualTo(refDataPath))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(prdResponseJson)));
    }

}
