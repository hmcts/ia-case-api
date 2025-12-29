package uk.gov.hmcts.reform.bailcaseapi.component.testutils.wiremock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.BailCaseForTest;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;


import static uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.BailCaseForTest.anBailCase;
import static uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest.PreSubmitCallbackResponseForTestBuilder.someCallbackResponseWith;

public abstract class CallbackTransformer extends ResponseDefinitionTransformer {

    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Object> additionalBailCaseData = new HashMap<>();

    public CallbackTransformer() {
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public ResponseDefinition transform(
        Request request,
        ResponseDefinition responseDefinition,
        FileSource files,
        Parameters parameters
    ) {
        Callback<BailCase> bailCaseCallback = readValue(
            request,
            new TypeReference<Callback<BailCase>>() {});

        BailCase incomingBailCase = bailCaseCallback
            .getCaseDetails()
            .getCaseData();

        BailCaseForTest bailCaseToBeReturned = anBailCase()
            .withCaseDetails(incomingBailCase)
            .writeOrOverwrite(additionalBailCaseData);

        PreSubmitCallbackResponseForTest preSubmitCallbackResponseForTest =
            someCallbackResponseWith()
                .data(bailCaseToBeReturned)
                .build();

        ResponseDefinitionBuilder responseDefinitionBuilder = new ResponseDefinitionBuilder()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
                writeValue(
                    preSubmitCallbackResponseForTest));

        return responseDefinitionBuilder.build();
    }

    @Override
    public abstract String getName();

    @Override
    public boolean applyGlobally() {
        return false;
    }

    public void addAdditionalBailCaseData(
        String fieldname,
        Object value
    ) {
        additionalBailCaseData.put(fieldname, value);
    }

    private String writeValue(PreSubmitCallbackResponseForTest preSubmitCallbackResponseForTest) {
        try {
            return objectMapper.writeValueAsString(preSubmitCallbackResponseForTest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json parsing exception", e);
        }
    }

    private Callback<BailCase> readValue(Request request, TypeReference<Callback<BailCase>> typeRef) {
        try {
            return objectMapper.readValue(request.getBodyAsString(), typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Json parsing exception", e);
        }
    }
}
