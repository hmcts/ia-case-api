package uk.gov.hmcts.reform.iacaseapi.ccd.event;

import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsAdditionalRequestsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsAppellantInterpreterLanguageCategoryPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsAppellantInterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsAppellantInterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsAttendTheHearingPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsDatesToAvoidPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsDraftHearingRequirementsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsInCameraCourtPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsInterpreterLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsIsAnyWitnessInterpreterRequiredPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsIsEvidenceFromOutsideUkInCountryPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsIsEvidenceFromOutsideUkOocPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsIsHearingLoopNeededPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsIsHearingRoomNeededPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsIsInterpreterServicesNeededPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsIsWitnessesAttendingPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsMultimediaEvidencePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsOralEvidencePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsPastExperiencesPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsPhysicalOrMentalHealthIssuesPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsRemoteVideoCallPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsSingleSexCourtPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsSingleSexCourtTypePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsSubmitHearingRequirementsAdditionalRequestsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWhichWitnessRequiresInterpreterPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness10InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness10InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness1InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness1InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness2InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness2InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness3InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness3InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness4InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness4InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness5InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness5InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness6InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness6InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness7InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness7InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness8InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness8InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness9InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.DraftHearingRequirementsWitness9InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.State;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.UserRole;

/**
 * Generated by ccd-definition-converter — do not edit by hand.
 *
 * <p>Event {@code draftHearingRequirements} for case type {@code Asylum}.
 */
@Component
public class DraftHearingRequirements implements CCDConfig<CaseData, State, UserRole> {
    /**
     * The CCD event ID.
     */
    public static final String DRAFT_HEARING_REQUIREMENTS = "draftHearingRequirements";

    /**
     * Registers event {@code draftHearingRequirements}.
     *
     * @param builder the config builder
     */
    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> builder) {
        var fields = builder.event(DRAFT_HEARING_REQUIREMENTS)
            .forStateTransition(EnumSet.of(State.submitHearingRequirements, State.awaitingRespondentEvidence, State.caseBuilding, State.caseUnderReview, State.respondentReview), State.listing)
            .name("Submit hearing requirements")
            .displayOrder(36)
            .showCondition("[STATE]=\"submitHearingRequirements\" OR adaHearingRequirementsSubmittable=\"Yes\"")
            .showSummary()
            .endButtonLabel("Submit")
            .publishToCamunda()
            .explicitGrants()
            .grant(Permission.CRUD, UserRole.CASEWORKER_IA_ADMOFFICER, UserRole.CASEWORKER_IA_LEGALREP_SOLICITOR, UserRole.CITIZEN)
            .grant(Set.of(Permission.R), UserRole.CASEWORKER_IA_CASEOFFICER, UserRole.CASEWORKER_IA_IACJUDGE)
            .fields();
        DraftHearingRequirementsDraftHearingRequirementsPage.apply(fields);
        DraftHearingRequirementsIsEvidenceFromOutsideUkOocPage.apply(fields);
        DraftHearingRequirementsAttendTheHearingPage.apply(fields);
        DraftHearingRequirementsOralEvidencePage.apply(fields);
        DraftHearingRequirementsIsWitnessesAttendingPage.apply(fields);
        DraftHearingRequirementsIsEvidenceFromOutsideUkInCountryPage.apply(fields);
        DraftHearingRequirementsIsInterpreterServicesNeededPage.apply(fields);
        DraftHearingRequirementsAppellantInterpreterLanguageCategoryPage.apply(fields);
        DraftHearingRequirementsInterpreterLanguagePage.apply(fields);
        DraftHearingRequirementsAppellantInterpreterSpokenLanguagePage.apply(fields);
        DraftHearingRequirementsAppellantInterpreterSignLanguagePage.apply(fields);
        DraftHearingRequirementsIsAnyWitnessInterpreterRequiredPage.apply(fields);
        DraftHearingRequirementsWhichWitnessRequiresInterpreterPage.apply(fields);
        DraftHearingRequirementsWitness1InterpreterSpokenLanguagePage.apply(fields);
        DraftHearingRequirementsWitness1InterpreterSignLanguagePage.apply(fields);
        DraftHearingRequirementsWitness2InterpreterSpokenLanguagePage.apply(fields);
        DraftHearingRequirementsWitness2InterpreterSignLanguagePage.apply(fields);
        DraftHearingRequirementsWitness3InterpreterSpokenLanguagePage.apply(fields);
        DraftHearingRequirementsWitness3InterpreterSignLanguagePage.apply(fields);
        DraftHearingRequirementsWitness4InterpreterSpokenLanguagePage.apply(fields);
        DraftHearingRequirementsWitness4InterpreterSignLanguagePage.apply(fields);
        DraftHearingRequirementsWitness5InterpreterSpokenLanguagePage.apply(fields);
        DraftHearingRequirementsWitness5InterpreterSignLanguagePage.apply(fields);
        DraftHearingRequirementsWitness6InterpreterSpokenLanguagePage.apply(fields);
        DraftHearingRequirementsWitness6InterpreterSignLanguagePage.apply(fields);
        DraftHearingRequirementsWitness7InterpreterSpokenLanguagePage.apply(fields);
        DraftHearingRequirementsWitness7InterpreterSignLanguagePage.apply(fields);
        DraftHearingRequirementsWitness8InterpreterSpokenLanguagePage.apply(fields);
        DraftHearingRequirementsWitness8InterpreterSignLanguagePage.apply(fields);
        DraftHearingRequirementsWitness9InterpreterSpokenLanguagePage.apply(fields);
        DraftHearingRequirementsWitness9InterpreterSignLanguagePage.apply(fields);
        DraftHearingRequirementsWitness10InterpreterSpokenLanguagePage.apply(fields);
        DraftHearingRequirementsWitness10InterpreterSignLanguagePage.apply(fields);
        DraftHearingRequirementsIsHearingRoomNeededPage.apply(fields);
        DraftHearingRequirementsIsHearingLoopNeededPage.apply(fields);
        DraftHearingRequirementsSubmitHearingRequirementsAdditionalRequestsPage.apply(fields);
        DraftHearingRequirementsRemoteVideoCallPage.apply(fields);
        DraftHearingRequirementsPhysicalOrMentalHealthIssuesPage.apply(fields);
        DraftHearingRequirementsPastExperiencesPage.apply(fields);
        DraftHearingRequirementsMultimediaEvidencePage.apply(fields);
        DraftHearingRequirementsSingleSexCourtPage.apply(fields);
        DraftHearingRequirementsSingleSexCourtTypePage.apply(fields);
        DraftHearingRequirementsInCameraCourtPage.apply(fields);
        DraftHearingRequirementsAdditionalRequestsPage.apply(fields);
        DraftHearingRequirementsDatesToAvoidPage.apply(fields);
    }
}
