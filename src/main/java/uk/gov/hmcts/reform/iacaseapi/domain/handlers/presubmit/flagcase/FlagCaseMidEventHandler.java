package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.flagcase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_DEPORT_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_SET_ASIDE_REHEARD_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FLAG_CASE_ADDITIONAL_INFORMATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FLAG_CASE_TYPE_OF_FLAG;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class FlagCaseMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
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

        String additionalInfo;
        switch (flagType) {
            case ANONYMITY:
                additionalInfo = asylumCase.read(CASE_FLAG_ANONYMITY_ADDITIONAL_INFORMATION, String.class)
                    .orElse(StringUtils.EMPTY);
                asylumCase.write(FLAG_CASE_ADDITIONAL_INFORMATION, additionalInfo);
                break;
            case COMPLEX_CASE:
                additionalInfo = asylumCase.read(CASE_FLAG_COMPLEX_CASE_ADDITIONAL_INFORMATION, String.class)
                    .orElse(StringUtils.EMPTY);
                asylumCase.write(FLAG_CASE_ADDITIONAL_INFORMATION, additionalInfo);
                break;
            case DEPORT:
                additionalInfo = asylumCase.read(CASE_FLAG_DEPORT_ADDITIONAL_INFORMATION, String.class)
                    .orElse(StringUtils.EMPTY);
                asylumCase.write(FLAG_CASE_ADDITIONAL_INFORMATION, additionalInfo);
                break;
            case DETAINED_IMMIGRATION_APPEAL:
                additionalInfo = asylumCase.read(
                    CASE_FLAG_DETAINED_IMMIGRATION_APPEAL_ADDITIONAL_INFORMATION, String.class).orElse(StringUtils.EMPTY);
                asylumCase.write(FLAG_CASE_ADDITIONAL_INFORMATION, additionalInfo);
                break;
            case FOREIGN_NATIONAL_OFFENDER:
                additionalInfo = asylumCase.read(
                    CASE_FLAG_FOREIGN_NATIONAL_OFFENDER_ADDITIONAL_INFORMATION, String.class).orElse(StringUtils.EMPTY);
                asylumCase.write(FLAG_CASE_ADDITIONAL_INFORMATION, additionalInfo);
                break;
            case POTENTIALLY_VIOLENT_PERSON:
                additionalInfo = asylumCase.read(
                    CASE_FLAG_POTENTIALLY_VIOLENT_PERSON_ADDITIONAL_INFORMATION, String.class).orElse(StringUtils.EMPTY);
                asylumCase.write(FLAG_CASE_ADDITIONAL_INFORMATION, additionalInfo);
                break;
            case UNACCEPTABLE_CUSTOMER_BEHAVIOUR:
                additionalInfo = asylumCase.read(
                    CASE_FLAG_UNACCEPTABLE_CUSTOMER_BEHAVIOUR_ADDITIONAL_INFORMATION, String.class).orElse(StringUtils.EMPTY);
                asylumCase.write(FLAG_CASE_ADDITIONAL_INFORMATION, additionalInfo);
                break;
            case UNACCOMPANIED_MINOR:
                additionalInfo = asylumCase.read(CASE_FLAG_UNACCOMPANIED_MINOR_ADDITIONAL_INFORMATION, String.class)
                    .orElse(StringUtils.EMPTY);
                asylumCase.write(FLAG_CASE_ADDITIONAL_INFORMATION, additionalInfo);
                break;
            case SET_ASIDE_REHEARD:
                additionalInfo = asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_ADDITIONAL_INFORMATION, String.class)
                    .orElse(StringUtils.EMPTY);
                asylumCase.write(FLAG_CASE_ADDITIONAL_INFORMATION, additionalInfo);
                break;
            default:
                break;
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
