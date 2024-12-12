package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TtlCcdObject;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.TtlProvider;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class AppealSetDraftTtlHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private static final String TTL_SUSPENDED_NO = "No";

    private final TtlProvider ttlProvider;

    public AppealSetDraftTtlHandler(TtlProvider ttlProvider) {
        this.ttlProvider = ttlProvider;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String ttlString = ttlProvider.getTtl().toString();

        TtlCcdObject ttlCcdObject = TtlCcdObject.builder()
                .suspended(TTL_SUSPENDED_NO)
                .overrideTTL(ttlString)
                .systemTTL(ttlString)
                .build();

        asylumCase.write(AsylumCaseFieldDefinition.TTL, ttlCcdObject);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String ttlJsonString = objectMapper.writeValueAsString(ttlCcdObject);
            log.info(
                    "Setting deletionDate when starting appeal, caseId {}, ttlJsonString {}",
                    callback.getCaseDetails().getId(),
                    ttlJsonString
            );
        } catch (JsonProcessingException e) {
            log.error("Error", e);
        }
        
        log.info(
            "Setting deletionDate when starting appeal, caseId {}, ttlDetails {}",
            callback.getCaseDetails().getId(),
            ttlCcdObject
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.START_APPEAL;
    }
}
