package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseFlagAppender;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class FlagCaseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final CaseFlagAppender caseFlagAppender;
    private final FeatureToggler featureToggler;

    public FlagCaseHandler(
        CaseFlagAppender caseFlagAppender,
        FeatureToggler featureToggler
    ) {
        this.caseFlagAppender = caseFlagAppender;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.FLAG_CASE;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final CaseFlagType flagType = asylumCase.read(FLAG_CASE_TYPE_OF_FLAG, CaseFlagType.class)
            .orElse(CaseFlagType.UNKNOWN);

        final String additionalInformation = asylumCase.read(FLAG_CASE_ADDITIONAL_INFORMATION, String.class)
            .orElse("");

        asylumCase.clear(FLAG_CASE_TYPE_OF_FLAG);
        asylumCase.clear(FLAG_CASE_ADDITIONAL_INFORMATION);

        final Optional<List<IdValue<CaseFlag>>> maybeExistingCaseFlags = asylumCase.read(CASE_FLAGS);
        final List<IdValue<CaseFlag>> existingCaseFlags = maybeExistingCaseFlags.orElse(Collections.emptyList());

        final List<IdValue<CaseFlag>> allCaseFlags = caseFlagAppender.append(
            existingCaseFlags,
            flagType,
            additionalInformation
        );

        asylumCase.write(CASE_FLAGS, allCaseFlags);

        switch (flagType) {
            case ANONYMITY:
                asylumCase.write(CASE_FLAG_ANONYMITY_EXISTS, YesOrNo.YES);
                asylumCase.write(CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION, additionalInformation);
                break;
            case COMPLEX_CASE:
                asylumCase.write(CASE_FLAG_COMPLEX_CASE_EXISTS, YesOrNo.YES);
                asylumCase.write(CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION, additionalInformation);
                break;
            case DEPORT:
                asylumCase.write(CASE_FLAG_DEPORT_EXISTS, YesOrNo.YES);
                asylumCase.write(CASE_FLAG_DEPORT_ADDITIONAL_INFORMATION, additionalInformation);
                break;
            case DETAINED_IMMIGRATION_APPEAL:
                asylumCase.write(CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_EXISTS, YesOrNo.YES);
                asylumCase.write(CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION, additionalInformation);
                break;
            case FOREIGN_NATIONAL_OFFENDER:
                asylumCase.write(CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_EXISTS, YesOrNo.YES);
                asylumCase.write(CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION, additionalInformation);
                break;
            case POTENTIALLY_VIOLENT_PERSON:
                asylumCase.write(CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_EXISTS, YesOrNo.YES);
                asylumCase.write(CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION, additionalInformation);
                break;
            case UNACCEPTABLE_CUSTOMER_BEHAVIOUR:
                asylumCase.write(CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_EXISTS, YesOrNo.YES);
                asylumCase.write(CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION, additionalInformation);
                break;
            case UNACCOMPANIED_MINOR:
                asylumCase.write(CASE_FLAG_UNACCOMPANIED_MINOR_EXISTS, YesOrNo.YES);
                asylumCase.write(CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION, additionalInformation);
                break;
            case SET_ASIDE_REHEARD:
                asylumCase.write(AsylumCaseFieldDefinition.IS_REHEARD_APPEAL_ENABLED, featureToggler.getValue("reheard-feature", false) ? YesOrNo.YES : YesOrNo.NO);
                asylumCase.write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.YES);
                asylumCase.write(CASE_FLAG_SET_ASIDE_REHEARD_ADDITIONAL_INFORMATION, additionalInformation);
                break;
            default:
                break;
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
