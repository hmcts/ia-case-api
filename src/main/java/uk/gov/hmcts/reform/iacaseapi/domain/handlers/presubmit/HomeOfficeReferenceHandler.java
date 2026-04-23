package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isEntryClearanceDecision;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
public class HomeOfficeReferenceHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public static final int REQUIRED_CID_REF_LENGTH = 9;
    public static final Pattern HOME_OFFICE_REF_PATTERN = Pattern
            .compile("^(([0-9]{4}\\-[0-9]{4}\\-[0-9]{4}\\-[0-9]{4})|(GWF[0-9]{9}))$");
    private final HomeOfficeReferenceService homeOfficeReferenceService;

    public HomeOfficeReferenceHandler(HomeOfficeReferenceService homeOfficeReferenceService) {
        this.homeOfficeReferenceService = homeOfficeReferenceService;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
                && (callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL)
                &&
                (callback.getPageId().equals("homeOfficeReferenceNumber")
                ||
                // TODO - add logic for this case below (the other two have been implemented, whereas this one hasn't)
                callback.getPageId().equals("oocHomeOfficeReferenceNumber")
                ||
                callback.getPageId().equals("appellantBasicDetails"));
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        // >>>ASK DAVID: why these restrictions on when the validation is run?<<< I
        // THINK THEY MAY NEED TO BE REMOVED (at least some of them) because we always
        // want to go through validation, don't we?
        if (!isInternalCase(asylumCase)
                && !outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase)
                || isInternalCase(asylumCase)
                        && !isEntryClearanceDecision(asylumCase)) {

            String homeOfficeReferenceNumber = asylumCase
                    .read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                    .orElseThrow(() -> new IllegalStateException("homeOfficeReferenceNumber is missing"));

            if (callback.getPageId().equals("homeOfficeReferenceNumber_TEMPORARILY_DISABLED")) {
                if (!isWellFormedHomeOfficeReference(homeOfficeReferenceNumber)) {
                    PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                    response.addError(
                            "The Home Office reference number must be either a UAN, which is 16 digits with dashes, or the letters GWF followed by 9 digits.  Please check your decision letter and try again.");
                    return response;
                }

                if (!isRealHomeOfficeCaseNumber(homeOfficeReferenceNumber, callback)) {
                    // An error occurred - display a suitable message to the user
                    PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                    response.addError(
                        asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class)
                                  .orElse(HomeOfficeApiResponseStatusType.UNKNOWN)
                                  .getUserFacingErrorText(homeOfficeReferenceNumber)
                    );
                    return response;
                }
            }

            if (callback.getPageId().equals("appellantBasicDetails_TEMPORARILY_DISABLED")) {
                if (!isMatchingHomeOfficeCaseDetails(homeOfficeReferenceNumber, asylumCase, callback)) {
                    // Check whether an error occurred or if there just wasn't a match
                    HomeOfficeApiResponseStatusType responseStatus = 
                                  asylumCase.read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, HomeOfficeApiResponseStatusType.class)
                                  .orElse(HomeOfficeApiResponseStatusType.UNKNOWN);
                    String errorMessage = "";
                    if (responseStatus.equals(HomeOfficeApiResponseStatusType.OK)) {
                        errorMessage = "The details provided do not match those held by the Home Office for reference number "
                                     + homeOfficeReferenceNumber + ".  Please check your decision letter and try again.";
                        // Log this - if it happens repeatedly, that's suspicious
                        log.info("The details provided did not match the Home Office biographic data retrieved for case with reference ID {}.", homeOfficeReferenceNumber);
                    } else {
                        // This shouldn't happen as the Home Office API ought not to have been called, since the data has already
                        // been retrieved from the Home Office.  But we'll check for it anyway just in case something unexpected has happened.
                        errorMessage = responseStatus.getUserFacingErrorText(homeOfficeReferenceNumber);
                    }
                    PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
                    response.addError(errorMessage);
                    return response;
                }
            }

        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public static boolean isWellFormedHomeOfficeReference(String hoReference) {
        return hoReference != null && HOME_OFFICE_REF_PATTERN.matcher(hoReference).matches();
    }

    public boolean isRealHomeOfficeCaseNumber(String hoReference, Callback<AsylumCase> callback) {
        if (hoReference == null) {
            return false;
        } else {
            Optional<List<IdValue<HomeOfficeAppellant>>> homeOfficeAppellants = homeOfficeReferenceService.getHomeOfficeReferenceData(hoReference, callback);
            if (homeOfficeAppellants.isEmpty()) {
                return false;
            } else {
                return !homeOfficeAppellants.get().isEmpty();
            }
        }
    }

    public boolean isMatchingHomeOfficeCaseDetails(String hoReference, AsylumCase asylumCase, Callback<AsylumCase> callback) {

        if (hoReference == null) {
            return false;
        } else {
            Optional<List<IdValue<HomeOfficeAppellant>>> homeOfficeAppellants = homeOfficeReferenceService.getHomeOfficeReferenceData(hoReference, callback);
            if (homeOfficeAppellants.isEmpty() || homeOfficeAppellants.get().isEmpty()) {
                // This should not have happened - we should always have at least one appellant from the Home Office by this point. Log an error.
                log.error(
                        "No appellants returned from the Home Office for reference number {} although it appeared to match a record in Atlas.",
                        hoReference);
                return false;
            }
            // Extract appellants
            final List<HomeOfficeAppellant> homeOfficeAppellantDataObjects = homeOfficeAppellants
                    .orElseThrow(() -> new IllegalStateException("No appellants were returned by the Home Office."))
                    .stream()
                    .map(IdValue::getValue)
                    .collect(Collectors.toList());
            // Retrieve information currently entered (for comparison).
            String appellantFamilyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse("");
            String appellantGivenNames = asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse("");
            String appellantDateOfBirth = asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class).orElse("");
            // Loop through the Home Office appellants (usually just one) and see if one of them has details matching those entered.
            for (int i = 0; i < homeOfficeAppellantDataObjects.size(); i++) {
                HomeOfficeAppellant homeOfficeAppellant = homeOfficeAppellantDataObjects.get(i);
                // Check for matching first name(s), surname and date of birth.
                if (matchesName(homeOfficeAppellant.getFamilyName(), appellantFamilyName) &&
                        matchesName(homeOfficeAppellant.getGivenNames(), appellantGivenNames) &&
                        matchesDateOfBirth(homeOfficeAppellant.getDateOfBirth().toString(), appellantDateOfBirth)) {
                    return true;
                }
            }
            // If here, we didn't find a match.
            return false;
        }

    }

    private boolean matchesName(String homeOfficeName, String appellantName) {
        if (homeOfficeName == null || appellantName == null) {
            return false;
        }
        return normaliseName(homeOfficeName).equals(normaliseName(appellantName));
    }

    public static String normaliseName(String name) {
        if (name == null) {
            return "";
        }

        // Step 1: Normalise spaces and convert to lowercase
        String normalised = name.trim().toLowerCase().replaceAll("\\s+", " ");

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
