package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

public class HandlerUtils {

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
        return (asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).orElse(YesOrNo.NO) == YesOrNo.YES;
    }

    public static boolean isAppellantInDetention(AsylumCase asylumCase) {
        return (asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).orElse(YesOrNo.NO) == YesOrNo.YES;
    }

    public static boolean isInternalCase(AsylumCase asylumCase) {
        return (asylumCase.read(IS_ADMIN, YesOrNo.class)).orElse(YesOrNo.NO) == YesOrNo.YES;
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
        return (asylumCase.read(IS_NABA_ENABLED, YesOrNo.class)).orElse(YesOrNo.NO) == YesOrNo.YES;
    }

    public static boolean isEjpCase(AsylumCase asylumCase) {
        return (asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).orElse(null) == SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL;
    }
}
