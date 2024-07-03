package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_LANGUAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_EJP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_INTERPRETER_SERVICES_NEEDED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_LEGALLY_REPRESENTED_EJP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_NABA_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_NOTIFICATION_TURNED_OFF;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREV_JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SOURCE_OF_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

public class HandlerUtils {

    private static final String SIGN_LANGUAGE = "Sign language - ";
    private static final String YES = "Yes";

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

    public static boolean isAgeAssessmentAppeal(AsylumCase asylumCase) {
        return (asylumCase.read(APPEAL_TYPE, AppealType.class)).orElse(null) == AppealType.AG;
    }

    public static boolean isAcceleratedDetainedAppeal(AsylumCase asylumCase) {
        return (asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).orElse(NO) == YesOrNo.YES;
    }

    public static boolean isAppellantInDetention(AsylumCase asylumCase) {
        return (asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).orElse(NO) == YesOrNo.YES;
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

    public static void populateAppellantInterpreterLanguageFieldsIfRequired(AsylumCase asylumCase) {
        Optional<List<IdValue<InterpreterLanguage>>> interpreterLanguageOptional =
            asylumCase.read(INTERPRETER_LANGUAGE);

        Optional<YesOrNo> isInterpreterServicesNeededOptional =
            asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class);

        asylumCase
            .read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)
            .map(yesOrNo -> Objects.equals(yesOrNo, YesOrNo.YES))
            .orElse(false);

        if (!interpreterLanguageOptional.isPresent()
            || interpreterLanguageOptional.get().isEmpty()
            || (isInterpreterServicesNeededOptional.isPresent() && isInterpreterServicesNeededOptional.get().equals(NO))) {
            return;
        }

        asylumCase.write(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.YES);

        List<String> appellantInterpreterLanguageCategory = new ArrayList<>();
        List<IdValue<InterpreterLanguage>> interpreterLanguageList = interpreterLanguageOptional.get();
        StringBuilder signLanguages = new StringBuilder();
        StringBuilder spokenLanguages = new StringBuilder();

        interpreterLanguageList.forEach(language -> {
            if (language.getValue().getLanguage().startsWith(SIGN_LANGUAGE)) {
                appellantInterpreterLanguageCategory.add(SIGN_LANGUAGE_INTERPRETER.toString());
                signLanguages.append(newLanguageManualEntry(language)).append("; ");

            } else {
                appellantInterpreterLanguageCategory.add(SPOKEN_LANGUAGE_INTERPRETER.toString());
                spokenLanguages.append(newLanguageManualEntry(language)).append("; ");

            }
        });

        if (interpreterLanguageList.removeIf(l -> l.getValue().getLanguage().equals("yo")
            || l.getValue().getLanguage().equals("za"))) {
            asylumCase.write(INTERPRETER_LANGUAGE, interpreterLanguageList);
        }

        asylumCase.write(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY, appellantInterpreterLanguageCategory
            .stream().distinct().collect(Collectors.toList()));

        if (!signLanguages.isEmpty()) {
            asylumCase.write(APPELLANT_INTERPRETER_SIGN_LANGUAGE, new InterpreterLanguageRefData(
                null,
                List.of(YES),
                signLanguages.substring(0, signLanguages.length() - 2))
            );
        }
        if (!spokenLanguages.isEmpty()) {
            asylumCase.write(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE, new InterpreterLanguageRefData(
                null,
                List.of(YES),
                spokenLanguages.substring(0, spokenLanguages.length() - 2))
            );
        }
    }

    private static String dialectAddendum(String dialect) {
        return StringUtils.equals(dialect.toUpperCase(), "N/A") ? ("") : (" " + dialect);
    }

    private static String newLanguageManualEntry(IdValue<InterpreterLanguage> language) {
        String mainLanguage = language.getValue().getLanguage();
        String dialect = language.getValue().getLanguageDialect();
        return dialect != null ? mainLanguage + dialectAddendum(dialect) : mainLanguage;
    }
}
