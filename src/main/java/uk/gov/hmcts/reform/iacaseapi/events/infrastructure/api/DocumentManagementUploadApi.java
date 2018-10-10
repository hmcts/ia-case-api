package uk.gov.hmcts.reform.iacaseapi.events.infrastructure.api;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.document.domain.Classification;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

@Service
public class DocumentManagementUploadApi {

    private static final String CLASSIFICATION = "classification";
    private static final String FILES = "files";
    private static final String DOCUMENTS_PATH = "/documents";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String USER_ID = "user-id";
    private final String dmUri;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public DocumentManagementUploadApi(
        @Value("${documentManagementApi.baseUrl}") final String dmUri,
        final RestTemplate restTemplate,
        final ObjectMapper objectMapper
    ) {
        this.dmUri = dmUri;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public synchronized UploadResponse uploadFiles(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuth,
        @RequestHeader(USER_ID) String userId,
        @RequestPart List<MultipartFile> files
    ) {
        List<ClientHttpRequestInterceptor> originalInterceptors = restTemplate.getInterceptors();

        try {

            restTemplate.setInterceptors(Collections.singletonList(new RequestResponseLoggingInterceptor()));

            try {

                MultiValueMap<String, Object> parameters = prepareRequest(files);

                HttpHeaders httpHeaders = setHttpHeaders(authorisation, serviceAuth, userId);

                HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(
                    parameters, httpHeaders
                );

                restTemplate.postForObject(dmUri + DOCUMENTS_PATH, httpEntity, String.class);

                if (responseBody == null) {
                    throw new IllegalArgumentException("No data returned from document management");
                }

                return objectMapper.readValue(responseBody, UploadResponse.class);

            } catch (IOException e) {
                throw new IllegalStateException(e);
            }

        } finally {
            responseBody = null;
            restTemplate.setInterceptors(originalInterceptors);
        }
    }

    private HttpHeaders setHttpHeaders(String authorizationToken, String serviceAuth, String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, authorizationToken);
        headers.add(SERVICE_AUTHORIZATION, serviceAuth);
        headers.add(USER_ID, userId);

        headers.set(HttpHeaders.CONTENT_TYPE, MULTIPART_FORM_DATA_VALUE);

        return headers;
    }

    private static MultiValueMap<String, Object> prepareRequest(List<MultipartFile> files) {
        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        files.stream()
            .map(DocumentManagementUploadApi::buildPartFromFile)
            .forEach(file -> parameters.add(FILES, file));
        parameters.add(CLASSIFICATION, Classification.RESTRICTED.name());
        return parameters;
    }

    private static HttpEntity<Resource> buildPartFromFile(MultipartFile file) {
        return new HttpEntity<>(buildByteArrayResource(file), buildPartHeaders(file));
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

    // @todo clearly this is not the way to get the response body, but without a working alternative,
    //       this provides a temporary approach using the RequestResponseLoggingInterceptor

    private String responseBody = null;

    private class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

        private final Logger log = LoggerFactory.getLogger(this.getClass());

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            logRequest(request, body);
            ClientHttpResponse response = execution.execute(request, body);
            logResponse(response);
            return response;
        }

        private void logRequest(HttpRequest request, byte[] body) throws IOException {
            //log.info("===========================request begin================================================");
            //log.info("URI         : {}", request.getURI());
            //log.info("Method      : {}", request.getMethod());
            //log.info("Headers     : {}", request.getHeaders());
            //log.info("Request body: {}", new String(body, "UTF-8"));
            //log.info("==========================request end================================================");
        }

        private void logResponse(ClientHttpResponse response) throws IOException {
            //log.info("============================response begin==========================================");
            //log.info("Status code  : {}", response.getStatusCode());
            //log.info("Status text  : {}", response.getStatusText());
            //log.info("Headers      : {}", response.getHeaders());
            //log.info("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.forName("UTF-8")));
            //log.info("=======================response end=================================================");

            responseBody = StreamUtils.copyToString(response.getBody(), Charset.forName("UTF-8"));
        }
    }
}
