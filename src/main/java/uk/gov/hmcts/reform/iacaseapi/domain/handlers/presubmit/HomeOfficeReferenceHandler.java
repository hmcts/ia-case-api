package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeReferenceService;

@Slf4j
@Component
@ConditionalOnProperty(
    name = "app.home-office-validation.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class HomeOfficeReferenceHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public static final int REQUIRED_CID_REF_LENGTH = 9;
    public static final Pattern HOME_OFFICE_REF_PATTERN = Pattern
            .compile("^(([0-9]{4}\\-[0-9]{4}\\-[0-9]{4}\\-[0-9]{4})|(GWF[0-9]{9}))$");
    private final HomeOfficeReferenceService homeOfficeReferenceService;
    private static final String USER_ERROR_HELP_TEXT = "  If you need help, please use the Home Office help form in the bullet points on this page.";
    private static final String INVALID_HOME_OFFICE_REFERENCE = "You should enter the UAN or GWF reference exactly as it appears on the decision letter.  " + 
                                                        "This can often be found in the 'How to appeal' section.  The UAN is 16 digits with dashes.  " + 
                                                        "The GWF starts with the letters \"GWF\" and then has 9 digits." + USER_ERROR_HELP_TEXT;

    public HomeOfficeReferenceHandler(HomeOfficeReferenceService homeOfficeReferenceService) {
        this.homeOfficeReferenceService = homeOfficeReferenceService;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        // TODO - check logic for oocHomeOfficeReferenceNumber (and do any other screens that display the UAN)
        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && List.of(Event.START_APPEAL, Event.EDIT_APPEAL).contains(callback.getEvent())
                && List.of(
                    "homeOfficeReferenceNumber", "oocHomeOfficeReferenceNumber", "appellantBasicDetails", // ExUI pages
                    "cuiHomeOfficeReferenceNumber", "cuiAppellantName", "cuiAppellantDob") // CUI pages
                    .contains(callback.getPageId());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String homeOfficeReferenceNumber = asylumCase
                .read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("homeOfficeReferenceNumber is missing"));

        String pageId = callback.getPageId();

        PreSubmitCallbackResponse<AsylumCase> response;

        switch (pageId) {
            case "homeOfficeReferenceNumber", "oocHomeOfficeReferenceNumber", "cuiHomeOfficeReferenceNumber":
                response = validateHomeOfficeReference(callback, asylumCase, homeOfficeReferenceNumber);
                break;
        
            case "appellantBasicDetails", "cuiAppellantDob":
                response = validateNameAndDateOfBirth(callback, asylumCase, homeOfficeReferenceNumber);
                break;

            case "cuiAppellantName":
                response = validateName(callback, asylumCase, homeOfficeReferenceNumber);
                break;

            default:
                response = new PreSubmitCallbackResponse<>(asylumCase);
                break;
        }

        return response;
    }

    private PreSubmitCallbackResponse<AsylumCase> validateHomeOfficeReference(
        Callback<AsylumCase> callback, AsylumCase asylumCase, String homeOfficeReferenceNumber) {

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (!isWellFormedHomeOfficeReference(homeOfficeReferenceNumber)) {
            response.addError(INVALID_HOME_OFFICE_REFERENCE);
        } else if (!isRealHomeOfficeCaseNumber(homeOfficeReferenceNumber, callback)) {
            // An error occurred - display a suitable message to the user
            response.addError(
                asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class)
                            .orElse(HomeOfficeApiResponseStatusType.UNKNOWN)
                            .getUserFacingErrorText(homeOfficeReferenceNumber)
            );
        }
        return response;
    }

    private PreSubmitCallbackResponse<AsylumCase> validateName(
        Callback<AsylumCase> callback, AsylumCase asylumCase, String homeOfficeReferenceNumber) {

        return validateBiographicDetails(callback, asylumCase, homeOfficeReferenceNumber, true);
    }

    private PreSubmitCallbackResponse<AsylumCase> validateNameAndDateOfBirth(
        Callback<AsylumCase> callback, AsylumCase asylumCase, String homeOfficeReferenceNumber) {

        return validateBiographicDetails(callback, asylumCase, homeOfficeReferenceNumber, false);
    }
    
    private PreSubmitCallbackResponse<AsylumCase> validateBiographicDetails(
        Callback<AsylumCase> callback, AsylumCase asylumCase, String homeOfficeReferenceNumber, boolean nameOnly) {

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        boolean match = nameOnly ? isMatchingName(homeOfficeReferenceNumber, asylumCase, callback)
                                 : isMatchingNameAndDob(homeOfficeReferenceNumber, asylumCase, callback);

        if (!match) {
            // Check whether an error occurred or if there just wasn't a match
            HomeOfficeApiResponseStatusType responseStatus = 
                        asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class)
                        .orElse(HomeOfficeApiResponseStatusType.UNKNOWN);
            String errorMessage = "";
            if (responseStatus.equals(HomeOfficeApiResponseStatusType.OK)) {
                errorMessage = "The information entered does not match the details held by the Home Office for reference number " +
                                homeOfficeReferenceNumber + 
                                ".  You should enter the appellant's details exactly as they appear on the decision letter, so that we can verify them." +
                                "  These details can often be found in the 'How to appeal' section." + USER_ERROR_HELP_TEXT;
                // Log this - if it happens repeatedly, that's suspicious
                log.info("The details provided did not match the Home Office biographic data retrieved for case with reference ID {}.", homeOfficeReferenceNumber);
            } else {
                // This shouldn't happen as the Home Office API ought not to have been called, since the data has already
                // been retrieved from the Home Office.  But we'll check for it anyway just in case something unexpected has happened.
                errorMessage = responseStatus.getUserFacingErrorText(homeOfficeReferenceNumber);
            }
            response.addError(errorMessage);
        }
        return response;
    }

    public static boolean isWellFormedHomeOfficeReference(String hoReference) {
        return hoReference != null && HOME_OFFICE_REF_PATTERN.matcher(hoReference).matches();
    }

    public boolean isRealHomeOfficeCaseNumber(String hoReference, Callback<AsylumCase> callback) {
        if (hoReference == null) {
            return false;
        } else {
            Optional<List<IdValue<HomeOfficeAppellant>>> homeOfficeAppellants = homeOfficeReferenceService.getHomeOfficeReferenceData(hoReference, callback);
            return !(homeOfficeAppellants.isEmpty() || homeOfficeAppellants.get().isEmpty());
        }
    }

    public boolean isMatchingName(String hoReference, AsylumCase asylumCase, Callback<AsylumCase> callback) {
        // Check for a match on the first and last names only (no middle names or initials).
        // This is triggered from CUI because the names are on a separate page to the date of birth.
        if (hoReference == null) {
            return false;
        } else {
            // Retrieve appellant data objects
            final List<HomeOfficeAppellant> homeOfficeAppellantDataObjects = retrieveHomeOfficeAppellantDOs(hoReference, callback);
            // Retrieve information currently entered (for comparison).
            String appellantFamilyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("");
            String appellantGivenNames = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("");
            // Loop through the Home Office appellants (usually just one) and see if one of them has details matching those entered.
            for (int i = 0; i < homeOfficeAppellantDataObjects.size(); i++) {
                HomeOfficeAppellant homeOfficeAppellant = homeOfficeAppellantDataObjects.get(i);
                // Check for matching first name(s), surname and date of birth.
                if (matchesFamilyName(homeOfficeAppellant.getFamilyName(), appellantFamilyName) &&
                        matchesGivenNames(homeOfficeAppellant.getGivenNames(), appellantGivenNames)) {
                    return true;
                }
            }
            // If here, we didn't find a match.
            return false;
        }
    }

    public boolean isMatchingNameAndDob(String hoReference, AsylumCase asylumCase, Callback<AsylumCase> callback) {
        // Note: even if we have already found a match for the name(s), we must do so again here to ensure that for multi-person
        // applications we don't match the date of birth for a different person than the one we matched the names for.
        // This happens in CUI because the names are on a separate page to the date of birth.
        if (hoReference == null) {
            return false;
        } else {
            // Retrieve appellant data objects
            final List<HomeOfficeAppellant> homeOfficeAppellantDataObjects = retrieveHomeOfficeAppellantDOs(hoReference, callback);
            // Retrieve information currently entered (for comparison).
            String appellantFamilyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("");
            String appellantGivenNames = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("");
            String appellantDateOfBirth = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class).orElse("");
            // Loop through the Home Office appellants (usually just one) and see if one of them has details matching those entered.
            for (int i = 0; i < homeOfficeAppellantDataObjects.size(); i++) {
                HomeOfficeAppellant homeOfficeAppellant = homeOfficeAppellantDataObjects.get(i);
                // Check for matching first name(s), surname and date of birth.
                if (matchesFamilyName(homeOfficeAppellant.getFamilyName(), appellantFamilyName) &&
                        matchesGivenNames(homeOfficeAppellant.getGivenNames(), appellantGivenNames) &&
                        matchesDateOfBirth(homeOfficeAppellant.getDateOfBirth().toString(), appellantDateOfBirth)) {
                    return true;
                }
            }
            // If here, we didn't find a match.
            return false;
        }
    }

    private List<HomeOfficeAppellant> retrieveHomeOfficeAppellantDOs(String hoReference, Callback<AsylumCase> callback) {
        Optional<List<IdValue<HomeOfficeAppellant>>> homeOfficeAppellants = homeOfficeReferenceService.getHomeOfficeReferenceData(hoReference, callback);
        if (homeOfficeAppellants.isEmpty() || homeOfficeAppellants.get().isEmpty()) {
            // This should not have happened - we should always have at least one appellant from the Home Office by this point. Log an error.
            log.error(
                    "No appellants returned from the Home Office for reference number {} although it appeared to match a record in Atlas.",
                    hoReference);
            return List.of();
        }
        // Extract and return appellants
        return homeOfficeAppellants
                .orElseThrow(() -> new IllegalStateException("No appellants were returned by the Home Office."))
                .stream()
                .map(IdValue::getValue)
                .collect(Collectors.toList());
    }

    private boolean matchesFamilyName(String homeOfficeFamilyName, String appellantFamilyName) {
        return matchesName(homeOfficeFamilyName, appellantFamilyName, false);
    }

    private boolean matchesGivenNames(String homeOfficeFamilyName, String appellantFamilyName) {
        return matchesName(homeOfficeFamilyName, appellantFamilyName, true);
    }

    private boolean matchesName(String homeOfficeName, String appellantName, boolean firstWordOnly) {
        if (homeOfficeName == null || appellantName == null) {
            return false;
        }
        return normaliseName(homeOfficeName, firstWordOnly).equals(normaliseName(appellantName, firstWordOnly));
    }

    public static String normaliseName(String name, boolean firstWordOnly) {
        if (name == null) {
            return "";
        }

        // Step 1: Normalise spaces and convert to lowercase
        String normalised = name.trim().toLowerCase().replaceAll("\\s+", " ");

        // Step 1.5 (if specified): Take only the first word (to avoid checking middle names)
        if (firstWordOnly && normalised.contains(" ")) {
            normalised = normalised.substring(0, normalised.indexOf(" "));
        }

        // Step 2: Normalise to NFD (decomposed form) to separate base characters from
        // diacritical marks
        normalised = Normalizer.normalize(normalised, Normalizer.Form.NFD);

        // Step 3: Remove diacritical marks (accents, etc.)
        normalised = normalised.replaceAll("\\p{M}", "");

        return normalised;
    }

    private boolean matchesDateOfBirth(String homeOfficeDob, String appellantDob) {
        if (homeOfficeDob == null || appellantDob == null) {
            return false;
        }
        return homeOfficeDob.trim().equals(appellantDob.trim());
    }

}
