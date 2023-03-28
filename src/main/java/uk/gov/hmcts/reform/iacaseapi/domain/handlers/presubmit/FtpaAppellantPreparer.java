package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class FtpaAppellantPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private static final int FTPA_DAYS_ALLOWED_UK = 14;
    private static final int FTPA_DAYS_ALLOWED_OOC = 28;
    private final DateProvider dateProvider;
    private final FeatureToggler featureToggler;

    public FtpaAppellantPreparer(
        DateProvider dateProvider,
        FeatureToggler featureToggler
    ) {
        this.dateProvider = dateProvider;
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

        if (isFtpaSetAsideAndReheard) {
            asylumCase.clear(FTPA_APPELLANT_GROUNDS_DOCUMENTS);
            asylumCase.clear(FTPA_APPELLANT_EVIDENCE_DOCUMENTS);
        }

        final Optional<String> ftpaApplicationDeadline =
                asylumCase.read(AsylumCaseFieldDefinition.FTPA_APPLICATION_DEADLINE, String.class);

        if (ftpaApplicationDeadline.isEmpty()) {
            // For in-flight cases
            asylumCase.write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, checkInFlightCaseFtpaOutOfTime(asylumCase));
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        LocalDate ftpaApplicationDeadlineDate = LocalDate.parse(ftpaApplicationDeadline.get());

        if (dateProvider.now().isAfter(ftpaApplicationDeadlineDate)) {
            asylumCase.write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, YES);
        } else {
            asylumCase.write(FTPA_APPELLANT_SUBMISSION_OUT_OF_TIME, NO);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private YesOrNo checkInFlightCaseFtpaOutOfTime(AsylumCase asylumCase) {
        Optional<String> appealDate = asylumCase.read(APPEAL_DATE, String.class);
        Optional<YesOrNo> appellantInUk = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class);
        if (appealDate.isEmpty()) {
            throw new RequiredFieldMissingException("Appeal date missing.");
        }
        if (appellantInUk.isEmpty()) {
            throw new RequiredFieldMissingException("Appellant in UK missing.");
        }

        LocalDate ftpaApplicationDeadline;
        if (appellantInUk.equals(Optional.of(YES))) {
            ftpaApplicationDeadline = LocalDate.parse(appealDate.get()).plusDays(FTPA_DAYS_ALLOWED_UK);
        } else {
            ftpaApplicationDeadline = LocalDate.parse(appealDate.get()).plusDays(FTPA_DAYS_ALLOWED_OOC);
        }

        return dateProvider.now().isBefore(ftpaApplicationDeadline) ? NO : YES;
    }

}
