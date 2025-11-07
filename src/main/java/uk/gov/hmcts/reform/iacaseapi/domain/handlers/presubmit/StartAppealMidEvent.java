package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.HomeOfficeReferenceHandler.HOME_OFFICE_REF_PATTERN;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class StartAppealMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String HOME_OFFICE_REFERENCE_NUMBER_PAGE_ID = "homeOfficeReferenceNumber";
    private static final String OUT_OF_COUNTRY_PAGE_ID = "outOfCountry";
    private static final String DETENTION_FACILITY_PAGE_ID = "detentionFacility";
    private static final String SUITABILITY_ATTENDANCE_PAGE_ID = "suitabilityAppellantAttendance";
    private static final String UPPER_TRIBUNAL_REFERENCE_NUMBER_PAGE_ID = "utReferenceNumber";
    private static final String APPELLANTS_ADDRESS_PAGE_ID = "appellantAddress";
    protected static final String APPELLANTS_ADDRESS_ADMIN_J_PAGE_ID = "appellantAddressAdminJ";
    private static final Pattern UPPER_TRIBUNAL_REFERENCE_NUMBER_PATTERN = Pattern.compile("^UI-[0-9]{4}-[0-9]{6}$");

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && (callback.getEvent() == Event.START_APPEAL
                    || callback.getEvent() == Event.EDIT_APPEAL
                    || callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT
                    || callback.getEvent() == Event.UPDATE_DETENTION_LOCATION)
               && (callback.getPageId().equals(HOME_OFFICE_REFERENCE_NUMBER_PAGE_ID)
                    || callback.getPageId().equals(OUT_OF_COUNTRY_PAGE_ID)
                    || callback.getPageId().equals(DETENTION_FACILITY_PAGE_ID)
                    || callback.getPageId().equals(SUITABILITY_ATTENDANCE_PAGE_ID)
                    || callback.getPageId().equals(UPPER_TRIBUNAL_REFERENCE_NUMBER_PAGE_ID)
                    || callback.getPageId().equals(APPELLANTS_ADDRESS_PAGE_ID)
                    || callback.getPageId().equals(APPELLANTS_ADDRESS_ADMIN_J_PAGE_ID));
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

        YesOrNo isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO);
        YesOrNo appellantInUk = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).orElse(YesOrNo.NO);

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (callback.getPageId().equals(DETENTION_FACILITY_PAGE_ID)
                && List.of(Event.START_APPEAL, Event.EDIT_APPEAL).contains(callback.getEvent())
                && !asylumCase.read(DETENTION_FACILITY, String.class).orElse("").equals(DetentionFacility.IRC.toString())
        ) {
            asylumCase.write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.NO);
        }

        if (callback.getPageId().equals(HOME_OFFICE_REFERENCE_NUMBER_PAGE_ID)) {
            if (!asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class).map(
                value -> (OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS.equals(value)
                    || OutOfCountryDecisionType.REFUSE_PERMIT.equals(value))).orElse(false)) {
                String homeOfficeReferenceNumber = asylumCase
                        .read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                        .orElseThrow(() -> new IllegalStateException("homeOfficeReferenceNumber is missing"));

                if (!HOME_OFFICE_REF_PATTERN.matcher(homeOfficeReferenceNumber).matches()) { // need to update this as the Case ID is no longer used by the HO
                    response.addError("Enter the Home office reference or Case ID in the correct format. The Home office reference or Case ID cannot include letters and must be either 9 digits or 16 digits with dashes.");
                }
            }
        } else if (callback.getPageId().equals(OUT_OF_COUNTRY_PAGE_ID)) {
            if (callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT) {
                // Will only happen for LR as Admins are not allowed to make a OOC appeal as of now.
                CaseDetails<AsylumCase> previousCaseDetails = callback.getCaseDetailsBefore().orElseThrow(() -> new RequiredFieldMissingException("Previous Case Details not found"));
                AsylumCase previousCaseData = previousCaseDetails.getCaseData();
                if (previousCaseData.read(APPELLANT_IN_UK, YesOrNo.class).equals(Optional.of(YesOrNo.NO)) && appellantInUk.equals(YesOrNo.YES)) {
                    // Clear out the OOC data
                    asylumCase.clear(OUT_OF_COUNTRY_DECISION_TYPE);
                    asylumCase.clear(GWF_REFERENCE_NUMBER);
                    asylumCase.clear(DATE_ENTRY_CLEARANCE_DECISION);
                    asylumCase.clear(DATE_CLIENT_LEAVE_UK);
                    clearSponsorDetails(asylumCase);
                }
            }
            if (isAdmin.equals(YesOrNo.YES) && appellantInUk.equals(YesOrNo.NO)) {
                response.addError("This option is currently unavailable");
            }
        } else if (callback.getEvent() == Event.UPDATE_DETENTION_LOCATION && callback.getPageId().equals(DETENTION_FACILITY_PAGE_ID)) {
            boolean isAda = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES);
            String detentionFacilityValue = asylumCase.read(AsylumCaseFieldDefinition.DETENTION_FACILITY, String.class).orElse("");
            if (isAda && !detentionFacilityValue.equals("immigrationRemovalCentre")) {
                response.addError("You cannot update the detention location to a " +  detentionFacilityValue + " because this is an accelerated detained appeal.");
            }
        }

        if (callback.getPageId().equals(SUITABILITY_ATTENDANCE_PAGE_ID) && (callback.getEvent() == Event.EDIT_APPEAL || callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT || callback.getEvent() == Event.START_APPEAL)) {
            boolean suitabilityHearingType = asylumCase.read(SUITABILITY_HEARING_TYPE_YES_OR_NO, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES);

            if (suitabilityHearingType) {
                asylumCase.write(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2, YesOrNo.NO);
            } else {
                asylumCase.write(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1, YesOrNo.NO);
            }

        }

        if (callback.getPageId().equals(UPPER_TRIBUNAL_REFERENCE_NUMBER_PAGE_ID) && (callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL || callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT)) {
            String upperTribunalReferenceNumber = asylumCase
                .read(UPPER_TRIBUNAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("upperTribunalReferenceNumber is missing"));

            if (!UPPER_TRIBUNAL_REFERENCE_NUMBER_PATTERN.matcher(upperTribunalReferenceNumber).matches()) {
                response.addError("Enter the Upper Tribunal reference number in the format UI-Year of submission-6 digit number. For example, UI-2020-123456.");
            }
        }

        if (isAdmin.equals(YesOrNo.YES)
            && (callback.getPageId().equals(APPELLANTS_ADDRESS_PAGE_ID) || callback.getPageId().equals(APPELLANTS_ADDRESS_ADMIN_J_PAGE_ID))
            && (callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL || callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT)
            && (asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class).equals(Optional.of(YesOrNo.NO))
                || asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class).equals(Optional.of(YesOrNo.NO)))) {
            response.addError("The appellant must have provided a postal address");
        }

        return response;
    }

    private void clearSponsorDetails(AsylumCase asylumCase) {
        asylumCase.clear(HAS_SPONSOR);
        asylumCase.clear(SPONSOR_GIVEN_NAMES);
        asylumCase.clear(SPONSOR_FAMILY_NAME);
        asylumCase.clear(SPONSOR_ADDRESS);
        asylumCase.clear(SPONSOR_CONTACT_PREFERENCE);
        asylumCase.clear(SPONSOR_EMAIL);
        asylumCase.clear(SPONSOR_MOBILE_NUMBER);
        asylumCase.clear(SPONSOR_AUTHORISATION);
    }
}
