package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isEntryClearanceDecision;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.HomeOfficeMissingApplicationException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService;

@Slf4j
@Component
public class HomeOfficeReferenceHandler implements PreSubmitCallbackHandler<AsylumCase> {

    enum CauseOfHomeOfficeException {
        NOT_SET,
        CLIENT_ERROR,
        SERVER_ERROR,
        CASE_NOT_FOUND
    }

    public static final int REQUIRED_CID_REF_LENGTH = 9;
    public static final Pattern HOME_OFFICE_REF_PATTERN = Pattern.compile("^(([0-9]{4}\\-[0-9]{4}\\-[0-9]{4}\\-[0-9]{4})|(GWF[0-9]{9}))$");
    private final HomeOfficeReferenceService homeOfficeReferenceService;
    private CauseOfHomeOfficeException causeOfHomeOfficeException;

    public HomeOfficeReferenceHandler(HomeOfficeReferenceService homeOfficeReferenceService) {
        this.homeOfficeReferenceService = homeOfficeReferenceService;
        this.causeOfHomeOfficeException = CauseOfHomeOfficeException.NOT_SET;
    }
    
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && (callback.getEvent() == Event.START_APPEAL
                || callback.getEvent() == Event.EDIT_APPEAL)
                // && (callback.getPageId().equals("homeOfficeReferenceNumber_TEMPORARILY_DISABLED") || 
                //     // TODO - add logic for this case below (the other two have been implemented, whereas this one hasn't)
                //     callback.getPageId().equals("oocHomeOfficeReferenceNumber_TEMPORARILY_DISABLED") ||
                //     callback.getPageId().equals("appellantBasicDetails_TEMPORARILY_DISABLED"));
                && (callback.getPageId().equals("homeOfficeReferenceNumber") || 
                    // TODO - add logic for this case below (the other two have been implemented, whereas this one hasn't)
                    callback.getPageId().equals("oocHomeOfficeReferenceNumber") ||
                    callback.getPageId().equals("appellantBasicDetails"));
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final long caseId = callback.getCaseDetails().getId();

