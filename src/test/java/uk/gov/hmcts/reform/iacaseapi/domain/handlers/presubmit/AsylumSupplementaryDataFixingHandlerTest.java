package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority.EARLIEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
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
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private UserDetails userDetails;

    private AsylumSupplementaryDataFixingHandler asylumSupplementaryDataFixingHandler;

    @BeforeEach
    public void setUp() {
        asylumSupplementaryDataFixingHandler = new AsylumSupplementaryDataFixingHandler(ccdSupplementaryUpdater, userDetailsProvider);
    }

    @Test
    void set_to_earliest() {
        assertThat(asylumSupplementaryDataFixingHandler.getDispatchPriority()).isEqualTo(EARLIEST);
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
            ABOUT_TO_SUBMIT, callback);

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
            ABOUT_TO_SUBMIT, callback);

        verify(ccdSupplementaryUpdater, times(0)).setHmctsServiceIdSupplementary(callback);
    }

    @Test
    void should_not_handle_if_citizen_starts_case() {
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of("citizen"));
        when(callback.getEvent()).thenReturn(START_APPEAL);

        assertFalse(asylumSupplementaryDataFixingHandler.canHandle(ABOUT_TO_START, callback));
    }

    @Test
    void should_handle_if_not_citizen_starts_case_and_not_start_appeal() {
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of("ctsc"));
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);

        assertTrue(asylumSupplementaryDataFixingHandler.canHandle(ABOUT_TO_START, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {
        "ABOUT_TO_START", "ABOUT_TO_SUBMIT", "MID_EVENT"
    })
    void should_not_handle_if_event_is_start_appeal_for_any_callback_stage(PreSubmitCallbackStage callbackStage) {
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getRoles()).thenReturn(List.of("ctsc"));
        when(callback.getEvent()).thenReturn(START_APPEAL);

        assertFalse(asylumSupplementaryDataFixingHandler.canHandle(callbackStage, callback));
    }
}