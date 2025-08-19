package uk.gov.hmcts.reform.iacaseapi.testutils.data;

import static com.google.common.collect.Lists.newArrayList;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.StartEventTrigger;
import uk.gov.hmcts.reform.iacaseapi.testutils.clients.ExtendedCcdApi;
import uk.gov.hmcts.reform.iacaseapi.util.IdamAuthProvider;

public class CaseDataFixture {

    private final String jurisdiction = "IA";
    private final String caseType = "Asylum";

    private final ObjectMapper objectMapper;
    private final ExtendedCcdApi ccdApi;
    private final AuthTokenGenerator s2sAuthTokenGenerator;
    private final Resource minimalAppealStarted;
    private final IdamAuthProvider idamAuthProvider;
    private final MapValueExpander mapValueExpander;

    private String s2sToken;

    private String legalRepToken;
    private String legalRepUserId;

    private long caseId;
    private Map<String, Object> caseData;

    public CaseDataFixture(
        ExtendedCcdApi ccdApi,
        ObjectMapper objectMapper,
        AuthTokenGenerator s2sAuthTokenGenerator,
        Resource minimalAppealStarted,
        IdamAuthProvider idamAuthProvider,
        MapValueExpander mapValueExpander
    ) {
        this.ccdApi = ccdApi;
        this.objectMapper = objectMapper;
        this.s2sAuthTokenGenerator = s2sAuthTokenGenerator;
        this.minimalAppealStarted = minimalAppealStarted;
        this.idamAuthProvider = idamAuthProvider;
        this.mapValueExpander = mapValueExpander;
    }

    public String getS2sToken() {
        return s2sToken;
    }

    public String getLegalRepToken() {
        return legalRepToken;
    }

    public long getCaseId() {
        return caseId;
    }

    public void startAppeal() {
        authenticateUsers();

        String event = "startAppeal";
        StartEventTrigger startEventResponse = ccdApi.startCaseCreation(
            legalRepToken,
            s2sToken,
            legalRepUserId,
            jurisdiction,
            caseType,
            event
        );

        Map<String, Object> data = Collections.emptyMap();
        try {
            data = objectMapper.readValue(
                asString(minimalAppealStarted),
                new TypeReference<Map<String, Object>>() {
                }
            );
        } catch (Exception e) {
            // ignore - test will fail
        }

        mapValueExpander.expandValues(data);
        CaseDataContent content = new CaseDataContent(
            new Event(event, event, event),
            startEventResponse.getToken(),
            true,
            data
        );

        CaseDetails submit = ccdApi.submitCaseCreation(
            legalRepToken,
            s2sToken,
            legalRepUserId,
            jurisdiction,
            caseType,
            content
        );

        caseId = submit.getId();
        caseData = submit.getCaseData();
    }

    public String submitAppeal() {

        Map<String, Object> noticeOfDecisionDoc = getDocumentToUpload();

        Map<String, Object> data = new HashMap<>();
        data.put("uploadTheNoticeOfDecisionDocs", newArrayList(noticeOfDecisionDoc));

        return triggerEvent(
            legalRepToken,
            s2sToken,
            legalRepUserId,
            caseId,
            "submitAppeal",
            data
        );
    }

    protected void authenticateUsers() {
        s2sToken = s2sAuthTokenGenerator.generate();

        legalRepToken = idamAuthProvider.getLegalRepToken();
        legalRepUserId = idamAuthProvider.getUserId(legalRepToken);
    }

    private String triggerEvent(String userToken,
                                String s2sToken,
                                String userId,
                                long caseId,
                                String event,
                                Map<String, Object> data) {

        StartEventTrigger startEventResponse = ccdApi.startEvent(
            userToken,
            s2sToken,
            userId,
            jurisdiction,
            caseType,
            String.valueOf(caseId),
            event
        );

        mapValueExpander.expandValues(data);

        CaseDataContent content = new CaseDataContent(
            new Event(event, event, event),
            startEventResponse.getToken(),
            true,
            data
        );

        CaseDetails submit = ccdApi.submitEvent(
            userToken,
            s2sToken,
            userId,
            jurisdiction,
            caseType,
            String.valueOf(caseId),
            content
        );

        return submit.getState();
    }

    private String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Map<String, Object> getDocumentToUpload() {

        Map<String, Object> doc = new HashMap<>();
        doc.put("document_url", "{$FIXTURE_DOC1_PDF_URL}");
        doc.put("document_binary_url", "{$FIXTURE_DOC1_PDF_URL_BINARY}");
        doc.put("document_filename", "{$FIXTURE_DOC1_PDF_FILENAME}");

        Map<String, Object> document = new HashMap<>();
        document.put("document", doc);
        document.put("description", "Some new evidence");

        Map<String, Object> documentWithId = new HashMap<>();
        documentWithId.put("id", "1");
        documentWithId.put("value", document);

        return documentWithId;
    }
}
