package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class RequestRespondentEvidencePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final int requestRespondentEvidenceDueInDays;
    private final int requestRespondentEvidenceDueInDaysAda;
    private final int requestRespondentEvidenceDueInDaysDetained;
    private final FeatureToggler featureToggler;
    private final DateProvider dateProvider;
    private final DueDateService dueDateService;

    public RequestRespondentEvidencePreparer(
        @Value("${requestRespondentEvidence.dueInDays}") int requestRespondentEvidenceDueInDays,
        @Value("${requestRespondentEvidence.dueInDaysAda}") int requestRespondentEvidenceDueInDaysAda,
        @Value("${requestRespondentEvidence.dueInDaysDetained}") int requestRespondentEvidenceDueInDaysDetained,
        FeatureToggler featureToggler,
        DateProvider dateProvider,
        DueDateService dueDateService
    ) {
        this.requestRespondentEvidenceDueInDays = requestRespondentEvidenceDueInDays;
        this.requestRespondentEvidenceDueInDaysAda = requestRespondentEvidenceDueInDaysAda;
        this.requestRespondentEvidenceDueInDaysDetained = requestRespondentEvidenceDueInDaysDetained;
        this.featureToggler = featureToggler;
        this.dateProvider = dateProvider;
        this.dueDateService = dueDateService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.REQUEST_RESPONDENT_EVIDENCE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = new PreSubmitCallbackResponse<>(asylumCase);

        final AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("AppealType is not present."));

        if (featureToggler.getValue("home-office-uan-feature", false)
                && HomeOfficeAppealTypeChecker.isAppealTypeEnabled(featureToggler, appealType)) {

            boolean isInCountryAppeal = asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class).map(ooc -> NO == ooc).orElse(true);
            boolean isRehydratedAppeal = HandlerUtils.isRehydratedAppeal(asylumCase);

            if (isInCountryAppeal
                && shouldMatchAppellantDetails(asylumCase)
                && appellantDetailsNotMatchedOrFailed(asylumCase)
                && !isRehydratedAppeal) {
                callbackResponse
                    .addError("You need to match the appellant details before you can request the respondent evidence.");
                return callbackResponse;
            }
        }

        YesOrNo recordedOutOfTimeDecision = asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class).orElse(NO);

        if (recordedOutOfTimeDecision == YES) {

            OutOfTimeDecisionType outOfTimeDecisionType =
                asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)
                    .orElseThrow(() -> new IllegalStateException("Out of time decision type is not present"));

            if (outOfTimeDecisionType == OutOfTimeDecisionType.REJECTED) {

                callbackResponse.addError("Record out of time decision is rejected. The appeal must be ended.");
                return callbackResponse;
            }
        }


        asylumCase.write(SEND_DIRECTION_EXPLANATION, getDirectionExplanation(asylumCase));

        asylumCase.write(SEND_DIRECTION_PARTIES, Parties.RESPONDENT);

        LocalDate dueDate = getDueDate(asylumCase);

        asylumCase.write(SEND_DIRECTION_DATE_DUE, dueDate.toString());

        asylumCase.write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean shouldMatchAppellantDetails(AsylumCase asylumCase) {
        return !(HandlerUtils.isAgeAssessmentAppeal(asylumCase));
    }

    private boolean appellantDetailsNotMatchedOrFailed(AsylumCase asylumCase) {
        Optional<String> homeOfficeSearchStatus = asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class);

        return homeOfficeSearchStatus.isEmpty()
            || Arrays.asList("FAIL", "MULTIPLE").contains(homeOfficeSearchStatus.get());
    }

    private LocalDate getDueDate(AsylumCase asylumCase) {
        LocalDate dueDate;

        if (HandlerUtils.isAcceleratedDetainedAppeal(asylumCase)) {
            dueDate = dueDateService.calculateDueDate(dateProvider.now().atStartOfDay(ZoneOffset.UTC), requestRespondentEvidenceDueInDaysAda).toLocalDate();
        } else if (HandlerUtils.isAppellantInDetention(asylumCase)) {
            dueDate = dateProvider.now().plusDays(requestRespondentEvidenceDueInDaysDetained);
        } else {
            dueDate = dateProvider.now().plusDays(requestRespondentEvidenceDueInDays);
        }

        return dueDate;
    }

    private String getDirectionExplanation(AsylumCase asylumCase) {
        if (HandlerUtils.isAppellantInDetention(asylumCase)) {
            return "A notice of appeal has been lodged against this decision.\n\n"
                + "You must now upload all documents to the Tribunal. The Tribunal will make them accessible to the other party. You have until the date indicated below to supply your bundle.\n\n"
                + "The bundle must comply with (i) Rule 24 of the Tribunal Procedure Rules 2014 and (ii) Practice Direction, Part 3, sections 7.1 - 7.4.\n"
                + "Specifically, the bundle must contain:\n\n"
                + "- The explanation for refusal;\n"
                + "- the deportation order and/or the notice of decision to make the order (if any);\n"
                + "- Interview record (if any);\n"
                + "- Appellant's representations (if any);\n"
                + "- a copy of any relevant indictment; pre-sentence report and/or OASys/ARNS reports (if any);\n"
                + "- a copy of the Appellant's criminal record (if any);\n"
                + "- a copy of the certificate of conviction (if any);\n"
                + "- a transcript of the Sentencing Judge's Remarks (if any).\n"
                + "- a copy of any Parole Report or other document relating to the appellant's period in custody and/or release (if any);\n"
                + "- a copy of any medical report (if any).\n\n"
                + "Parties must ensure they conduct proceedings with procedural rigour.\n"
                + "The Tribunal will not overlook breaches of the requirements of the Procedure Rules, Practice Statement or Practice Direction, "
                + "nor failures to comply with directions issued by the Tribunal. Parties are reminded of the possible sanctions for "
                + "non-compliance set out in paragraph 5.3 of the Practice Direction.";
        } else {
            return "A notice of appeal has been lodged against this decision.\n\n"
                + "By the date indicated below the respondent is directed to supply the documents:\n\n"
                + "The bundle must comply with (i) Rule 23 or Rule 24 of the Tribunal Procedure Rules 2014 (as applicable) "
                + "and (ii) Practice Direction (1.11.2024) Part 3, sections 7.1 - 7.4. Specifically, the bundle must contain:\n\n"
                + "- the notice of decision appealed against.\n"
                + "- any other document provided to the appellant giving reasons for that decision.\n"
                + "- any evidence or material relevant to the disputed issues.\n"
                + "- any statements of evidence.\n"
                + "- the application form.\n"
                + "- any record of interview with the appellant in relation to the decision being appealed.\n"
                + "- any previous decision(s) of the Tribunal and Upper Tribunal (IAC) relating to the appellant.\n"
                + "- any other unpublished documents on which you rely.\n"
                + "- the notice of any other appealable decision made in relation to the appellant.\n\n"
                + "Where the appeal involves deportation, you must also include the following evidence:\n\n"
                + "- a copy of the Certificate of Conviction.\n"
                + "- a copy of any indictment/charge.\n"
                + "- a transcript of the Sentencing Judge's Remarks.\n"
                + "- a copy of any Pre-Sentence Report.\n"
                + "- a copy of the appellant's criminal record.\n"
                + "- a copy of any Parole Report or other document relating to the appellant's period in custody and/or release.\n"
                + "- a copy of any mental health report.\n\n"
                + "Parties must ensure they conduct proceedings with procedural rigour. "
                + "The Tribunal will not overlook breaches of the requirements of the Procedure Rules, Practice Statement or Practice Direction, "
                + "nor failures to comply with directions issued by the Tribunal. "
                + "Parties are reminded of the sanctions for non-compliance set out in paragraph 5.3 of the Practice Direction of 01.11.24.";
        }
    }
}
