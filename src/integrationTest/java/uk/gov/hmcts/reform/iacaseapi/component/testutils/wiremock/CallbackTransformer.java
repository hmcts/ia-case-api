package uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock;

import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest.PreSubmitCallbackResponseForTestBuilder.someCallbackResponseWith;

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
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

public abstract class CallbackTransformer extends ResponseDefinitionTransformer {

    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Object> additionalAsylumCaseData = new HashMap<>();

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
        Callback<AsylumCase> asylumCaseCallback = readValue(
            request,
            new TypeReference<Callback<AsylumCase>>() {});

        AsylumCase incomingAsylumCase = asylumCaseCallback
            .getCaseDetails()
            .getCaseData();

        AsylumCaseForTest asylumCaseToBeReturned = anAsylumCase()
            .withCaseDetails(incomingAsylumCase)
            .writeOrOverwrite(additionalAsylumCaseData);

        PreSubmitCallbackResponseForTest preSubmitCallbackResponseForTest =
            someCallbackResponseWith()
                .data(asylumCaseToBeReturned)
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

    private String writeValue(PreSubmitCallbackResponseForTest preSubmitCallbackResponseForTest) {
        try {
            return objectMapper.writeValueAsString(preSubmitCallbackResponseForTest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Json parsing exception", e);
        }
    }

    private Callback<AsylumCase> readValue(Request request, TypeReference<Callback<AsylumCase>> typeRef) {
        try {
            return objectMapper.readValue(request.getBodyAsString(), typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Json parsing exception", e);
        }
    }
}
