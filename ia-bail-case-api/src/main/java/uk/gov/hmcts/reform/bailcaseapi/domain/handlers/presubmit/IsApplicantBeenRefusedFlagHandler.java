package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.time.LocalDate.parse;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_BEEN_REFUSED_BAIL;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DECISION_DETAILS_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.BailCaseUtils;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.DecisionType;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class IsApplicantBeenRefusedFlagHandler implements PreSubmitCallbackHandler<BailCase> {

    private final DateProvider dateProvider;
    private final int bailRefusalWithInDays;

    public IsApplicantBeenRefusedFlagHandler(DateProvider dateProvider,
                                             @Value("${bailRefusalWithInDays}") int bailRefusalWithInDays) {
        this.dateProvider = dateProvider;
        this.bailRefusalWithInDays = bailRefusalWithInDays;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.MAKE_NEW_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        CaseDetails<BailCase> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);

        if (caseDetailsBefore == null) {
            throw new IllegalStateException("Case details before missing");
        }

        BailCase caseDataBefore = caseDetailsBefore.getCaseData();

        String recordDecisionType =
            caseDataBefore.read(RECORD_DECISION_TYPE, String.class).orElse("");

        if (recordDecisionType.equals(DecisionType.REFUSED.toString())
            || (BailCaseUtils.isImaEnabled(caseDataBefore)
            && recordDecisionType.equals(DecisionType.REFUSED_UNDER_IMA.toString()))) {
            String maybeRecordDecisionDate =
                caseDataBefore.read(DECISION_DETAILS_DATE, String.class).orElse("");

            if (maybeRecordDecisionDate.isBlank()) {
                throw new RequiredFieldMissingException("decisionDetailsDate is not present");
            }

            LocalDate decisionDate =
                parse(maybeRecordDecisionDate);

            if (decisionDate != null
                && decisionDate.plusDays(bailRefusalWithInDays).isAfter(dateProvider.now())) {
                bailCase.write(APPLICANT_BEEN_REFUSED_BAIL, YES);
            } else {
                bailCase.write(APPLICANT_BEEN_REFUSED_BAIL, NO);
            }
        } else {
            bailCase.write(APPLICANT_BEEN_REFUSED_BAIL, NO);
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
