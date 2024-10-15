package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo.YES;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.BailCaseUtils;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingHearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.DecisionType;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.*;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.Appender;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class DecisionTypeAppender implements PreSubmitCallbackHandler<BailCase> {

    private final Appender<PreviousDecisionDetails> previousDecisionDetailsAppender;
    private final DateProvider dateProvider;

    private static final String REFUSED = "refused";
    private static final String GRANTED = "granted";
    private static final String REFUSED_UNDER_IMA = "refusedUnderIma";

    public DecisionTypeAppender(
        Appender<PreviousDecisionDetails> previousDecisionDetailsAppender,
        DateProvider dateProvider
    ) {
        this.previousDecisionDetailsAppender = previousDecisionDetailsAppender;
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

        String decisionGrantedOrRefused = BailCaseUtils.isImaEnabled(bailCase)
            ? bailCase.read(DECISION_GRANTED_OR_REFUSED_IMA, String.class).orElse("")
            : bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class).orElse("");
        String recordTheDecisionList = BailCaseUtils.isImaEnabled(bailCase)
            ? bailCase.read(RECORD_THE_DECISION_LIST_IMA, String.class).orElse("")
            : bailCase.read(RECORD_THE_DECISION_LIST, String.class).orElse("");
        YesOrNo releaseStatusYesOrNo = bailCase.read(RELEASE_STATUS_YES_OR_NO, YesOrNo.class).orElse(NO);
        YesOrNo ssConsentDecision = bailCase.read(SS_CONSENT_DECISION, YesOrNo.class).orElse(NO);
        YesOrNo secretaryOfStateConsentYesOrNo = bailCase.read(SECRETARY_OF_STATE_YES_OR_NO, YesOrNo.class).orElse(NO);

        String decisionDate = dateProvider.now().toString();

        if (
            BailCaseUtils.isImaEnabled(bailCase)
                && (decisionGrantedOrRefused.equals(REFUSED_UNDER_IMA)
                || recordTheDecisionList.equals(REFUSED_UNDER_IMA))) {
            bailCase.write(RECORD_DECISION_TYPE, DecisionType.REFUSED_UNDER_IMA);

        } else if (decisionGrantedOrRefused.equals(REFUSED) || recordTheDecisionList.equals(REFUSED)
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
        // Following two definitions are needed for UI only.
        // It is to have different section for Unsigned Decision Details for Admin and Judges.
        bailCase.write(DECISION_UNSIGNED_DETAILS_DATE, decisionDate);
        bailCase.write(
            RECORD_UNSIGNED_DECISION_TYPE,
            bailCase.read(RECORD_DECISION_TYPE, String.class)
                .orElseThrow(() -> new IllegalStateException("Record decision type missing"))
        );
        bailCase.clear(HAS_CASE_BEEN_FORCED_TO_HEARING);
        CaseDetails<BailCase> caseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);
        BailCase bailCaseBefore = caseDetailsBefore == null ? null : caseDetailsBefore.getCaseData();
        if (bailCaseBefore != null) {
            String prevDecisionDetailsDate = bailCaseBefore.read(DECISION_DETAILS_DATE, String.class)
                .orElse(null);
            String prevRecordDecisionType = bailCaseBefore.read(RECORD_DECISION_TYPE, String.class)
                .orElse(null);
            Document prevUploadSignedDecisionNoticeDocument = bailCaseBefore.read(
                    UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, Document.class)
                .orElse(null);
            if (prevDecisionDetailsDate != null && prevRecordDecisionType != null && prevUploadSignedDecisionNoticeDocument != null) {
                Optional<List<IdValue<PreviousDecisionDetails>>> maybeExistingPreviousDecisionDetails =
                    bailCase.read(PREVIOUS_DECISION_DETAILS);
                final PreviousDecisionDetails newPreviousDecisionDetails = new PreviousDecisionDetails(
                    prevDecisionDetailsDate, prevRecordDecisionType, prevUploadSignedDecisionNoticeDocument);
                List<IdValue<PreviousDecisionDetails>> allPreviousDecisionDetails = previousDecisionDetailsAppender
                    .append(
                        newPreviousDecisionDetails,
                        maybeExistingPreviousDecisionDetails.orElseGet(Collections::emptyList)
                    );
                bailCase.write(PREVIOUS_DECISION_DETAILS, allPreviousDecisionDetails);
                bailCase.clear(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT);
            }
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

}
