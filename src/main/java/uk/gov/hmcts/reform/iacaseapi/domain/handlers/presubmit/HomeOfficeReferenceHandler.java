package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService;

import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AGE_ASSESSMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isEntryClearanceDecision;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit;


@Component
public class HomeOfficeReferenceHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public static final int REQUIRED_CID_REF_LENGTH = 9;
    public static final Pattern HOME_OFFICE_REF_PATTERN = Pattern.compile("^\\d{9}$|^\\d{4}-\\d{4}-\\d{4}-\\d{4}$");

    private final HomeOfficeReferenceService homeOfficeReferenceService;

    public HomeOfficeReferenceHandler(HomeOfficeReferenceService homeOfficeReferenceService) {
        this.homeOfficeReferenceService = homeOfficeReferenceService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && (callback.getEvent() == Event.START_APPEAL
                || callback.getEvent() == Event.EDIT_APPEAL)
                && (callback.getPageId().equals("homeOfficeDecision") || 
                    callback.getPageId().equals("appellantBasicDetails"))
                && HandlerUtils.isRepJourney(asylumCase);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (!isInternalCase(asylumCase)
            && !outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)
            && !isAgeAssessmentAppealType(asylumCase)
            || isInternalCase(asylumCase)
            && !isEntryClearanceDecision(asylumCase)) {

            String homeOfficeReferenceNumber = asylumCase
                .read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("homeOfficeReferenceNumber is missing"));

                
            if (callback.getPageId().equals("homeOfficeDecision")){    
                if (!isWelformedHomeOfficeReference(homeOfficeReferenceNumber)) {
                    PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                    response.addError("Enter the Home office reference or Case ID in the correct format. The Home office reference or Case ID cannot include letters and must be either 9 digits or 16 digits with dashes.");
                    return response;
                }

                if (!isMatchingHomeOfficeCaseNumber(homeOfficeReferenceNumber)) {
                    PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                    response.addError("Enter the Home office case number from your letter. The Home office case number provided does not match any existing case in home office systems.");
                    return response;
                }
            }

            if (callback.getPageId().equals("appellantBasicDetails")){  
                if (!isMatchingHomeOfficeCaseDetails(homeOfficeReferenceNumber, asylumCase)) {
                    PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                    response.addError("Enter the Home office details from the letters. The details provided does not match the case in the home office systems.");
                    return response;
                }
            }

        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public static boolean isWelformedHomeOfficeReference(String reference) {
        return reference != null && HOME_OFFICE_REF_PATTERN.matcher(reference).matches();
    }

    private boolean isAgeAssessmentAppealType(AsylumCase asylumCase) {
        return asylumCase.read(AGE_ASSESSMENT, YesOrNo.class).orElse(NO).equals(YES);
    }

    public boolean isMatchingHomeOfficeCaseNumber(String reference) {
        if (reference == null) {
            return false;
        }
        
        return homeOfficeReferenceService.getHomeOfficeReferenceData(reference)
            .map(data -> {
                String uan = data.getUan();
                return uan != null && uan.trim().toLowerCase().equals(reference.trim().toLowerCase());
            })
            .orElse(false);
    }

    public boolean isMatchingHomeOfficeCaseDetails(String reference, AsylumCase asylumCase) {
        if (reference == null) {
            return false;
        }
        
        return homeOfficeReferenceService.getHomeOfficeReferenceData(reference)
            .map(data -> {
                if (data.getAppellants() == null || data.getAppellants().isEmpty()) {
                    return false;
                }
                
                String appellantGivenNames = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("");
                String appellantFamilyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("");
                String appellantDateOfBirth = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class).orElse("");
                
                return data.getAppellants().stream().anyMatch(appellant ->
                    matchesName(appellant.getGivenNames(), appellantGivenNames) &&
                    matchesName(appellant.getFamilyName(), appellantFamilyName) &&
                    matchesDateOfBirth(appellant.getDateOfBirth(), appellantDateOfBirth)
                );
            })
            .orElse(false);
    }
    
    private boolean matchesName(String homeOfficeName, String appellantName) {
        if (homeOfficeName == null || appellantName == null) {
            return false;
        }
        return homeOfficeName.trim().toLowerCase().equals(appellantName.trim().toLowerCase());
    }
    
    private boolean matchesDateOfBirth(String homeOfficeDob, String appellantDob) {
        if (homeOfficeDob == null || appellantDob == null) {
            return false;
        }
        return homeOfficeDob.trim().equals(appellantDob.trim());
    }
}
