package uk.gov.hmcts.reform.iacaseapi.ccd.event;

import java.util.Set;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsAdditionalInstructionsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsAdditionalRequestsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsAppellantInterpreterLanguageCategoryPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsAppellantInterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsAppellantInterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsAttendTheHearingPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsDatesToAvoidPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsDraftHearingRequirementsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsInCameraCourtPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsInterpreterLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsIsAnyWitnessInterpreterRequiredPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsIsEvidenceFromOutsideUkInCountryPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsIsEvidenceFromOutsideUkOocPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsIsHearingLoopNeededPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsIsHearingRoomNeededPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsIsInterpreterServicesNeededPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsIsWitnessesAttendingPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsMultimediaEvidencePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsNlrHearingRequirementsPagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsOralEvidencePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsPastExperiencesPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsPhysicalOrMentalHealthIssuesPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsRemoteVideoCallPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsSingleSexCourtPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsSingleSexCourtTypePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsSubmitHearingRequirementsAdditionalRequestsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWhichWitnessRequiresInterpreterPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness10InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness10InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness1InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness1InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness2InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness2InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness3InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness3InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness4InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness4InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness5InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness5InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness6InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness6InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness7InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness7InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness8InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness8InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness9InterpreterSignLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.UpdateHearingRequirementsWitness9InterpreterSpokenLanguagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.State;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.UserRole;

/**
 * Generated by ccd-definition-converter — do not edit by hand.
 *
 * <p>Event {@code updateHearingRequirements} for case type {@code Asylum}.
 */
@Component
public class UpdateHearingRequirements implements CCDConfig<CaseData, State, UserRole> {
    /**
     * The CCD event ID.
     */
    public static final String UPDATE_HEARING_REQUIREMENTS = "updateHearingRequirements";

    /**
     * Registers event {@code updateHearingRequirements}.
     *
     * @param builder the config builder
     */
    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> builder) {
        var fields = builder.event(UPDATE_HEARING_REQUIREMENTS)
            .forStates(State.listing, State.prepareForHearing, State.preHearing, State.finalBundling, State.decision, State.adjourned, State.respondentReview)
            .name("Update hearing requirements")
            .displayOrder(36)
            .showSummary()
            .endButtonLabel("Update")
            .publishToCamunda()
            .explicitGrants()
            .grant(Permission.CRUD, UserRole.CASEWORKER_IA_CASEOFFICER, UserRole.CASEWORKER_IA_IACJUDGE)
            .grant(Set.of(Permission.R), UserRole.CASEWORKER_IA_ADMOFFICER)
            .fields();
        UpdateHearingRequirementsDraftHearingRequirementsPage.apply(fields);
        UpdateHearingRequirementsIsEvidenceFromOutsideUkOocPage.apply(fields);
        UpdateHearingRequirementsAttendTheHearingPage.apply(fields);
        UpdateHearingRequirementsOralEvidencePage.apply(fields);
        UpdateHearingRequirementsIsWitnessesAttendingPage.apply(fields);
        UpdateHearingRequirementsIsEvidenceFromOutsideUkInCountryPage.apply(fields);
        UpdateHearingRequirementsIsInterpreterServicesNeededPage.apply(fields);
        UpdateHearingRequirementsAppellantInterpreterLanguageCategoryPage.apply(fields);
        UpdateHearingRequirementsInterpreterLanguagePage.apply(fields);
        UpdateHearingRequirementsAppellantInterpreterSpokenLanguagePage.apply(fields);
        UpdateHearingRequirementsAppellantInterpreterSignLanguagePage.apply(fields);
        UpdateHearingRequirementsIsAnyWitnessInterpreterRequiredPage.apply(fields);
        UpdateHearingRequirementsWhichWitnessRequiresInterpreterPage.apply(fields);
        UpdateHearingRequirementsWitness1InterpreterSpokenLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness1InterpreterSignLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness2InterpreterSpokenLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness2InterpreterSignLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness3InterpreterSpokenLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness3InterpreterSignLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness4InterpreterSpokenLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness4InterpreterSignLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness5InterpreterSpokenLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness5InterpreterSignLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness6InterpreterSpokenLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness6InterpreterSignLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness7InterpreterSpokenLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness7InterpreterSignLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness8InterpreterSpokenLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness8InterpreterSignLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness9InterpreterSpokenLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness9InterpreterSignLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness10InterpreterSpokenLanguagePage.apply(fields);
        UpdateHearingRequirementsWitness10InterpreterSignLanguagePage.apply(fields);
        UpdateHearingRequirementsIsHearingRoomNeededPage.apply(fields);
        UpdateHearingRequirementsIsHearingLoopNeededPage.apply(fields);
        UpdateHearingRequirementsNlrHearingRequirementsPagePage.apply(fields);
        UpdateHearingRequirementsSubmitHearingRequirementsAdditionalRequestsPage.apply(fields);
        UpdateHearingRequirementsRemoteVideoCallPage.apply(fields);
        UpdateHearingRequirementsPhysicalOrMentalHealthIssuesPage.apply(fields);
        UpdateHearingRequirementsPastExperiencesPage.apply(fields);
        UpdateHearingRequirementsMultimediaEvidencePage.apply(fields);
        UpdateHearingRequirementsSingleSexCourtPage.apply(fields);
        UpdateHearingRequirementsSingleSexCourtTypePage.apply(fields);
        UpdateHearingRequirementsInCameraCourtPage.apply(fields);
        UpdateHearingRequirementsAdditionalRequestsPage.apply(fields);
        UpdateHearingRequirementsDatesToAvoidPage.apply(fields);
        UpdateHearingRequirementsAdditionalInstructionsPage.apply(fields);
    }
}
