package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REMOVE_DETAINED_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class RemoveDetainedStatusHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == REMOVE_DETAINED_STATUS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        if (HandlerUtils.isAppellantInDetention(asylumCase)) {

            clearDetentionRelatedFields(asylumCase);

            asylumCase.write(APPELLANT_IN_DETENTION, NO);

        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void clearDetentionRelatedFields(AsylumCase asylumCase) {

        log.info("Event: Remove Detained status - Clearing Detention related fields");
        asylumCase.clear(DETENTION_FACILITY);
        asylumCase.clear(IRC_NAME);
        asylumCase.clear(PRISON_NAME);
        asylumCase.clear(OTHER_DETENTION_FACILITY_NAME);
        asylumCase.clear(PRISON_NOMS);
        asylumCase.clear(CUSTODIAL_SENTENCE);
        asylumCase.clear(DATE_CUSTODIAL_SENTENCE);
        asylumCase.clear(HAS_PENDING_BAIL_APPLICATIONS);
        asylumCase.clear(BAIL_APPLICATION_NUMBER);
        asylumCase.clear(REMOVAL_ORDER_OPTIONS);
        asylumCase.clear(REMOVAL_ORDER_DATE);
        asylumCase.clear(DETENTION_STATUS);
    }

}
