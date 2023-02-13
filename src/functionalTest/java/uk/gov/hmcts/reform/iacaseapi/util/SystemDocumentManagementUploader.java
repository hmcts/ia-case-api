package uk.gov.hmcts.reform.iacaseapi.util;

import static io.micrometer.core.instrument.binder.BaseUnits.FILES;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Classification.PUBLIC;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;


@Service
@ComponentScan("uk.gov.hmcts.reform.ccd.document.am.feign")
public class SystemDocumentManagementUploader {

    private final AuthorizationHeadersProvider authorizationHeadersProvider;

    private static final String CLASSIFICATION = "classification";

    private static final String CASE_TYPE_ID = "caseTypeId";

    private static final String JURISDICTION_ID = "jurisdictionId";

    private static final String DOCUMENTS = "documents";

    private static final int FIRST = 0;

    @Autowired
    private RestTemplate restTemplate;

    public SystemDocumentManagementUploader(
            AuthorizationHeadersProvider authorizationHeadersProvider
    ) {
        this.authorizationHeadersProvider = authorizationHeadersProvider;
    }

    public Document upload(
            Resource resource,
            String contentType
    ) {

        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {

            final MultipartFile file = new MockMultipartFile(
                    resource.getFilename(),
                    resource.getFilename(),
                    contentType,
                    ByteStreams.toByteArray(resource.getInputStream())
                 );

                final String t = restTemplate.postForObject("http://127.0.0.1:4455/cases/documents", httpEntity(file), String.class);

                JsonNode jsonNode =
                    Objects.requireNonNull(mapper.readTree(t))
                            .get(DOCUMENTS)
                            .get(FIRST);

                return mapper.treeToValue(jsonNode, Document.class);

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    private HttpEntity<MultiValueMap<String, Object>> httpEntity(final MultipartFile file) {

        final String serviceAuthorizationToken =
                authorizationHeadersProvider
                        .getLegalRepresentativeAuthorization()
                        .getValue("ServiceAuthorization");

        final String accessToken =
                authorizationHeadersProvider
                        .getLegalRepresentativeAuthorization()
                        .getValue("Authorization");

        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        HttpEntity<Resource> fileResource = new HttpEntity<>(buildByteArrayResource(file), buildPartHeaders(file));
        parameters.add(FILES, fileResource);
        parameters.add(CLASSIFICATION, PUBLIC.toString());
        parameters.add(CASE_TYPE_ID, "Asylum");
        parameters.add(JURISDICTION_ID, "IA");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        headers.set("ServiceAuthorization", serviceAuthorizationToken);
        return new HttpEntity<>(parameters, headers);

    }

    private static HttpHeaders buildPartHeaders(MultipartFile file) {
        requireNonNull(file.getContentType());
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(file.getContentType()));
        return headers;
    }

    private static ByteArrayResource buildByteArrayResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                      @Override
                     public String getFilename() {
                          return file.getOriginalFilename();
                      }
              };
        } catch (IOException ioException) {
            throw new IllegalStateException(ioException);
        }
    }
}
