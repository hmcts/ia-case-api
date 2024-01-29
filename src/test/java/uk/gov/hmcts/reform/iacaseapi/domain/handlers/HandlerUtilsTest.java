package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
class HandlerUtilsTest {

    @Mock
    private AsylumCase asylumCase;

    @Test
    void given_journey_type_aip_returns_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertTrue(HandlerUtils.isAipJourney(asylumCase));
    }

    @Test
    void given_journey_type_rep_returns_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertTrue(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void given_journey_type_legal_rep_returns_true() {
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of("Legal Rep Name"));
        assertTrue(HandlerUtils.isLegalRepJourney(asylumCase));
    }

    @Test
    void given_journey_type_legal_rep_returns_false() {
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        assertFalse(HandlerUtils.isLegalRepJourney(asylumCase));
    }

    @Test
    void no_journey_type_defaults_to_rep() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        assertTrue(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void given_aip_journey_rep_test_should_fail() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertFalse(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void given_rep_journey_aip_test_should_fail() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertFalse(HandlerUtils.isAipJourney(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {
        "RP", "PA", "EA", "HU", "DC"
    })
    void given_non_aaa_test_should_fail(AppealType appealType) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        assertFalse(HandlerUtils.isAgeAssessmentAppeal(asylumCase));
    }

    @Test
    void given_aaa_test_should_pass() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));
        assertTrue(HandlerUtils.isAgeAssessmentAppeal(asylumCase));
    }

    @Test
    void isInternalCase_should_return_true() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(HandlerUtils.isInternalCase(asylumCase));
    }

    @Test
    void isInternalCase_should_return_false() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertFalse(HandlerUtils.isInternalCase(asylumCase));
    }

    @Test
    void sourceOfAppealEjp_should_return_true() {
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL));
        assertTrue(HandlerUtils.sourceOfAppealEjp(asylumCase));
    }

    @Test
    void sourceOfAppealEjp_should_return_false() {
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.PAPER_FORM));
        assertFalse(HandlerUtils.sourceOfAppealEjp(asylumCase));
    }

    @Test
    void isEjpCase_should_return_true() {
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(HandlerUtils.isEjpCase(asylumCase));
    }

    @Test
    void isEjpCase_should_return_false() {
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertFalse(HandlerUtils.isEjpCase(asylumCase));
    }

    @Test
    void isNotificationTurnedOff_should_return_true() {
        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(HandlerUtils.isNotificationTurnedOff(asylumCase));
    }

    @Test
    void isNotificationTurnedOff_should_return_false() {
        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertFalse(HandlerUtils.isNotificationTurnedOff(asylumCase));
    }

    @Test
    void isLegallyRepresentedEjpCase_should_return_true() {
        when(asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(HandlerUtils.isLegallyRepresentedEjpCase(asylumCase));
    }

    @Test
    void isLegallyRepresentedEjpCase_should_return_false() {
        when(asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertFalse(HandlerUtils.isLegallyRepresentedEjpCase(asylumCase));
    }
}
