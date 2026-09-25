package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.time.LocalDate;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.STF_24W_DETERMINATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.handle24wValidity;

@Component
@Slf4j
public class Stf24wDeterminationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final UpdateStatutoryTimeframe24WeeksService updateStatutoryTimeframe24WeeksService;

    private final LocalDate stf24wLiveDate;

    public Stf24wDeterminationHandler(@Value("${app.statutory-timeframe.live-date}") String stf24wLiveDate,
                                      @Autowired UpdateStatutoryTimeframe24WeeksService updateStatutoryTimeframe24WeeksService) {
        this.stf24wLiveDate = LocalDate.parse(stf24wLiveDate);
        this.updateStatutoryTimeframe24WeeksService = updateStatutoryTimeframe24WeeksService;

    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage.equals(ABOUT_TO_SUBMIT)
            && callback.getEvent().equals(STF_24W_DETERMINATION)
            && callback.getCaseDetails().getCaseData()
            .read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class).isPresent();
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PreSubmitCallbackResponse<AsylumCase> response = handle24wValidity(callback, stf24wLiveDate);
        if (!response.getErrors().isEmpty()) {
            log.error("Error in STF 24w determination for case ID: {}\n {}",
                callback.getCaseDetails().getId(), String.join(",", response.getErrors()));
            return response;
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        YesOrNo status = asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class).orElse(YesOrNo.NO);
        AsylumCase updatedAsylum = updateStatutoryTimeframe24WeeksService.updateAsylumCaseFromDetermination(asylumCase, status);
        return new PreSubmitCallbackResponse<>(updatedAsylum);
    }
}
