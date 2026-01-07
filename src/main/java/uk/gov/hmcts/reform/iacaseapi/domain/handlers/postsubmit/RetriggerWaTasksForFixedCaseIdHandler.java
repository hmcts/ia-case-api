package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CcdDataService;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_ID_LIST;

@Slf4j
@Component
public class RetriggerWaTasksForFixedCaseIdHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final CcdDataService ccdDataService;


    public RetriggerWaTasksForFixedCaseIdHandler(CcdDataService ccdDataService) {
        this.ccdDataService = ccdDataService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.RE_TRIGGER_WA_BULK_TASKS;

    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String caseIdList = asylumCase.read(CASE_ID_LIST, String.class)
            .orElse("");

        if (!caseIdList.contains(",")) {
            String trimmedCaseId = caseIdList.trim();
            if (trimmedCaseId.length() != 16) {
                log.info("No valid Case Ids found to re-trigger WA tasks");
                return new PreSubmitCallbackResponse<>(asylumCase);
            } else {
                ccdDataService.retriggerWaTasks(trimmedCaseId);
                asylumCase.clear(CASE_ID_LIST);
                return new PreSubmitCallbackResponse<>(asylumCase);
            }
        }
        String[] caseIdListList = caseIdList.split(",");

        Arrays.stream(caseIdListList)
            .forEach(caseId -> {
                    String trimmedCaseId = caseId.trim();
                    if (trimmedCaseId.length() != 16) {
                        log.info("Invalid Case Id found to re-trigger WA tasks: {}", trimmedCaseId);
                        return;
                    }
                    ccdDataService.retriggerWaTasks(trimmedCaseId);
                }
            );
        asylumCase.clear(CASE_ID_LIST);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
