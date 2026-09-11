package uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock;

import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest.PreSubmitCallbackResponseForTestBuilder.someCallbackResponseWith;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import java.util.HashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;

public abstract class CallbackTransformer extends ResponseDefinitionTransformer {

    private ObjectMapper objectMapper = new JsonMapper();
    private Map<String, Object> additionalAsylumCaseData = new HashMap<>();

    public CallbackTransformer() {
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
        } catch (JacksonException e) {
            throw new RuntimeException("Json parsing exception", e);
        }
    }

    private Callback<AsylumCase> readValue(Request request, TypeReference<Callback<AsylumCase>> typeRef) {
        try {
            return objectMapper.readValue(request.getBodyAsString(), typeRef);
        } catch (JacksonException e) {
            throw new RuntimeException("Json parsing exception", e);
        }
    }
}
