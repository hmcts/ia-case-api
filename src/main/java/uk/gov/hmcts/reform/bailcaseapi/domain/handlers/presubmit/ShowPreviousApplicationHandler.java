package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS1_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS1_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS2_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS2_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS3_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS3_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS4_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FCS4_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_2;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_3;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.HAS_FINANCIAL_COND_SUPPORTER_4;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREVIOUS_APPLICATION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_APPLICANT_DOCS_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_APPLICANT_INFO;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_DECISION_DETAILS_LABEL;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_DIRECTION_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_COMMITMENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER1;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER2;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER3;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_FINANCIAL_COND_SUPPORTER4;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_GROUNDS_FOR_BAIL;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_HEARING_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_HEARING_REQ_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_ID;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_LEGAL_REP_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_PERSONAL_INFO_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_PROBATION_OFFENDER_MANAGER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PREV_APP_SUBMISSION_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PRIOR_APPLICATIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_ADDRESS_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_FAMILY_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_HAS_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_IMMIGRATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_NATIONALITY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_OCCUPATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_RELATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_TELEPHONE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_ADDRESS_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_FAMILY_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_HAS_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_IMMIGRATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_NATIONALITY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_OCCUPATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_RELATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_TELEPHONE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_ADDRESS_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_FAMILY_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_HAS_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_IMMIGRATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_NATIONALITY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_OCCUPATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_RELATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_TELEPHONE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_ADDRESS_DETAILS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_FAMILY_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_GIVEN_NAMES;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_HAS_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_IMMIGRATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_NATIONALITY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_OCCUPATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_PASSPORT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_RELATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_TELEPHONE_NUMBER;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.VIEW_PREVIOUS_APPLICATIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.MakeNewApplicationService;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.ShowPreviousApplicationService;

@Slf4j
@Component
public class ShowPreviousApplicationHandler implements PreSubmitCallbackHandler<BailCase> {

    private final MakeNewApplicationService makeNewApplicationService;
    private final ShowPreviousApplicationService showPreviousApplicationService;

