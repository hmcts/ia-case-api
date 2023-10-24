package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.HEARING_LOOP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.INTERPRETER_LANGUAGE_FLAG;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.STEP_FREE_WHEELCHAIR_ACCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.ACTIVE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.service.StrategicCaseFlagService.INACTIVE_STATUS;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Language;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;

class StrategicCaseFlagServiceTest {

    private StrategicCaseFlag strategicCaseFlag;
    private StrategicCaseFlagService strategicCaseFlagService;
    private CaseFlagDetail caseFlagDetail;

    @BeforeEach
    void setup() {
        strategicCaseFlag = new StrategicCaseFlag(
            "partyName", "roleOnCase", Collections.emptyList());
        caseFlagDetail = new CaseFlagDetail("id", CaseFlagValue
            .builder()
            .flagCode(HEARING_LOOP.getFlagCode())
            .name(HEARING_LOOP.getName())
            .status(ACTIVE_STATUS)
            .build());
    }

    @Test
    void should_build_non_empty_service_object() {
        strategicCaseFlagService = new StrategicCaseFlagService(strategicCaseFlag);
        assertTrue(strategicCaseFlagService.isPresent());

        strategicCaseFlagService = new StrategicCaseFlagService(
            "partyName", "roleOnCase", Collections.emptyList());
        assertTrue(strategicCaseFlagService.isPresent());

        strategicCaseFlagService = new StrategicCaseFlagService("partyName", "roleOnCase");
        assertTrue(strategicCaseFlagService.isPresent());

        strategicCaseFlagService = new StrategicCaseFlagService();
        assertTrue(strategicCaseFlagService.isPresent());
    }

    @Test
    void should_activate_flag() {
        strategicCaseFlagService = new StrategicCaseFlagService(
            "partyName",
            "roleOnCase");

        boolean activated = strategicCaseFlagService.activateFlag(HEARING_LOOP, YES, "dateTime");
        strategicCaseFlag = strategicCaseFlagService.getStrategicCaseFlag();

        assertTrue(activated);
        assertNotNull(strategicCaseFlag);
        CaseFlagValue caseFlagValue = strategicCaseFlag.getDetails().get(0).getValue();
        assertEquals(ACTIVE_STATUS, caseFlagValue.getStatus());
        assertEquals(HEARING_LOOP.getFlagCode(), caseFlagValue.getFlagCode());
        assertEquals(HEARING_LOOP.getName(), caseFlagValue.getName());
    }

    @Test
    void should_activate_language_flag() {
        strategicCaseFlagService = new StrategicCaseFlagService(
            "partyName",
            "roleOnCase");

        boolean activated = strategicCaseFlagService
            .activateFlag(INTERPRETER_LANGUAGE_FLAG, YES, "dateTime", new Language("code", "text"));
        strategicCaseFlag = strategicCaseFlagService.getStrategicCaseFlag();

        assertTrue(activated);
        assertNotNull(strategicCaseFlag);
        CaseFlagValue caseFlagValue = strategicCaseFlag.getDetails().get(0).getValue();
        assertEquals(ACTIVE_STATUS, caseFlagValue.getStatus());
        assertEquals(INTERPRETER_LANGUAGE_FLAG.getFlagCode(), caseFlagValue.getFlagCode());
        assertEquals(INTERPRETER_LANGUAGE_FLAG.getName(), caseFlagValue.getName());
        assertEquals("code", caseFlagValue.getSubTypeKey());
        assertEquals("text", caseFlagValue.getSubTypeValue());
    }

    @Test
    void should_replace_existing_active_language_flag() {
        caseFlagDetail = new CaseFlagDetail("id", CaseFlagValue
            .builder()
            .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
            .name(INTERPRETER_LANGUAGE_FLAG.getName())
            .subTypeKey("code1")
            .subTypeValue("abc")
            .status("Active")
            .build());
        strategicCaseFlagService = new StrategicCaseFlagService(
            "partyName",
            "roleOnCase", List.of(caseFlagDetail));

        boolean activated = strategicCaseFlagService
            .activateFlag(INTERPRETER_LANGUAGE_FLAG, YES, "dateTime", new Language("code2", "text"));
        strategicCaseFlag = strategicCaseFlagService.getStrategicCaseFlag();

        assertTrue(activated);
        assertNotNull(strategicCaseFlag);
        List<CaseFlagValue> values = strategicCaseFlag.getDetails().stream().map(CaseFlagDetail::getValue).toList();
        CaseFlagValue activeCaseFlagValue = values.stream().filter(value -> "Active".equals(value.getStatus()))
            .findAny().orElse(null);
        CaseFlagValue inactiveCaseFlagValue = values.stream().filter(value -> INACTIVE_STATUS.equals(value.getStatus()))
            .findAny().orElse(null);
        assertTrue(activeCaseFlagValue != null && "Active".equals(activeCaseFlagValue.getStatus()));
        assertTrue(inactiveCaseFlagValue != null && INACTIVE_STATUS.equals(inactiveCaseFlagValue.getStatus()));
        assertEquals("code2", activeCaseFlagValue.getSubTypeKey());
        assertEquals("code1", inactiveCaseFlagValue.getSubTypeKey());
    }

