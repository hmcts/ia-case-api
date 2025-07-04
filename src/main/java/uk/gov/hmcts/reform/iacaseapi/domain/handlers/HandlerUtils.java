package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryCircumstances.ENTRY_CLEARANCE_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType.REFUSE_PERMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.AUDIO_VIDEO_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.FOREIGN_NATIONAL_OFFENDER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.LACKING_CAPACITY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.PRESIDENTIAL_PANEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ACTIVE_STATUS;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption.WILL_PAY_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.AUDIO_VIDEO_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.FOREIGN_NATIONAL_OFFENDER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.LACKING_CAPACITY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.PRESIDENTIAL_PANEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ACTIVE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryCircumstances.ENTRY_CLEARANCE_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType.REFUSE_PERMIT;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryCircumstances;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;


public class HandlerUtils {

    public static final String ON_THE_PAPERS = "ONPPRS";

    private HandlerUtils() {
    }

    public static boolean isAipJourney(AsylumCase asylumCase) {
        return asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(journeyType -> journeyType == JourneyType.AIP)
            .orElse(false);
    }

    public static boolean isRepJourney(AsylumCase asylumCase) {
        return asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(journeyType -> journeyType == JourneyType.REP)
            .orElse(true);
    }

    public static boolean isRepToAipJourney(AsylumCase asylumCase) {
        return (asylumCase.read(PREV_JOURNEY_TYPE, JourneyType.class).orElse(null) == JourneyType.REP)
            && isAipJourney(asylumCase);
    }

    public static boolean isAipToRepJourney(AsylumCase asylumCase) {
        return (asylumCase.read(PREV_JOURNEY_TYPE, JourneyType.class).orElse(null) == JourneyType.AIP)
            && isRepJourney(asylumCase);
    }

    public static void formatHearingAdjustmentResponses(AsylumCase asylumCase) {
        formatHearingAdjustmentResponse(asylumCase, VULNERABILITIES_TRIBUNAL_RESPONSE, IS_VULNERABILITIES_ALLOWED)
                .ifPresent(response -> asylumCase.write(VULNERABILITIES_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, IS_REMOTE_HEARING_ALLOWED)
                .ifPresent(response -> asylumCase.write(REMOTE_HEARING_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, MULTIMEDIA_TRIBUNAL_RESPONSE, IS_MULTIMEDIA_ALLOWED)
                .ifPresent(response -> asylumCase.write(MULTIMEDIA_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, IS_SINGLE_SEX_COURT_ALLOWED)
                .ifPresent(response -> asylumCase.write(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, IN_CAMERA_COURT_TRIBUNAL_RESPONSE, IS_IN_CAMERA_COURT_ALLOWED)
                .ifPresent(response -> asylumCase.write(IN_CAMERA_COURT_DECISION_FOR_DISPLAY, response));
        formatHearingAdjustmentResponse(asylumCase, ADDITIONAL_TRIBUNAL_RESPONSE, IS_ADDITIONAL_ADJUSTMENTS_ALLOWED)
                .ifPresent(response -> asylumCase.write(OTHER_DECISION_FOR_DISPLAY, response));
    }

    public static String getAppellantFullName(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class).orElseGet(() -> {
            final String appellantGivenNames = asylumCase
                    .read(APPELLANT_GIVEN_NAMES, String.class)
                    .orElseThrow(() -> new IllegalStateException("Appellant given names required"));
            final String appellantFamilyName = asylumCase
                    .read(APPELLANT_FAMILY_NAME, String.class)
                    .orElseThrow(() -> new IllegalStateException("Appellant family name required"));
            return appellantGivenNames + " " + appellantFamilyName;
        });
    }

    private static Optional<String> formatHearingAdjustmentResponse(
            AsylumCase asylumCase,
            AsylumCaseFieldDefinition responseDefinition,
            AsylumCaseFieldDefinition decisionDefinition) {
        String response = asylumCase.read(responseDefinition, String.class).orElse(null);
        String decision = asylumCase.read(decisionDefinition, String.class).orElse(null);

        return !(response == null || decision == null) ? Optional.of(decision + " - " + response) : Optional.empty();
    }

    public static void checkAndUpdateAutoHearingRequestEnabled(LocationBasedFeatureToggler locationBasedFeatureToggler, AsylumCase asylumCase) {
        if (locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase) == YES) {
            asylumCase.write(AUTO_HEARING_REQUEST_ENABLED, YES);
        } else {
            asylumCase.write(AUTO_HEARING_REQUEST_ENABLED, NO);
        }
    }

    public static void setDefaultAutoListHearingValue(AsylumCase asylumCase) {
        boolean isHearingOnThePaper = asylumCase.read(HEARING_CHANNEL, DynamicList.class)
            .map(hearingChannels -> hearingChannels.getListItems().stream()
                .anyMatch(c -> c.getCode().equals(ON_THE_PAPERS)))
            .orElse(false);

        if (isHearingOnThePaper || hasActiveFlags(asylumCase)) {
            asylumCase.write(AUTO_LIST_HEARING, NO);
        } else {
            asylumCase.write(AUTO_LIST_HEARING, YES);
        }
    }

    private static boolean hasActiveFlags(AsylumCase asylumCase) {
        List<StrategicCaseFlag> appellantLevelFlags = asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());

        List<StrategicCaseFlag> caseLevelFlag = asylumCase.read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class)
            .map(List::of).orElse(Collections.emptyList());

        boolean hasActiveFlags = false;

        List<String> flagTypesToCheck = List.of(
            SIGN_LANGUAGE_INTERPRETER.getFlagCode(), FOREIGN_NATIONAL_OFFENDER.getFlagCode(),
            AUDIO_VIDEO_EVIDENCE.getFlagCode(), LITIGATION_FRIEND.getFlagCode(), LACKING_CAPACITY.getFlagCode()
        );

        if (!appellantLevelFlags.isEmpty()) {
            hasActiveFlags = generateFlagDetailsList(appellantLevelFlags)
                .stream().anyMatch(detail -> flagTypesToCheck.contains(detail.getValue().getFlagCode())
                    && ACTIVE_STATUS.equals(detail.getValue().getStatus()));
        }

        if (!caseLevelFlag.isEmpty()) {
            hasActiveFlags |= generateFlagDetailsList(caseLevelFlag)
                .stream().anyMatch(detail -> PRESIDENTIAL_PANEL.getFlagCode().equals(detail.getValue().getFlagCode())
                    && ACTIVE_STATUS.equals(detail.getValue().getStatus()));
        }

        return hasActiveFlags;
    }

