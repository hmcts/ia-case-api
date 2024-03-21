package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;


@Component
public class FtpaAppellantPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final int ftpaAppellantAppealOutOfTimeDaysUk;
    private final int ftpaAppellantAppealOutOfTimeDaysOoc;
    private final FeatureToggler featureToggler;

    public FtpaAppellantPreparer(
        DateProvider dateProvider,
        @Value("${ftpaAppellantAppealOutOfTimeDaysUk}") int ftpaAppellantAppealOutOfTimeDaysUk,
        @Value("${ftpaAppellantAppealOutOfTimeDaysOoc}") int ftpaAppellantAppealOutOfTimeDaysOoc,
        FeatureToggler featureToggler
    ) {
        this.dateProvider = dateProvider;
        this.ftpaAppellantAppealOutOfTimeDaysUk = ftpaAppellantAppealOutOfTimeDaysUk;
        this.ftpaAppellantAppealOutOfTimeDaysOoc = ftpaAppellantAppealOutOfTimeDaysOoc;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.APPLY_FOR_FTPA_APPELLANT;
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

        final Optional<String> mayBeAppellantAppealSubmitted = asylumCase.read(FTPA_APPELLANT_SUBMITTED);

        final boolean isFtpaSetAsideAndReheard =
            asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
            && featureToggler.getValue("reheard-feature", false);

        if (mayBeAppellantAppealSubmitted.isPresent() && mayBeAppellantAppealSubmitted.get().equals("Yes") && !isFtpaSetAsideAndReheard) {
            final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("You've already submitted an application. You can only make one application at a time.");
            return asylumCasePreSubmitCallbackResponse;
        }

        asylumCase.clear(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
        asylumCase.clear(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);

        final Optional<String> mayBeAppealDate = asylumCase.read(APPEAL_DATE);

        Optional<OutOfCountryDecisionType> maybeOutOfCountryDecisionType = asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);

        final int ftpaAppellantAppealOutOfTimeDays =  maybeOutOfCountryDecisionType.isPresent() ? ftpaAppellantAppealOutOfTimeDaysOoc : ftpaAppellantAppealOutOfTimeDaysUk;

        if (mayBeAppealDate.filter(s -> dateProvider.now().isAfter(LocalDate.parse(s).plusDays(ftpaAppellantAppealOutOfTimeDays))).isPresent()) {
            asylumCase.write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YES);
        } else {
            asylumCase.write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, NO);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
