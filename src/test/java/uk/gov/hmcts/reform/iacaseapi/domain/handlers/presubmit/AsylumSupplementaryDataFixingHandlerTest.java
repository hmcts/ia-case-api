package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSupplementaryUpdater;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AsylumSupplementaryDataFixingHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CcdSupplementaryUpdater ccdSupplementaryUpdater;

    private AsylumSupplementaryDataFixingHandler asylumSupplementaryDataFixingHandler;

    @BeforeEach
    public void setUp() {
        asylumSupplementaryDataFixingHandler = new AsylumSupplementaryDataFixingHandler(ccdSupplementaryUpdater);
    }

    @Test
    void should_invoke_supplementary_updater() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode orgsAssignedUsersValue = null;

        try {
            orgsAssignedUsersValue = mapper.readValue("\"testValue\"", JsonNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Map<String, JsonNode> supplementaryData = new HashMap<>();
        supplementaryData.put("OrgAssignedUsers:", orgsAssignedUsersValue);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getSupplementaryData()).thenReturn(supplementaryData);

        PreSubmitCallbackResponse<AsylumCase> response = asylumSupplementaryDataFixingHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(ccdSupplementaryUpdater).setHmctsServiceIdSupplementary(callback);
    }

    @Test
    void should_not_update_as_service_id_exists() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode orgsAssignedUsersValue = null;

        try {
            orgsAssignedUsersValue = mapper.readValue("\"testServiceId\"", JsonNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Map<String, JsonNode> supplementaryData = new HashMap<>();
        supplementaryData.put("HMCTSServiceId", orgsAssignedUsersValue);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getSupplementaryData()).thenReturn(supplementaryData);

        PreSubmitCallbackResponse<AsylumCase> response = asylumSupplementaryDataFixingHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(ccdSupplementaryUpdater, times(0)).setHmctsServiceIdSupplementary(callback);
    }
}