    public ShowPreviousApplicationHandler(MakeNewApplicationService makeNewApplicationService,
                                          ShowPreviousApplicationService showPreviousApplicationService) {
        this.makeNewApplicationService = makeNewApplicationService;
        this.showPreviousApplicationService = showPreviousApplicationService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == MID_EVENT && callback.getEvent() == VIEW_PREVIOUS_APPLICATIONS;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        DynamicList previousApplication = bailCase.read(PREVIOUS_APPLICATION_LIST, DynamicList.class)
            .orElseThrow(() -> new RequiredFieldMissingException("Previous application details missing"));

        Value selectedApplicationValue = previousApplication.getValue();
        String applicationId = selectedApplicationValue.getCode();

        Optional<List<IdValue<PriorApplication>>> mayBePriorApplications = bailCase.read(PRIOR_APPLICATIONS);

        List<IdValue<PriorApplication>> priorApplications = mayBePriorApplications
            .orElseThrow(() -> new RequiredFieldMissingException("Prior Applications missing"));

        Optional<IdValue<PriorApplication>> mayBeSelectedApplication = priorApplications
            .stream()
            .filter(idValue -> idValue.getValue().getApplicationId().equals(applicationId))
            .findFirst();
        IdValue<PriorApplication> priorSelectedApplication = mayBeSelectedApplication
            .orElseThrow(() -> new RequiredFieldMissingException("No application found"));

        BailCase previousBailCase = makeNewApplicationService
            .getBailCaseFromString(priorSelectedApplication.getValue().getCaseDataJson());
        bailCase.write(PREV_APP_ID, priorSelectedApplication.getValue().getApplicationId());


        String decisionLabel = showPreviousApplicationService
            .getDecisionLabel(previousBailCase, selectedApplicationValue);

        String documentsLabel = showPreviousApplicationService.getDocumentsLabel(previousBailCase);

        String directionLabel = showPreviousApplicationService.getDirectionLabel(previousBailCase);

        String hearingLabel = showPreviousApplicationService.getHearingDetails(previousBailCase);
        String hearingReqLabel = showPreviousApplicationService.getHearingReqDetails(previousBailCase);

        String submissionDetails = showPreviousApplicationService.getSubmissionDetails(previousBailCase);

        String personalInfo = showPreviousApplicationService.getPersonalInfoLabel(previousBailCase);

        String applicantInformationLabel = showPreviousApplicationService.getApplicantInfo(previousBailCase);

        String financialCondCommitment = showPreviousApplicationService.getFinancialCondCommitment(previousBailCase);

        String financialCondSupporter1 = getFinancialCondSupporter1(previousBailCase);
        String financialCondSupporter2 = getFinancialCondSupporter2(previousBailCase);
        String financialCondSupporter3 = getFinancialCondSupporter3(previousBailCase);
        String financialCondSupporter4 = getFinancialCondSupporter4(previousBailCase);

        String groundsForBail = showPreviousApplicationService.getGroundsForBail(previousBailCase);

        String legalRepDetails = showPreviousApplicationService.getLegalRepDetails(previousBailCase);

        String probationOffenderManager = showPreviousApplicationService.getProbationOffenderManager(previousBailCase);

        bailCase.write(PREV_APP_SUBMISSION_DETAILS, submissionDetails);
        bailCase.write(PREV_APP_HEARING_DETAILS, hearingLabel);
        bailCase.write(PREV_APP_HEARING_REQ_DETAILS, hearingReqLabel);
        bailCase.write(PREV_APP_APPLICANT_DOCS_DETAILS, documentsLabel);
        bailCase.write(PREV_APP_DECISION_DETAILS_LABEL, decisionLabel);
        bailCase.write(PREV_APP_DIRECTION_DETAILS, directionLabel);
        bailCase.write(PREV_APP_PERSONAL_INFO_DETAILS, personalInfo);
        bailCase.write(PREV_APP_APPLICANT_INFO, applicantInformationLabel);
        bailCase.write(PREV_APP_FINANCIAL_COND_COMMITMENT, financialCondCommitment);
        bailCase.write(PREV_APP_FINANCIAL_COND_SUPPORTER1, financialCondSupporter1);
        bailCase.write(PREV_APP_FINANCIAL_COND_SUPPORTER2, financialCondSupporter2);
        bailCase.write(PREV_APP_FINANCIAL_COND_SUPPORTER3, financialCondSupporter3);
        bailCase.write(PREV_APP_FINANCIAL_COND_SUPPORTER4, financialCondSupporter4);
        bailCase.write(PREV_APP_GROUNDS_FOR_BAIL, groundsForBail);
        bailCase.write(PREV_APP_LEGAL_REP_DETAILS, legalRepDetails);
        bailCase.write(PREV_APP_PROBATION_OFFENDER_MANAGER, probationOffenderManager);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private String getFinancialCondSupporter1(BailCase previousBailCase) {
        return showPreviousApplicationService.getFinancialConditionSupporterLabel(
            previousBailCase,
            HAS_FINANCIAL_COND_SUPPORTER,
            SUPPORTER_GIVEN_NAMES,
            SUPPORTER_FAMILY_NAMES,
            SUPPORTER_ADDRESS_DETAILS,
            SUPPORTER_TELEPHONE_NUMBER,
            SUPPORTER_MOBILE_NUMBER,
            SUPPORTER_EMAIL_ADDRESS,
            SUPPORTER_DOB,
            SUPPORTER_RELATION,
            SUPPORTER_OCCUPATION,
            SUPPORTER_IMMIGRATION,
            SUPPORTER_NATIONALITY,
            SUPPORTER_HAS_PASSPORT,
            SUPPORTER_PASSPORT,
            FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES,
            FCS1_INTERPRETER_SPOKEN_LANGUAGE,
            FCS1_INTERPRETER_SIGN_LANGUAGE
        );
    }

    private String getFinancialCondSupporter2(BailCase previousBailCase) {
        return showPreviousApplicationService.getFinancialConditionSupporterLabel(
            previousBailCase,
            HAS_FINANCIAL_COND_SUPPORTER_2,
            SUPPORTER_2_GIVEN_NAMES,
            SUPPORTER_2_FAMILY_NAMES,
            SUPPORTER_2_ADDRESS_DETAILS,
            SUPPORTER_2_TELEPHONE_NUMBER,
            SUPPORTER_2_MOBILE_NUMBER,
            SUPPORTER_2_EMAIL_ADDRESS,
            SUPPORTER_2_DOB,
            SUPPORTER_2_RELATION,
            SUPPORTER_2_OCCUPATION,
            SUPPORTER_2_IMMIGRATION,
            SUPPORTER_2_NATIONALITY,
            SUPPORTER_2_HAS_PASSPORT,
            SUPPORTER_2_PASSPORT,
            FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES,
            FCS2_INTERPRETER_SPOKEN_LANGUAGE,
            FCS2_INTERPRETER_SIGN_LANGUAGE
        );
    }

    private String getFinancialCondSupporter3(BailCase previousBailCase) {
        return showPreviousApplicationService.getFinancialConditionSupporterLabel(
            previousBailCase,
            HAS_FINANCIAL_COND_SUPPORTER_3,
            SUPPORTER_3_GIVEN_NAMES,
            SUPPORTER_3_FAMILY_NAMES,
            SUPPORTER_3_ADDRESS_DETAILS,
            SUPPORTER_3_TELEPHONE_NUMBER,
            SUPPORTER_3_MOBILE_NUMBER,
            SUPPORTER_3_EMAIL_ADDRESS,
            SUPPORTER_3_DOB,
            SUPPORTER_3_RELATION,
            SUPPORTER_3_OCCUPATION,
            SUPPORTER_3_IMMIGRATION,
            SUPPORTER_3_NATIONALITY,
            SUPPORTER_3_HAS_PASSPORT,
            SUPPORTER_3_PASSPORT,
            FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES,
            FCS3_INTERPRETER_SPOKEN_LANGUAGE,
            FCS3_INTERPRETER_SIGN_LANGUAGE
        );
    }

    private String getFinancialCondSupporter4(BailCase previousBailCase) {
        return showPreviousApplicationService.getFinancialConditionSupporterLabel(
            previousBailCase,
            HAS_FINANCIAL_COND_SUPPORTER_4,
            SUPPORTER_4_GIVEN_NAMES,
            SUPPORTER_4_FAMILY_NAMES,
            SUPPORTER_4_ADDRESS_DETAILS,
            SUPPORTER_4_TELEPHONE_NUMBER,
            SUPPORTER_4_MOBILE_NUMBER,
            SUPPORTER_4_EMAIL_ADDRESS,
            SUPPORTER_4_DOB,
            SUPPORTER_4_RELATION,
            SUPPORTER_4_OCCUPATION,
            SUPPORTER_4_IMMIGRATION,
            SUPPORTER_4_NATIONALITY,
            SUPPORTER_4_HAS_PASSPORT,
            SUPPORTER_4_PASSPORT,
            FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES,
            FCS4_INTERPRETER_SPOKEN_LANGUAGE,
            FCS4_INTERPRETER_SIGN_LANGUAGE
        );
    }
}