        // >>>ASK DAVID: why these restrictions on when the validation is run?<<< I THINK THEY MAY NEED TO BE REMOVED (at least some of them) because we always want to go through validation, don't we?
        if (!isInternalCase(asylumCase)
            && !outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)
            || isInternalCase(asylumCase)
            && !isEntryClearanceDecision(asylumCase)) {

            String homeOfficeReferenceNumber = asylumCase
                .read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("homeOfficeReferenceNumber is missing"));

            //if (callback.getPageId().equals("homeOfficeReferenceNumber_TEMPORARILY_DISABLED")) {    
            if (callback.getPageId().equals("homeOfficeReferenceNumber")) {    
                if (!isWellFormedHomeOfficeReference(homeOfficeReferenceNumber)) {
                    PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                    response.addError("The Home Office reference number must be either a UAN, which is 16 digits with dashes, or the letters GWF followed by 9 digits.  Please check your decision letter and try again.");
                    return response;
                }

                if (!isMatchingHomeOfficeCaseNumber(homeOfficeReferenceNumber, caseId, asylumCase)) {
                    PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                    String errorMessage = "";
                    switch (causeOfHomeOfficeException) {
                        case CLIENT_ERROR:
                            errorMessage = "An error occurred.  Please report this to HMCTS.";
                            break;
                        case SERVER_ERROR:
                            errorMessage = "An error occurred.  Please try again in 15-20 minutes.  If it occurs again, please report this to HMCTS.";
                            break;
                        case CASE_NOT_FOUND:
                            errorMessage = "The Home Office reference number " + homeOfficeReferenceNumber + " does not match any existing case records in Home Office systems.  Please check your decision letter and try again.";
                            break;
                        default:
                            // This should never happen.  Deliberately use different wording in the error message so that we know it did.
                            errorMessage = "There was a problem.  Please report this to HMCTS.";
                            break;
                    }
                    response.addError(errorMessage);
                    return response;
                }
            }

            //if (callback.getPageId().equals("appellantBasicDetails_TEMPORARILY_DISABLED")) {  
            if (callback.getPageId().equals("appellantBasicDetails")) {  
                if (!isMatchingHomeOfficeCaseDetails(homeOfficeReferenceNumber, caseId, asylumCase)) {
                    PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                    String errorMessage = "";
                    // There should be no exception here as this function ought not to be called unless the data has already been retrieved from the Home Office.
                    // But we'll check for it anyway just in case something unexpected has happened.
                    switch (causeOfHomeOfficeException) {
                        case CLIENT_ERROR:
                            errorMessage = "An error occurred.  Please report this to HMCTS.";
                            break;
                        case SERVER_ERROR:
                            errorMessage = "An error occurred.  Please try again in 15-20 minutes.  If it occurs again, please report this to HMCTS.";
                            break;
                        case CASE_NOT_FOUND:
                            errorMessage = "The Home Office reference number " + homeOfficeReferenceNumber + " does not match any existing case records in Home Office systems.  Please check your decision letter and try again.";                    
                            break;
                        default:
                            // No exception thrown - there just wasn't a match.
                            errorMessage = "The details provided do not match those held by the Home Office for reference number " + homeOfficeReferenceNumber + ".  Please check your decision letter and try again.";                    
                            break;
                    }
                    response.addError(errorMessage);
                    return response;
                }
            }

        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public static boolean isWellFormedHomeOfficeReference(String reference) {
        return reference != null && HOME_OFFICE_REF_PATTERN.matcher(reference).matches();
    }

    public boolean isMatchingHomeOfficeCaseNumber(String reference, long caseId, AsylumCase asylumCase) {

        if (reference == null) {
            return false;
        }
                
        try {
            Optional<List<HomeOfficeAppellant>> appellants = homeOfficeReferenceService.getHomeOfficeReferenceData(reference, caseId, asylumCase);
            if (appellants.isEmpty()) {
                return false;
            } else {
                return !appellants.get().isEmpty();
            }
        } catch (HomeOfficeMissingApplicationException exception) {
            handleHomeOfficeException(exception);
            return false;
        }
    }

    public boolean isMatchingHomeOfficeCaseDetails(String reference, long caseId, AsylumCase asylumCase) {   

        if (reference == null) {
            return false;
        }

        try {
            Optional<List<HomeOfficeAppellant>> homeOfficeAppellants = homeOfficeReferenceService.getHomeOfficeReferenceData(reference, caseId, asylumCase);

            if (homeOfficeAppellants.isEmpty() || homeOfficeAppellants.get().isEmpty()) {
                // This should not have happened - we should always have at least one appellant from the Home Office by this point.  Log an error.
                log.error("No appellants returned from the Home Office for reference number {} although it appeared to match a record in Atlas.", reference);
                return false;
            }

            // Retrieve information currently entered (for comparison).
            String appellantFamilyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("");
            String appellantGivenNames = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("");
            String appellantDateOfBirth = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class).orElse("");
            // Loop through the Home Office appellants (usually just one) and see if one of them has details matching those entered. 
            for (HomeOfficeAppellant homeOfficeAppellant : homeOfficeAppellants.get()) {
                // Check for matching first name(s), surname and date of birth.
                if (matchesName(homeOfficeAppellant.getFamilyName(), appellantFamilyName) &&
                    matchesName(homeOfficeAppellant.getGivenNames(), appellantGivenNames) &&
                    matchesDateOfBirth(homeOfficeAppellant.getDateOfBirth().toString(), appellantDateOfBirth)) {
                    return true;
                }
            }
            // If here, we didn't find a match.
            return false;
        } catch (HomeOfficeMissingApplicationException exception) {
            handleHomeOfficeException(exception);
            return false;
        }        
        
    }
    
    private boolean matchesName(String homeOfficeName, String appellantName) {
        if (homeOfficeName == null || appellantName == null) {
            return false;
        }
        return normalizeName(homeOfficeName).equals(normalizeName(appellantName));
    }
    
    public static String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        
        // Step 1: Normalize spaces and convert to lowercase
        String normalized = name.trim().toLowerCase().replaceAll("\\s+", " ");
        
        // Step 2: Normalize to NFD (decomposed form) to separate base characters from diacritical marks
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        
        // Step 3: Remove diacritical marks (accents, etc.)
        normalized = normalized.replaceAll("\\p{M}", "");
        
        return normalized;
    }
    
    private boolean matchesDateOfBirth(String homeOfficeDob, String appellantDob) {
        if (homeOfficeDob == null || appellantDob == null) {
            return false;
        }
        return homeOfficeDob.trim().equals(appellantDob.trim());
    }

    private void handleHomeOfficeException(HomeOfficeMissingApplicationException exception) {
        // Log as an error if the return status indicates a problem somewhere in our code (which may be a result of something changing at the Home Office's end)
        switch (exception.getHttpStatus()) {
            case -1:
                // This means we didn't get a response from the Home Office (time-out)
                causeOfHomeOfficeException = CauseOfHomeOfficeException.SERVER_ERROR;
                break;
            case 0:
                // This means we somehow lost the response from the Home Office
                causeOfHomeOfficeException = CauseOfHomeOfficeException.CLIENT_ERROR;
                break;
            case 400, 401, 403:
                // If the request is malformed, unauthenticated or unauthorised, it's a problem in our code
                causeOfHomeOfficeException = CauseOfHomeOfficeException.CLIENT_ERROR;
                break;
            case 404:
                // This will happen regularly due to user error; the code is fine
                causeOfHomeOfficeException = CauseOfHomeOfficeException.CASE_NOT_FOUND;
                break;
            case 500, 501, 502, 503, 504:
                // One of these signifies a problem at the Home Office's end - nothing we can do
                causeOfHomeOfficeException = CauseOfHomeOfficeException.SERVER_ERROR;
                break;
            default:
                // Don't know - safest to assume it's a problem with our own code
                causeOfHomeOfficeException = CauseOfHomeOfficeException.CLIENT_ERROR;
                break;
        }

        switch (causeOfHomeOfficeException) {
            case CLIENT_ERROR:
                log.error(exception.getMessage());
                break;
            case SERVER_ERROR:
                log.warn(exception.getMessage());
                break;
            case CASE_NOT_FOUND:
                log.info(exception.getMessage());
                break;
            default:
                log.info(exception.getMessage());
                break;
        }
    }
}
