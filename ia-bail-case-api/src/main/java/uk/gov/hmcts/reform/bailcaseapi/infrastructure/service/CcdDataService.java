package uk.gov.hmcts.reform.bailcaseapi.infrastructure.service;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.AddressUK;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.SystemTokenGenerator;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.SystemUserProvider;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam.IdentityManagerResponseException;

@Service
@Slf4j
public class CcdDataService {

    private final CcdDataApi ccdDataApi;
    private final SystemTokenGenerator systemTokenGenerator;
    private final SystemUserProvider systemUserProvider;
    private final AuthTokenGenerator serviceAuthorization;
    private static final String CASE_TYPE = "Bail";

    public CcdDataService(CcdDataApi ccdDataApi,
                          SystemTokenGenerator systemTokenGenerator,
                          SystemUserProvider systemUserProvider,
                          AuthTokenGenerator serviceAuthorization) {

        this.ccdDataApi = ccdDataApi;
        this.systemTokenGenerator = systemTokenGenerator;
        this.systemUserProvider = systemUserProvider;
        this.serviceAuthorization = serviceAuthorization;
    }

    public SubmitEventDetails clearLegalRepDetails(Callback<BailCase> callback) {
        String caseId = String.valueOf(callback.getCaseDetails().getId());
        Tokens tokens = getTokens(callback, Event.CLEAR_LEGAL_REPRESENTATIVE_DETAILS);
        CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        String jurisdiction = caseDetails.getJurisdiction();

        final StartEventDetails startEventDetails =
            getCase(
                tokens,
                jurisdiction,
                caseId,
                Event.CLEAR_LEGAL_REPRESENTATIVE_DETAILS
            );
        log.info("Case details found for the caseId: {}", callback.getCaseDetails().getId());

        if (isLegalRepDetailsMissing(startEventDetails.getCaseDetails().getCaseData())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Legal Rep Details not found for the caseId: " + caseId
            );
        }

        // Clear Legal Rep Details
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(LEGAL_REP_NAME.value(), "");
        caseData.put(LEGAL_REP_FAMILY_NAME.value(), "");
        caseData.put(LEGAL_REP_COMPANY.value(), "");
        caseData.put(LEGAL_REP_PHONE.value(), "");
        caseData.put(LEGAL_REP_REFERENCE.value(), "");
        caseData.put(LEGAL_REP_COMPANY_ADDRESS.value(), "");
        caseData.put(LEGAL_REP_EMAIL_ADDRESS.value(), "");
        caseData.put(IS_LEGALLY_REPRESENTED_FOR_FLAG.value(), "No");

        return submitEvent(
            tokens,
            caseId,
            startEventDetails,
            Event.CLEAR_LEGAL_REPRESENTATIVE_DETAILS,
            caseData
        );
    }

    public SubmitEventDetails setActiveInterpreterFlag(Callback<BailCase> callback) {
        String caseId = String.valueOf(callback.getCaseDetails().getId());
        Tokens tokens = getTokens(callback, Event.UPDATE_INTERPRETER_WA_TASK);
        CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        String jurisdiction = caseDetails.getJurisdiction();

        final StartEventDetails startEventDetails =
            getCase(
                tokens,
                jurisdiction,
                caseId,
                Event.UPDATE_INTERPRETER_WA_TASK
            );
        log.info("Case details found for the caseId: {}", callback.getCaseDetails().getId());

        Map<String, Object> caseData = new HashMap<>();

        Optional<StrategicCaseFlag> appellantLevelFlagsOptional = caseDetails.getCaseData().read(
            APPELLANT_LEVEL_FLAGS,
            StrategicCaseFlag.class
        );

        appellantLevelFlagsOptional.ifPresentOrElse(
            (appellantLevelFlags) -> {
                List<CaseFlagDetail> flags = appellantLevelFlags.getDetails();
                boolean hasInterpreterFlag = flags.stream()
                    .anyMatch(flag ->
                                  flag.getCaseFlagValue().getName().toLowerCase().contains("interpreter")
                                      && flag.getCaseFlagValue().getStatus().equals("Active"));
                caseData.put(HAS_ACTIVE_INTERPRETER_FLAG.value(), hasInterpreterFlag ? "Yes" : "No");
            },
            () -> caseData.put(HAS_ACTIVE_INTERPRETER_FLAG.value(), "No")
        );

        return submitEvent(
            tokens,
            caseId,
            startEventDetails,
            Event.UPDATE_INTERPRETER_WA_TASK,
            caseData
        );
    }

    private Tokens getTokens(Callback<BailCase> callback, Event event) {
        Tokens tokens = new Tokens();
        CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        String caseId = String.valueOf(caseDetails.getId());
        try {
            String userToken = systemTokenGenerator.generate();
            tokens.setUserToken(userToken);
            log.info("System user token has been generated for event: {}, caseId: {}.", event, caseId);

            tokens.setS2sToken(serviceAuthorization.generate());

            log.info("S2S token has been generated for event: {}, caseId: {}.", event, caseId);

            tokens.setUid(systemUserProvider.getSystemUserId(userToken));
            log.info("System user id has been fetched for event: {}, caseId: {}.", event, caseId);

        } catch (IdentityManagerResponseException ex) {

            log.error("Unauthorized access to getCaseById", ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }
        return tokens;
    }

    private StartEventDetails getCase(Tokens tokens, String jurisdiction, String caseId, Event event) {
        return ccdDataApi.startEvent(
            tokens.getUserToken(), tokens.getS2sToken(), tokens.getUid(), jurisdiction, CcdDataService.CASE_TYPE,
            caseId, event.toString()
        );
    }

    private SubmitEventDetails submitEvent(
        Tokens tokens, String caseId, StartEventDetails startEventDetails, Event event, Map<String, Object> caseData) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", event.toString());
        CaseDataContent request =
            new CaseDataContent(caseId, caseData, eventData, startEventDetails.getToken(), true);

        return ccdDataApi.submitEvent(tokens.getUserToken(), tokens.getS2sToken(), caseId, request);

    }

    private boolean isLegalRepDetailsMissing(BailCase bailCase) {

        return bailCase.read(LEGAL_REP_NAME, String.class).isEmpty()
            && bailCase.read(LEGAL_REP_EMAIL_ADDRESS, String.class).isEmpty()
            && bailCase.read(LEGAL_REP_PHONE, String.class).isEmpty()
            && bailCase.read(LEGAL_REP_COMPANY, String.class).isEmpty()
            && bailCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUK.class).isEmpty()
            && bailCase.read(LEGAL_REP_REFERENCE, String.class).isEmpty();
    }

    @Data
    private class Tokens {
        private String userToken;
        private String s2sToken;
        private String uid;
    }

}
