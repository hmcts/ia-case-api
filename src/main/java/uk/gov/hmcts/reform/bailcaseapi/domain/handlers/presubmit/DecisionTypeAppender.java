package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_DETAILS_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_GRANTED_OR_REFUSED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_THE_DECISION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RELEASE_STATUS_YES_OR_NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SECRETARY_OF_STATE_YES_OR_NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SS_CONSENT_DECISION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.DecisionType;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class DecisionTypeAppender implements PreSubmitCallbackHandler<BailCase> {

    private final DateProvider dateProvider;

    private static final String REFUSED = "refused";
    private static final String GRANTED = "granted";

    public DecisionTypeAppender(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.RECORD_THE_DECISION;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        String decisionGrantedOrRefused = bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class).orElse("");
        String recordTheDecisionList = bailCase.read(RECORD_THE_DECISION_LIST, String.class).orElse("");
        YesOrNo releaseStatusYesOrNo = bailCase.read(RELEASE_STATUS_YES_OR_NO, YesOrNo.class).orElse(NO);
        YesOrNo ssConsentDecision = bailCase.read(SS_CONSENT_DECISION, YesOrNo.class).orElse(NO);
        YesOrNo secretaryOfStateConsentYesOrNo = bailCase.read(SECRETARY_OF_STATE_YES_OR_NO, YesOrNo.class).orElse(NO);

        String decisionDate = dateProvider.now().toString();

        if (decisionGrantedOrRefused.equals(REFUSED) || recordTheDecisionList.equals(REFUSED)
            || (secretaryOfStateConsentYesOrNo.equals(YES) && ssConsentDecision == NO)) {
            bailCase.write(RECORD_DECISION_TYPE, DecisionType.REFUSED);

        } else if ((decisionGrantedOrRefused.equals(GRANTED) && releaseStatusYesOrNo == YES)
                   || (ssConsentDecision == YES && releaseStatusYesOrNo == YES)) {
            bailCase.write(RECORD_DECISION_TYPE, DecisionType.GRANTED);

        } else if ((decisionGrantedOrRefused.equals(GRANTED) && releaseStatusYesOrNo == NO)
                   || (ssConsentDecision == YES && releaseStatusYesOrNo == NO)) {
            bailCase.write(RECORD_DECISION_TYPE, DecisionType.CONDITIONAL_GRANT);

        } else {
            throw new RuntimeException("Cannot assign a decision type");
        }

        bailCase.write(DECISION_DETAILS_DATE, decisionDate);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

}
