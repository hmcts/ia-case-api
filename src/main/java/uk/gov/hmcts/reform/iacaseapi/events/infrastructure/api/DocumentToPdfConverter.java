package uk.gov.hmcts.reform.iacaseapi.events.infrastructure.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.springframework.web.client.RestTemplate;

@Service
public class DocumentToPdfConverter {

    private final String docmosisAccessKey;
    private final String docmosisConvertEndpoint;
    private final RestTemplate restTemplate;

    public DocumentToPdfConverter(
        @Value("${docmosis.accessKey}") String docmosisAccessKey,
        @Value("${docmosis.convert.endpoint}") String docmosisConvertEndpoint,
        @Autowired RestTemplate restTemplate
    ) {
        this.docmosisAccessKey = docmosisAccessKey;
        this.docmosisConvertEndpoint = docmosisConvertEndpoint;
        this.restTemplate = restTemplate;
    }

    public synchronized Resource convert(
        Resource originalResource
    ) {
        final String originalFilename = originalResource.getFilename();

        if (originalFilename
            .toLowerCase()
            .endsWith(".pdf")) {
            return originalResource;
        }

        final String convertedFileName =
            originalFilename.substring(0, originalFilename.lastIndexOf('.')) + ".pdf";

        synchronized (restTemplate) {

            List<ClientHttpRequestInterceptor> originalInterceptors = restTemplate.getInterceptors();

            try {

                restTemplate.setInterceptors(Collections.singletonList(new ResponseCaptureInterceptor()));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                headers.setAccept(Arrays.asList(
                    MediaType.APPLICATION_PDF,
                    MediaType.APPLICATION_OCTET_STREAM,
                    MediaType.ALL
                ));

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("accessKey", docmosisAccessKey);
                body.add("outputName", convertedFileName);
                body.add("file", originalResource);

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

                restTemplate
                    .postForObject(
                        docmosisConvertEndpoint,
                        requestEntity,
                        byte[].class
                    );

                if (convertedData == null) {
                    throw new IllegalStateException("No data returned from document converter");
                }

                return new ByteArrayResource(convertedData) {
                    public String getFilename() {
                        return convertedFileName;
                    }
                };

            } finally {
                convertedData = null;
                restTemplate.setInterceptors(originalInterceptors);
            }
        }
    }

    // @todo clearly this is not the way to get the response body, but without a working alternative,
    //       this provides a temporary approach using the ResponseCaptureInterceptor

    private byte[] convertedData = null;

    private class ResponseCaptureInterceptor implements ClientHttpRequestInterceptor {

        public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] requestBody,
            ClientHttpRequestExecution execution
        ) throws IOException {
            ClientHttpResponse response = execution.execute(request, requestBody);
            convertedData = StreamUtils.copyToByteArray(response.getBody());
            return response;
        }
    }
}
