package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ReviewDraftHearingRequirementsPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    public ReviewDraftHearingRequirementsPreparer(
        UserDetails userDetails, UserDetailsHelper userDetailsHelper
    ) {
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.REVIEW_HEARING_REQUIREMENTS;
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

        final Optional<YesOrNo> reviewedHearingRequirements =
            asylumCase.read(AsylumCaseFieldDefinition.REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class);

        final boolean exAdaWithSubmittedHearingRequirements =
            asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES)
            && asylumCase.read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES);

        // If Judge tries to trigger this for any non-ADA case, an error will be thrown on UI
        if (isJudgeAndNonAdaAppeal(asylumCase)) {
            final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("This option is not available. You can only review hearing requirements for accelerated detained appeals.");
            return asylumCasePreSubmitCallbackResponse;
        }

        if (!reviewedHearingRequirements.isPresent()) {
            final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("The case is already listed, you can't request hearing requirements");
            return asylumCasePreSubmitCallbackResponse;
        }

        // prevent triggering if hearing requirements already reviewed or if case has transferred out of ADA after having
        // already submitted hearing requirements

        if (callback.getEvent() == Event.REVIEW_HEARING_REQUIREMENTS
            && (reviewedHearingRequirements.get().equals(YesOrNo.YES) || exAdaWithSubmittedHearingRequirements)) {
            final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("You've made an invalid request. The hearing requirements have already been reviewed.");
            return asylumCasePreSubmitCallbackResponse;
        }

        decorateWitnessAndInterpreterDetails(asylumCase);

        decorateOutsideEvidenceDefaultsForOldCases(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    static void decorateWitnessAndInterpreterDetails(AsylumCase asylumCase) {

        final Optional<List<IdValue<WitnessDetails>>> witnessDetails = asylumCase.read(WITNESS_DETAILS);
        final Optional<List<IdValue<InterpreterLanguage>>> interpreterLanguage = asylumCase.read(INTERPRETER_LANGUAGE);

        witnessDetails.ifPresent(idValues -> asylumCase.write(WITNESS_DETAILS_READONLY, idValues
            .stream()
            .map(w ->
                "Name\t\t" + w.getValue().getWitnessName())
            .collect(Collectors.joining("\n"))));

        interpreterLanguage.ifPresent(idValues -> asylumCase.write(INTERPRETER_LANGUAGE_READONLY, idValues
            .stream()
            .map(i ->
                "Language\t\t" + i.getValue().getLanguage() + "\nDialect\t\t\t" + i.getValue().getLanguageDialect() + "\n")
            .collect(Collectors.joining("\n"))));
    }

    static void decorateOutsideEvidenceDefaultsForOldCases(AsylumCase asylumCase) {

        final Optional<YesOrNo> isAppealOutOfCountry =
                asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class);

        final Optional<YesOrNo> isEvidenceFromOutsideUkOoc =
            asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_OOC, YesOrNo.class);

        final Optional<YesOrNo> isEvidenceFromOutsideUkInCountry =
            asylumCase.read(IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY, YesOrNo.class);


        if (!isAppealOutOfCountry.isPresent()) {
            asylumCase.write(APPEAL_OUT_OF_COUNTRY, YesOrNo.NO);
        }

        if (!isEvidenceFromOutsideUkOoc.isPresent()) {
            asylumCase.write(IS_EVIDENCE_FROM_OUTSIDE_UK_OOC, YesOrNo.NO);
        }

        if (!isEvidenceFromOutsideUkInCountry.isPresent()) {
            asylumCase.write(IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY, YesOrNo.NO);
        }
    }

    private boolean isJudgeAndNonAdaAppeal(AsylumCase asylumCase) {

        boolean isAcceleratedDetainedAppeal = HandlerUtils.isAcceleratedDetainedAppeal(asylumCase);

        return userDetailsHelper.getLoggedInUserRoleLabel(userDetails).equals(UserRoleLabel.JUDGE) && !isAcceleratedDetainedAppeal;
    }

}
