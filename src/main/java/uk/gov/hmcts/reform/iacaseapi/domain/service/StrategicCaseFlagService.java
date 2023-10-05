package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.NonNull;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Language;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

public class StrategicCaseFlagService {

    public static String ROLE_ON_CASE_APPELLANT = "Appellant";
    public static String ROLE_ON_CASE_WITNESS = "Witness";
    public static String ROLE_ON_CASE_INTERPRETER = "Interpreter";
    public static String ACTIVE_STATUS = "Active";
    public static String INACTIVE_STATUS = "Inactive";

    private String partyName;
    private String roleOnCase;

    private Map<FlagKey, CaseFlagDetail> details;

    public StrategicCaseFlagService(@NonNull StrategicCaseFlag strategicCaseFlag) {
        this(strategicCaseFlag.getPartyName(), strategicCaseFlag.getRoleOnCase(), strategicCaseFlag.getDetails());
    }

    public StrategicCaseFlagService(String partyName, String roleOnCase, List<CaseFlagDetail> details) {
        this.partyName = partyName;
        this.roleOnCase = roleOnCase;
        this.details = details == null ? new HashMap<>() : details.stream()
            .collect(Collectors.toMap(detail ->
                new FlagKey(detail.getValue().getFlagCode(), detail.getValue().getStatus()), detail -> detail));
    }

    public StrategicCaseFlagService(String partyName, String roleOnCase) {
        this(partyName, roleOnCase, null);
    }

    public StrategicCaseFlagService() {
        this(null, null, null);
    }

    public StrategicCaseFlag getStrategicCaseFlag() {
        if (isPresent()) {
            List<CaseFlagDetail> detailsList = details.isEmpty()
                ? Collections.emptyList()
                : new ArrayList<>(details.values());
            return new StrategicCaseFlag(partyName, roleOnCase, detailsList);
        }

        return null;
    }

    public boolean activateFlag(
        StrategicCaseFlagType caseFlagType, YesOrNo hearingRelevant, String dateTimeCreated) {

        if (hasActiveFlag(caseFlagType)) {
            return false;
        }

        CaseFlagValue caseFlagValue = CaseFlagValue.builder()
            .flagCode(caseFlagType.getFlagCode())
            .name(buildFlagName(caseFlagType, null))
            .status(ACTIVE_STATUS)
            .hearingRelevant(hearingRelevant)
            .dateTimeCreated(dateTimeCreated)
            .build();
        CaseFlagDetail caseFlagDetail = new CaseFlagDetail(caseFlagValue);

        details.put(new FlagKey(caseFlagValue.getFlagCode(), caseFlagValue.getStatus()), caseFlagDetail);

        return true;
    }

    public boolean activateFlag(
        StrategicCaseFlagType caseFlagType, YesOrNo hearingRelevant, String dateTimeCreated, Language language) {

        String languageFlagName = buildFlagName(caseFlagType, language.getLanguageText());
        if (hasActiveLanguageFlag(caseFlagType, languageFlagName)) {
            return false;
        }

        deactivateFlag(caseFlagType, dateTimeCreated);

        CaseFlagValue caseFlagValue = CaseFlagValue.builder()
            .flagCode(caseFlagType.getFlagCode())
            .name(languageFlagName)
            .status(ACTIVE_STATUS)
            .hearingRelevant(hearingRelevant)
            .dateTimeCreated(dateTimeCreated)
            .build();
        CaseFlagDetail caseFlagDetail = new CaseFlagDetail(caseFlagValue);

        details.put(new FlagKey(caseFlagValue.getFlagCode(), caseFlagValue.getStatus()), caseFlagDetail);

        return true;
    }

    public boolean deactivateFlag(StrategicCaseFlagType caseFlagType, String dateTimeModified) {

        FlagKey existingActiveFlagKey = new FlagKey(caseFlagType.getFlagCode(), ACTIVE_STATUS);
        CaseFlagDetail existingActiveFlagDetail = details.get(existingActiveFlagKey);
        if (existingActiveFlagDetail != null) {
            details.remove(existingActiveFlagKey);

            CaseFlagValue newFlagValue = CaseFlagValue.builder()
                .flagCode(existingActiveFlagDetail.getValue().getFlagCode())
                .name(existingActiveFlagDetail.getValue().getName())
                .status(INACTIVE_STATUS)
                .hearingRelevant(existingActiveFlagDetail.getValue().getHearingRelevant())
                .dateTimeModified(dateTimeModified)
                .dateTimeCreated(existingActiveFlagDetail.getValue().getDateTimeCreated())
                .build();

            details.put(
                new FlagKey(newFlagValue.getFlagCode(), newFlagValue.getStatus()),
                new CaseFlagDetail(existingActiveFlagDetail.getId(), newFlagValue));

            return true;
        } else {
            return false;
        }
    }

    protected void clear() {
        this.partyName = null;
        this.roleOnCase = null;
        this.details = null;
    }

    protected boolean isEmpty() {
        return details == null;
    }

    protected boolean isPresent() {
        return !isEmpty();
    }

    private boolean hasActiveFlag(StrategicCaseFlagType caseFlagType) {
        if (isEmpty() || details.isEmpty()) {
            return false;
        }

        return details.containsKey(new FlagKey(caseFlagType.getFlagCode(), ACTIVE_STATUS));
    }

    private boolean hasActiveLanguageFlag(StrategicCaseFlagType caseFlagType, String languageFlagName) {
        FlagKey activeFlagKey = new FlagKey(caseFlagType.getFlagCode(), ACTIVE_STATUS);

        if (isEmpty() || details.isEmpty() || !details.containsKey(activeFlagKey)) {
            return false;
        }

        return Objects.equals(details.get(activeFlagKey).getValue().getName(), languageFlagName);
    }

    private String buildFlagName(StrategicCaseFlagType caseFlagType, String caseFlagNamePostfix) {

        return caseFlagNamePostfix == null || caseFlagNamePostfix.isBlank()
            ? caseFlagType.getName()
            : caseFlagType.getName().concat(" " + caseFlagNamePostfix);
    }

    public record FlagKey(String code, String status) {
    }

}
