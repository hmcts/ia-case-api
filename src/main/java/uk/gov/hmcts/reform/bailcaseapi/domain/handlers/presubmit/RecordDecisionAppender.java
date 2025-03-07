package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class RecordDecisionAppender implements PreSubmitCallbackHandler<BailCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == Event.RECORD_THE_DECISION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        String conditionAppearance = "The applicant is to appear before an Immigration Officer at [location] between"
                + " [times] on [date] or any other place and on any other date and time that may be required by the Home"
                + " Office or an Immigration Officer."
                + "\n\n"
                + "-OR-"
                + "\n\n"
                + "The Applicant must appear before an Immigration Officer on a date and time and by such means as may be"
                + " notified to him by the Home Office in writing.";
        String conditionActivities = "The Applicant shall not undertake the activities listed below while on"
                + " immigration bail without further order:"
                + "\n\n"
                + "The Applicant is not allowed to work"
                + "\n\n"
                + "The Applicant is not allowed to study";
        String conditionResidence = "The applicant must reside at ADDRESS."
                + "\n\n"
                + "Where an Immigration Judge makes a conditional grant with deferred commencement:"
                + "\n\n"
                + "The applicant will reside at an address approved by the probation/offender manager."
                + "\n\n"
                + "By virtue of para 3(8) of schedule 10 of The Immigration Act 2016 this grant of bail will not commence"
                + " until such address has been approved by probation."
                + "\n\n"
                + "The approval of a residence address will be reviewed by the Tribunal on the first available date"
                + " after [ ] days unless the Tribunal is notified prior to that date that accommodation has been"
                + " approved and bail commenced in which case the matter will be dealt with administratively."
                + "\n\n"
                + "In the event that the applicant has not been released then at least 2 clear days before the bail"
                + " review hearing, the Secretary of State is to update the Tribunal in writing as to the progress made"
                + " in relation to sourcing and/or approving accommodation for the applicant.";
        String conditionReporting = "The Applicant must report to an Immigration Officer at [ADDRESS, TIME, DATE"
                + " AND FREQUENCY].";
        String conditionElectronicMonitoring = "Note: Where the Tribunal has directed that the Applicant should be"
                + " subject to an electronic monitoring condition there may be a delay of release of up to 72 hours"
                + " pending arrangements for the electronic monitoring device."
                + "\n\n"
                + "Bail is granted conditional upon:"
                + "\n\n"
                + "the applicant being compliant with the fitting of an electronic monitoring device at the point"
                + " of release from detention; and"
                + "\n\n"
                + "the Secretary of State promptly arranging the fitting of the"
                + " electronic monitoring device at the place of the Applicant's detention. If the secretary of State does"
                + " not complete the fitting of the electronic monitoring device within 72 hours then this grant of bail"
                + " will commence and the applicant is to be released subject to the other conditions of this grant"
                + " of bail. In such an event the Secretary of State will make arrangements for the fitting of the"
                + " electronic monitoring device post release.";
        String bailTransferDirections = "The Tribunal directs that on commencement of bail future management including"
                + " any application for variation shall be exercised by the Secretary of State pursuant by paragraph 6(3)"
                + " of Schedule 10 to the Immigration Act 2016."
                + "\n\n"
                + "Please note: Where the Tribunal directs that bail management shall be transferred to the Secretary"
                + " of State, all future proceedings will be conducted by the Secretary of State (including any hearing to"
                + " determine liability for payment of a financial condition).";
        String secretaryOfStateRefusalReasons = "The Tribunal was minded to grant bail for the reasons given. The Home"
                + " Office have refused to consent to the grant of bail so in accordance with paragraph 3(4) of Schedule 10"
                + " Immigration Act 2016 bail is refused.";

        bailCase.write(BailCaseFieldDefinition.CONDITION_APPEARANCE, conditionAppearance);
        bailCase.write(BailCaseFieldDefinition.CONDITION_ACTIVITIES, conditionActivities);
        bailCase.write(BailCaseFieldDefinition.CONDITION_RESIDENCE, conditionResidence);
        bailCase.write(BailCaseFieldDefinition.CONDITION_REPORTING, conditionReporting);
        bailCase.write(BailCaseFieldDefinition.CONDITION_ELECTRONIC_MONITORING, conditionElectronicMonitoring);
        bailCase.write(BailCaseFieldDefinition.BAIL_TRANSFER_DIRECTIONS, bailTransferDirections);
        bailCase.write(BailCaseFieldDefinition.SECRETARY_OF_STATE_REFUSAL_REASONS, secretaryOfStateRefusalReasons);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
