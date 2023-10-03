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

    private String partyName;
    private String roleOnCase;

    private Map<String, CaseFlagDetail> details;

    public StrategicCaseFlagService(@NonNull StrategicCaseFlag strategicCaseFlag) {
        this(strategicCaseFlag.getPartyName(), strategicCaseFlag.getRoleOnCase(), strategicCaseFlag.getDetails());
    }

    public StrategicCaseFlagService(String partyName, String roleOnCase, List<CaseFlagDetail> details) {
        this.partyName = partyName;
        this.roleOnCase = roleOnCase;
        this.details = details == null ? new HashMap<>() : details.stream()
            .collect(Collectors.toMap(detail -> detail.getCaseFlagValue().getFlagCode(), detail -> detail));
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
            .status("Active")
            .hearingRelevant(hearingRelevant)
            .dateTimeCreated(dateTimeCreated)
            .build();

        details.put(caseFlagType.getFlagCode(), new CaseFlagDetail(caseFlagValue));

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
            .status("Active")
            .hearingRelevant(hearingRelevant)
            .dateTimeCreated(dateTimeCreated)
            .build();

        details.put(caseFlagType.getFlagCode(), new CaseFlagDetail(caseFlagValue));

        return true;
    }

    public boolean deactivateFlag(StrategicCaseFlagType caseFlagType, String dateTimeModified) {

        if (hasActiveFlag(caseFlagType)) {
            CaseFlagDetail targetFlagDetails = details.get(caseFlagType.getFlagCode());
            CaseFlagValue targetFlagValue = targetFlagDetails.getCaseFlagValue();
            details.put(targetFlagValue.getFlagCode(), new CaseFlagDetail(targetFlagDetails.getId(), CaseFlagValue.builder()
                .flagCode(targetFlagValue.getFlagCode())
                .name(targetFlagValue.getName())
                .status("Inactive")
                .hearingRelevant(targetFlagValue.getHearingRelevant())
                .dateTimeModified(dateTimeModified)
                .dateTimeCreated(targetFlagValue.getDateTimeCreated())
                .build()));

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
        if (isEmpty() || details.isEmpty() || !details.containsKey(caseFlagType.getFlagCode())) {
            return false;
        }

        CaseFlagValue targetFlag = details.get(caseFlagType.getFlagCode()).getCaseFlagValue();
        return Objects.equals(targetFlag.getStatus(), "Active");
    }

    private boolean hasActiveLanguageFlag(StrategicCaseFlagType caseFlagType, String languageFlagName) {
        if (isEmpty() || details.isEmpty() || !details.containsKey(caseFlagType.getFlagCode())) {
            return false;
        }

        CaseFlagValue targetFlag = details.get(caseFlagType.getFlagCode()).getCaseFlagValue();
        return Objects.equals(targetFlag.getName(), languageFlagName)
            && Objects.equals(targetFlag.getStatus(), "Active");
    }

    private String buildFlagName(StrategicCaseFlagType caseFlagType, String caseFlagNamePostfix) {

        return caseFlagNamePostfix == null || caseFlagNamePostfix.isBlank()
            ? caseFlagType.getName()
            : caseFlagType.getName().concat(" " + caseFlagNamePostfix);
    }
}