    @Test
    void should_not_activate_case_flag() {
        strategicCaseFlagService = new StrategicCaseFlagService(
            "partyName",
            "roleOnCase",
            List.of(caseFlagDetail));

        assertFalse(strategicCaseFlagService.activateFlag(HEARING_LOOP, YES, "dateTime"));
    }

    @Test
    void should_not_activate_language_flag() {
        caseFlagDetail = new CaseFlagDetail("id", CaseFlagValue
            .builder()
            .flagCode(INTERPRETER_LANGUAGE_FLAG.getFlagCode())
            .name("Language Interpreter")
            .subTypeKey("code")
            .subTypeValue("text")
            .status("Active")
            .build());
        strategicCaseFlagService = new StrategicCaseFlagService(
            "partyName",
            "roleOnCase", List.of(caseFlagDetail));

        assertFalse(strategicCaseFlagService
            .activateFlag(INTERPRETER_LANGUAGE_FLAG, YES, "dateTime", new Language("code", "text")));
    }

    @Test
    void should_deactivate_case_flag() {
        caseFlagDetail = new CaseFlagDetail("id", CaseFlagValue
            .builder()
            .flagCode(HEARING_LOOP.getFlagCode())
            .name(HEARING_LOOP.getName())
            .status("Active")
            .build());
        strategicCaseFlagService = new StrategicCaseFlagService(
            "partyName",
            "roleOnCase",
            List.of(caseFlagDetail));

        assertTrue(strategicCaseFlagService.deactivateFlag(HEARING_LOOP, "dateTime"));
        strategicCaseFlag = strategicCaseFlagService.getStrategicCaseFlag();
        CaseFlagValue caseFlagValue = strategicCaseFlag.getDetails().get(0).getValue();
        assertEquals(INACTIVE_STATUS, caseFlagValue.getStatus());
        assertEquals(HEARING_LOOP.getFlagCode(), caseFlagValue.getFlagCode());
        assertEquals(HEARING_LOOP.getName(), caseFlagValue.getName());
    }

    @Test
    void should_not_deactivate_case_flag() {
        assertFalse(new StrategicCaseFlagService("partyName", "roleOnCase")
            .deactivateFlag(HEARING_LOOP, "dateTime"));

        caseFlagDetail = new CaseFlagDetail("id", CaseFlagValue
            .builder()
            .flagCode(HEARING_LOOP.getFlagCode())
            .name(HEARING_LOOP.getName())
            .status("Active")
            .build());
        strategicCaseFlagService = new StrategicCaseFlagService(
            "partyName",
            "roleOnCase",
            List.of(caseFlagDetail));

        assertFalse(strategicCaseFlagService.deactivateFlag(STEP_FREE_WHEELCHAIR_ACCESS, "dateTime"));
        strategicCaseFlag = strategicCaseFlagService.getStrategicCaseFlag();
        CaseFlagValue caseFlagValue = strategicCaseFlag.getDetails().get(0).getValue();
        assertEquals("Active", caseFlagValue.getStatus());
        assertEquals(HEARING_LOOP.getFlagCode(), caseFlagValue.getFlagCode());
        assertEquals(HEARING_LOOP.getName(), caseFlagValue.getName());
    }

    @Test
    void testGetStrategicCaseFlag() {
        strategicCaseFlagService = new StrategicCaseFlagService(strategicCaseFlag);
        StrategicCaseFlag caseFlag = strategicCaseFlagService.getStrategicCaseFlag();

        assertNotNull(caseFlag);
        assertEquals(caseFlag.getRoleOnCase(), "roleOnCase");
        assertEquals(caseFlag.getPartyName(), "partyName");
        assertNotNull(caseFlag.getDetails());
    }

    @Test
    void testClear() {
        strategicCaseFlagService = new StrategicCaseFlagService(strategicCaseFlag);
        strategicCaseFlagService.clear();

        assertTrue(strategicCaseFlagService.isEmpty());
    }

    @Test
    void testIsPresent() {
        strategicCaseFlagService = new StrategicCaseFlagService(strategicCaseFlag);

        assertTrue(strategicCaseFlagService.isPresent());
    }

}