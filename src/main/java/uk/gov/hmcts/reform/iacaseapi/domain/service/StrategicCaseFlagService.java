package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.ArrayList;
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

    private Map<String, CaseFlagDetail> activeDetails;
    private final List<CaseFlagDetail> inactiveDetails;

    public StrategicCaseFlagService(@NonNull StrategicCaseFlag strategicCaseFlag) {
        this(strategicCaseFlag.getPartyName(), strategicCaseFlag.getRoleOnCase(), strategicCaseFlag.getDetails());
    }

    public StrategicCaseFlagService(String partyName, String roleOnCase, List<CaseFlagDetail> activeDetails) {
        this.partyName = partyName;
        this.roleOnCase = roleOnCase;
        this.activeDetails = activeDetails == null ? new HashMap<>() : activeDetails.stream()
            .filter(detail -> Objects.equals(detail.getValue().getStatus(), ACTIVE_STATUS))
            .collect(Collectors.toMap(detail -> detail.getValue().getFlagCode(), detail -> detail));
        this.inactiveDetails = activeDetails == null ? new ArrayList<>() : activeDetails.stream()
            .filter(detail -> Objects.equals(detail.getValue().getStatus(), INACTIVE_STATUS))
            .collect(Collectors.toList());
    }

    public StrategicCaseFlagService(String partyName, String roleOnCase) {
        this(partyName, roleOnCase, null);
    }

    public StrategicCaseFlagService() {
        this(null, null, null);
    }

    public StrategicCaseFlag getStrategicCaseFlag() {
        if (isPresent()) {
            List<CaseFlagDetail> details = activeDetails.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(activeDetails.values());
            details.addAll(inactiveDetails);
            return new StrategicCaseFlag(partyName, roleOnCase, details);
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

        activeDetails.put(caseFlagValue.getFlagCode(), caseFlagDetail);

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

        activeDetails.put(caseFlagValue.getFlagCode(), caseFlagDetail);

        return true;
    }

    public boolean deactivateFlag(StrategicCaseFlagType caseFlagType, String dateTimeModified) {

        CaseFlagDetail existingActiveFlagDetail = activeDetails.get(caseFlagType.getFlagCode());
        if (existingActiveFlagDetail != null) {
            activeDetails.remove(caseFlagType.getFlagCode());

            CaseFlagValue newFlagValue = CaseFlagValue.builder()
                .flagCode(existingActiveFlagDetail.getValue().getFlagCode())
                .name(existingActiveFlagDetail.getValue().getName())
                .status(INACTIVE_STATUS)
                .hearingRelevant(existingActiveFlagDetail.getValue().getHearingRelevant())
                .dateTimeModified(dateTimeModified)
                .dateTimeCreated(existingActiveFlagDetail.getValue().getDateTimeCreated())
                .build();

            inactiveDetails.add(new CaseFlagDetail(existingActiveFlagDetail.getId(), newFlagValue));

            return true;
        } else {
            return false;
        }
    }

    protected void clear() {
        this.partyName = null;
        this.roleOnCase = null;
        this.activeDetails = null;
    }

    protected boolean isEmpty() {
        return activeDetails == null;
    }

    protected boolean isPresent() {
        return !isEmpty();
    }

    private boolean hasActiveFlag(StrategicCaseFlagType caseFlagType) {
        if (isEmpty() || activeDetails.isEmpty()) {
            return false;
        }

        return activeDetails.containsKey(caseFlagType.getFlagCode());
    }

    private boolean hasActiveLanguageFlag(StrategicCaseFlagType caseFlagType, String languageFlagName) {

        if (isEmpty() || activeDetails.isEmpty() || !activeDetails.containsKey(caseFlagType.getFlagCode())) {
            return false;
        }

        return Objects.equals(activeDetails.get(caseFlagType.getFlagCode()).getValue().getName(), languageFlagName);
    }

    private String buildFlagName(StrategicCaseFlagType caseFlagType, String caseFlagNamePostfix) {

        return caseFlagNamePostfix == null || caseFlagNamePostfix.isBlank()
            ? caseFlagType.getName()
            : caseFlagType.getName().concat(" " + caseFlagNamePostfix);
    }

}