    private static List<CaseFlagDetail> generateFlagDetailsList(List<StrategicCaseFlag> allCaseFlags) {
        return allCaseFlags.stream()
            .filter(flag -> !isEmpty(flag.getDetails()))
            .flatMap(flag -> flag.getDetails().stream())
            .collect(Collectors.toList());
    }

    public static boolean isPanelRequired(AsylumCase asylumCase) {
        return asylumCase.read(IS_PANEL_REQUIRED, YesOrNo.class)
            .map(yesOrNo -> YES == yesOrNo).orElse(false);
    }

    public static boolean isIntegrated(AsylumCase asylumCase) {
        return asylumCase.read(IS_INTEGRATED, YesOrNo.class)
            .map(integrated -> YES == integrated).orElse(false);
    }

    public static boolean relistCaseImmediately(AsylumCase asylumCase, boolean required) {
        Optional<YesOrNo> relistOptional = asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class);
        if (relistOptional.isPresent() || !required) {
            return relistOptional.map(relist -> YES == relist).orElse(false);
        } else {
            throw new IllegalStateException("Response to relist case immediately is not present");
        }
    }

    public static boolean adjournedBeforeHearingDay(AsylumCase asylumCase) {
        return asylumCase
            .read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class)
            .map(adjournedDay -> BEFORE_HEARING_DATE == adjournedDay)
            .orElseThrow(() -> new IllegalStateException("'Hearing adjournment when' is not present"));
    }

    public static boolean adjournedOnHearingDay(AsylumCase asylumCase) {
        return asylumCase
            .read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class)
            .map(adjournedDay -> ON_HEARING_DATE == adjournedDay)
            .orElseThrow(() -> new IllegalStateException("'Hearing adjournment when' is not present"));
    }

    public static boolean isAgeAssessmentAppeal(AsylumCase asylumCase) {
        return (asylumCase.read(APPEAL_TYPE, AppealType.class)).orElse(null) == AppealType.AG;
    }

    public static boolean isAcceleratedDetainedAppeal(AsylumCase asylumCase) {
        return (asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).orElse(NO) == YesOrNo.YES;
    }

    public static boolean isAppellantInDetention(AsylumCase asylumCase) {
        return (asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).orElse(NO) == YesOrNo.YES;
    }

    public static boolean isAppellantsRepresentation(AsylumCase asylumCase) {
        return (asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).orElse(NO) == YesOrNo.YES;
    }

    public static boolean isInternalCase(AsylumCase asylumCase) {
        return (asylumCase.read(IS_ADMIN, YesOrNo.class)).orElse(NO) == YesOrNo.YES;
    }

    // This method uses the field isNotificationTurnedOff to check if
    // notification need to be sent, in scope of EJP transfer down cases.
    public static boolean isNotificationTurnedOff(AsylumCase asylumCase) {
        return (asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).orElse(NO) == YesOrNo.YES;
    }

    public static String getAdaSuffix() {
        return "_ada";
    }

    public static boolean isAppealPaid(AsylumCase asylumCase) {
        return asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).orElse(null) == PaymentStatus.PAID;
    }

    public static String getAfterHearingReqSuffix() {
        return "_afterHearingReq";
    }

    public static boolean isNabaEnabled(AsylumCase asylumCase) {
        return (asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).orElse(NO) == YesOrNo.YES;
    }

    //Updated method to check if it is a LegalRep journey
    public static boolean isLegalRepJourney(AsylumCase asylumCase) {
        String legalRepName = asylumCase.read(LEGAL_REP_NAME, String.class).orElse("");
        return !legalRepName.isEmpty();
    }

    // This method uses the Source of Appeal value to check if it is EJP during Start Appeal event
    public static boolean sourceOfAppealEjp(AsylumCase asylumCase) {
        return (asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).orElse(SourceOfAppeal.PAPER_FORM) == SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL;
    }

    // This method uses the isEjp field which is set yes for EJP when a case is saved or no if paper form
    public static boolean isEjpCase(AsylumCase asylumCase) {
        return asylumCase.read(IS_EJP, YesOrNo.class).orElse(NO) == YesOrNo.YES;
    }

    // This method uses the isLegallyRepresentedEjp field to check for Legally Represented EJP cases
    public static boolean isLegallyRepresentedEjpCase(AsylumCase asylumCase) {
        return asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class).orElse(NO) == YesOrNo.YES;
    }

    public static List<String> readJsonFileList(String filePath, String key) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassPathResource fileResource = new ClassPathResource(filePath);
        InputStream file = fileResource.getInputStream();

        JsonNode rootNode = objectMapper.readTree(file);

        JsonNode listNode = rootNode.get(key);
        List<String> valueList = new ArrayList<>();

        if (listNode != null && listNode.isArray()) {
            Iterator<JsonNode> elements = listNode.elements();
            while (elements.hasNext()) {
                JsonNode element = elements.next();
                valueList.add(element.asText());
            }
        }

        return valueList;
    }

    public static boolean isCaseUsingLocationRefData(AsylumCase asylumCase) {
        return asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)
            .map(yesOrNo -> yesOrNo.equals(YES))
            .orElse(false);
    }

    // Assigns value to the field that is used for searching cases from hearing centre
    public static void setSelectedHearingCentreRefDataField(AsylumCase asylumCase, String hearingCentreLabel) {
        asylumCase.write(SELECTED_HEARING_CENTRE_REF_DATA, hearingCentreLabel);
    }

    public static boolean isOnlyRemoteToRemoteHearingChannelUpdate(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        boolean currentHearingIsRemote = asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class)
            .map(remote -> YES == remote).orElse(false);
        Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        if (caseDetailsBefore.isPresent()) {
            AsylumCase asylumCaseBefore = caseDetailsBefore.get().getCaseData();
            boolean prevHearingIsRemote = asylumCaseBefore.read(IS_REMOTE_HEARING, YesOrNo.class)
                .map(remote -> YES == remote).orElse(false);

            return hearingCenterUnchanged(asylumCase, asylumCaseBefore)
                   && hearingDateUnchanged(asylumCase, asylumCaseBefore)
                   && prevHearingIsRemote && currentHearingIsRemote;
        } else {
            return false;
        }
    }

    private static boolean hearingCenterUnchanged(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {

        HearingCentre prevHearingCentre = asylumCaseBefore
            .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(null);
        HearingCentre currentHearingCentre = asylumCase
            .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(null);

        return prevHearingCentre == currentHearingCentre;
    }

    private static boolean hearingDateUnchanged(AsylumCase asylumCase, AsylumCase asylumCaseBefore) {

        String prevHearingDate = asylumCaseBefore
            .read(LIST_CASE_HEARING_DATE, String.class).orElse("");
        String currentHearingDate = asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse("");

        return prevHearingDate.equalsIgnoreCase(currentHearingDate);
    }

    public static boolean isRemissionExists(Optional<RemissionType> remissionType) {
        return remissionType.isPresent()
            && remissionType.get() != RemissionType.NO_REMISSION;
    }

    public  static boolean isRemissionExistsAip(Optional<RemissionOption> remissionOption, Optional<HelpWithFeesOption> helpWithFeesOption, boolean isDlrmFeeRemissionFlag) {
        return isDlrmFeeRemissionFlag
            && ((remissionOption.isPresent() && remissionOption.get() != RemissionOption.NO_REMISSION)
            || (helpWithFeesOption.isPresent() && helpWithFeesOption.get() != WILL_PAY_FOR_APPEAL));
    }

    public static boolean outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(AsylumCase asylumCase) {
        return asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class).map(
            value -> (List.of(REFUSAL_OF_HUMAN_RIGHTS, REFUSE_PERMIT).contains(value))).orElse(false);
    }

    public static boolean isEntryClearanceDecision(AsylumCase asylumCase) {
        return asylumCase.read(OOC_APPEAL_ADMIN_J, OutOfCountryCircumstances.class)
            .map(ENTRY_CLEARANCE_DECISION::equals)
            .orElse(false);
    }

    public static boolean isAdmin(AsylumCase asylumCase) {
        return (asylumCase.read(IS_ADMIN, YesOrNo.class)).orElse(NO) == YesOrNo.YES;
    }

    public static boolean isAppellantInPersonManual(AsylumCase asylumCase) {
        return isAdmin(asylumCase) && isAppellantsRepresentation(asylumCase);
    }
}
