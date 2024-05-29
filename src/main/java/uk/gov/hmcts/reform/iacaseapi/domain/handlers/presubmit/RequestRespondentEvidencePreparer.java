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
    private final FeatureToggler featureToggler;
    private final DateProvider dateProvider;
    private final DueDateService dueDateService;

    public RequestRespondentEvidencePreparer(
        @Value("${requestRespondentEvidence.dueInDays}") int requestRespondentEvidenceDueInDays,
        @Value("${requestRespondentEvidence.dueInDaysAda}") int requestRespondentEvidenceDueInDaysAda,
        FeatureToggler featureToggler,
        DateProvider dateProvider,
        DueDateService dueDateService
    ) {
        this.requestRespondentEvidenceDueInDays = requestRespondentEvidenceDueInDays;
        this.requestRespondentEvidenceDueInDaysAda = requestRespondentEvidenceDueInDaysAda;
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

            if (isInCountryAppeal
                && shouldMatchAppellantDetails(asylumCase)
                && appellantDetailsNotMatchedOrFailed(asylumCase)) {

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


        asylumCase.write(SEND_DIRECTION_EXPLANATION,
            "A notice of appeal has been lodged against this decision.\n\n"
            + "You must now upload all documents to the Tribunal. The Tribunal will make them accessible to the other party. "
            + "You have until the date indicated below to supply the documents.\n\n"
            + "You must include:\n"
            + "- the notice of decision\n"
            + "- any other document provided to the appellant giving reasons for that decision\n"
            + "- any statements of evidence\n"
            + "- the application form\n"
            + "- any record of interview with the appellant in relation to the decision being appealed\n"
            + "- any other unpublished documents on which you rely\n"
            + "- the notice of any other appealable decision made in relation to the appellant"
        );

        asylumCase.write(SEND_DIRECTION_PARTIES, Parties.RESPONDENT);

        LocalDate dueDate = HandlerUtils.isAcceleratedDetainedAppeal(asylumCase)
                ? dueDateService.calculateDueDate(dateProvider.now().atStartOfDay(ZoneOffset.UTC), requestRespondentEvidenceDueInDaysAda).toLocalDate()
                : dateProvider.now().plusDays(requestRespondentEvidenceDueInDays);

        asylumCase.write(SEND_DIRECTION_DATE_DUE, dueDate.toString());

        asylumCase.write(UPLOAD_HOME_OFFICE_BUNDLE_ACTION_AVAILABLE, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean shouldMatchAppellantDetails(AsylumCase asylumCase) {
        return !(HandlerUtils.isAgeAssessmentAppeal(asylumCase) || HandlerUtils.isAppellantInDetention(asylumCase));
    }

    private boolean appellantDetailsNotMatchedOrFailed(AsylumCase asylumCase) {
        Optional<String> homeOfficeSearchStatus = asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class);

        return homeOfficeSearchStatus.isEmpty()
            || Arrays.asList("FAIL", "MULTIPLE").contains(homeOfficeSearchStatus.get());
    }
}
