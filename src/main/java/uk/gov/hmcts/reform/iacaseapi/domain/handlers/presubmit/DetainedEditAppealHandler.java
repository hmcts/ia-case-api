package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.OTHER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class DetainedEditAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.EDIT_APPEAL);
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

        YesOrNo appellantInDetention = asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(NO);
        YesOrNo isAda = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(NO);
        YesOrNo adaSuitabilityHearingType = asylumCase.read(SUITABILITY_HEARING_TYPE_YES_OR_NO, YesOrNo.class).orElse(NO);
        YesOrNo suitabilityAppellantAttendanceYesOrNo1 = asylumCase.read(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1, YesOrNo.class).orElse(NO);
        YesOrNo suitabilityAppellantAttendanceYesOrNo2 = asylumCase.read(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2, YesOrNo.class).orElse(NO);

        if (appellantInDetention.equals(YES)) {
            //Clear all 'non-detained' fields when switching to a detained case
            asylumCase.clear(APPELLANT_HAS_FIXED_ADDRESS);
            String detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class)
                .orElseThrow(() -> new IllegalStateException("detentionFacility missing for appellant in detention"));

            if (!detentionFacility.equals(OTHER.getValue())) {
                // We use this to store "Other" detention facility address
                asylumCase.clear(APPELLANT_ADDRESS);
            }
            asylumCase.clear(SEARCH_POSTCODE);

            asylumCase.clear(HAS_CORRESPONDENCE_ADDRESS);
            asylumCase.clear(APPELLANT_OUT_OF_COUNTRY_ADDRESS);

            asylumCase.clear(CONTACT_PREFERENCE);
            asylumCase.clear(EMAIL);
            asylumCase.clear(MOBILE_NUMBER);

            // For ADA cases, clear unused suitability attendance value after editing hearing type value
            if (isAda.equals(YES)) {
                if (adaSuitabilityHearingType.equals(YES)) {
                    asylumCase.clear(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2);
                } else {
                    asylumCase.clear(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1);
                }

                // For either attendance value edited to 'No', clear interpreter screen fields
                // Midevent will write old value to No by default
                if ((suitabilityAppellantAttendanceYesOrNo1.equals(NO) && suitabilityAppellantAttendanceYesOrNo2.equals(NO))) {
                    asylumCase.clear(SUITABILITY_INTERPRETER_SERVICES_YES_OR_NO);
                    asylumCase.clear(SUITABILITY_INTERPRETER_SERVICES_LANGUAGE);
                }
            } else {
                asylumCase.clear(SUITABILITY_HEARING_TYPE_YES_OR_NO);
                asylumCase.clear(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1);
                asylumCase.clear(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2);
                asylumCase.clear(SUITABILITY_INTERPRETER_SERVICES_YES_OR_NO);
                asylumCase.clear(SUITABILITY_INTERPRETER_SERVICES_LANGUAGE);
            }

        } else if (appellantInDetention.equals(NO)) {
            // Clear all 'detained' fields when switching to non-detained case
            asylumCase.clear(DETENTION_STATUS);
            asylumCase.clear(DETENTION_FACILITY);

            asylumCase.clear(PRISON_NAME);
            asylumCase.clear(PRISON_NOMS);
            asylumCase.clear(CUSTODIAL_SENTENCE);
            asylumCase.clear(DATE_CUSTODIAL_SENTENCE);

            asylumCase.clear(OTHER_DETENTION_FACILITY_NAME);

            asylumCase.clear(IRC_NAME);

            asylumCase.clear(HAS_PENDING_BAIL_APPLICATIONS);
            asylumCase.clear(BAIL_APPLICATION_NUMBER);

            asylumCase.clear(IS_ACCELERATED_DETAINED_APPEAL);
            asylumCase.clear(REMOVAL_ORDER_OPTIONS);
            asylumCase.clear(REMOVAL_ORDER_DATE);

            asylumCase.clear(SUITABILITY_HEARING_TYPE_YES_OR_NO);
            asylumCase.clear(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1);
            asylumCase.clear(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2);
            asylumCase.clear(SUITABILITY_INTERPRETER_SERVICES_YES_OR_NO);
            asylumCase.clear(SUITABILITY_INTERPRETER_SERVICES_LANGUAGE);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
