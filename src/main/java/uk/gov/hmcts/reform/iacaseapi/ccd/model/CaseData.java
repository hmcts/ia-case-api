package uk.gov.hmcts.reform.iacaseapi.ccd.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.ccd.sdk.type.SearchCriteria;
import uk.gov.hmcts.ccd.sdk.type.TTL;
import uk.gov.hmcts.ccd.sdk.type.WaysToPay;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerCaseofficerCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerCaseofficerCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerCaseofficerLegalrepSolicitorCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerCaseofficerLegalrepSolicitorSystemCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerCaseofficerRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerCaseofficerSystemCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerCitizenCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerIacjudgeCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerIacjudgeRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerLegalrepSolicitorCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerRuAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerSpecificAccessCtscProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.AdmofficerSystemCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.ApproverCaaCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseflagsProfileCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseflagsProfileReadonlyProfileCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseflagsReadonlyProfileCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaselinksProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaselinksProfileSystemCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerCitizenCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerCitizenIacjudgeWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerCitizenRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerCrAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerIacjudgeRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerLegalrepSolicitorCruWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerLegalrepSolicitorSystemCrudAdmofficerRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerLegalrepSolicitorWaTaskConfigurationRuAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerSpecificAccessCtscProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerSystemCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerSystemWaTaskConfigurationCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CaseofficerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CftTtlManagerCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenIacjudgeCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenIacjudgeRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenIacjudgeSpecificAccessAdminCtscLegalOpsProfileCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenLegalrepSolicitorCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenRuAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenSpecificAccessCtscProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenSystemCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenWaTaskConfigurationCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.CitizenWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.GsProfileCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.GsProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.HomeofficeapcHomeofficepouRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.HomeofficeapcRespondentofficerCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.HomeofficebailHomeofficepouLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.HomeofficelartRespondentofficerCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.HomeofficelartRespondentofficerSystemCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.IacjudgeCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.IacjudgeCruLegalrepSolicitorSpecificAccessCtscProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.IacjudgeCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.IacjudgeLegalrepSolicitorCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.IacjudgeRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.IacjudgeSpecificAccessCtscProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.IacjudgeSystemCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.IacjudgeWaTaskConfigurationCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.InternalCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.InternalSystemCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.LegalrepSolicitorCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.LegalrepSolicitorCruSpecificAccessAdminJudgeLegalOpsProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.LegalrepSolicitorCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.LegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.LegalrepSolicitorSpecificAccessAdminJudgeLegalOpsProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.LegalrepSolicitorSpecificAccessCtscProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.LegalrepSolicitorSystemCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.LegalrepSolicitorWaTaskConfigurationCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.NextHearingDateAdminCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.RasValidationRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.RparobotCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.RparobotRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessAdminCtscJudgeLegalOpsProfileCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessAdminJudgeLegalOpsProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessAdminLegalOpsProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessAdminProfileCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessAdminProfileCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessAdminProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessAdminProfileWaTaskConfigurationCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessCtscJudgeLegalOpsProfileCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessCtscProfileCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessCtscProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessJudgeLegalOpsProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessJudgeProfileCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessJudgeProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessLegalOpsProfileCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SpecificAccessLegalOpsProfileRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SystemAccessRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SystemCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.SystemCrudAdmofficerLegalrepSolicitorRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.WaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.WaTaskConfigurationCrudAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.WaTaskConfigurationCrudCaseofficerRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.access.WaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.AdditionalAddendumDocument;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.AdditionalEvidenceDocument;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.AdditionalEvidenceHomeOfficeDocument;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.AdjournmentDetail;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.AppealNumber;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.Application;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.Bundle;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.CaseManagementLocation;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.CaseManagementLocationRefData;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.CaseNoteMigration;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.Checklist;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.ChecklistOutOfCountry;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.ClarifyingQuestion;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.ClarifyingQuestionAnswer;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.DatesToAvoid;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.DecisionAndReasons;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.Direction;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.EditableDirection;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.HearingRecordingDocument;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.HearingSchedule;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.HoursAndMinutes;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.HoursMinutes;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.InterpreterDetails;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.InterpreterLanguage;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.InterpreterSignLanguage;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.InterpreterSpokenLanguage;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.IsDocumentSignedToday;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.IsFeeConsistentWithDecision;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.LatestQuery;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.LegacyCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.NlrDetails;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.Notification;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.OtherDetentionFacilityComplexType;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.OutOfTimeDecisionDetails;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.Parties;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.PinInPost;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.PreviousHearing;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.PreviousRepresentation;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.PrisonNOMSComplexType;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.ReheardHearingDecisionComplex;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.ReheardHearingDocumentsComplex;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.RemissionDetails;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.RemittalDocument;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.Subscription;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.complex.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.AdminDeclaration;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.AllocateTheCaseTo;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.AppellantsRepresentation;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.BaseLocation;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.ChangeHearingUpdateReason;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.DecisionHearingFeeOption;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.DesiredStateCorrect;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.DetentionFacilityType;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.EaHuAppealTypePaymentOptions;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.EndAppealApproverType;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.EndAppealOutcome;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.HearingDateChangingValues;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.HearingLength;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.HearingVenue;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.HearingYesNoRadioButton;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.HelpWithFeesOptions;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.InterpreterLanguageCategory;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.IrcNameList;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.IsAdditionalAdjustmentsAllowed;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.IsAppellantRespondent;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.IsDecisionAllowed;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.IsInCameraCourtAllowed;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.IsMultimediaAllowed;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.IsRemoteHearingAllowed;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.IsSingleSexCourtAllowed;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.IsVulnerabilitiesAllowed;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.IsoCountriesGovUk;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.JourneyTypes;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.LateRemissionTypes;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.LegacyCaseFlags;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.ListOfStates;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.MakeAnApplicationDecision;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.OocAppealAdminJ;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.OtherAppeals;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.OutOfTimeDecisionType;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.PaAppealTypeAipPaymentOptions;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.PaAppealTypePaymentOptions;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.PartiesList;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.PendingBailApplication;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.PrisonNameList;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.ReasonForLinkAppealOptions;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.RemissionOptions;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.ReserveOrExcludeJudge;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.RpDcAppealHearingOptions;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.SingleSexCourtType;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.SourceOfRemittalList;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.TimeExtensionDecision;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.UpdateTribunalRuleList;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.enums.UploadSensitiveDocsIsAppellantRespondentOptions;

/**
 * Generated by ccd-definition-converter from CaseField — do not edit by hand.
 */
@Getter
@Setter
@NoArgsConstructor
public class CaseData {
    @JsonUnwrapped(prefix = "appeal")
    @CCD(access = {SystemCrudAccess.class})
    private Appeal appeal = new Appeal();

    @JsonUnwrapped(prefix = "appellant")
    private Appellant appellant = new Appellant();

    @JsonUnwrapped(prefix = "application")
    @CCD(
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private ApplicationFields application = new ApplicationFields();

    @JsonUnwrapped(prefix = "decision")
    @CCD(access = {SystemCrudAccess.class})
    private Decision decision = new Decision();

    @JsonUnwrapped(prefix = "fee")
    @CCD(access = {SystemCrudAccess.class})
    private Fee fee = new Fee();

    @JsonUnwrapped(prefix = "ftpa")
    @CCD(access = {SystemCrudAccess.class})
    private Ftpa ftpa = new Ftpa();

    @JsonUnwrapped(prefix = "hearing")
    private Hearing hearing = new Hearing();

    @JsonUnwrapped(prefix = "homeOffice")
    private HomeOffice homeOffice = new HomeOffice();

    @JsonUnwrapped
    private Journey journey = new Journey();

    @JsonUnwrapped(prefix = "legal")
    @CCD(access = {SystemCrudAccess.class})
    private Legal legal = new Legal();

    @JsonUnwrapped(prefix = "migration")
    private Migration migration = new Migration();

    @JsonUnwrapped(prefix = "nlr")
    private Nlr nlr = new Nlr();

    @JsonUnwrapped(prefix = "payment")
    @CCD(access = {SystemCrudAccess.class})
    private Payment payment = new Payment();

    @JsonUnwrapped
    private Progress progress = new Progress();

    @JsonUnwrapped
    private ProgressAdmin progressAdmin = new ProgressAdmin();

    @JsonUnwrapped
    private ProgressAdminOfficer progressAdminOfficer = new ProgressAdminOfficer();

    @JsonUnwrapped
    private ProgressCase progressCase = new ProgressCase();

    @JsonUnwrapped
    private ProgressCaseOfficer progressCaseOfficer = new ProgressCaseOfficer();

    @JsonUnwrapped
    private ProgressHomeOfficeAll progressHomeOfficeAll = new ProgressHomeOfficeAll();

    @JsonUnwrapped
    private ProgressHomeOfficeApc progressHomeOfficeApc = new ProgressHomeOfficeApc();

    @JsonUnwrapped
    private ProgressHomeOfficeGeneric progressHomeOfficeGeneric = new ProgressHomeOfficeGeneric();

    @JsonUnwrapped
    private ProgressHomeOfficeLart progressHomeOfficeLart = new ProgressHomeOfficeLart();

    @JsonUnwrapped
    private ProgressHomeOfficePou progressHomeOfficePou = new ProgressHomeOfficePou();

    @JsonUnwrapped
    private ProgressJudge progressJudge = new ProgressJudge();

    @JsonUnwrapped
    private ProgressLegalRep progressLegalRep = new ProgressLegalRep();

    @JsonUnwrapped
    private ProgressLegalRepresentative progressLegalRepresentative = new ProgressLegalRepresentative();

    @JsonUnwrapped
    private ProgressLr progressLr = new ProgressLr();

    @JsonUnwrapped
    private ProgressRespondentReview progressRespondentReview = new ProgressRespondentReview();

    @JsonUnwrapped(prefix = "remission")
    @CCD(access = {SystemCrudAccess.class})
    private Remission remission = new Remission();

    @JsonUnwrapped(prefix = "sponsor")
    private Sponsor sponsor = new Sponsor();

    @JsonUnwrapped(prefix = "suitability")
    @CCD(access = {SystemCrudAccess.class})
    private Suitability suitability = new Suitability();

    @JsonUnwrapped(prefix = "witness")
    @CCD(access = {SystemCrudAccess.class})
    private Witness witness = new Witness();

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String checklistTitle;

    @CCD(
            label = "If you cannot use the new online service due to your client's circumstances, you can <a href=\"https://immigrationappealsonline.justice.gov.uk/IACFees\" target=\"_blank\">appeal using the current online service</a> or appeal by post or fax using <a href=\"https://www.gov.uk/government/publications/appeal-a-visa-or-immigration-decision-within-the-uk-form-iaft-5\" target=\"_blank\">form IAFT-5</a>.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String checklistTitle2;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CaseofficerLegalrepSolicitorSystemCrudAdmofficerRAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class, SpecificAccessAdminProfileCrudAccess.class}
    )
    private String checklistTitle3;

    @CCD(
            label = "Out of country feature flag value",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, LegalrepSolicitorSystemCrudAccess.class, AdmofficerIacjudgeRAccess.class, CaseofficerCitizenCrudAccess.class}
    )
    private YesOrNo isOutOfCountryEnabled;

    @CCD(
            label = " ",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private Checklist checklist;

    @CCD(
            label = " ",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, LegalrepSolicitorSystemCrudAccess.class, AdmofficerRAccess.class, CaseofficerCrudAccess.class}
    )
    private ChecklistOutOfCountry checklistOutOfCountry;

    @CCD(
            label = " \r\n"
                            + "Address of where the appellant is detained.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, AdmofficerCaseofficerCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String detaineeAddressSubTitle;

    @CCD(
            label = "Address line 1",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String addressLine1AdminJ;

    @CCD(
            label = "Address line 2",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String addressLine2AdminJ;

    @CCD(
            label = "Address line 3",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String addressLine3AdminJ;

    @CCD(
            label = "Address line 4",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String addressLine4AdminJ;

    @CCD(
            label = "Country",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "isoCountriesGovUk",
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private IsoCountriesGovUk countryGovUkOocAdminJ;

    @CCD(label = " ", access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class})
    private String searchPostcode;

    @CCD(
            label = "Communication Preference",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class, SpecificAccessAdminProfileWaTaskConfigurationCrudAccess.class}
    )
    private ContactPreference contactPreference;

    @CCD(
            label = "This must be the appellant's email address. Please do not give your own",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {IacjudgeLegalrepSolicitorCrudAccess.class, AdmofficerCaseofficerCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class, SpecificAccessAdminProfileWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String emailInfoText;

    @CCD(
            label = "Appellant's email address",
            typeOverride = FieldType.Email,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class, SpecificAccessAdminProfileWaTaskConfigurationCrudAccess.class}
    )
    private String email;

    @CCD(
            label = "Retype appellant's email address",
            typeOverride = FieldType.Email,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class, SpecificAccessAdminProfileWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String emailRetype;

    @CCD(
            label = "This must be the appellant's mobile phone number. Please do not give your own",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class, SpecificAccessAdminProfileWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String mobilePhoneNumberInfoText;

    @CCD(
            label = "Appellant's mobile phone number",
            regex = "^(?=(?:\\D*\\d){7,15}\\D*$)\\+?(\\d[\\d-. ]+)?(\\([\\d-. ]+\\))?[\\d-. ]*\\d$",
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class, SpecificAccessAdminProfileWaTaskConfigurationCrudAccess.class}
    )
    private String mobileNumber;

    @CCD(
            label = "Retype appellant's mobile phone number",
            regex = "^(?=(?:\\D*\\d){7,15}\\D*$)\\+?(\\d[\\d-. ]+)?(\\([\\d-. ]+\\))?[\\d-. ]*\\d$",
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class, SpecificAccessAdminProfileWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String mobileNumberRetype;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String newMattersTitle;

    @CCD(
            label = "Are there any reasons the appellant wishes to remain in the UK or any new grounds on which they should be permitted to stay?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo hasNewMatters;

    @CCD(
            label = "Explain these new matters and their relevance to the appeal",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String newMatters;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String hasOtherAppealsTitle;

    @CCD(
            label = "Other appeals",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private OtherAppeals hasOtherAppeals;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String otherAppealsTitle;

    @CCD(
            label = "Appeal number",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<AppealNumber>> otherAppeals;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class, IacjudgeRAccess.class}
    )
    private Set<AdminDeclaration> adminDeclaration1;

    @CCD(
            label = "Address line 1",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private String oocAddressLine1;

    @CCD(
            label = "Address line 2",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private String oocAddressLine2;

    @CCD(
            label = "Address line 3",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private String oocAddressLine3;

    @CCD(
            label = "Address line 4",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private String oocAddressLine4;

    @CCD(
            label = "Country",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "isoCountriesGovUk",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private IsoCountriesGovUk oocLrCountryGovUkAdminJ;

    @CCD(
            label = "Online case reference number",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String ccdReferenceNumberForDisplay;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
    private String queriesTabTitle;

    @CCD(
            label = "Component Launcher",
            searchable = false,
            typeOverride = FieldType.ComponentLauncher,
            access = {AdmofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String queriesComponentLauncher;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.CaseQueriesCollection,
            access = {AdmofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String qmLegalRepresentativeQueries;

    @CCD(label = " ", searchable = false, access = {AdmofficerLegalrepSolicitorRAccess.class, SystemCrudAccess.class})
    private LatestQuery qmLatestQuery;

    @CCD(
            label = "![]()",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerLegalrepSolicitorRAccess.class, SystemCrudAccess.class}
    )
    private String spacer;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String documentsTabTitle;

    @CCD(
            label = "Additional evidence from appellant",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenWaTaskConfigurationCrudAccess.class, IacjudgeRAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> additionalEvidenceDocuments;

    @CCD(
            label = "Reheard hearing documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> reheardHearingDocuments;

    @CCD(
            label = "Respondent documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> respondentDocuments;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String directionsTabTitle;

    @CCD(
            label = "Directions",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private List<ListValue<Direction>> directions;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private Direction lastModifiedDirection;

    @CCD(
            label = "Directions",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, IacjudgeCrudAccess.class}
    )
    private List<ListValue<EditableDirection>> editableDirections;

    @CCD(
            label = "[Send a new direction](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/sendDirection)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String sendDirectionAction;

    @CCD(
            label = "Explain the direction you are issuing",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, SpecificAccessAdminProfileCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private String sendDirectionExplanation;

    @CCD(
            label = "Who are you giving the direction to?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "parties",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private PartiesList sendDirectionParties;

    @CCD(
            label = "By what date must they comply?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private LocalDate sendDirectionDateDue;

    @CCD(
            label = "[Change a direction due date](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/changeDirectionDueDate)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String changeDirectionDueDateAction;

    @CCD(
            label = "Select the direction that needs updating.\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, IacjudgeCrudAccess.class}
    )
    private String changeDirectionDueDateTitle;

    @CCD(
            label = "#### Enter the new due date.\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String changeDirectionDueDateTitle2;

    @CCD(
            label = "You are directing the Home Office to supply their documents and evidence.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String requestRespondentEvidenceTitle;

    @CCD(
            label = "You are completing the validation of the case",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String completeCaseReviewStf24Title;

    @CCD(
            label = "Case review completed date",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private LocalDate completeCaseReviewDate;

    @CCD(
            label = "You are sending the case to Admin for listing",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String sendToAdminForListingTitle;

    @CCD(
            label = "Upload any case documents and evidence supplied by the respondent.\r\n"
                            + "\r\n"
                            + "#### Advice on uploads\r\n"
                            + "- Files must be no more than 100MB in size.\r\n"
                            + "- Upload a Word doc or PDF file\r\n"
                            + "- Before uploading a file, give it a meaningful file name. For example, respondent-evidence.pdf.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String uploadRespondentEvidenceTitle;

    @CCD(
            label = "Upload case documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> respondentEvidence;

    @CCD(
            label = "#### Files should be:\r\n"
                            + "- a maximum of 100MB in size (larger files must be split)\r\n"
                            + "- labelled clearly, e.g. HObundle.pdf\r\n"
                            + "- in Word or PDF format - other file formats must be sent to both the appellant and Tribunal",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficeapcRespondentofficerCrudAccess.class, SystemCrudAccess.class}
    )
    private String uploadHomeOfficeBundleTitle;

    @CCD(
            label = "You must now build your case by uploading your appeal argument and evidence.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class}
    )
    private String requestCaseBuildingTitle;

    @CCD(
            label = "Request to review the Home Office response",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class}
    )
    private String requestResponseReviewTitle;

    @CCD(
            label = "Request to submit new hearing requirements",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class}
    )
    private String requestNewHearingRequirementsTitle;

    @CCD(
            label = "You must provide all the information for the Home Office to conduct a thorough review of their decision at this stage.\r\n"
                            + "\r\n"
                            + "## 1. Download the template below and use it to create your appeal skeleton argument.\r\n"
                            + "\r\n"
                            + "<a href=\"https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/docs/AppealArgumentTemplate-v6.docx\" target=\"_blank\">Legal_Argument_Template_10_11_18.doc</a>\r\n"
                            + "\r\n"
                            + "Advice: Your appeal skeleton argument must explain why this appeal should be allowed. It must comply with the practice directions.\r\n"
                            + "\r\n"
                            + "## 2. Upload your appeal skeleton argument below.\r\n"
                            + "\r\n"
                            + "#### Advice on uploads\r\n"
                            + "- Files must be no more than 100MB in size.\r\n"
                            + "- Upload a Word doc or PDF file\r\n"
                            + "- Before uploading a file, give it a meaningful file name. For example, JSmith-Skeleton.pdf.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String buildCaseTitle;

    @CCD(
            label = "### 3. Upload the evidence upon which you rely, including any witness statements.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String buildCaseEvidenceTitle;

    @CCD(
            label = "Already uploaded files:",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficelartRespondentofficerCrudAccess.class, HomeofficeapcHomeofficepouRAccess.class}
    )
    private String uploadedLegalRepBuildCaseDocs;

    @CCD(
            label = "Appeal skeleton argument",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private Document caseArgumentDocument;

    @CCD(
            label = "Description",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String caseArgumentDescription;

    @CCD(
            label = "Evidence",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> caseArgumentEvidence;

    @CCD(
            label = "#### You must upload your appeal skeleton argument to submit your case",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String caseArgumentDocumentRequiredLabel;

    @CCD(
            label = "You are directing the respondent to review and respond to the appeal skeleton argument.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String requestRespondentReviewTitle;

    @CCD(
            label = "Anything you put here will be added to the appeal tab. The legal representative will be notified of the update.\r\n"
                            + "\r\n"
                            + "## Upload the appeal response as a document\r\n"
                            + "\r\n"
                            + "#### Advice on uploads\r\n"
                            + "- Files must be no more than 100MB in size.\r\n"
                            + "- Upload a Word doc or PDF file\r\n"
                            + "- Before uploading a file, give it a meaningful file name. For example, appeal_response.pdf.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String addAppealResponseTitle;

    @CCD(
            label = "#### Files should be:\r\n"
                            + "- a maximum of 100MB in size (larger files must be split)\r\n"
                            + "- labelled clearly, e.g. HOappealresponse.pdf\r\n"
                            + "- in Word or PDF format - other file formats must be sent to both the appellant and Tribunal",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficelartRespondentofficerSystemCrudAccess.class}
    )
    private String addHomeOfficeAppealResponseTitle;

    @CCD(
            label = "[Upload additional evidence](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/uploadAdditionalEvidence)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String uploadAdditionalEvidenceAction;

    @CCD(
            label = "Additional evidence is anything submitted after the appeal skeleton argument.\r\n"
                            + "\r\n"
                            + "#### Advice on uploads\r\n"
                            + "- Files must be no more than 100MB in size.\r\n"
                            + "- Upload a Word doc or PDF file\r\n"
                            + "- Before uploading a file, give it a meaningful file name. For example, health-report-JSmith.pdf.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String additionalEvidenceTitle;

    @CCD(
            label = "Upload documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CitizenLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<AdditionalEvidenceDocument>> additionalEvidence;

    @CCD(
            label = "[Mark evidence as reviewed](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/markEvidenceAsReviewed)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {CaseofficerSystemCrudAccess.class}
    )
    private String markEvidenceAsReviewedAction;

    @CCD(
            label = "Add the hearing details below.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String listCaseHearingTitle;

    @CCD(
            label = "Location",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingVenue",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private HearingVenue listCaseHearingCentre;

    @CCD(
            label = "Hearing Centre address",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenLegalrepSolicitorRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String listCaseHearingCentreAddress;

    @CCD(
            label = "Length",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingLength",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, CitizenRAccess.class}
    )
    private HearingLength listCaseHearingLength;

    @CCD(
            label = "Listing length",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, CitizenRAccess.class}
    )
    private HoursMinutes listingLength;

    @CCD(
            label = "Is the appeal suitable to float?",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo isAppealSuitableToFloat;

    @CCD(
            label = "Is a panel required for this hearing?",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo isPanelRequired;

    @CCD(
            label = "Are there any additional instructions for the hearing?",
            searchable = false,
            access = {LegalrepSolicitorSpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private YesOrNo additionalInstructions;

    @CCD(
            label = "Additional Instructions",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {LegalrepSolicitorSpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private String additionalInstructionsDescription;

    @CCD(
            label = "Date and time",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private LocalDateTime listCaseHearingDate;

    @CCD(
            label = "Appointment date and time",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String listCaseHearingDateAdjourned;

    @CCD(
            label = "Enter the agreed hearing requirements in the fields below.<br>\r\n"
                            + "Don't enter the reason the appellant made the request. The respondent will be shown this information and they must not be told of the appellant's personal circumstances.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String listCaseRequirementsTitle;

    @CCD(
            label = "Physical or mental health conditions",
            hint = "If the appellant says they have any personal vulnerabilities, state what adjustments the Tribunal will offer.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String listCaseRequirementsVulnerabilities;

    @CCD(
            label = "Multimedia equipment",
            hint = "If the appellant needs multimedia equipment, state what the Tribunal will provide.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String listCaseRequirementsMultimedia;

    @CCD(
            label = "Single-sex court",
            hint = "If the appellant asked for a single-sex court, state which gender was requested and whether the Tribunal can accommodate this.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String listCaseRequirementsSingleSexCourt;

    @CCD(
            label = "In camera court",
            hint = "If the appellant asked for an in camera court, state whether the Tribunal can accommodate this.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String listCaseRequirementsInCameraCourt;

    @CCD(
            label = "Other adjustments",
            hint = "If the appellant made any other requests, state what adjustments the Tribunal will offer.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String listCaseRequirementsOther;

    @CCD(
            label = "## Create a case summary and upload it below\r\n"
                            + "\r\n"
                            + "Use the case summary template provided to you.\r\n"
                            + "\r\n"
                            + "You must use this template to summarise the most relevant information in the case. This will be used by all parties in the hearing. You do not need to complete all the fields in the template.\r\n"
                            + "\r\n"
                            + "Give the file a meaningful name before you upload it. For example, PA-54321-2019-Smith-CaseSummary.PDF\r\n"
                            + "\r\n"
                            + "#### Upload your case summary below",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseSummaryDocumentTitle;

    @CCD(
            label = "Case summary document",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private Document caseSummaryDocument;

    @CCD(
            label = "Case summary description",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String caseSummaryDescription;

    @CCD(
            label = "## Write a brief introduction to the case\r\n"
                            + "\r\n"
                            + "This can cover:\r\n"
                            + "- the appellant's country of origin\r\n"
                            + "- their date of birth\r\n"
                            + "- the decision they are appealing against",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseIntroductionTitle;

    @CCD(
            label = "Introduction",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String caseIntroductionDescription;

    @CCD(
            label = "Do both parties agree the immigration history?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private YesOrNo immigrationHistoryAgreement;

    @CCD(
            label = "Agreed immigration history",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String agreedImmigrationHistoryDescription;

    @CCD(
            label = "If the parties do not agree on the immigration history, you must give the respondent's accounts of the immigration history and the areas of disagreement.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String immigrationDisagreementInstructionsLabel;

    @CCD(
            label = "Give the respondent's account of the immigration history",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String respondentsImmigrationHistoryDescription;

    @CCD(
            label = "Give the areas of disagreement in relation to immigration history",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String immigrationHistoryDisagreementDescription;

    @CCD(
            label = "Do both parties agree the schedule of issues?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private YesOrNo scheduleOfIssuesAgreement;

    @CCD(
            label = "Go to the documents tab and find the appeal skeleton argument for this case. This should contain the schedule of issues written by the appellant's legal representative. You can copy and paste the schedule of issues into the box below.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String scheduleOfIssuesAgreementInstructionsLabel;

    @CCD(
            label = "The appellant's schedule of issues",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String appellantsAgreedScheduleOfIssuesDescription;

    @CCD(
            label = "Any discussion of the schedule of issues is likely to be found in the document tab. You should check the appeal skeleton argument and the Home Office's response and include the relevant information in the boxes below.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String scheduleOfIssuesDisagreementInstructionsLabel;

    @CCD(
            label = "The appellant's schedule of issues as stated in the appeal skeleton argument",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String appellantsDisputedScheduleOfIssuesDescription;

    @CCD(
            label = "Areas of disagreement between the parties concerning the appellant's schedule of issues",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerRAccess.class}
    )
    private String scheduleOfIssuesDisagreementDescription;

    @CCD(
            label = "### Are you giving an anonymity direction?",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String anonymityOrderLabel;

    @CCD(
            label = "Anonymity direction",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, IacjudgeCrudAccess.class}
    )
    private YesOrNo anonymityOrder;

    @CCD(
            label = "Legal representative for the respondent",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, IacjudgeCrudAccess.class}
    )
    private String respondentRepresentative;

    @CCD(
            label = "Decision and reasons documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> draftDecisionAndReasonsDocuments;

    @CCD(
            label = "Reheard decision and reasons documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> draftReheardDecisionAndReasons;

    @CCD(
            label = "Internal timed event ID for request hearing requirements automatic direction",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String automaticDirectionRequestingHearingRequirements;

    @CCD(
            label = "Internal timed event ID for automatic ending appeal",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String automaticEndAppealTimedEventId;

    @CCD(
            label = "Internal timed event ID for internal send payment reminder",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String automaticSendPaymentReminderTimedEventId;

    @CCD(
            label = "Internal collection managed by API to track notifications sent using GovNotify for this case",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private List<ListValue<String>> notificationsSent;

    @CCD(
            label = "Internal flag managed by API to enable/disable changeDirectionDueDateAction (inline action)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo changeDirectionDueDateActionAvailable;

    @CCD(
            label = "Internal flag managed by API to enable/disable sendDirectionAction (inline action)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo sendDirectionActionAvailable;

    @CCD(
            label = "Internal flag managed by API to enable/disable uploadHomeOfficeBundleAction (inline action)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class}
    )
    private YesOrNo uploadHomeOfficeBundleActionAvailable;

    @CCD(
            label = "Internal flag managed by API to enable/disable uploadHomeOfficeBundleAction validation (inline action)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerRAccess.class}
    )
    private YesOrNo uploadHomeOfficeBundleAvailable;

    @CCD(
            label = "Internal flag managed by API to enable/disable requestRespondentEvidence event for stf24w cases",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo shouldHideRequestRespondentEvidence;

    @CCD(
            label = "Already uploaded files:",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, HomeofficeapcRespondentofficerCrudAccess.class}
    )
    private String uploadedHomeOfficeBundleDocs;

    @CCD(
            label = "Internal flag managed by API to enable/disable uploadHomeOfficeAppealResponseAction (inline action)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo uploadHomeOfficeAppealResponseActionAvailable;

    @CCD(
            label = "Already uploaded files:",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, HomeofficelartRespondentofficerCrudAccess.class}
    )
    private String uploadedHomeOfficeAppealResponseDocs;

    @CCD(
            label = "Internal flag managed by API to enable/disable uploadAdditionalEvidenceAction (inline action)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo uploadAdditionalEvidenceActionAvailable;

    @CCD(
            label = "Internal flag managed by API to enable/disable markEvidenceAsReviewedAction (inline action)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo markEvidenceAsReviewedActionAvailable;

    @CCD(
            label = "Internal flag managed by API to show correct case progression within the caseBuilding state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo caseBuildingReadyForSubmission;

    @CCD(
            label = "Internal flag managed by API to show correct case progression within the respondentReview state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo respondentReviewAppealResponseAdded;

    @CCD(
            label = "Internal flag managed by API to enable/disable reviewResponseAction (inline action)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo reviewResponseActionAvailable;

    @CCD(
            label = "Internal flag managed by API to enable/disable amendResponseAction (inline action)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo amendResponseActionAvailable;

    @CCD(
            label = "Internal flag managed by API to show correct case state for case officers",
            searchable = false,
            access = {SystemCrudAccess.class, SpecificAccessLegalOpsProfileRAccess.class}
    )
    private String currentCaseStateVisibleToCaseOfficer;

    @CCD(
            label = "Internal flag managed by API to show correct case state for Judges",
            searchable = false,
            access = {SystemCrudAccess.class, SpecificAccessJudgeProfileRAccess.class}
    )
    private String currentCaseStateVisibleToJudge;

    @CCD(
            label = "Internal flag managed by API to show correct case state for legal representatives",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String currentCaseStateVisibleToLegalRepresentative;

    @CCD(
            label = "Internal flag managed by API to show correct case state for admin",
            searchable = false,
            access = {SystemCrudAccess.class, SpecificAccessAdminProfileRAccess.class}
    )
    private String currentCaseStateVisibleToAdminOfficer;

    @CCD(
            label = "Internal flag managed by API to show correct case state for home office apc",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String currentCaseStateVisibleToHomeOfficeApc;

    @CCD(
            label = "Internal flag managed by API to show correct case state for home office lart",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String currentCaseStateVisibleToHomeOfficeLart;

    @CCD(
            label = "Internal flag managed by API to show correct case state for home office pou",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String currentCaseStateVisibleToHomeOfficePou;

    @CCD(
            label = "Internal flag managed by API to show correct case state for home office generic",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String currentCaseStateVisibleToHomeOfficeGeneric;

    @CCD(
            label = "Internal flag managed by API to show correct case state for home office all",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String currentCaseStateVisibleToHomeOfficeAll;

    @CCD(
            label = "Internal flag managed by API to show correct case state for home office and legal rep",
            searchable = false,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private YesOrNo reviewHomeOfficeResponseByLegalRep;

    @CCD(
            label = "Internal flag managed by API to show correct case state for case officers",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String updateHearingRequirementsExists;

    @CCD(
            label = "Internal flag managed by API to show that Judge allocation exists",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class}
    )
    private YesOrNo judgeAllocationExists;

    @CCD(
            label = "Was the appeal submission late?",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo submissionOutOfTime;

    @CCD(
            label = "Internal flag managed by API to enable/disable case argument in appeal tab",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo caseArgumentAvailable;

    @CCD(
            label = "Internal flag managed by API to identify if final bundling stitching is complete",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String stitchingStatus;

    @CCD(
            label = "Internal flag managed by API to identify final bundling file name prefix",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String bundleFileNamePrefix;

    @CCD(
            label = "## What is your decision?  \r\n"
                            + "\r\n"
                            + "If the appeal is dismissed on all grounds, select 'Dismissed on all grounds'. If the appeal is allowed on any grounds, you must select 'Allowed'.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String isDecisionAllowedTitle;

    @CCD(
            label = "Decision",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, CitizenCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private IsDecisionAllowed isDecisionAllowed;

    @CCD(
            label = "## Upload your decision and reasons as a PDF ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String uploadDecisionAndReasonsTitle;

    @CCD(
            label = "Decision and reasons",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private Document finalDecisionAndReasonsDocument;

    @CCD(
            label = "       ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private IsDocumentSignedToday isDocumentSignedToday;

    @CCD(
            label = "       ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private IsFeeConsistentWithDecision isFeeConsistentWithDecision;

    @CCD(
            label = "Decision and reasons document",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private Document finalDecisionAndReasonsPdf;

    @CCD(
            label = "Decision and reasons documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> finalDecisionAndReasonsDocuments;

    @CCD(
            label = "Reheard decision and reasons documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> reheardDecisionReasonsDocuments;

    @CCD(
            label = "### Add your case note below",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, RparobotRAccess.class, SystemCrudAccess.class}
    )
    private String addCaseNoteTitle;

    @CCD(
            label = "Subject",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, RparobotCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String addCaseNoteSubject;

    @CCD(
            label = "Case note",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, RparobotCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String addCaseNoteDescription;

    @CCD(
            label = "Upload a document",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, RparobotCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private Document addCaseNoteDocument;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String caseNotesTabTitle;

    @CCD(
            label = "[Add case note](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/addCaseNote)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String addCaseNoteLink;

    @CCD(
            label = "Case notes",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<CaseNote>> caseNotes;

    @CCD(
            label = "You're editing the details of a case that's already been listed. Amend the hearing details below. ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String editCaseListingTitle;

    @CCD(
            label = "![Attention: Notifications turned off. You should turn notifications on when you have progressed the case to the correct state](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/redydrate_turnoff.svg)",
            searchable = false,
            typeOverride = FieldType.Label
    )
    private String notifications_turned_off_banner;

    @CCD(label = "Service Request", searchable = false, access = {SystemCrudAccess.class})
    private WaysToPay serviceRequest;

    @CCD(
            label = "![]()\r\n"
                            + "## Case details",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String caseDetailsTitle;

    @CCD(
            label = "![]()\r\n"
                            + "## Appointment attendance",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String appointmentAttendanceTitle;

    @CCD(
            label = "![]()\r\n"
                            + "## Submission details",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String submissionDetailsTitle;

    @CCD(
            label = "Internal flag managed by API to enable/disable uploadAddendumEvidenceAction (inline action)",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo uploadAddendumEvidenceActionAvailable;

    @CCD(
            label = "Evidence supplied after the hearing bundle",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> addendumEvidenceDocuments;

    @CCD(
            label = "[Add additional evidence as an addendum](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/uploadAddendumEvidence)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private String uploadAddendumEvidenceAction;

    @CCD(
            label = "Internal flag managed by API to enable/disable uploadAddendumEvidenceLegalRepAction (inline action)",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo uploadAddendumEvidenceLegalRepActionAvailable;

    @CCD(
            label = "[Add additional evidence as an addendum](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/uploadAddendumEvidenceLegalRep)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String uploadAddendumEvidenceLegalRepAction;

    @CCD(
            label = "Internal flag managed by API to enable/disable uploadAddendumEvidenceHomeOfficeAction (inline action)",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo uploadAddendumEvidenceHomeOfficeActionAvailable;

    @CCD(
            label = "[Add additional evidence as an addendum](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/uploadAddendumEvidenceHomeOffice)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String uploadAddendumEvidenceHomeOfficeAction;

    @CCD(
            label = "Internal flag managed by API to enable/disable uploadAddendumEvidenceAdminOfficerAction (inline action)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo uploadAddendumEvidenceAdminOfficerActionAvailable;

    @CCD(
            label = "Internal flag managed by API to enable/disable markAddendumEvidenceAsReviewedAction (inline action)",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo markAddendumEvidenceAsReviewedActionAvailable;

    @CCD(
            label = "[Add additional evidence as an addendum](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/uploadAddendumEvidenceAdminOfficer)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String uploadAddendumEvidenceAdminOfficerAction;

    @CCD(
            label = "[Mark evidence as reviewed](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/markAddendumEvidenceAsReviewed)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String markAddendumEvidenceAsReviewedAction;

    @CCD(
            label = "Add the evidence and the context. This evidence will not be included in the hearing bundle, it will be added as an addendum.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String addendumEvidencePageTitle;

    @CCD(
            label = "If you have evidence from both the appellant and the respondent, submit these seperately.\r\n"
                            + "\r\n"
                            + "#### Advice on uploads\r\n"
                            + "- Files must be no more than 100MB in size.\r\n"
                            + "- You can upload jpg, png, svg, gif, doc and PDF files.\r\n"
                            + "- Before uploading a file, give it a meaningful file name. For example, health-report-Jsmith.pdf.\r\n"
                            + "\r\n"
                            + "#### Upload evidence",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String addendumEvidencePageTitle2;

    @CCD(
            label = "Add the evidence and the context. This evidence will not be included in the hearing bundle, it will be added as an addendum.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String addendumEvidenceLegalRepPageTitle;

    @CCD(
            label = "#### Advice on uploads\r\n"
                            + "- Files must be no more than 100MB in size.\r\n"
                            + "- You can upload jpg, png, svg, gif, doc and PDF files.\r\n"
                            + "- Before uploading a file, give it a meaningful file name. For example, health-report-Jsmith.pdf.\r\n"
                            + "\r\n"
                            + "#### Upload evidence",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String addendumEvidenceLegalRepPageTitle2;

    @CCD(
            label = "Add the evidence and the context. This evidence will not be included in the hearing bundle, it will be added as an addendum.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SystemCrudAccess.class}
    )
    private String addendumEvidenceHomeOfficePageTitle;

    @CCD(
            label = "#### Advice on uploads\r\n"
                            + "- Files must be no more than 100MB in size.\r\n"
                            + "- You can upload jpg, png, svg, gif, doc and PDF files.\r\n"
                            + "- Before uploading a file, give it a meaningful file name. For example, health-report-Jsmith.pdf.\r\n"
                            + "\r\n"
                            + "#### Upload evidence",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SystemCrudAccess.class}
    )
    private String addendumEvidenceHomeOfficePageTitle2;

    @CCD(
            label = "Supplied by",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private IsAppellantRespondent isAppellantRespondent;

    @CCD(
            label = "You'll need to explain why this evidence is being submitted after the hearing bundle has been produced.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String addendumEvidenceTitle;

    @CCD(
            label = "Evidence",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<AdditionalAddendumDocument>> addendumEvidence;

    @CCD(
            label = "Listing reference",
            hint = "Example: LP/12345/2019",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerIacjudgeRAccess.class}
    )
    private String ariaListingReference;

    @CCD(
            label = "Applications",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<Application>> applications;

    @CCD(
            label = "The appellant or the respondent has made an application. Record the reasons and the outcome of the application. If the application has been granted, you may need to edit listing, edit direction due date or end the appeal.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class}
    )
    private String applicationsTitle;

    @CCD(
            label = "Attach a copy of the application email and any other communication.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class}
    )
    private String applicationsTitle2;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private String applicationsTabTitle;

    @CCD(
            label = "[Review a time extension application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/reviewTimeExtension)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String timeExtensionLink;

    @CCD(
            label = "[Direct the appellant to answer clarifying questions](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/sendDirectionWithQuestions)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String sendDirectionWithQuestionsLink;

    @CCD(
            label = "[Direct the appellant to submit requirements for a case management appointment](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestCmaRequirements)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String requestCmaRequirementsLink;

    @CCD(
            label = "Internal flag managed by API to enable/disable overview page",
            searchable = false,
            access = {CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private YesOrNo disableOverviewPage;

    @CCD(label = "Internal flag managed by API", searchable = false, access = {AdmofficerSystemCrudAccess.class})
    private YesOrNo haveHearingAttendeesAndDurationBeenRecorded;

    @CCD(
            label = "Add the attendees and case duration below. If someone didn't attend, leave it blank.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String recordAttendeesAndDurationTitle;

    @CCD(
            label = "The judge",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String attendingJudge;

    @CCD(
            label = "The appellant",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String attendingAppellant;

    @CCD(
            label = "The appellant's legal representative",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String attendingAppellantsLegalRepresentative;

    @CCD(
            label = "Home Office representative",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String attendingHomeOfficeLegalRepresentative;

    @CCD(
            label = "Duration",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private HoursAndMinutes actualCaseHearingLength;

    @CCD(
            label = "This appeal has ended. Record the outcome and reasons below.",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerCitizenCruAccess.class, IacjudgeCruLegalrepSolicitorSpecificAccessCtscProfileRAccess.class}
    )
    private String endAppealTitle;

    @CCD(
            label = "Outcome of the appeal",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerCitizenCruAccess.class, IacjudgeCruAccess.class}
    )
    private EndAppealOutcome endAppealOutcome;

    @CCD(
            label = "Reasons for this outcome",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerCitizenCruAccess.class, IacjudgeCruAccess.class}
    )
    private String endAppealOutcomeReason;

    @CCD(
            label = "Record who approved this outcome.",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerCitizenCruAccess.class, IacjudgeCruLegalrepSolicitorSpecificAccessCtscProfileRAccess.class}
    )
    private String endAppealTitle2;

    @CCD(
            label = "Approved by",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerIacjudgeCruAccess.class}
    )
    private EndAppealApproverType endAppealApproverType;

    @CCD(
            label = "Name of approver",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemCrudAccess.class, AdmofficerIacjudgeCruAccess.class}
    )
    private String endAppealApproverName;

    @CCD(
            label = "Outcome date",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCruAccess.class}
    )
    private LocalDate endAppealDate;

    @CCD(
            label = "Tribunal documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> tribunalDocuments;

    @CCD(
            label = "## Ended appeal details",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCruAccess.class}
    )
    private String endAppealDetailsTitle;

    @CCD(
            label = "Internal flag managed by API",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo addCaseNoteActionDisabled;

    @CCD(
            label = "Internal flag managed by API",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo recordApplicationActionDisabled;

    @CCD(
            label = "State before End Appeal",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String stateBeforeEndAppeal;

    @CCD(
            label = "Reinstated Decision Maker",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String reinstatedDecisionMaker;

    @CCD(
            label = "Date reinstated",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private LocalDate reinstateAppealDate;

    @CCD(
            label = "Reason for reinstating",
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class}
    )
    private String reinstateAppealReason;

    @CCD(
            label = "[Upload additional evidence](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/uploadAdditionalEvidenceHomeOffice)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String uploadAdditionalEvidenceHomeOfficeAction;

    @CCD(
            label = "Internal flag managed by API to enable/disable uploadAdditionalEvidenceAction (inline action)",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo uploadAdditionalEvidenceHomeOfficeActionAvailable;

    @CCD(
            label = "#### Files should be:\r\n"
                            + "- a maximum of 100MB in size (larger file must be split) \r\n"
                            + "- labelled clearly, e.g. MedicalRecord.pdf\r\n"
                            + "- in Word or PDF format – other file formats must be sent to both the appelant and Tribunal\r\n"
                            + "\r\n"
                            + "You'll need to explain why this evidence is being submitted late.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, SystemCrudAccess.class}
    )
    private String additionalEvidenceHomeOfficeTitle;

    @CCD(
            label = "Additional evidence",
            searchable = false,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SystemCrudAccess.class}
    )
    private List<ListValue<AdditionalEvidenceHomeOfficeDocument>> additionalEvidenceHomeOffice;

    @CCD(
            label = "Subscriptions",
            searchable = false,
            access = {LegalrepSolicitorSystemCrudAccess.class, CitizenWaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<Subscription>> subscriptions;

    @CCD(
            label = "Type of Journey",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "journeyTypes",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private JourneyTypes journeyType;

    @CCD(
            label = "Will the appellant or anyone else be giving oral evidence from outside the United Kingdom?",
            hint = "In certain circumstances an appellant or witness who is outside the United Kingdom may be able to participate in the hearing remotely.",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo isEvidenceFromOutsideUkOoc;

    @CCD(
            label = "Will the appellant or anyone else be giving oral evidence from outside the United Kingdom?",
            hint = "In certain circumstances an appellant or witness who is outside the United Kingdom may be able to participate in the hearing remotely.",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class, IacjudgeRAccess.class}
    )
    private YesOrNo isEvidenceFromOutsideUkInCountry;

    @CCD(
            label = "Will the appellant attend the hearing?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class, CitizenRAccess.class}
    )
    private YesOrNo isAppellantAttendingTheHearing;

    @CCD(
            label = "Will the appellant give oral evidence at the hearing?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class, CitizenRAccess.class}
    )
    private YesOrNo isAppellantGivingOralEvidence;

    @CCD(
            label = "Will any witnesses attend the hearing?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo isWitnessesAttending;

    @CCD(
            label = "Witness details",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private List<ListValue<WitnessDetails>> witnessDetails;

    @CCD(
            label = "Do you need interpreter services on the day?",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo isInterpreterServicesNeeded;

    @CCD(
            label = "Interpreter details",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<InterpreterLanguage>> interpreterLanguage;

    @CCD(
            label = "Tell us which spoken language is needed for the appellant",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSpokenLanguage appellantInterpreterSpokenLanguage;

    @CCD(
            label = "Tell us which sign language is needed for the appellant",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSignLanguage appellantInterpreterSignLanguage;

    @CCD(
            label = "Will any of the witnesses require a spoken or sign language interpreter at the hearing?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private YesOrNo isAnyWitnessInterpreterRequired;

    @CCD(
            label = "Select the witnesses that need a spoken or sign language interpreter and the kind of interpreter they need",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, SystemCrudAdmofficerLegalrepSolicitorRAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class, CitizenIacjudgeRAccess.class, WaTaskConfigurationCrudCaseofficerRAccess.class}
    )
    private String whichWitnessRequiresInterpreterLabel;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private WitnessDetails witness1;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private WitnessDetails witness2;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private WitnessDetails witness3;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private WitnessDetails witness4;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private WitnessDetails witness5;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private WitnessDetails witness6;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private WitnessDetails witness7;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private WitnessDetails witness8;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private WitnessDetails witness9;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private WitnessDetails witness10;

    @CCD(
            label = "What kind of interpreter does ${witness1.witnessName} ${witness1.witnessFamilyName} need?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private Set<InterpreterLanguageCategory> witness1InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${witness2.witnessName} ${witness2.witnessFamilyName} need?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private Set<InterpreterLanguageCategory> witness2InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${witness3.witnessName} ${witness3.witnessFamilyName} need?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private Set<InterpreterLanguageCategory> witness3InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${witness4.witnessName} ${witness4.witnessFamilyName} need?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private Set<InterpreterLanguageCategory> witness4InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${witness5.witnessName} ${witness5.witnessFamilyName} need?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private Set<InterpreterLanguageCategory> witness5InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${witness6.witnessName} ${witness6.witnessFamilyName} need?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private Set<InterpreterLanguageCategory> witness6InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${witness7.witnessName} ${witness7.witnessFamilyName} need?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private Set<InterpreterLanguageCategory> witness7InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${witness8.witnessName} ${witness8.witnessFamilyName} need?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private Set<InterpreterLanguageCategory> witness8InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${witness9.witnessName} ${witness9.witnessFamilyName} need?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private Set<InterpreterLanguageCategory> witness9InterpreterLanguageCategory;

    @CCD(
            label = "What kind of interpreter does ${witness10.witnessName} ${witness10.witnessFamilyName} need?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessJudgeLegalOpsProfileRAccess.class}
    )
    private Set<InterpreterLanguageCategory> witness10InterpreterLanguageCategory;

    @CCD(
            label = "Which spoken language interpreter is needed for ${witness1.witnessName} ${witness1.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSpokenLanguage witness1InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${witness1.witnessName} ${witness1.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSignLanguage witness1InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language interpreter is needed for ${witness2.witnessName} ${witness2.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSpokenLanguage witness2InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${witness2.witnessName} ${witness2.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSignLanguage witness2InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language interpreter is needed for ${witness3.witnessName} ${witness3.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSpokenLanguage witness3InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${witness3.witnessName} ${witness3.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSignLanguage witness3InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language interpreter is needed for ${witness4.witnessName} ${witness4.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSpokenLanguage witness4InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${witness4.witnessName} ${witness4.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSignLanguage witness4InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language interpreter is needed for ${witness5.witnessName} ${witness5.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSpokenLanguage witness5InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${witness5.witnessName} ${witness5.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSignLanguage witness5InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language interpreter is needed for ${witness6.witnessName} ${witness6.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSpokenLanguage witness6InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${witness6.witnessName} ${witness6.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSignLanguage witness6InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language interpreter is needed for ${witness7.witnessName} ${witness7.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSpokenLanguage witness7InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${witness7.witnessName} ${witness7.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSignLanguage witness7InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language interpreter is needed for ${witness8.witnessName} ${witness8.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSpokenLanguage witness8InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${witness8.witnessName} ${witness8.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSignLanguage witness8InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language interpreter is needed for ${witness9.witnessName} ${witness9.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSpokenLanguage witness9InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${witness9.witnessName} ${witness9.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSignLanguage witness9InterpreterSignLanguage;

    @CCD(
            label = "Which spoken language interpreter is needed for ${witness10.witnessName} ${witness10.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSpokenLanguage witness10InterpreterSpokenLanguage;

    @CCD(
            label = "Which sign language is needed for ${witness10.witnessName} ${witness10.witnessFamilyName}?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private InterpreterSignLanguage witness10InterpreterSignLanguage;

    @CCD(
            label = "Do you need a hearing room with step-free access?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo isHearingRoomNeeded;

    @CCD(
            label = "Do you need a hearing loop?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo isHearingLoopNeeded;

    @CCD(
            label = "There may be some things you'd like the Tribunal to consider when deciding if a video call is suitable. For example, will the appellant and/or any witnesses have:\r\n"
                            + "- reliable access to a computer or mobile device, and the internet?\r\n"
                            + "- access to a quiet, private room?\r\n"
                            + "- a disability or anything else that would make it difficult to attend by video?",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String remoteVideoCallTitle;

    @CCD(
            label = "Is there anything you'd like the Tribunal to consider when deciding if a video call is suitable?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenCrudAccess.class}
    )
    private YesOrNo remoteVideoCall;

    @CCD(
            label = "Explain in detail anything you would like the Tribunal to consider",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenCrudAccess.class}
    )
    private String remoteVideoCallDescription;

    @CCD(
            label = "Does the appellant have any physical or mental health issues that may impact them on the day?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo physicalOrMentalHealthIssues;

    @CCD(
            label = "Explain in detail how any physical or mental health issues may affect them on the day.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String physicalOrMentalHealthIssuesDescription;

    @CCD(
            label = "Has the appellant had any past experiences that may impact them on the day?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo pastExperiences;

    @CCD(
            label = "Explain in detail how any past experiences may affect them on the day?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String pastExperiencesDescription;

    @CCD(
            label = "Do you have multimedia evidence?",
            hint = "For example, video or sound recordings.",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo multimediaEvidence;

    @CCD(
            label = "You should provide the equipment to play this evidence. If this is not possible, explain why and what equipment you'll need to play it.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String multimediaEvidenceDescription;

    @CCD(
            label = "Does the appellant need a single-sex court?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo singleSexCourt;

    @CCD(
            label = "Does the appellant bring the equipment to play this evidence?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, CitizenSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private YesOrNo bringOwnMultimediaEquipment;

    @CCD(
            label = "What type of court do they need?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private SingleSexCourtType singleSexCourtType;

    @CCD(
            label = "Explain in detail why the appellant needs a single-sex court.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String singleSexCourtTypeDescription;

    @CCD(
            label = "Does the appellant need an in camera court?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo inCameraCourt;

    @CCD(
            label = "Explain in detail why the appellant needs an in camera court.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String inCameraCourtDescription;

    @CCD(
            label = "Is there anything else you would like to request?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo additionalRequests;

    @CCD(
            label = "Provide details of any additional requests and why they are necessary.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String additionalRequestsDescription;

    @CCD(
            label = "## Hearing dates to avoid",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String datesToAvoidTitle;

    @CCD(
            label = "Are there any dates that the appellant or their on-day representation cannot attend?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, AdmofficerCaseofficerLegalrepSolicitorCruAccess.class, WaTaskConfigurationCruAccess.class, CitizenCrudAccess.class}
    )
    private YesOrNo datesToAvoidYesNo;

    @CCD(
            label = "Tell us which dates and explain why the case cannot be heard on these dates.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerSystemCrudAccess.class, CitizenCrudAccess.class}
    )
    private String datesToAvoidDescription;

    @CCD(
            label = "Dates to avoid",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, AdmofficerCaseofficerLegalrepSolicitorCruAccess.class, WaTaskConfigurationCruAccess.class, CitizenCrudAccess.class}
    )
    private List<ListValue<DatesToAvoid>> datesToAvoid;

    @CCD(
            label = "You should only remove an appeal when you're sure it cannot continue through the online service. This decision cannot be undone and may need to be reviewed by a judge.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CaseofficerWaTaskConfigurationCruAccess.class, SystemCrudAccess.class}
    )
    private String removeAppealFromOnlineTitle;

    @CCD(
            label = "Reasons for removal",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerIacjudgeRAccess.class, CaseofficerWaTaskConfigurationCruAccess.class, SystemCrudAccess.class}
    )
    private String removeAppealFromOnlineReason;

    @CCD(
            label = "Removal date",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CitizenSystemCrudAccess.class, CaseofficerWaTaskConfigurationCruAccess.class}
    )
    private LocalDate removeAppealFromOnlineDate;

    @CCD(
            label = "# Appointment",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String appointmentTitle;

    @CCD(
            label = "![]()\r\n"
                            + "## Appointment requirements",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String appointmentRequirementsTabTitle;

    @CCD(
            label = "![]()\r\n"
                            + "## Requests for additional adjustments\r\n"
                            + "These requests are only visible to the Tribunal and appellant.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String additionalAdjustmentsTitle;

    @CCD(
            label = "![]()\r\n"
                            + "## Dates to avoid\r\n"
                            + "These dates are only visible to the appellant and the Tribunal. When listing the case, take these dates into consideration where possible.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String datesToAvoidTabTitle;

    @CCD(
            label = "![]()\r\n"
                            + "## Record of requirements and requests",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String recordOfHearingTitle;

    @CCD(
            label = "Previous record of requirements and requests",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CitizenSystemCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> previousHearingRequirements;

    @CCD(
            label = "Internal flag managed by API to enable/disable Hearing tab (inline action)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class, IacjudgeRAccess.class}
    )
    private YesOrNo submitHearingRequirementsAvailable;

    @CCD(
            label = "Internal flag managed by API to manage validation",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo updatedHearingRequirements;

    @CCD(
            label = "Internal flag managed by API to manage validation",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo reviewedUpdatedHearingRequirements;

    @CCD(
            label = "Choose colleague from below list to share the case",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorRAccess.class, SystemCrudAccess.class}
    )
    private String shareACaseTitle;

    @CCD(
            label = "Colleague",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String orgListOfUsers;

    @CCD(label = "Colleagues", searchable = false, access = {LegalrepSolicitorSystemCrudAccess.class})
    private List<ListValue<String>> shareACaseUserList;

    @CCD(
            label = "Review the appellant's hearing requirements and select length of hearing.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String reviewHearingRequirementsTitle;

    @CCD(
            label = "Case has already been shared with Users:",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String shareACaseUserListReadOnly;

    @CCD(
            label = "Internal flag managed by API to show correct case state for Legal representative",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerRAccess.class}
    )
    private YesOrNo reviewedHearingRequirements;

    @CCD(
            label = "Internal flag managed by API to show correct case state for case officer",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo caseListedWithoutHearingRequirements;

    @CCD(
            label = "Internal flag managed by API to show correct reheard case data",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo reheardCaseListedWithoutHearingRequirements;

    @CCD(
            label = "Interpreter details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String interpreterLanguageReadonly;

    @CCD(
            label = "Continue to see requests for additional adjustments.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerLegalrepSolicitorRAccess.class, SystemCrudAccess.class}
    )
    private String continueToSeeAdditionalAdjustments;

    @CCD(
            label = "\r\n"
                            + "## Additional adjustments",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerLegalrepSolicitorRAccess.class, SystemCrudAccess.class}
    )
    private String reviewAdjustmentsTitle;

    @CCD(
            label = "Check if the appellant has requested any additional adjustments and record your response in the relevant field. Do not enter the reason the appellant made the request. The respondent will be able to see this information and must not be informed of the appellant's personal circumstances.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String additionalAdjustmentsDescription;

    @CCD(
            label = "\r\n"
                            + "## Adjustment request",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String vulnerabilitiesAdjustmentRequestTitle;

    @CCD(
            label = "\r\n"
                            + "## Tribunal response",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String vulnerabilitiesTribunalResponseTitle;

    @CCD(
            label = "Vulnerabilities decision",
            searchable = false,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerLegalrepSolicitorRAccess.class, IacjudgeCrudAccess.class}
    )
    private IsVulnerabilitiesAllowed isVulnerabilitiesAllowed;

    @CCD(
            label = "Adjustments to accommodate vulnerabilities",
            hint = "State what adjustments the Tribunal will provide, or explain why this request cannot be fulfilled.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, IacjudgeCrudAccess.class}
    )
    private String vulnerabilitiesTribunalResponse;

    @CCD(
            label = "\r\n"
                            + "## Additional adjustments",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerLegalrepSolicitorRAccess.class, CaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String remoteHearingReviewAdjustmentsTitle;

    @CCD(
            label = "Check if the appellant has requested any additional adjustments and record your response in the relevant field. Do not enter the reason the appellant made the request. The respondent will be able to see this information and must not be informed of the appellant's personal circumstances.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String remoteHearingAdditionalAdjustmentsDescription;

    @CCD(
            label = "\r\n"
                            + "## Adjustment request",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String remoteVideoCallAdjustmentRequestTitle;

    @CCD(
            label = "\r\n"
                            + "## Tribunal response",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String remoteVideoCallTribunalResponseTitle;

    @CCD(
            label = "Remote hearing decision",
            searchable = false,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, AdmofficerLegalrepSolicitorRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private IsRemoteHearingAllowed isRemoteHearingAllowed;

    @CCD(
            label = "Remote hearing",
            hint = "State what adjustments the Tribunal will provide, or explain why this request cannot be fulfilled.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, AdmofficerRAccess.class, CaseofficerCrudAccess.class}
    )
    private String remoteVideoCallTribunalResponse;

    @CCD(
            label = "\r\n"
                            + "## Adjustment request",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String multimediaAdjustmentRequestTitle;

    @CCD(
            label = "\r\n"
                            + "## Tribunal response",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String multimediaTribunalResponseTitle;

    @CCD(
            label = "Multimedia decision",
            searchable = false,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerLegalrepSolicitorRAccess.class, IacjudgeCrudAccess.class}
    )
    private IsMultimediaAllowed isMultimediaAllowed;

    @CCD(
            label = "Multimedia equipment",
            hint = "State what adjustments the Tribunal will provide, or explain why this request cannot be fulfilled.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, IacjudgeCrudAccess.class}
    )
    private String multimediaTribunalResponse;

    @CCD(
            label = "\r\n"
                            + "## Adjustment request",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String singleSexCourtAdjustmentRequestTitle;

    @CCD(
            label = "\r\n"
                            + "## Tribunal response",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String singleSexCourtTribunalResponseTitle;

    @CCD(
            label = "Single-sex court decision",
            searchable = false,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerLegalrepSolicitorRAccess.class, IacjudgeCrudAccess.class}
    )
    private IsSingleSexCourtAllowed isSingleSexCourtAllowed;

    @CCD(
            label = "Single-sex court",
            hint = "State the gender requested and whether the Tribunal can accommodate this. If not, explain why this request cannot be fulfilled.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, IacjudgeCrudAccess.class}
    )
    private String singleSexCourtTribunalResponse;

    @CCD(
            label = "\r\n"
                            + "## Adjustment request",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String inCameraCourtAdjustmentRequestTitle;

    @CCD(
            label = "\r\n"
                            + "## Tribunal response",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String inCameraCourtTribunalResponseTitle;

    @CCD(
            label = "In camera court decision",
            searchable = false,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerLegalrepSolicitorRAccess.class, IacjudgeCrudAccess.class}
    )
    private IsInCameraCourtAllowed isInCameraCourtAllowed;

    @CCD(
            label = "In camera court",
            hint = "State whether the Tribunal can accommodate this, or explain why this request cannot be fulfilled.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, IacjudgeCrudAccess.class}
    )
    private String inCameraCourtTribunalResponse;

    @CCD(
            label = "\r\n"
                            + "## Adjustment request",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String additionalAdjustmentRequestTitle;

    @CCD(
            label = "\r\n"
                            + "## Tribunal response",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String additionalTribunalResponseTitle;

    @CCD(
            label = "Other adjustments decision",
            searchable = false,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerLegalrepSolicitorRAccess.class, IacjudgeCrudAccess.class}
    )
    private IsAdditionalAdjustmentsAllowed isAdditionalAdjustmentsAllowed;

    @CCD(
            label = "Other adjustments",
            hint = "State what adjustments the Tribunal will provide, or explain why their request's cannot be fulfilled.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, IacjudgeCrudAccess.class}
    )
    private String additionalTribunalResponse;

    @CCD(
            label = "Are there any additional instructions for the hearing?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, IacjudgeCrudAccess.class}
    )
    private YesOrNo isAdditionalInstructionAllowed;

    @CCD(
            label = "Additional Instructions",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, IacjudgeCrudAccess.class}
    )
    private String additionalInstructionsTribunalResponse;

    @CCD(
            label = "\r\n"
                            + "## Adjustment request",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String datesToAvoidRequestTitle;

    @CCD(
            label = "\r\n"
                            + "## Tribunal response",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String datesToAvoidTribunalResponseTitle;

    @CCD(
            label = "Dates to avoid",
            hint = "State whether the Tribunal can accommodate this, or explain why this request cannot be fulfilled.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String datesToAvoidTribunalResponse;

    @CCD(
            label = "Dates to avoid",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, IacjudgeCrudAccess.class}
    )
    private String datesToAvoidReadonly;

    @CCD(
            label = "![]()\r\n"
                            + "## Agreed additional adjustments",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private String agreedAdditionalAdjustmentsTitle;

    @CCD(
            label = "Direction to be updated",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String directionList;

    @CCD(
            label = "Explanation",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String directionEditExplanation;

    @CCD(
            label = "Parties",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "parties",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private PartiesList directionEditParties;

    @CCD(
            label = "Date due",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private LocalDate directionEditDateDue;

    @CCD(
            label = "Date sent",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private LocalDate directionEditDateSent;

    @CCD(
            label = "Appeal reasons",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CitizenSystemCrudAccess.class}
    )
    private String reasonsForAppealDecision;

    @CCD(
            label = "Appeal reasons supporting evidence",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CitizenSystemCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> reasonsForAppealDocuments;

    @JsonProperty("journey_type_migrated-lrm-detained")
    @CCD(
            label = "![The case migrated appellant in person and detained ](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/migrated-lrm-detained.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class}
    )
    private String journey_type_migrated_lrm_detained;

    @JsonProperty("journey_type_migrated-aipm-detained")
    @CCD(
            label = "![The case migrated appellant in person and detained ](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/migrated-aipm-detained.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class}
    )
    private String journey_type_migrated_aipm_detained;

    @JsonProperty("journey_type_migrated-lrm-detained-s94b")
    @CCD(
            label = "![The case migrated appellant in person and detained ](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/migrated-lrm-detained-s9.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class}
    )
    private String journey_type_migrated_lrm_detained_s94b;

    @JsonProperty("journey_type_migrated-aipm-detained-s94b")
    @CCD(
            label = "![The case migrated appellant in person and detained ](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/migrated-aipm-detained-s9.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class}
    )
    private String journey_type_migrated_aipm_detained_s94b;

    @JsonProperty("journey_type_migrated_fee-exempt_lrm_detained")
    @CCD(
            label = "![The case migrated appellant in person and detained ](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/migrated-feeexempt-lrm-detained.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class}
    )
    private String journey_type_migrated_fee_exempt_lrm_detained;

    @JsonProperty("journey_type_migrated_fee-exempt_aipm_detained")
    @CCD(
            label = "![The case migrated appellant in person and detained ](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/migrated-feeexempt-aipm-detained.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class}
    )
    private String journey_type_migrated_fee_exempt_aipm_detained;

    @JsonProperty("journey_type_migrated_fee-exempt_lrm_detained_s94b")
    @CCD(
            label = "![The case migrated appellant in person and detained ](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/migrated-feeexempt-lrm-detained-s9.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class}
    )
    private String journey_type_migrated_fee_exempt_lrm_detained_s94b;

    @JsonProperty("journey_type_migrated_fee-exempt_aipm_detained_s94b")
    @CCD(
            label = "![The case migrated appellant in person and detained ](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/migrated-feeexempt-aipm-detained-s9.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class}
    )
    private String journey_type_migrated_fee_exempt_aipm_detained_s94b;

    @CCD(
            label = "#### Files should be:\r\n"
                            + "- files must be no more than 500Mb in size\r\n"
                            + "- you can upload mp3 files\r\n"
                            + "- before uploading a file, give it a meaningful file name. For example, health-report -JSmith.mp3\r\n"
                            + "\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String uploadHearingRecordingTitle;

    @CCD(
            label = "Previous recordings",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<HearingRecordingDocument>> previousHearingRecordingDocuments;

    @CCD(
            label = "Select the new hearing centre.\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String changeHearingCentreTitle;

    @CCD(
            label = "Reasons for editing or removing the documents",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String editDocumentsReason;

    @CCD(
            label = "FTPA Appellant Decision and Reasons documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> allFtpaAppellantDecisionDocs;

    @CCD(
            label = "Internal flag managed by API for ftpa appellant decided by leadership judge",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaAppellantDecided;

    @CCD(
            label = "FTPA Home Office Decision and Reasons documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> allFtpaRespondentDecisionDocs;

    @CCD(
            label = "Internal flag managed by API for ftpa respondent decided by leadership judge",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaRespondentDecided;

    @CCD(
            label = "Internal flag managed by API for leadership judge appellant ftpa decision visible to all",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo isAppellantFtpaDecisionVisibleToAll;

    @CCD(
            label = "Internal flag managed by API for leadership judge respondent ftpa decision visible to all",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo isRespondentFtpaDecisionVisibleToAll;

    @CCD(
            label = "Internal flag managed by API for ftpa appellant docs visible in ftpa submitted state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isFtpaAppellantDocsVisibleInSubmitted;

    @CCD(
            label = "Internal flag managed by API for ftpa appellant grounds docs visible in ftpa submitted state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isFtpaAppellantGroundsDocsVisibleInSubmitted;

    @CCD(
            label = "Internal flag managed by API for ftpa appellant evidence docs visible in ftpa submitted state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isFtpaAppellantEvidenceDocsVisibleInSubmitted;

    @CCD(
            label = "Internal flag managed by API for ftpa appellant out of time docs visible in ftpa submitted state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isFtpaAppellantOotDocsVisibleInSubmitted;

    @CCD(
            label = "Internal flag managed by API for ftpa appellant out of time explanation visible in ftpa submitted state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isFtpaAppellantOotExplanationVisibleInSubmitted;

    @CCD(
            label = "Internal flag managed by API for ftpa appellant docs visible in ftpa decided state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo isFtpaAppellantDocsVisibleInDecided;

    @CCD(
            label = "Internal flag managed by API for ftpa appellant grounds docs visible in ftpa decided state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isFtpaAppellantGroundsDocsVisibleInDecided;

    @CCD(
            label = "Internal flag managed by API for ftpa appellant evidence docs visible in ftpa decided state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isFtpaAppellantEvidenceDocsVisibleInDecided;

    @CCD(
            label = "Internal flag managed by API for ftpa appellant out of time docs visible in ftpa decided state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isFtpaAppellantOotDocsVisibleInDecided;

    @CCD(
            label = "Internal flag managed by API for ftpa appellant out of time explanation visible in ftpa decided state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isFtpaAppellantOotExplanationVisibleInDecided;

    @CCD(
            label = "Internal flag managed by API for ftpa respondent docs visible in ftpa submitted state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaRespondentDocsVisibleInSubmitted;

    @CCD(
            label = "Internal flag managed by API for ftpa respondent grounds docs visible in ftpa submitted state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaRespondentGroundsDocsVisibleInSubmitted;

    @CCD(
            label = "Internal flag managed by API for ftpa respondent evidence docs visible in ftpa submitted state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaRespondentEvidenceDocsVisibleInSubmitted;

    @CCD(
            label = "Internal flag managed by API for ftpa respondent out of time docs visible in ftpa submitted state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaRespondentOotDocsVisibleInSubmitted;

    @CCD(
            label = "Internal flag managed by API for ftpa respondent out of time explanation visible in ftpa submitted state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaRespondentOotExplanationVisibleInSubmitted;

    @CCD(
            label = "Internal flag managed by API for ftpa respondent docs visible in ftpa decided state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaRespondentDocsVisibleInDecided;

    @CCD(
            label = "Internal flag managed by API for ftpa respondent grounds docs visible in ftpa decided state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaRespondentGroundsDocsVisibleInDecided;

    @CCD(
            label = "Internal flag managed by API for ftpa respondent evidence docs visible in ftpa decided state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaRespondentEvidenceDocsVisibleInDecided;

    @CCD(
            label = "Internal flag managed by API for ftpa respondent out of time docs visible in ftpa decided state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaRespondentOotDocsVisibleInDecided;

    @CCD(
            label = "Internal flag managed by API for ftpa respondent out of time explanation visible in ftpa decided state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class}
    )
    private YesOrNo isFtpaRespondentOotExplanationVisibleInDecided;

    @CCD(
            label = "## Tribunal notes\r\n"
                            + "These notes are only visible to the Tribunal.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String tribunalNotesTitle;

    @CCD(
            label = "Only record this after the Judge has been notified.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, SystemCrudAccess.class}
    )
    private String allocatedJudgeTitle;

    @CCD(
            label = "Judge assigned to the application",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class}
    )
    private String allocatedJudge;

    @CCD(
            label = "Judge assigned to the application",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String allocatedJudgeEdit;

    @CCD(
            label = "Previous judges",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class}
    )
    private List<ListValue<String>> previousJudgeAllocations;

    @CCD(
            label = "Internal flag managed by API for leadership judge appellant ftpa decision visible to respondent",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo isAppellantFtpaDecisionVisibleToRespondent;

    @CCD(
            label = "Internal flag managed by API for leadership judge respondent ftpa decision visible to appellant",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo isRespondentFtpaDecisionVisibleToAppellant;

    @CCD(
            label = "Time extensions",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenCrudAccess.class}
    )
    private List<ListValue<TimeExtension>> timeExtensions;

    @CCD(
            label = "review time extension required",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenCrudAccess.class}
    )
    private YesOrNo reviewTimeExtensionRequired;

    @CCD(
            label = "Date application was made",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private LocalDate reviewTimeExtensionDate;

    @CCD(
            label = "Party",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "parties",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private PartiesList reviewTimeExtensionParty;

    @CCD(
            label = "Why do you need more time?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private String reviewTimeExtensionReason;

    @CCD(
            label = "Decision",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private TimeExtensionDecision reviewTimeExtensionDecision;

    @CCD(
            label = "Reason for decision",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private String reviewTimeExtensionDecisionReason;

    @CCD(
            label = "Due Date",
            hint = "The new direction due date must be at least one day after the original direction due date",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private LocalDate reviewTimeExtensionDueDate;

    @CCD(
            label = "**Before this decision, did you send a Notice of Intention to Set Aside to both parties?**",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String isFtpaAppellantNoticeOfDecisionSetAsideTitle;

    @CCD(
            label = "Notice of Intention to Set Aside sent?",
            hint = "Before this decision, did you send a Notice of Intention to Set Aside to both parties?\r\n"
                            + "\r\n"
                            + "(If you are not a Resident Judge, select No.)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, AdmofficerCaseofficerRAccess.class}
    )
    private YesOrNo isFtpaAppellantNoticeOfDecisionSetAside;

    @CCD(
            label = "**Before this decision, did you send a Notice of Intention to Set Aside to both parties?**",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String isFtpaRespondentNoticeOfDecisionSetAsideTitle;

    @CCD(
            label = "Notice of Intention to Set Aside sent?",
            hint = "Before this decision, did you send a Notice of Intention to Set Aside to both parties?\r\n"
                            + "\r\n"
                            + "(If you are not a Resident Judge, select No.)",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, AdmofficerCaseofficerRAccess.class}
    )
    private YesOrNo isFtpaRespondentNoticeOfDecisionSetAside;

    @CCD(label = "Draft Clarifying Questions and Answers", searchable = false, access = {CitizenSystemCrudAccess.class})
    private List<ListValue<ClarifyingQuestionAnswer>> draftClarifyingQuestionsAnswers;

    @CCD(
            label = "Is the appellant answering clarifying questions?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private YesOrNo isAnsweringClarifyingQuestions;

    @CCD(
            label = "Clarifying Questions and Answers",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CitizenSystemCrudAccess.class}
    )
    private List<ListValue<ClarifyingQuestionAnswer>> clarifyingQuestionsAnswers;

    @CCD(
            label = "Questions",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<ClarifyingQuestion>> sendDirectionQuestions;

    @CCD(
            label = "![]()\n"
                            + "## Clarifying Questions",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String clarifyingQuestionsTitle;

    @CCD(
            label = "![]()\n"
                            + "## Direct the appellant to answer clarifying questions",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String sendDirectionWithQuestionsTitle;

    @CCD(
            label = "Internal field to store the current state",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String preClarifyingState;

    @CCD(
            label = "Appellant is under the age of 18",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, CaseofficerSystemCrudAccess.class}
    )
    private YesOrNo isAppellantMinor;

    @CCD(
            label = "This flag will only be visible to the Tribunal.\r\n"
                            + "\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String flagCaseDescription;

    @CCD(
            label = "Type of flag",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "legacyCaseFlags",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private LegacyCaseFlags flagCaseTypeOfFlag;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String flagCaseAdditionalInformation;

    @CCD(
            label = "## Flags\r\n"
                            + "\r\n"
                            + "These flags are only visible to the Tribunal.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagsTitle;

    @CCD(
            label = "Directions",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<LegacyCaseFlag>> legacyCaseFlags;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label, access = {SystemCrudAccess.class})
    private String caseFlagsTabTitle;

    @CCD(label = " ", access = {CaseflagsProfileReadonlyProfileCruAccess.class, SystemCrudAccess.class})
    private Flags caseFlags;

    @CCD(
            label = " ",
            access = {SystemCrudAccess.class, CaseflagsProfileCrudAccess.class, CaseflagsReadonlyProfileCruAccess.class}
    )
    private List<ListValue<Flags>> interpreterLevelFlags;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FlagLauncher,
            access = {CaseflagsProfileReadonlyProfileCruAccess.class, SystemCrudAccess.class}
    )
    private String flagLauncher;

    @CCD(
            label = "Internal flag managed by API for the flags display in case",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo caseFlagAnonymityExists;

    @CCD(
            label = "Internal flag managed by API for the flags display in case",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo caseFlagComplexCaseExists;

    @CCD(
            label = "Internal flag managed by API for the flags display in case",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo caseFlagDeportExists;

    @CCD(
            label = "Internal flag managed by API for the flags display in case",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo caseFlagDetainedImmigrationAppealExists;

    @CCD(
            label = "Internal flag managed by API for the flags display in case",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo caseFlagForeignNationalOffenderExists;

    @CCD(
            label = "Internal flag managed by API for the flags display in case",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo caseFlagPotentiallyViolentPersonExists;

    @CCD(
            label = "Internal flag managed by API for the flags display in case",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo caseFlagUnacceptableCustomerBehaviourExists;

    @CCD(
            label = "Internal flag managed by API for the flags display in case",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo caseFlagUnaccompaniedMinorExists;

    @CCD(
            label = "Internal flag managed by API for the flags display in case",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class, IacjudgeRAccess.class}
    )
    private YesOrNo caseFlagSetAsideReheardExists;

    @CCD(
            label = "Internal flag managed by API for the flags display in case",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class}
    )
    private YesOrNo caseFlagS94bOutOfCountryExists;

    @CCD(
            label = "Internal flag managed by API for the flags display in case",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo caseFlagAppealOnHoldExists;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String caseFlagAnonymityAdditionalInformation;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String caseFlagComplexCaseAdditionalInformation;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String caseFlagDeportAdditionalInformation;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String caseFlagDetainedImmigrationAppealAdditionalInformation;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String caseFlagForeignNationalOffenderAdditionalInformation;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String caseFlagPotentiallyViolentPersonAdditionalInformation;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String caseFlagUnacceptableCustomerBehaviourAdditionalInformation;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String caseFlagUnaccompaniedMinorAdditionalInformation;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String caseFlagSetAsideReheardAdditionalInformation;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class}
    )
    private String caseFlagS94bOutOfCountryAdditionalInformation;

    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String caseFlagAppealOnHoldAdditionalInformation;

    @CCD(
            label = "![This appellant in this case has an anonymity order](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/caseFlagAnonymity.png)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagAnonymityImage;

    @CCD(
            label = "![This appeal has been marked as complex case](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/caseFlagComplexCase.png)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagComplexCaseImage;

    @CCD(
            label = "![This appellant in this case has a deportation order](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/caseFlagDeport.png)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagDeportImage;

    @CCD(
            label = "![The appellant in this case is in detention or prison](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/caseFlagDetainedImmigrationAppeal.png)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagDetainedImmigrationAppealImage;

    @CCD(
            label = "![The appellant in this case is a foreign national offender](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/caseFlagForeignNationalOffender.png)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagForeignNationalOffenderImage;

    @CCD(
            label = "![The appellant in this case is a potentially violent person](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/caseFlagPotentiallyViolentPerson.png)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagPotentiallyViolentPersonImage;

    @CCD(
            label = "![The appellant in this case has displayed unacceptable customer behaviour](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/caseFlagUnacceptableCustomerBehaviour.png)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagUnacceptableCustomerBehaviourImage;

    @CCD(
            label = "![The appellant in this case is an unaccompanied minor](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/caseFlagUnaccompaniedMinor.png)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagUnaccompaniedMinorImage;

    @CCD(
            label = "![The decision has been set aside and this case will be reheard](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/caseFlagSetAsideReheard.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagSetAsideReheardImage;

    @CCD(
            label = "![The decision has been set aside and this case will be reheard](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/caseFlagS94BOutofCountry.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerCaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagS94BOutOfCountryImage;

    @CCD(
            label = "![This appellant case is on hold](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/caseFlagAppealOnHold.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String caseFlagAppealOnHoldImage;

    @CCD(
            label = "Select a flag to remove from the case\r\n"
                            + "\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String removeFlagDescription;

    @CCD(
            label = "Type of flag",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String removeFlagTypeOfFlag;

    @CCD(
            label = "![]()\n"
                            + "## Reason for appeal",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private String reasonsForAppealTitle;

    @CCD(
            label = "Date uploaded",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CitizenSystemCrudAccess.class}
    )
    private LocalDate reasonsForAppealDateUploaded;

    @CCD(label = "Time Extension Reason", searchable = false, access = {CitizenSystemCrudAccess.class})
    private String submitTimeExtensionReason;

    @CCD(label = "Time Extension Evidence", searchable = false, access = {CitizenSystemCrudAccess.class})
    private List<ListValue<Document>> submitTimeExtensionEvidence;

    @CCD(
            label = "Direct the appellant to submit requirements for a case management appointment",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String requestCmaRequirementsTitle;

    @CCD(
            label = "Reasons for appointment",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String requestCmaRequirementsReasons;

    @CCD(label = "Bundle Configuration", searchable = false, access = {SystemCrudAccess.class})
    private String bundleConfiguration;

    @CCD(
            label = "Case bundles",
            hint = "Bundle to hold documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<Bundle>> caseBundles;

    @CCD(
            label = "Review the appellant's case management appointment requirements and select length of appointment.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String reviewCmaRequirementsTitle;

    @CCD(
            label = "\n"
                            + "## Adjustment request",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String pastExperiencesAdjustmentRequestTitle;

    @CCD(
            label = "\r\n"
                            + "## Tribunal response",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String pastExperiencesResponseTitle;

    @CCD(
            label = "Past experiences",
            hint = "State what adjustments the Tribunal will provide, or explain why their request's cannot be fulfilled.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String pastExperiencesTribunalResponse;

    @CCD(
            label = "# Force case to case under review\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String forceCaseToCaseUnderReviewTitle;

    @CCD(
            label = "Reasons to force the case progression",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String reasonToForceCaseToCaseUnderReview;

    @CCD(
            label = "# Force case to submit hearing requirements\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String forceCaseToSubmitHearingRequirementsTitle;

    @CCD(
            label = "Reasons to force the case progression",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String reasonToForceCaseToSubmitHearingRequirements;

    @CCD(label = "Company", searchable = false, access = {LegalrepSolicitorCruAccess.class, SystemCrudAccess.class})
    private String updateLegalRepCompany;

    @CCD(label = "Given names", searchable = false, access = {LegalrepSolicitorSystemCrudAccess.class})
    private String updateLegalRepName;

    @CCD(label = "Family name", searchable = false, access = {LegalrepSolicitorSystemCrudAccess.class})
    private String updateLegalRepFamilyName;

    @CCD(
            label = "Email",
            hint = "This is for correspondence only. You cannot change your sign-in email.",
            regex = "^([a-zA-Z0-9\\_\\-\\.]+)@([a-zA-Z0-9\\_\\-\\.]+)\\.([a-zA-Z]+)$",
            searchable = false,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String updateLegalRepEmailAddress;

    @CCD(
            label = "Contact number",
            searchable = false,
            typeOverride = FieldType.PhoneUK,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String updateLegalRepMobilePhoneNumber;

    @CCD(label = "Own reference", searchable = false, access = {LegalrepSolicitorSystemCrudAccess.class})
    private String updateLegalRepReferenceNumber;

    @CCD(
            label = "The hearing will be adjourned and a new hearing date can be added in the future.\r\n"
                            + "Both parties will be notified and a Notice of Adjournment will be generated.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String adjournHearingWithoutDateReasonsTitle;

    @CCD(
            label = "Reasons for adjournment",
            hint = "This will be visible to both parties",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String adjournHearingWithoutDateReasons;

    @CCD(
            label = "The state before adjourn without a date event.",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String stateBeforeAdjournWithoutDate;

    @CCD(
            label = "The hearing date before adjourn without a date event.",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String dateBeforeAdjournWithoutDate;

    @CCD(
            label = "Internal flag managed by API used for display logic.",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo doesTheCaseNeedToBeRelisted;

    @CCD(
            label = "Hearing centre",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingVenue",
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenLegalrepSolicitorRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private HearingVenue listCmaHearingCentre;

    @CCD(
            label = "Length of appointment",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingLength",
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenLegalrepSolicitorRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private HearingLength listCmaHearingLength;

    @CCD(
            label = "Appointment date and time",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenLegalrepSolicitorRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private LocalDateTime listCmaHearingDate;

    @CCD(
            label = "![]()\n"
                            + "## Attendees",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String updateDetailsAfterCmaTitle;

    @CCD(
            label = "![]()\n"
                            + "## Duration",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String updateDetailsAfterCmaDurationTitle;

    @CCD(
            label = "![]()\n"
                            + "## Appointment recordings",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String updateDetailsAfterCmaAppRecordingsTitle;

    @CCD(
            label = "![]()\n"
                            + "## Appointment summary",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String updateDetailsAfterCmAppSummaryTitle;

    @CCD(
            label = "Add the attendees and case duration below. If someone didn't attend. leave it blank.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String updateDetailsAfterCmaDesc;

    @CCD(
            label = "The Tribunal Caseworker",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenWaTaskConfigurationCrudAccess.class}
    )
    private String attendingTCW;

    @CCD(
            label = "Summary",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private Document appointmentSummary;

    @CCD(
            label = "Appointment details",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String appointmentDetailsTitle;

    @CCD(
            label = "Add the hearing details below.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String listCmaHearingTitle;

    @CCD(
            label = "## Do this next\r\n"
                            + "The application decision has been recorded and is available in the applications tab.\r\n"
                            + "\r\n"
                            + "You must now [edit the appeal details](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/editAppealAfterSubmit) based on the new information provided in the application.\r\n"
                            + "\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String doThisNext_caseOfficer_editAppealAfterSubmit;

    @CCD(
            label = "Linked appeal",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class}
    )
    private ReasonForLinkAppealOptions reasonForLinkAppeal;

    @CCD(
            label = "| **Reason for link** | **${reasonForLinkAppeal}** |\r\n"
                            + "| --- | --- |",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String unlinkAppealReasonForLink;

    @CCD(
            label = "## Review the link you want to remove",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String unlinkAppeaTitle;

    @CCD(label = " ", searchable = false, access = {LegalrepSolicitorSystemCrudAccess.class})
    private String submitNotificationStatus;

    @CCD(
            label = "isFeePaymentEnabled",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenCrudAccess.class}
    )
    private String isFeePaymentEnabled;

    @CCD(
            label = "isReheardAppealEnabled",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String isReheardAppealEnabled;

    @CCD(
            label = "# Make a payment",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String makePayment;

    @CCD(
            label = "Enter your Payment by Account reference number",
            hint = "For example, PBA1234567",
            regex = "PBA[0-9]{7}",
            searchable = false,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String pbaNumber;

    @CCD(
            label = "### Can't see your Payment by Account number?",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String cantSeePaymentByAccount;

    @CCD(
            label = "<a href=\"https://www.gov.uk/guidance/hmcts-payment-by-account-for-online-services\" target=\"_blank\">Apply to use Payment by Account (Opens in new window)</a>",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String applyLinkToUsePba;

    @CCD(
            label = "Reasons to force the case progression",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String reasonToForceRequestCaseBuilding;

    @CCD(
            label = "# cover sheet page logo",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String hmcts;

    @CCD(
            label = "Additional evidence from appellant",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customAdditionalEvidenceDocuments;

    @CCD(
            label = "Hearing documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customHearingDocuments;

    @CCD(
            label = "Legal representative documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customLegalRepDocuments;

    @CCD(
            label = "Respondent documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customRespondentDocuments;

    @CCD(
            label = "  ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String uploadTheNoticeOfDecisionTitle;

    @CCD(
            label = " \r\n"
                            + "You must upload the Notice of Decision. If it is not possible, you must provide a valid reason.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String uploadTheNoticeOfDecisionTips;

    @CCD(
            label = "Notice of Decision",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> uploadTheNoticeOfDecisionDocs;

    @CCD(
            label = "Notice of Decision",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> uploadRehydratedNod;

    @CCD(
            label = "Notice of Decision",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private Document uploadTheNoticeOfDecisionDocument;

    @CCD(
            label = "Reason you cannot provide the Notice of Decision",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String uploadTheNoticeOfDecisionExplanation;

    @CCD(
            label = "Appeal Form",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> uploadTheAppealFormDocs;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String deportationOrderTitle;

    @CCD(
            label = "Has a deportation order been made against the appellant?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SystemAccessRAccess.class}
    )
    private YesOrNo deportationOrderOptions;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
    private String removalDirectionsTitle;

    @CCD(
            label = "Removal directions set",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo removalOrderOptions;

    @CCD(
            label = "Removal date and time",
            hint = "For example 16 4 2021, 14 30 00",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private LocalDateTime removalOrderDate;

    @CCD(
            label = "If you want to use PBA, Email MyHMCTSsupport@justice.gov.uk to ask for your PBA number to be registered with your MyHMCTS account. You will be able to select your PBA number from the list once this is done. It usually takes 3 days.\n"
                            + "\n"
                            + "If you need to submit the appeal urgently, you can [change the payment method to card](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/editPaymentMethod) and pay using that method after you've submitted your appeal.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorRAccess.class, SystemCrudAccess.class}
    )
    private String supportPaymentAccount;

    @CCD(
            label = "Email MyHMCTSsupport@justice.gov.uk to ask for your Payment by Account number to be registered with your MyHMCTS account. You will be able to see your Payment by Account number once the is done. It usually takes 3 days.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorRAccess.class, SystemCrudAccess.class}
    )
    private String pbaAdvice;

    @CCD(
            label = "If you need to submit the appeal urgently, you can [edit your appeal](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/editAppeal) and change the payment method.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorRAccess.class, SystemCrudAccess.class}
    )
    private String editYourAppeal;

    @CCD(
            label = "### Don't have Payment by Account?",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String dontHavePaymentByAccount;

    @CCD(label = " ", searchable = false, access = {LegalrepSolicitorSystemCrudAccess.class})
    private String howToPayLabel;

    @CCD(
            label = "Payment method",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerIacjudgeRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private PaAppealTypePaymentOptions paAppealTypePaymentOption;

    @CCD(
            label = "Payment method",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, CitizenSystemCrudAccess.class, AdmofficerRAccess.class}
    )
    private PaAppealTypeAipPaymentOptions paAppealTypeAipPaymentOption;

    @CCD(
            label = "Payment method",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, LegalrepSolicitorSystemCrudAccess.class, AdmofficerRAccess.class}
    )
    private EaHuAppealTypePaymentOptions eaHuAppealTypePaymentOption;

    @CCD(
            label = "## Attendees",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, SystemCrudAccess.class}
    )
    private String recordAttendeesTitle;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String validationTabTitle;

    @CCD(
            label = "## Home Office integration enabled flag",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String isHomeOfficeIntegrationEnabled;

    @CCD(
            label = "Review the appeal application form data and cross reference it with Home Office data. If the appeal looks valid, you must tell the respondent to supply their evidence.\r\n"
                            + "\r\n"
                            + "[Request respondent evidence](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/requestRespondentEvidence).\r\n"
                            + "\r\n"
                            + "The first column of information is sourced from the appeal application form, the second column of information is from Home Office records.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String validation_caseOfficer_appealSubmitted;

    @CCD(
            label = "${homeOfficeSearchStatusMessage}",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String validation_caseOfficer_requestHomeOfficeData;

    @CCD(
            label = "Contact preference description",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String contactPreferenceDescription;

    @CCD(
            label = "|${homeOfficeCaseStatusData.displayAppellantDetailsTitle}||| \r\n"
                            + "| ------------- |:------------- |:------------- |\r\n"
                            + "|Given name|${appellantGivenNames}|${homeOfficeCaseStatusData.person.givenName}| \r\n"
                            + "|Family name|${appellantFamilyName}|${homeOfficeCaseStatusData.person.familyName}| \r\n"
                            + "|Full name|${appellantGivenNames} ${appellantFamilyName}|${homeOfficeCaseStatusData.person.fullName}| \r\n"
                            + "|Gender||${homeOfficeCaseStatusData.person.gender.description}| \r\n"
                            + "|Date of birth|${appellantDateOfBirth}|${homeOfficeCaseStatusData.displayDateOfBirth}| \r\n"
                            + "|HO role||${homeOfficeCaseStatusData.applicationStatus.roleType.description}| \r\n"
                            + "|HO sub-role||${homeOfficeCaseStatusData.applicationStatus.roleSubType.description}| \r\n"
                            + "|Nationality|${appellantNationalitiesDescription}|${homeOfficeCaseStatusData.person.nationality.description}|\r\n"
                            + "|${homeOfficeCaseStatusData.displayApplicationDetailsTitle}|||\r\n"
                            + "|HO reference|${homeOfficeReferenceNumber}|${homeOfficeCaseStatusData.applicationStatus.documentReference} |\r\n"
                            + "|HO decision||${homeOfficeCaseStatusData.applicationStatus.decisionType.description}|\r\n"
                            + "|HO decision date|${homeOfficeDecisionDate}|${homeOfficeCaseStatusData.displayDecisionDate}|\r\n"
                            + "|HO decision sent|${homeOfficeDecisionDate}|${homeOfficeCaseStatusData.displayDecisionSentDate}|\r\n"
                            + "|HO decision communication|${contactPreferenceDescription}|${homeOfficeCaseStatusData.applicationStatus.decisionCommunication.type}|\r\n"
                            + "|HO application|${appealTypeDescription}|${homeOfficeCaseStatusData.applicationStatus.applicationType.description}|\r\n"
                            + "|HO claim reason|| ${homeOfficeCaseStatusData.applicationStatus.claimReasonType.description}|\r\n"
                            + "|HO rejection reason||${homeOfficeCaseStatusData.displayRejectionReasons}|\r\n"
                            + "|HO appealable||${homeOfficeCaseStatusData.displayMetadataValueBoolean}",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String validationAppealData;

    @CCD(
            label = "Staff Location",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerLegalrepSolicitorCruAccess.class, CitizenWaTaskConfigurationCruAccess.class, SystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String staffLocation;

    @CCD(
            label = "Staff Location Id",
            searchable = false,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerRuAccess.class, SystemCrudAccess.class, CitizenRuAccess.class, CaseofficerLegalrepSolicitorWaTaskConfigurationRuAccess.class}
    )
    private String staffLocationId;

    @CCD(
            label = "Uploaded sensitive documents appear in the documents tab and are only visible to HMCTS and judicial users.  \r\n"
                            + "No users are notified when these are added. These documents are not addded to the hearing bundle. ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String uploadSensitiveDocsHeaderDescription;

    @CCD(
            label = "Document supplied by",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private UploadSensitiveDocsIsAppellantRespondentOptions uploadSensitiveDocsIsAppellantRespondent;

    @CCD(
            label = "#### Advice on uploads\r\n"
                            + "- files must be no more than 100MB in size\r\n"
                            + "- You can upload jpg, png, svg, gif, doc and PDF files\r\n"
                            + "- before uploading a file, give it a meaningful file name. For example, application-evidence.pdf",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerRAccess.class, SystemCrudAccess.class}
    )
    private String uploadSensitiveDocsAdviceOnUploads;

    @CCD(
            label = "Sensitive document",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> uploadSensitiveDocsFileUploads;

    @CCD(
            label = "Sensitive documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> uploadSensitiveDocs;

    @CCD(
            label = "Include the date the payment was made, the amount paid and any other relevant information.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, CaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String markAppealPaidDesc;

    @CCD(
            label = "Payment date",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, LegalrepSolicitorRAccess.class, WaTaskConfigurationRAccess.class}
    )
    private LocalDate paidDate;

    @CCD(
            label = "Amount paid",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenRAccess.class}
    )
    private String paidAmount;

    @CCD(
            label = "Additional payment information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class}
    )
    private String additionalPaymentInfo;

    @CCD(
            label = "Case management location",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class, GsProfileRAccess.class}
    )
    private CaseManagementLocation caseManagementLocation;

    @CCD(
            label = "Case management location",
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class, AdmofficerRAccess.class}
    )
    private CaseManagementLocationRefData caseManagementLocationRefData;

    @CCD(
            label = "[Make an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/makeAnApplication)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String makeAnApplicationLink;

    @CCD(
            label = "Select the type of application you are making.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, LegalrepSolicitorRAccess.class, SystemCrudAccess.class}
    )
    private String makeAnApplicationAdvice;

    @CCD(
            label = "Type of application",
            typeOverride = FieldType.DynamicList,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, AdmofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private String makeAnApplicationTypes;

    @CCD(
            label = "This application is to change Decision with a hearing or Decision without a hearing. To request a change to the hearing method (eg CVP) please use 'Other' application type.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, LegalrepSolicitorRAccess.class}
    )
    private String makeAnApplicationExplanation;

    @CCD(
            label = "Provide any further details and evidence that may help your application.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, LegalrepSolicitorRAccess.class, SystemCrudAccess.class}
    )
    private String makeAnApplicationHelp;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, LegalrepSolicitorSystemCrudAccess.class}
    )
    private String makeAnApplicationType;

    @CCD(
            label = " ",
            searchable = false,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, AdmofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String makeAnApplicationDetailsLabel;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, AdmofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private String makeAnApplicationDetails;

    @CCD(
            label = "#### Upload any evidence that supports your application\r\n"
                            + "- files must be no more than 100MB in size\r\n"
                            + "- You can upload jpg, png, svg, gif, doc and PDF files\r\n"
                            + "- before uploading a file, give it a meaningful file name. For example, application-evidence.pdf",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, AdmofficerLegalrepSolicitorRAccess.class, SystemCrudAccess.class}
    )
    private String makeAnApplicationUploadEvidenceAdvice;

    @CCD(
            label = "Supporting evidence",
            searchable = false,
            access = {HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, AdmofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private List<ListValue<Document>> makeAnApplicationEvidence;

    @CCD(
            label = "Application",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class}
    )
    private List<ListValue<MakeAnApplication>> makeAnApplications;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private MakeAnApplication lastModifiedApplication;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo hasApplicationsToDecide;

    @CCD(
            label = "[Decide an application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/decideAnApplication)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String decideAnApplicationLink;

    @CCD(
            label = "Select the application that you are deciding.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String decideAnApplicationAdvice;

    @CCD(
            label = "Review the application and any evidence before recording the decision and the reasons for the decision.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String decideAnApplicationReviewAdvice;

    @CCD(
            label = "Application",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenIacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String makeAnApplicationsList;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String decideAnApplicationHeader;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenIacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String makeAnApplicationFieldsLabel;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenIacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String makeAnApplicationFields;

    @CCD(
            label = "Decision",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerCitizenIacjudgeWaTaskConfigurationCruAccess.class, SystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private MakeAnApplicationDecision makeAnApplicationDecision;

    @CCD(
            label = "Reasons for decision",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerCitizenIacjudgeWaTaskConfigurationCruAccess.class, SystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String makeAnApplicationDecisionReason;

    @CCD(
            label = "Internal flag managed by API of decide an application id to send notification",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, IacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String decideAnApplicationId;

    @CCD(
            label = "Internal flag managed by API to show/hide current hearing details",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo currentHearingDetailsVisible;

    @CCD(
            label = "![]()\r\n"
                            + "## Previous hearings",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private String previousHearingsTitle;

    @CCD(
            label = "Previous hearings",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<PreviousHearing>> previousHearings;

    @CCD(
            label = "Internal flag managed by API to show/hide previous hearing details",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo previousHearingDetailsVisible;

    @CCD(
            label = "Internal flag managed by API to show/hide current hearing length",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo listCaseHearingLengthVisible;

    @CCD(
            label = "Respondent Evidence after the hearing bundle",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> respondentAddendumEvidenceDocs;

    @CCD(
            label = "Appellant additional evidence following FTPA",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> appAdditionalEvidenceDocs;

    @CCD(
            label = "Respondent additional evidence following FTPA",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> respAdditionalEvidenceDocs;

    @CCD(
            label = "Appellant additional evidence following FTPA",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customAppAdditionalEvidenceDocs;

    @CCD(
            label = "Respondent additional evidence following FTPA",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customRespAdditionalEvidenceDocs;

    @CCD(
            label = "Appellant FTPA documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customFtpaAppellantDocs;

    @CCD(
            label = "Respondent FTPA documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customFtpaRespondentDocs;

    @CCD(
            label = "Decision and reasons documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customFinalDecisionAndReasonsDocs;

    @CCD(
            label = "Reheard hearing documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customReheardHearingDocs;

    @CCD(
            label = "Appellant Evidence after the hearing bundle",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customAppAddendumEvidenceDocs;

    @CCD(
            label = "Respondent Evidence after the hearing bundle",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customRespAddendumEvidenceDocs;

    @CCD(
            label = "Remittal documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customLatestRemittalDocs;

    @CCD(
            label = "Internal flag managed by case api for remissions feature",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerLegalrepSolicitorCruAccess.class, SystemCrudAccess.class, WaTaskConfigurationCruAccess.class}
    )
    private YesOrNo isRemissionsEnabled;

    @CCD(
            label = "How does the appellant want the appeal to be decided?",
            searchable = false,
            access = {LegalrepSolicitorCruSpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerWaTaskConfigurationCruAccess.class, IacjudgeRAccess.class, CitizenCrudAccess.class}
    )
    private RpDcAppealHearingOptions rpDcAppealHearingOption;

    @CCD(
            label = "### More about Help with Fees\r\n"
                            + "Your client can <a href=\"https://www.gov.uk/get-help-with-court-fees\" target=\"_blank\">apply for Help with Fees (opens in new tab).</a> They will be sent a reference number immediately after applying.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String helpWithFeesAdvice;

    @CCD(
            label = "### More about Exceptional Circumstances Remissions\r\n"
                            + "<a href=\"https://www.gov.uk/government/publications/immigration-and-appeals-tribunal-fees-t495\" target=\"_blank\">Read the Tribunal Fees guidance (opens in a new tab)</a> for more information on Exceptional Circumstances Remissions.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String exceptionalCircumstancesRemissionAdvice;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String asylumSupport;

    @CCD(
            label = "Enter the Asylum Support reference number. You can find this on the Asylum Support confirmation letter your client received from the Home Office.\n"
                            + "\n"
                            + "![HomeOffice_AsylumSupportLetter](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/homeOffice_asylum_support_letter.svg)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String asylumSupportAdvice;

    @CCD(
            label = "Asylum Support reference number",
            hint = "For example, 20/02/1234",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private String asylumSupportReference;

    @CCD(
            label = "Asylum Support confirmation letter",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private Document asylumSupportDocument;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String section17Header;

    @CCD(
            label = "You need to provide a copy of a letter from the local authority confirming the appellant's benefit or accommodation support.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String section17Advice;

    @CCD(
            label = "Provide a copy of a letter from the local authority confirming the appellant's benefits or accommodation support.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String section17AdminAdvice;

    @CCD(
            label = "Local authority letter",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private Document section17Document;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String section20Header;

    @CCD(
            label = "You need to provide a copy of a letter from the local authority confirming the appellant's benefit or accommodation support.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String section20Advice;

    @CCD(
            label = "Provide a copy of a letter from the local authority confirming the appellant's benefits or accommodation support.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String section20AdminAdvice;

    @CCD(
            label = "Local authority letter",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private Document section20Document;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String helpWithFeesHeader;

    @CCD(
            label = "If the appellant has included an EX160 Apply for help with fees form, give the form to the fees team to\n"
                            + "process and ask for the help with fees reference number.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String helpWithFeesAdminAdvice;

    @CCD(
            label = "Help with Fees reference number",
            hint = "For example, HWF-A1B-23C",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private String helpWithFeesReferenceNumber;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String exceptionalCircumstancesHeader;

    @CCD(
            label = "Explain the exceptional circumstances which justify remission of the fee",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private String exceptionalCircumstances;

    @CCD(
            label = "###  Provide supporting evidence\n"
                            + "\n"
                            + "You should provide evidence to support your explanation such as notices threatening legal action due to non-payment of bills or housing costs; details of income, savings or expenses; or any other relevant information.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String exceptionalCircumstancesSupportEvidenceAdvice;

    @CCD(
            label = "Amount remitted",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCruAccess.class, SystemCrudAccess.class}
    )
    private String amountRemitted;

    @CCD(
            label = "Amount left to pay",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCitizenCruAccess.class, SystemCrudAccess.class}
    )
    private String amountLeftToPay;

    @CCD(
            label = "Allocation type",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private AllocateTheCaseTo allocateTheCaseTo;

    @CCD(
            label = "Choose a location to find a caseworker\r\n"
                            + "\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String caseWorkerLocationTitle;

    @CCD(
            label = "Location",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "baseLocation",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private BaseLocation caseWorkerLocationList;

    @CCD(
            label = "Select a caseworker to allocate the case to\r\n"
                            + "\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAccess.class}
    )
    private String caseWorkerNameTitle;

    @CCD(
            label = "Caseworker",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String caseWorkerNameList;

    @CCD(
            label = "Caseworker",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String caseWorkerName;

    @CCD(
            label = "New fee amount",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerLegalrepSolicitorCruAccess.class, SystemCrudAccess.class, CitizenRAccess.class}
    )
    private String newFeeAmount;

    @CCD(
            label = "Internal flag managed by case api for displaying fee update status checkboxes",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerCruAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo displayFeeUpdateStatus;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.CaseHistoryViewer,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String caseHistoryViewer;

    @CCD(
            label = "Does your client have a correspondence address outside the UK?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenCrudAccess.class}
    )
    private YesOrNo hasCorrespondenceAddress;

    @CCD(
            label = "Does the appellant have a sponsor?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenCrudAccess.class}
    )
    private YesOrNo hasSponsor;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String entryClearanceDecisionTitle;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String entryClearanceDecisionLetterTitle;

    @CCD(
            label = "You can find the Global Web Form reference and the date the letter was sent on the appellant's Home Office decision letter.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String gwfReferenceNumberTitle;

    @CCD(
            label = "Home Office UAN or GWF reference",
            hint = "Enter the 16-digit UAN reference (four groups of four digits) or the 9-digit GWF reference (including the GWF prefix) in the decision letter",
            regex = "([0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}|GWF[0-9]{9}|.*)",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenCrudAccess.class}
    )
    private String gwfReferenceNumber;

    @CCD(
            label = "Advice for entering the correct number:\n"
                            + "\n"
                            + "- You should enter the reference number exactly as it appears on the decision letter.  This can often be found in the 'How to appeal' section.\n"
                            + "- Your UAN reference will be a 16-digit number, for example 1234-1234-1234-1234; you should include all the numbers with the dashes as they appear\n"
                            + "- Your GWF reference will be a 9-digit number starting with GWF, for example GWF123456789",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String gwfReferenceNumberTips;

    @CCD(
            label = "Date of entry clearance decision",
            hint = "Format DD MM YYYY (for example, 01 12 2024 for 1st December 2024).",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class}
    )
    private LocalDate dateEntryClearanceDecision;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String departureDateTitle;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label, access = {AdmofficerSystemCrudAccess.class})
    private String departureDateTitleAdminJ;

    @CCD(
            label = "Departure date",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenCrudAccess.class}
    )
    private LocalDate dateClientLeaveUk;

    @CCD(
            label = "Departure date",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private LocalDate dateClientLeaveUkAdminJ;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, CaseofficerSystemCrudAccess.class}
    )
    private String outOfCountryDecisionTypeTitle;

    @CCD(
            label = "Out of country decision",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class}
    )
    private OutOfCountryDecisionType outOfCountryDecisionType;

    @CCD(
            label = " \n"
                            + "\n"
                            + "Please provide an email address or phone number for your client's sponsor, or a person responsible for the sponsor.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String adminJSponsorContactTitle;

    @CCD(
            label = "Contact details",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenCrudAccess.class}
    )
    private ContactPreference adminJSponsorContactPreference;

    @CCD(
            label = "Sponsor email",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {CitizenSystemCrudAccess.class, SpecificAccessAdminProfileRAccess.class, SpecificAccessJudgeProfileRAccess.class}
    )
    private String aipSponsorEmailForDisplay;

    @CCD(
            label = "Sponsor phone number",
            regex = "^((\\+44(\\s\\(0\\)\\s|\\s0\\s|\\s)?)|0)7\\d{3}(\\s)?\\d{6}$",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CitizenSystemCrudAccess.class}
    )
    private String aipSponsorMobileNumberForDisplay;

    @CCD(
            label = "Organisation policy",
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerLegalrepSolicitorSystemCrudAdmofficerRAccess.class, ApproverCaaCrudAccess.class}
    )
    private OrganisationPolicy localAuthorityPolicy;

    @CCD(
            label = "Change Organisation Request",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, ApproverCaaCrudAccess.class, IacjudgeRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private ChangeOrganisationRequest changeOrganisationRequestField;

    @CCD(
            label = "#### Once you've submitted this request\r\n"
                            + "- You will no longer have access to this case\r\n"
                            + "- If the case has been shared with any colleagues, they will also lose access\r\n"
                            + "- Linked appeals are not affected. If you want to stop representing a client on a linked appeal, you need to access the case and repeat this action.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorRAccess.class, SystemCrudAccess.class}
    )
    private String removeRepresentationDescription;

    @CCD(
            label = "#### Once you've submitted this request\r\n"
                            + "- The legal representative will no longer have access to this case\r\n"
                            + "- If the case has been shared with other legal representatives, they will also lose access\r\n"
                            + "- If there are any cases linked to this appeal, they will not be affected",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSpecificAccessCtscProfileRAccess.class, CaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String removeLegalRepresentativeDescription;

    @CCD(
            label = "Choose one of the following statements",
            access = {AdmofficerLegalrepSolicitorCruAccess.class, SystemCrudAccess.class}
    )
    private LateRemissionTypes lateRemissionType;

    @CCD(
            label = "Previous remissions",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerLegalrepSolicitorCruAccess.class, IacjudgeCruAccess.class, SystemCrudAccess.class}
    )
    private List<ListValue<RemissionDetails>> previousRemissionDetails;

    @CCD(
            label = "Previous remissions",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SpecificAccessAdminCtscJudgeLegalOpsProfileCrudAccess.class}
    )
    private List<ListValue<RemissionDetails>> tempPreviousRemissionDetails;

    @CCD(
            label = "Previous representation",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, LegalrepSolicitorSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private List<ListValue<PreviousRepresentation>> previousRepresentations;

    @CCD(
            label = "Case type tab place holder for physicalOrMentalHealthIssues",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo physicalOrMentalHealthIssuesFlag;

    @CCD(
            label = "Case type tab place holder for paAppealTypePaymentOptionFlag",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String paAppealTypePaymentOptionFlag;

    @CCD(
            label = "Case type tab place holder for eaHuAppealTypePaymentOptionFlag",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String eaHuAppealTypePaymentOptionFlag;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerLegalrepSolicitorRAccess.class, CaseofficerSpecificAccessCtscProfileRAccess.class, SystemCrudAccess.class}
    )
    private String dummyEmptyLabel;

    @CCD(
            label = "Decision maker",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemCrudAccess.class}
    )
    private String outOfTimeDecisionMaker;

    @CCD(
            label = "Internal flag managed by API for record out of time decision",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, LegalrepSolicitorSystemCrudAccess.class, CaseofficerCitizenCrudAccess.class}
    )
    private YesOrNo recordedOutOfTimeDecision;

    @CCD(
            label = "Decision",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class, CitizenCruAccess.class}
    )
    private OutOfTimeDecisionType outOfTimeDecisionType;

    @CCD(
            label = "Upload decision notice",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private Document outOfTimeDecisionDocument;

    @CCD(
            label = "![]()\r\n"
                            + "## Out of time decision details",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String outOfTimeDecisionDetailsTitle;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<Document>> outOfTimeDecisionDocuments;

    @CCD(
            label = "Previous out of time decisions",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private List<ListValue<OutOfTimeDecisionDetails>> previousOutOfTimeDecisionDetails;

    @CCD(
            label = "![Important message: Do this now. Select Update legal representative details from the Next step dropdown and enter your details. You will not receive important notifications about the appeal until you do this.](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/legalRep_doThisNow_updateDetails.svg)\r\n"
                            + "\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String doThisNowUpdateLegalRepDetails;

    @CCD(
            label = "Case management category",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class, GsProfileRAccess.class}
    )
    private String caseManagementCategory;

    @CCD(
            label = "The Home Office data has returned more than one appellant for this appeal. Review the details submitted to the Tribunal by the appellant and select the matching appellant from the list.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileRAccess.class, SystemCrudAccess.class}
    )
    private String hoAppellantDetailsFoundAdvice;

    @CCD(
            label = "Review the details submitted to the Tribunal by the appellant and select the matching appellant from the list.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSpecificAccessCtscProfileRAccess.class, SystemCrudAccess.class}
    )
    private String hoAppellantDetailsNotFoundAdvice;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String hoAppellantDetailsAdviceFlag;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String matchingAppellantDetailsFound;

    @CCD(
            label = "## Make a selection",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String makeASelection;

    @CCD(
            label = "## Appeal validation\r\n"
                            + "There are no matching details for this appellant. You can contact the Home Office if you need more information to validate the appeal.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String validation_caseOfficer_noMatch;

    @CCD(
            label = "Do not display - internal use only",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo hasBeenValidatedByNewHomeOfficeApi;

    @CCD(
            label = "Upper Tribunal documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> upperTribunalDocuments;

    @CCD(
            label = "Internal flag managed by API to identify if upper tribunal bundling stitching is complete",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String stitchingStatusUpperTribunal;

    @CCD(
            label = "Internal flag managed by API to store if second ftpa decision exists",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private YesOrNo secondFtpaDecisionExists;

    @CCD(label = " ", searchable = false, access = {LegalrepSolicitorSystemCrudAccess.class})
    private String hasPbaAccountsError;

    @CCD(label = " ", searchable = false, access = {LegalrepSolicitorSystemCrudAccess.class})
    private YesOrNo hasPbaAccounts;

    @CCD(
            label = "Case Name",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, LegalrepSolicitorSystemCrudAccess.class, CaseofficerSpecificAccessCtscProfileRAccess.class}
    )
    private String caseName;

    @CCD(label = "PCQ Id", access = {CitizenSystemCrudAccess.class})
    private String pcqId;

    @CCD(
            label = "WA Dummy Postcode",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerLegalrepSolicitorCruAccess.class, CitizenWaTaskConfigurationCruAccess.class, SystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String waDummyPostcode;

    @CCD(
            label = "Internal flag managed by API for logging AIP Appeal reference and CCD Id",
            searchable = false,
            access = {CitizenSystemCrudAccess.class}
    )
    private YesOrNo isAppealReferenceNumberAvailable;

    @CCD(
            label = "HMCTS Case Category",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String hmctsCaseCategory;

    @CCD(
            label = "Case Name",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CaselinksProfileRAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String hmctsCaseNameInternal;

    @CCD(
            label = " ",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, GsProfileRAccess.class}
    )
    private String caseNameHmctsInternal;

    @JsonProperty("SearchCriteria")
    @CCD(
            label = " ",
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, GsProfileRAccess.class, CitizenCrudAccess.class}
    )
    private SearchCriteria searchCriteria;

    @CCD(
            label = " ",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<Parties>> searchParties;

    @CCD(
            label = "Were you outside the UK when you made your application?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CitizenSystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private YesOrNo outsideUkWhenApplicationMade;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CitizenSystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private YesOrNo outsideUkWhenApplicationMadePreviousSelection;

    @CCD(
            label = " ",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo hasServiceRequestAlready;

    @CCD(label = " ", searchable = false, access = {LegalrepSolicitorRAccess.class, SystemCrudAccess.class})
    private YesOrNo isServiceRequestTabVisibleConsideringRemissions;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private YesOrNo displayMarkAsPaidEventForPartialRemission;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private YesOrNo requestFeeRemissionFlagForServiceRequest;

    @CCD(label = " ", access = {CaselinksProfileSystemCrudAccess.class})
    private String caseLinksFlag;

    @CCD(label = " ", searchable = false, access = {CaselinksProfileSystemCrudAccess.class})
    private String maintainCaseLinksFlag;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {CaselinksProfileSystemCrudAccess.class}
    )
    private String caseLinksTabTitle;

    @JsonProperty("LinkedCasesComponentLauncher")
    @CCD(
            label = "Component Launcher (for displaying Linked Cases data)",
            searchable = false,
            typeOverride = FieldType.ComponentLauncher,
            access = {CaselinksProfileSystemCrudAccess.class}
    )
    private String linkedCasesComponentLauncher;

    @CCD(label = "Linked Cases", searchable = false, access = {CaselinksProfileSystemCrudAccess.class})
    private List<ListValue<CaseLink>> caseLinks;

    @CCD(
            label = " ",
            access = {AdmofficerCaseofficerLegalrepSolicitorCruAccess.class, SpecificAccessAdminLegalOpsProfileRAccess.class, CitizenWaTaskConfigurationCruAccess.class, SystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String prevJourneyType;

    @CCD(
            label = "S94B appeal",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SystemAccessRAccess.class}
    )
    private YesOrNo s94bStatus;

    @CCD(
            label = "Remote hearing",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private String remoteHearingDecisionForDisplay;

    @CCD(
            label = "Multimedia equipment",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private String multimediaDecisionForDisplay;

    @CCD(
            label = "Single-sex court",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private String singleSexCourtDecisionForDisplay;

    @CCD(
            label = "In camera court",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private String inCameraCourtDecisionForDisplay;

    @CCD(
            label = "Adjustements to accommodate vulnerabilities",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private String vulnerabilitiesDecisionForDisplay;

    @CCD(
            label = "Other adjustments",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private String otherDecisionForDisplay;

    @CCD(
            label = "![]()\r\n"
                            + "## Interpreter booking status",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, GsProfileCrudAccess.class}
    )
    private String interpreterBookingStatusTitle;

    @CCD(
            label = "The booking status of interpreters can be managed here. After updating an interpreters status the hearing will be automatically updated. This information will then be visible in List Assist.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, GsProfileCrudAccess.class}
    )
    private String interpreterBookingStatusDescription;

    @CCD(
            label = "Interpreter details",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, GsProfileCrudAccess.class}
    )
    private List<ListValue<InterpreterDetails>> interpreterDetails;

    @CCD(
            label = "![]()\r\n"
                            + "## Interpreter details",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, GsProfileCrudAccess.class}
    )
    private String interpreterDetailsTitle;

    @CCD(label = " ", access = {SystemCrudAccess.class})
    private String epimsId;

    @CCD(
            label = "Which hearing are you updating?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {AdmofficerCaseofficerSystemCrudAccess.class}
    )
    private String changeHearings;

    @CCD(label = "Hearing type", searchable = false, access = {AdmofficerCaseofficerSystemCrudAccess.class})
    private String changeHearingType;

    @CCD(
            label = "Do you want to change the hearing channel type?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class}
    )
    private HearingYesNoRadioButton changeHearingTypeYesNo;

    @CCD(
            label = "Hearing location",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingVenue",
            access = {AdmofficerCaseofficerSystemCrudAccess.class}
    )
    private HearingVenue changeHearingVenue;

    @CCD(
            label = "Do you want to change the hearing location?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class}
    )
    private HearingYesNoRadioButton changeHearingLocationYesNo;

    @CCD(label = "Hearing duration", searchable = false, access = {AdmofficerCaseofficerSystemCrudAccess.class})
    private String changeHearingDuration;

    @CCD(
            label = "Do you want to change the hearing duration?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class}
    )
    private HearingYesNoRadioButton changeHearingDurationYesNo;

    @CCD(label = "Hearing date", searchable = false, access = {AdmofficerCaseofficerSystemCrudAccess.class})
    private String changeHearingDate;

    @CCD(
            label = "Do you want to change the hearing date?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class}
    )
    private HearingYesNoRadioButton changeHearingDateYesNo;

    @CCD(
            label = "When should the next hearing be?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class}
    )
    private HearingDateChangingValues changeHearingDateType;

    @CCD(label = "You can enter either or both dates.", searchable = false, typeOverride = FieldType.Label)
    private String changeHearingDateRangeLabel;

    @CCD(label = "Earliest hearing date", searchable = false, access = {AdmofficerCaseofficerSystemCrudAccess.class})
    private LocalDate changeHearingDateRangeEarliest;

    @CCD(label = "Latest hearing date", searchable = false, access = {AdmofficerCaseofficerSystemCrudAccess.class})
    private LocalDate changeHearingDateRangeLatest;

    @CCD(
            label = "The selected hearing will be updated based on the <br>\r\n"
                            + "changes requested. Please give a reason for the <br>\r\n"
                            + "update.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerCaseofficerSystemCrudAccess.class}
    )
    private String changeHearingUpdateReasonTitle;

    @CCD(
            label = "Update reason",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "changeHearingUpdateReason",
            access = {AdmofficerCaseofficerSystemCrudAccess.class}
    )
    private ChangeHearingUpdateReason changeHearingUpdateReason;

    @CCD(
            label = "List hearing in List Assist?",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private YesOrNo isIntegrated;

    @CCD(label = " ", searchable = false, access = {AdmofficerCaseofficerSystemCrudAccess.class})
    private YesOrNo updateHmcRequestSuccess;

    @CCD(
            label = "Which hearing are you recording adjournment details for?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private String adjournmentDetailsHearing;

    @CCD(
            label = "Is there anything about the adjournment you want to add?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private YesOrNo anyAdditionalAdjournmentInfo;

    @CCD(
            label = "Provide more information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private String additionalAdjournmentInfo;

    @CCD(
            label = "Can the case be relisted right away?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private YesOrNo relistCaseImmediately;

    @CCD(
            label = "Confirm the format of the next hearing",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private String nextHearingFormat;

    @CCD(
            label = "Where should the next hearing be listed?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private String nextHearingVenue;

    @CCD(
            label = "How long should the next hearing be listed for?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingLength",
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private HearingLength nextHearingDuration;

    @CCD(
            label = "When should the next hearing be?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private HearingDateChangingValues nextHearingDate;

    @CCD(
            label = "The date of the hearing must be",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private LocalDate nextHearingDateFixed;

    @CCD(
            label = "You can enter either or both dates.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSpecificAccessCtscProfileRAccess.class, CaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String nextHearingDateRangeLabel;

    @CCD(
            label = "Earliest hearing date",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private LocalDate nextHearingDateRangeEarliest;

    @CCD(
            label = "Latest hearing date",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private LocalDate nextHearingDateRangeLatest;

    @CCD(
            label = "Does the judge need to be reserved or excluded?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private YesOrNo shouldReserveOrExcludeJudge;

    @CCD(
            label = "Exclude or reserve",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "reserveOrExcludeJudge",
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private ReserveOrExcludeJudge reserveOrExcludeJudge;

    @CCD(label = "![]()\r\n" + "## Adjournment Details", searchable = false, typeOverride = FieldType.Label)
    private String adjournmentDetailsTabTitle;

    @CCD(
            label = "Previous adjournment details",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class}
    )
    private List<ListValue<AdjournmentDetail>> previousAdjournmentDetails;

    @CCD(label = " ", searchable = false, access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeRAccess.class})
    private AdjournmentDetail currentAdjournmentDetail;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private YesOrNo manualCanHearingRequired;

    @CCD(label = " ", searchable = false, access = {AdmofficerCaseofficerSystemCrudAccess.class})
    private YesOrNo manualUpdHearingRequired;

    @CCD(label = " ", searchable = false, access = {AdmofficerCaseofficerSystemCrudAccess.class})
    private YesOrNo manualCreHearingRequired;

    @CCD(label = " ", searchable = false, access = {AdmofficerCaseofficerSystemCrudAccess.class})
    private String requireManualHearingsCancellation;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class}
    )
    private YesOrNo autoHearingRequestEnabled;

    @CCD(label = " ", searchable = false, access = {AdmofficerCaseofficerSystemCrudAccess.class})
    private YesOrNo shouldTriggerReviewInterpreterTask;

    @CCD(
            label = "## Auto request hearing\r\n"
                            + "\r\n"
                            + "Because one or more hearing request have been granted that<br>\r\n"
                            + "may require additional case flags you must state whether the<br>\r\n"
                            + "hearing can be auto requested when this event is completed. If<br>\r\n"
                            + "new case flags need to be added to the case before requesting<br>\r\n"
                            + "the hearing then set this to No. You will then be able to add the<br>\r\n"
                            + "case flags before manually requesting the hearing.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {CaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String autoRequestHearingGuide;

    @CCD(label = "Hearing can be auto requested?", access = {CaseofficerSystemCrudAccess.class})
    private YesOrNo autoRequestHearing;

    @CCD(
            label = "Auto-list the hearing?",
            searchable = false,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerLegalrepSolicitorRAccess.class, IacjudgeCrudAccess.class}
    )
    private YesOrNo autoListHearing;

    @CCD(
            label = "Will this hearing need to be linked to other hearings?",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, AdmofficerRAccess.class, IacjudgeCrudAccess.class}
    )
    private YesOrNo isHearingLinked;

    @CCD(
            label = "Listing location",
            typeOverride = FieldType.DynamicList,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String listingLocation;

    @CCD(
            label = "What hearing channel type is required?",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, LegalrepSolicitorSpecificAccessCtscProfileRAccess.class}
    )
    private String requestHearingChannel;

    @CCD(
            label = "Length",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingLength",
            access = {LegalrepSolicitorSpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, CitizenSpecificAccessCtscProfileRAccess.class}
    )
    private HearingLength requestHearingLength;

    @CCD(
            label = "Date and time",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenLegalrepSolicitorRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private LocalDateTime requestHearingDate;

    @CCD(
            label = "Date",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenLegalrepSolicitorRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private LocalDate requestHearingDate1;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String serviceRequestReference;

    @CCD(
            label = "Will the hearing be held remotely?",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo isRemoteHearing;

    @CCD(
            label = "CASE USES LOCATION REF DATA",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class, CitizenIacjudgeRAccess.class}
    )
    private YesOrNo isCaseUsingLocationRefData;

    @CCD(
            label = "![]()\r\n"
                            + "## CASE USES LOCATION REF DATA",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerLegalrepSolicitorCruAccess.class, SystemCrudAccess.class}
    )
    private String isCaseUsingLocationRefDataLabel;

    @CCD(
            label = "Internal flag managed by API to show if FTPA list is populated",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isFtpaListVisible;

    @CCD(
            label = "Internal flag managed by API to check DLRM set aside feature flag value",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isDlrmSetAsideEnabled;

    @CCD(
            label = "Internal flag managed by API to check DLRM fee remission feature flag value",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAdmofficerLegalrepSolicitorRAccess.class, CitizenRAccess.class}
    )
    private YesOrNo isDlrmFeeRemissionEnabled;

    @CCD(
            label = "What is the reason for updating the decision?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class, CitizenRAccess.class}
    )
    private UpdateTribunalRuleList updateTribunalDecisionList;

    @CCD(
            label = "## Current decision \n"
                            + "\n"
                            + "${updatedAppealDecision}\n"
                            + "\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private String updateTribunalDynamicCurrentDecision;

    @CCD(
            label = "## Current decision \n"
                            + "\n"
                            + "${appealDecision}\n"
                            + "\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private String originalTribunalDynamicCurrentDecision;

    @CCD(
            label = "Do you want to change the decision?",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {IacjudgeSystemCrudAccess.class, CaseofficerCitizenCrudAccess.class}
    )
    private String typesOfUpdateTribunalDecision;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private String updateTribunalDecisionAndReasonsTitle;

    @CCD(
            label = "Do you want to upload a corrected Decision and Reasons document?",
            searchable = false,
            access = {IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class, CitizenRAccess.class}
    )
    private YesOrNo updateTribunalDecisionAndReasonsFinalCheck;

    @CCD(
            label = "Corrections made to the Decision and Reasons document",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private String summariseTribunalDecisionAndReasonsDocument;

    @CCD(
            label = "Asylum support reference number supplied by Appellant",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class}
    )
    private String asylumSupportRefNumber;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "helpWithFeesOptions",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private HelpWithFeesOptions helpWithFeesOption;

    @CCD(
            label = "Help with Fees reference number supplied by Appellant",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class}
    )
    private String helpWithFeesRefNumber;

    @CCD(
            label = "Local Authority Support Documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> localAuthorityLetters;

    @CCD(
            label = "#### Advice on uploads\r\n"
                            + "- files must be no more than 100MB in size\r\n"
                            + "- You can upload jpg, png, svg, gif, doc and PDF files\r\n"
                            + "- before uploading a file, give it a meaningful file name. For example, FTPA-Decision-and-reasons-JSmith.pdf\r\n"
                            + "\r\n"
                            + "#### Upload rule 32 notice",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String underRule32DocumentUploadAdvice;

    @CCD(
            label = "Rule 32 notice",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class, CitizenRAccess.class}
    )
    private Document rule32NoticeDocument;

    @CCD(
            label = "Specify any instructions for the Tribunal regarding:\r\n"
                            + "- the hearing\r\n"
                            + "- the judge who hears the case\r\n"
                            + "- any Directions to be sent before listing",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CaseofficerRAccess.class, SystemCrudAccess.class}
    )
    private String rule32ListingInsAdvice;

    @CCD(
            label = "Listing instructions",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private String rule32ListingAdditionalIns;

    @CCD(
            label = "Decision of appeal",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class, CitizenRAccess.class}
    )
    private String updatedAppealDecision;

    @CCD(
            label = "Corrected Decision and reasons",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CitizenSpecificAccessCtscProfileRAccess.class, SystemCrudAccess.class}
    )
    private List<ListValue<DecisionAndReasons>> correctedDecisionAndReasons;

    @CCD(
            label = "Decision date",
            searchable = false,
            access = {IacjudgeSystemCrudAccess.class, SpecificAccessAdminLegalOpsProfileRAccess.class, SpecificAccessJudgeProfileCrudAccess.class}
    )
    private LocalDate updateTribunalDecisionDate;

    @CCD(
            label = "Set aside documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> allSetAsideDocs;

    @CCD(
            label = "![]()\r\n"
                            + "## Rehearing details",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String rehearingDetailsTitle;

    @CCD(label = "Reason for rehearing", searchable = false, access = {SystemCrudAccess.class})
    private String reasonRehearingRule32;

    @CCD(
            label = "Decision date",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private LocalDate updateTribunalDecisionDateRule32;

    @CCD(
            label = "NABA feature flag value",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo isNabaEnabled;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerLegalrepSolicitorCruAccess.class, SpecificAccessAdminLegalOpsProfileRAccess.class, IacjudgeCruAccess.class, SystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String letterSentOrReceived;

    @CCD(
            label = "Internal flag managed by API to allow admin users to create a case on behalf of an unrepresented appellant",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerLegalrepSolicitorCruAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCruAccess.class, IacjudgeCruAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isAdmin;

    @CCD(
            label = "Age Assessment Appeal feature flag value",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo isAgeAssessmentEnabled;

    @CCD(
            label = "Age Assessment Appeal condition to determine if AAA page is visible",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerCitizenRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo isAgeAssessmentVisible;

    @CCD(
            label = "Date appeal received",
            hint = "For example, 03 04 2023",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenRAccess.class}
    )
    private LocalDate tribunalReceivedDate;

    @CCD(
            label = "Internal flag managed by API to enable/disable Hearing tab in accelerated detained appeals",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo acceleratedDetainedAppealListed;

    @CCD(
            label = "Internal flag managed by API to enable/disable Submit Hearing Requirements event for accelerated detained appeals",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo adaHearingRequirementsSubmittable;

    @CCD(
            label = "Internal flag managed by API to enable/disable Review Hearing Requirements event for accelerated detained appeals",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo adaHearingRequirementsToReview;

    @CCD(
            label = "Internal flag managed by API to enable/disable Update Hearing Requirements event for accelerated detained appeals",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo adaHearingRequirementsUpdatable;

    @CCD(
            label = "Internal flag managed by API to enable/disable Update Hearing Adjustments event for accelerated detained appeals",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo adaHearingAdjustmentsUpdatable;

    @CCD(
            label = "Internal flag managed by API to enable/disable Edit Case Listing event for accelerated detained appeals",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo adaEditListingAvailable;

    @CCD(label = "Is this an accelerated detained appeal?", searchable = false, access = {SystemCrudAccess.class})
    private YesOrNo isAcceleratedDetainedAppeal;

    @CCD(
            label = "Bail application number",
            hint = "For Example: AB/01234 or 1234-1234-1234-1234",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class}
    )
    private String bailApplicationNumber;

    @CCD(
            label = "### What date is on the decision letter?",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String dateOnDecisionLetterTitle;

    @CCD(label = "Are you appealing an age assessment decision?", searchable = false, access = {SystemCrudAccess.class})
    private YesOrNo ageAssessment;

    @CCD(label = "Date of birth", searchable = false, access = {SystemCrudAccess.class})
    private LocalDate aaAppellantDateOfBirth;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private YesOrNo listingAvailableForAda;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private String calculatedHearingDate;

    @CCD(label = "Does your client have a litigation friend?", searchable = false, access = {SystemCrudAccess.class})
    private YesOrNo litigationFriend;

    @CCD(label = "Given names", searchable = false, access = {SystemCrudAccess.class})
    private String litigationFriendGivenName;

    @CCD(label = "Family name", searchable = false, access = {SystemCrudAccess.class})
    private String litigationFriendFamilyName;

    @CCD(label = "Organisation/Company", searchable = false, access = {SystemCrudAccess.class})
    private String litigationFriendCompany;

    @CCD(
            label = " \r\n"
                            + "Select the communication method which best suits the appellant. \r\n"
                            + "\r\n"
                            + " The Tribunal needs this to: \r\n"
                            + "- provide standard guidance on the appeal process\r\n"
                            + "- update the appellant at key points in the appeal\r\n"
                            + "- send the Hearing Notice and guidance on what to expect at hearing\r\n"
                            + "- contact the appellant if, for any reason, your representation ends",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String litigationFriendContactPreferenceTitle;

    @CCD(label = "Email", searchable = false, typeOverride = FieldType.Email, access = {SystemCrudAccess.class})
    private String litigationFriendEmail;

    @CCD(
            label = "Text message",
            searchable = false,
            typeOverride = FieldType.PhoneUK,
            access = {SystemCrudAccess.class}
    )
    private String litigationFriendPhoneNumber;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private YesOrNo hasTransferredOutOfAda;

    @CCD(label = "Date transferred out of ADA", searchable = false, access = {SystemCrudAccess.class})
    private LocalDate transferOutOfAdaDate;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private String adaSuffix;

    @CCD(
            label = "Email address",
            typeOverride = FieldType.Email,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class}
    )
    private String internalAppellantEmail;

    @CCD(
            label = "Mobile number",
            regex = "^((\\+44(\\s\\(0\\)\\s|\\s0\\s|\\s)?)|0)7\\d{3}(\\s)?\\d{6}$",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class}
    )
    private String internalAppellantMobileNumber;

    @CCD(label = "Date marked as ADA", searchable = false, access = {SystemCrudAccess.class})
    private LocalDate dateMarkedAsAda;

    @CCD(
            label = "Explain why this appeal is being marked an accelerated detained appeal",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAccess.class}
    )
    private String markAppealAsAdaExplanation;

    @CCD(label = "Reason appeal marked as ADA", searchable = false, access = {SystemCrudAccess.class})
    private String reasonAppealMarkedAsAda;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private YesOrNo adaHearingRequirementsSubmitted;

    @CCD(
            label = "You will be asked to provide the following information about this appeal.\n"
                            + "\n"
                            + "* Appeal details, including the Notice of Decision\n"
                            + "* Appellant's personal details\n"
                            + "* Detention details, if applicable\n"
                            + "* Reasons the appeal is late, if applicable, and any supporting evidence\n"
                            + "\n"
                            + "Check if the appellant has appealed any other UK immigration decisions. If they have, you will need the appeal number to submit the appeal.\n"
                            + "\n"
                            + "Once you start, you will not be able to save your progress until you have answered all the questions.\n"
                            + "\n"
                            + "Please ensure you have all the information you need to hand and allow enough time to submit the appeal in one sitting.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class, SpecificAccessAdminProfileRAccess.class}
    )
    private String beforeYouStartLabel;

    @CCD(
            label = "Before you begin, please verify that the case does not already exist in CCD.\n"
                            + "\n"
                            + "Once you start, you must complete all the questions in one go. You will not be able to save your progress until all questions are answered. \n"
                            + "\n"
                            + "Creating a rehydrated case will pause the following: \n"
                            + "* Notifications\n"
                            + "* Work allocation tasks \n"
                            + "* Document generation (e.g. appeal submission summary) \n"
                            + "\n"
                            + "You will need to trigger an event to resume these functions only after the case has been moved into the correct state. Once resumed, these functions cannot be paused again.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerCrudAccess.class, SpecificAccessAdminProfileRAccess.class}
    )
    private String beforeYouStartRehydrateAppealLabel;

    @CCD(
            label = "This date will be used to determine if the appeal is in time.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerCitizenRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private String dateWillBeUsedToDetermineLabel;

    @CCD(
            label = "Has the appeal form been submitted by an appellant in person?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenRAccess.class}
    )
    private AppellantsRepresentation appellantsRepresentation;

    @CCD(
            label = "Enter the appellant's contact details.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class}
    )
    private String internalContactDetailsTitle;

    @CCD(
            label = "## Appeal transfer details",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String markReadyForUtTransferTitle;

    @CCD(
            label = "Provide the name and location of anyone who wishes to give oral evidence from outside the UK",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAccess.class}
    )
    private String isEvidenceFromOutsideUkDocText;

    @CCD(
            label = "Provide the name and location of anyone who wishes to give oral evidence from outside the UK",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAccess.class}
    )
    private String isEvidenceFromOutsideUkInCountryText;

    @CCD(
            label = "Tribunal documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerSystemWaTaskConfigurationCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithDescription>> customTribunalDocuments;

    @CCD(
            label = "Upload a copy of the appeal form.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class}
    )
    private String uploadTheAppealFormTitle;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label, access = {SystemCrudAccess.class})
    private String removalOrderTitle;

    @CCD(
            label = "![Notifications turned off: You should turn notifications on when you have progressed the case to the correct state.](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/ejp_notification_turned_off.svg)\r\n"
                            + "\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String notificationsTurnedOffBanner;

    @CCD(
            label = "![]()\r\n"
                            + "## Detention details",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private String detentionDetailsTitle;

    @CCD(
            label = "NABA feature flag OOC value",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo isNabaEnabledOoc;

    @CCD(
            label = "NABA ADA feature flag value",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo isNabaAdaEnabled;

    @CCD(
            label = "Date on decision letter",
            hint = "Format DD MM YYYY (for example, 01 12 2024 for 1st December 2024).",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private LocalDate dateOnDecisionLetter;

    @CCD(
            label = " \r\n"
                            + "Enter the name of the litigation friend",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String litigationFriendTitle;

    @CCD(
            label = "Further details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAccess.class}
    )
    private String transferOutOfAdaMoreDetails;

    @CCD(
            label = "Date detention status removed",
            hint = "For example, 03 04 2023",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private LocalDate dateRemovedDetainedStatus;

    @CCD(
            label = "Reasons for removal",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAccess.class}
    )
    private String reasonsRemovedFromDetention;

    @CCD(
            label = "Date appeal marked as detained",
            hint = "For example, 03 04 2023",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private LocalDate detentionDate;

    @CCD(
            label = "Reasons why appellant is detained",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAccess.class}
    )
    private String detentionReasons;

    @CCD(label = "Notification attachment documents", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<DocumentWithMetadata>> notificationAttachmentDocuments;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private String previousDetentionLocation;

    @CCD(
            label = "Upload the Notice of Decision for the expedited section 82A appeal in the Upper Tribunal.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String noticeOfDecisionUtTransferTitle;

    @CCD(label = "Notice of Decision", searchable = false, access = {SystemCrudAccess.class})
    private Document noticeOfDecisionUtTransferDocument;

    @CCD(
            label = "Enter the appeal reference for the expedited section 82A appeal in the Upper Tribunal.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String utAppealReferenceTitle;

    @CCD(
            label = "Upper Tribunal expedited section 82A appeal reference",
            hint = "For example, UI-2020-123456",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String utAppealReferenceNumber;

    @CCD(
            label = "Date First-tier Tribunal instructed to transfer appeal",
            hint = "For example, 03 04 2023",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private LocalDate utInstructionDate;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerLegalrepSolicitorSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCruAccess.class, IacjudgeCruAccess.class, SpecificAccessAdminProfileCruAccess.class, SpecificAccessCtscJudgeLegalOpsProfileCruAccess.class}
    )
    private YesOrNo isNotificationTurnedOff;

    @CCD(
            label = "You will be asked to provide the following information about this appeal.\n"
                            + "\n"
                            + "* Upper Tribunal reference\n"
                            + "* Date of appeal transfer to the First-tier tribunal\n"
                            + "* State of the appeal before it was transferred\n"
                            + "* Upper Tribunal transfer order\n"
                            + "* Detention details, if applicable\n"
                            + "* Appeal details, including the Notice of Decision\n"
                            + "* Appellant's personal details\n"
                            + "* Completed IAUT-2 appeal form\n"
                            + "\n"
                            + "Check if the appellant has appealed any other UK immigration decisions. If they have, you will need the appeal number to submit the appeal.\n"
                            + "\n"
                            + "Once you start, you will not be able to save your progress until you have answered all the questions.\n"
                            + "\n"
                            + "Please ensure you have all the information you need to hand and allow enough time to submit the appeal in one sitting.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String beforeYouStartEjpLabel;

    @CCD(
            label = "Upper Tribunal reference",
            hint = "For example, UI-2020-123456",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String upperTribunalReferenceNumber;

    @CCD(
            label = "Date of transfer to First-tier Tribunal",
            hint = "For example, 03 04 2023",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private LocalDate firstTierTribunalTransferDate;

    @CCD(label = "Upper Tribunal Transfer order", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<Document>> utTransferDoc;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo isEjp;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo isRehydratedAppeal;

    @CCD(label = "Is the appellant legally represented?", searchable = false, access = {SystemCrudAccess.class})
    private YesOrNo isLegallyRepresentedEjp;

    @CCD(
            label = "Upload a completed copy of the IAUT-2 form",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String uploadEjpAppealFormTitle;

    @CCD(label = "IAUT-2 appeal form", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<Document>> uploadEjpAppealFormDocs;

    @CCD(
            label = "Costs applications should always be clear and contain detailed grounds for why they should \r\n"
                            + " be awarded. They are granted when costs are incurred by a party as a result of any improper,\r unreasonable or negligent act or failure to act by a representative or other party.\n"
                            + "\n"
                            + " ### A costs application should contain the following:\n"
                            + "\n"
                            + " * a clear description of the conduct said to give rise to the application\n"
                            + "* evidence showing the conduct\n"
                            + "* reasons why the Tribunal should make a costs order\n"
                            + "* a detailed schedule of costs incurred, such as spreadsheet\n"
                            + "\n"
                            + "\n"
                            + " The other party will be able to respond to the application. The Tribunal will then review your application and make a decision on your claim.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String beforeYouStartApplyForCostsLabel;

    @CCD(
            label = "Which costs application are you applying for?",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {SystemCrudAccess.class}
    )
    private String appliedCostsTypes;

    @CCD(
            label = "For a judge to consider awarding costs, evidence of the conduct complained of should be provided. \n"
                            + "\n"
                            + " ### A costs application should contain the following: \n"
                            + " * a clear description of the conduct said to give rise to the application\n"
                            + "* evidence showing the conduct\n"
                            + "* reasons why the Tribunal should make a costs order\n"
                            + "* a detailed schedule of costs incurred, such as spreadsheet",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String evidenceAndCostsTitle;

    @CCD(
            label = "\n"
                            + "\n"
                            + " ### 1. Arguments and evidence \n"
                            + " #### Upload any evidence that supports your application \n"
                            + " * you can upload evidence as one file or as separate files \n"
                            + " * files must be no more than 100MB in size \n"
                            + " * you can upload jpg, png, svg, gif, doc and PDF files \n"
                            + " * before uploading a file, give it a meaningful file name. For example, YYYYMMDD-application-evidence.pdf",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String argumentsAndEvidenceTitle;

    @CCD(label = "Arguments and evidence", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<Document>> argumentsAndEvidenceDocuments;

    @CCD(
            label = " ",
            hint = "Explain how the evidence you've uploaded directly relate to poor or deficient conduct by the respondent.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAccess.class}
    )
    private String argumentsAndEvidenceDetails;

    @CCD(
            label = "\n"
                            + "\n"
                            + " ### 2. Schedule of costs \n"
                            + " If you have not included your schedule of costs, please upload ideally as a spreadsheet.\n"
                            + " #### Upload your schedule of costs\n"
                            + " * files must be no more than 100MB in size\n"
                            + " * before uploading a file, give it a meaningful file name, YYYYMMDD-schedule-of-costs.csv",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String scheduleOfCostsTitle;

    @CCD(label = "Add schedule of costs", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<Document>> scheduleOfCostsDocuments;

    @CCD(
            label = "The Tribunal generally decides costs applications without a hearing, but will consider requests for a hearing.\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String applyForCostsHearingTypeTitle;

    @CCD(
            label = "Do you believe this application requires a hearing?",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private YesOrNo applyForCostsHearingType;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea, access = {SystemCrudAccess.class})
    private String applyForCostsHearingTypeExplanation;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label, access = {SystemCrudAccess.class})
    private String applyForCosts;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label, access = {SystemCrudAccess.class})
    private String appliedForCostsTitle;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private String applyForCostsApplicantType;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private LocalDate applyForCostsCreationDate;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private String isAppliedForCosts;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private String appliedCostsType;

    @CCD(
            label = "[Apply for costs](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/applyForCosts)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String applyForCostsLink;

    @CCD(
            label = "[Add evidence for costs](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/addEvidenceForCosts)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String addEvidenceForCostsLink;

    @CCD(
            label = "This application is late. You must provide full reasons why it is being submitted more than 28 days \n"
                            + " after the Decision and Reasons document was uploaded",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String applyForCostsOotTitle;

    @CCD(
            label = "Reasons why the application is late",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAccess.class}
    )
    private String applyForCostsOotExplanation;

    @CCD(
            label = "#### Upload any evidence that supports the fact your application is late\n"
                            + " * files must be no more than 100MB in size \n"
                            + " * you can upload jpg, png, svg, gif, doc and PDF files \n"
                            + " * before uploading a file, give it a meaningful file name. For example, YYYYMMDD-application-evidence.pdf",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String applyForCostsOotUploadEvidenceTitle;

    @CCD(label = "Supporting evidence", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<Document>> ootUploadEvidenceDocuments;

    @CCD(label = " ", searchable = false, access = {SystemCrudAccess.class})
    private YesOrNo isApplyForCostsOot;

    @CCD(label = " ", access = {SystemCrudAccess.class})
    private String sendDecisionsAndReasonsDate;

    @CCD(label = "Email address", searchable = false, typeOverride = FieldType.Email, access = {SystemCrudAccess.class})
    private String emailUnrep;

    @CCD(label = "Mobile phone number", searchable = false, access = {SystemCrudAccess.class})
    private String mobileNumberUnrep;

    @CCD(
            label = "Which costs application do you want to respond to",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {SystemCrudAccess.class}
    )
    private String respondToCostsList;

    @CCD(
            label = "Use the text area and/or upload option to explain why a cost order should not be made against you. Refer to the other party's argument and schedule of cost to make your response.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String respondToApplicationLabel;

    @CCD(
            label = "Reasons why you feel a costs order should not be made against you",
            hint = "Outline your reasons below. If referring to documents you've uploaded, always reference the document name and page number you refer to.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAccess.class}
    )
    private String responseToApplicationTextArea;

    @CCD(
            label = "#### Upload any evidence that supports your application\r\n"
                            + "- you can upload evidence as one file or as separate files\r\n"
                            + "- files must be no more than 100MB in size\r\n"
                            + "- You can upload jpg, png, svg, gif, doc and PDF files\r\n"
                            + "- before uploading a file, give it a meaningful file name. For example, YYYYMMDD-application-evidence.pdf",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String respondToApplicationEvidenceAdvice;

    @CCD(label = "Upload your response", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<Document>> responseToApplicationEvidence;

    @CCD(
            label = "The Tribunal generally decides costs applications without a hearing, but will consider requests for a hearing.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String typeOfHearingLabel;

    @CCD(label = "Do you believe this application requires a hearing?", access = {SystemCrudAccess.class})
    private YesOrNo typeOfHearingOption;

    @CCD(
            label = "Explain why you believe an oral hearing is needed.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAccess.class}
    )
    private String typeOfHearingExplanation;

    @CCD(
            label = "[Respond to costs](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/respondToCosts)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String respondToCostsLink;

    @CCD(
            label = "Which costs application do you want to add evidence to?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {SystemCrudAccess.class}
    )
    private String addEvidenceForCostsList;

    @CCD(
            label = "#### Upload any evidence that supports your costs application\r\n"
                            + "- you can upload evidence as one file or as separate files\r\n"
                            + "- files must be no more than 100MB in size\r\n"
                            + "- You can upload jpg, png, svg, gif, doc and PDF files\r\n"
                            + "- before uploading a file, give it a meaningful file name. For example, YYYYMMDD-application-evidence.pdf",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String additionalEvidenceForCostsTitle;

    @CCD(label = "Upload additional evidence", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<Document>> additionalEvidenceForCosts;

    @CCD(
            label = "Which costs application do you want to decide?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {SystemCrudAccess.class}
    )
    private String decideCostsApplicationList;

    @CCD(label = "When was the oral hearing?", searchable = false, access = {SystemCrudAccess.class})
    private LocalDate costsOralHearingDate;

    @CCD(label = "Upload order", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<Document>> uploadCostsOrder;

    @CCD(
            label = "[Decide a costs application](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/decideCostsApplication)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String decideCostApplicationLink;

    @CCD(
            label = "What type of costs order are you considering?",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {SystemCrudAccess.class}
    )
    private String judgeAppliedCostsTypes;

    @CCD(
            label = "The tribunal is considering making a costs order because:",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SystemCrudAccess.class}
    )
    private String tribunalConsideringReason;

    @CCD(
            label = "#### Upload any evidence that supports your costs order\r\n"
                            + "- you can upload evidence as one file or as separate files\r\n"
                            + "- files must be no more than 100MB in size\r\n"
                            + "- You can upload jpg, png, svg, gif, doc and PDF files\r\n"
                            + "- before uploading a file, give it a meaningful file name. For example, YYYYMMDD-application-evidence.pdf",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String judgeEvidenceTitle;

    @CCD(label = "Upload evidence", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<Document>> judgeEvidenceForCostsOrder;

    @CCD(
            label = "[Consider making a costs order](/case/IA/Asylum/${[CASE_REFERENCE]}/trigger/considerMakingCostsOrder)",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String considerMakingCostsOrderLink;

    @CCD(
            label = "Excluded judges",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, IacjudgeSystemCrudAccess.class, AdmofficerCaseofficerCruAccess.class}
    )
    private String judgesNamesToExclude;

    @CCD(
            label = "Remitted from",
            searchable = false,
            access = {AdmofficerSystemCrudAccess.class, CitizenIacjudgeRAccess.class, CaseofficerCrAccess.class}
    )
    private SourceOfRemittalList sourceOfRemittal;

    @CCD(
            label = "What is the Upper Tribunal appeal reference number",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String upperTribunalCourtReferenceLabel;

    @CCD(
            label = "What is the Court of Appeal reference number",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String courtOfAppealReferenceLabel;

    @CCD(
            label = "${sourceOfRemittal} reference",
            hint = "For example, UI-2023-000001 or CA-2023-000001",
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String courtReferenceNumber;

    @CCD(
            label = "Listing instructions",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String remittedAdditionalInstructions;

    @CCD(label = "Remittal decision", searchable = false, access = {AdmofficerSystemCrudAccess.class})
    private Document uploadRemittalDecisionDoc;

    @CCD(
            label = "#### Advice on uploads\r\n"
                            + "- files must be no more than 100MB in size\r\n"
                            + "- You can upload jpg, png, svg, gif, doc and PDF files\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String remittalDecisionUploadAdvice;

    @CCD(
            label = "#### Upload any other documents relevant to the remission of this appeal.\n"
                            + "#### Advice on uploads\r\n"
                            + "- files must be no more than 100MB in size\r\n"
                            + "- You can upload jpg, png, svg, gif, doc and PDF files\r\n"
                            + "- before uploading a file, give it a meaningful file name. For example, UTPA-Decision-and-Reasons-JSmith.pdf",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SystemCrudAccess.class}
    )
    private String otherRemittalDocsUploadAdvice;

    @CCD(label = "Upload other documents", searchable = false, access = {AdmofficerSystemCrudAccess.class})
    private List<ListValue<DocumentWithDescription>> uploadOtherRemittalDocs;

    @CCD(
            label = "![]()\r\n"
                            + "## Rehearing details",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String rehearingTitle;

    @CCD(label = "Reason for rehearing", searchable = false, access = {AdmofficerSystemCrudAccess.class})
    private String rehearingReason;

    @CCD(
            label = "Remittal Documents",
            searchable = false,
            access = {AdmofficerSystemCrudAccess.class, CaseofficerCruAccess.class, CitizenCruAccess.class}
    )
    private List<ListValue<RemittalDocument>> remittalDocuments;

    @CCD(
            label = "Reheard hearing documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerSystemCrudAccess.class, CitizenCrudAccess.class}
    )
    private List<ListValue<ReheardHearingDocumentsComplex>> reheardHearingDocumentsCollection;

    @CCD(
            label = "Reheard decision and reason documents",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class}
    )
    private List<ListValue<ReheardHearingDecisionComplex>> reheardDecisionReasonsCollection;

    @CCD(
            label = "Decision and reason documents",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, AdmofficerLegalrepSolicitorRAccess.class, CaseofficerSystemCrudAccess.class, CitizenWaTaskConfigurationRAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> latestDecisionAndReasonsDocuments;

    @CCD(
            label = "Remittal Documents",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, AdmofficerLegalrepSolicitorRAccess.class, CaseofficerSystemCrudAccess.class, CitizenWaTaskConfigurationRAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> latestRemittalDocuments;

    @CCD(
            label = "Reheard hearing documents",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, AdmofficerLegalrepSolicitorRAccess.class, CaseofficerSystemCrudAccess.class, CitizenWaTaskConfigurationRAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> latestReheardHearingDocuments;

    @CCD(
            label = "Decision Without Hearing",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeRAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo isDecisionWithoutHearing;

    @CCD(
            label = " ",
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessAdminCtscJudgeLegalOpsProfileCrudAccess.class, IacjudgeCrudAccess.class, NextHearingDateAdminCrudAccess.class}
    )
    private HearingSchedule nextHearingDetails;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, SpecificAccessAdminCtscJudgeLegalOpsProfileCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo hasAddedLegalRepDetails;

    @CCD(
            label = "Internal flag managed by API to check DLRM fee refund feature flag value",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAdmofficerLegalrepSolicitorRAccess.class, CitizenRAccess.class}
    )
    private YesOrNo isDlrmFeeRefundEnabled;

    @CCD(
            label = "Internal flag managed by API to check whether fee remission request is late (refund) or not",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, SystemCrudAdmofficerLegalrepSolicitorRAccess.class, CitizenCruAccess.class}
    )
    private YesOrNo isLateRemissionRequest;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
    private String notificationsTabTitle;

    @CCD(label = "Notifications", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<Notification>> notifications;

    @CCD(
            label = " ",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "remissionOptions",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private RemissionOptions lateRemissionOption;

    @CCD(
            label = " ",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String lateAsylumSupportRefNumber;

    @CCD(
            label = " ",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "helpWithFeesOptions",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private HelpWithFeesOptions lateHelpWithFeesOption;

    @CCD(
            label = " ",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String lateHelpWithFeesRefNumber;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private List<ListValue<DocumentWithMetadata>> lateLocalAuthorityLetters;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private YesOrNo hasPreviousRemission;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private YesOrNo refundRequested;

    @CCD(label = "fee remission date", searchable = false, access = {SystemCrudAccess.class})
    private LocalDate requestFeeRemissionDate;

    @CCD(
            label = "Amount to be refunded",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class, CitizenRAccess.class}
    )
    private String manageFeeRefundedAmount;

    @CCD(
            label = "Additional payment requested",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, LegalrepSolicitorCruAccess.class, WaTaskConfigurationCrudAccess.class, CitizenRAccess.class}
    )
    private String manageFeeRequestedAmount;

    @CCD(
            label = "Internal timed event ID for record remission decision refused or partially granted",
            searchable = false,
            access = {SystemCrudAccess.class}
    )
    private String automaticRemissionReminderLegalRep;

    @CCD(
            label = " ",
            searchable = false,
            access = {LegalrepSolicitorSpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerCitizenCruAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo refundConfirmationApplied;

    @CCD(
            label = "How do you want the appeal to be decided?",
            searchable = false,
            access = {IacjudgeSpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, AdmofficerSystemCrudAccess.class, CitizenCrudAccess.class, CaseofficerLegalrepSolicitorCruWaTaskConfigurationRAccess.class}
    )
    private DecisionHearingFeeOption updatedDecisionHearingFeeOption;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerSystemCrudAccess.class, LegalrepSolicitorCruAccess.class}
    )
    private DecisionHearingFeeOption previousDecisionHearingFeeOption;

    @CCD(
            label = " ",
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private String selectedHearingCentreRefData;

    @CCD(
            label = "Fee",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerIacjudgeSpecificAccessCtscProfileWaTaskConfigurationRAccess.class, AdmofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class, CitizenCruAccess.class}
    )
    private String previousFeeAmountGbp;

    @CCD(
            label = "Enter IDAM User ID to revoke case access",
            searchable = false,
            access = {InternalSystemCrudAccess.class}
    )
    private String revokeAccessForUserId;

    @CCD(
            label = "Enter Legal representative's Organisation ID ",
            searchable = false,
            access = {InternalSystemCrudAccess.class}
    )
    private String revokeAccessForUserOrgId;

    @CCD(
            label = "Select user to revoke case access from",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, InternalCrudAccess.class, SpecificAccessAdminProfileCrudAccess.class, SpecificAccessCtscProfileCrudAccess.class, SpecificAccessLegalOpsProfileCrudAccess.class}
    )
    private String revokeAccessDl;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeSpecificAccessAdminCtscLegalOpsProfileCrudAccess.class}
    )
    private PinInPost joinAppealPin;

    @CCD(
            label = "Previous Non-Legal Representative Details",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeSpecificAccessAdminCtscLegalOpsProfileCrudAccess.class}
    )
    private NlrDetails previousNlrDetails;

    @CCD(
            label = "Has a non-legal representative been requested?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeSpecificAccessAdminCtscLegalOpsProfileCrudAccess.class}
    )
    private YesOrNo hasNonLegalRep;

    @CCD(
            label = "Are the sponsor and the non-legal representative the same person?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeSpecificAccessAdminCtscLegalOpsProfileCrudAccess.class}
    )
    private YesOrNo isSponsorSameAsNlr;

    @CCD(
            label = "Has the non-legal representative joined the appeal?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeSpecificAccessAdminCtscLegalOpsProfileCrudAccess.class}
    )
    private YesOrNo hasNonLegalRepJoined;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeSpecificAccessAdminCtscLegalOpsProfileCrudAccess.class}
    )
    private YesOrNo shouldInviteNlrToIdam;

    @CCD(label = " ", searchable = false, access = {CitizenSystemCrudAccess.class})
    private YesOrNo hasNlrSubmitted;

    @CCD(
            label = "Will the non-legal representative require a spoken or sign language interpreter at the hearing?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, CitizenIacjudgeSpecificAccessAdminCtscLegalOpsProfileCrudAccess.class}
    )
    private YesOrNo isNlrInterpreterRequired;

    @CCD(
            label = "What are the circumstances of the appellant's out of country appeal?",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private OocAppealAdminJ oocAppealAdminJ;

    @CCD(
            label = "Internal flag managed by API to check if decision is changed via Tribunal decision under rule 31",
            searchable = false,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, AdmofficerLegalrepSolicitorRAccess.class, IacjudgeSystemCrudAccess.class, CaseofficerCrudAccess.class}
    )
    private YesOrNo isDecisionRule31Changed;

    @CCD(label = "Letter notification documents", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<DocumentWithMetadata>> letterNotificationDocuments;

    @CCD(label = "Letter bundle documents", searchable = false, access = {SystemCrudAccess.class})
    private List<ListValue<DocumentWithMetadata>> letterBundleDocuments;

    @CCD(
            label = "Internal flag managed by API to check if case is ARIA migrated",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isAriaMigrated;

    @CCD(
            label = "Internal flag managed by API to allow case filtering",
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessAdminProfileRAccess.class}
    )
    private YesOrNo isAriaMigratedFilter;

    @CCD(
            label = "Select a desired state:",
            hint = "Choose a different desired state from the dropdown ",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "listOfStates",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerLegalrepSolicitorRAccess.class}
    )
    private ListOfStates ariaDesiredState;

    @CCD(
            label = "Internal flag managed by API to check if case is ARIA migrated cases is fee exemption",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isAriaMigratedFeeExemption;

    @CCD(
            label = " ",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerLegalrepSolicitorRAccess.class}
    )
    private String ariaDesiredStateSelectedValue;

    @CCD(
            label = "This case will progress to the state listed below.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerLegalrepSolicitorRAccess.class}
    )
    private String desiredStateTips;

    @CCD(
            label = "### Desired state:\r\n"
                            + "\r\n"
                            + "${ariaDesiredStateSelectedValue}",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerLegalrepSolicitorRAccess.class}
    )
    private String desiredStateTitle;

    @CCD(
            label = "Is the desired state correct?",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeSpecificAccessCtscProfileRAccess.class, AdmofficerSystemCrudAccess.class, CaseofficerLegalrepSolicitorRAccess.class}
    )
    private DesiredStateCorrect desiredStateCorrect;

    @CCD(
            label = "If this case was listed for a hearing, you must transfer the listing over to the hearing\r\n"
                            + "management component (HMC) as part of the migration process. ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private String migrationHMCSecondPartTitle;

    @CCD(label = "Upload a case note", searchable = false, access = {AdmofficerSystemCrudAccess.class})
    private List<ListValue<CaseNoteMigration>> addCaseNotesMigration;

    @CCD(label = " ", access = {SystemCrudAccess.class, WaTaskConfigurationCrudAccess.class})
    private String ariaMigrationTaskDueDays;

    @CCD(
            label = " ",
            searchable = false,
            access = {SpecificAccessAdminLegalOpsProfileRAccess.class, SystemCrudAccess.class}
    )
    private YesOrNo isHearingBundleUpdated;

    @CCD(
            label = "Current Hearing ID",
            access = {HomeofficebailHomeofficepouLegalrepSolicitorRAccess.class, AdmofficerCruAccess.class, IacjudgeRAccess.class, SystemCrudAccess.class}
    )
    private String currentHearingId;

    @CCD(
            label = "Virtual Hearing",
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, CitizenIacjudgeLegalrepSolicitorWaTaskConfigurationCrudAccess.class, SpecificAccessAdminCtscJudgeLegalOpsProfileCrudAccess.class}
    )
    private YesOrNo isVirtualHearing;

    @CCD(
            label = "Case IDs",
            hint = "Enter the list of caseIds without hyphens and separated by commas, e.g. 1234567812345678,1234567812345679,1234567812345680",
            access = {InternalSystemCrudAccess.class}
    )
    private String caseIdList;

    @CCD(
            label = " ",
            searchable = false,
            access = {CitizenLegalrepSolicitorRAccess.class, CaseofficerCruAccess.class, SystemCrudAccess.class, CftTtlManagerCruAccess.class}
    )
    private TTL TTL;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {LegalrepSolicitorSystemCrudAccess.class}
    )
    private String checklistTitle4;

    @CCD(
            label = "Detention facility type",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private DetentionFacilityType detentionFacility;

    @CCD(
            label = "NOMS number",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private PrisonNOMSComplexType prisonNOMSNumber;

    @CCD(
            label = "Detention facility name",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private OtherDetentionFacilityComplexType otherDetentionFacilityName;

    @CCD(
            label = "Detention facility name",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ircNameList",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private IrcNameList ircName;

    @CCD(
            label = "Detention facility name",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "prisonNameList",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private PrisonNameList prisonName;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {SpecificAccessAdminCtscJudgeLegalOpsProfileRAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerRAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, AdmofficerCaseofficerCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String custodialSentenceTitle;

    @CCD(
            label = "Custodial Sentence",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo releaseDateProvided;

    @CCD(
            label = "Custodial sentence release date",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private LocalDate releaseDate;

    @CCD(
            label = "Pending bail application",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class}
    )
    private PendingBailApplication hasPendingBailApplications;

    @CCD(
            label = "Internal flag managed by API to store if generate list CMR is requested",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private YesOrNo generateListCmrTaskRequested;

    @CCD(
            label = "Building",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class, SpecificAccessAdminProfileRAccess.class}
    )
    private String detentionBuilding;

    @CCD(
            label = "Address",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class, SpecificAccessAdminProfileRAccess.class}
    )
    private String detentionAddressLines;

    @CCD(
            label = "Postcode/Zipcode",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, LegalrepSolicitorCrudAccess.class, SpecificAccessAdminProfileRAccess.class}
    )
    private String detentionPostcode;

    @CCD(
            label = "Reason appellant was detained",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessAdminCtscJudgeLegalOpsProfileCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String addReasonAppellantWasDetained;

    @CCD(
            label = "Date detention status removed",
            hint = "For example, 03 04 2023",
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessAdminCtscJudgeLegalOpsProfileCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private LocalDate detentionRemovalDate;

    @CCD(
            label = "Reasons for removal",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, SpecificAccessAdminCtscJudgeLegalOpsProfileCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String detentionRemovalReason;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerSystemCrudAccess.class, CitizenLegalrepSolicitorCrudAccess.class}
    )
    private LocalDate lastEditAppealNotificationDate;

    @CCD(
            label = "Statutory Timeframe 24 Weeks",
            searchable = false,
            access = {AdmofficerCaseofficerCruAccess.class, IacjudgeCruAccess.class, SystemCrudAccess.class}
    )
    private StatutoryTimeframe24Weeks statutoryTimeframe24Weeks;

    @CCD(
            label = "Reason",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private String stf24wCurrentReasonAutoGenerated;

    @CCD(label = "Home Office Cohort", access = {AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class})
    private String stf24wHomeOfficeCohort;

    @CCD(
            label = "XUI banner text",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class, SystemAccessRAccess.class, RasValidationRAccess.class}
    )
    private String xuiBannerText;

    @CCD(
            label = "24 week statutory timeframe set on case?",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeWaTaskConfigurationCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCruAccess.class, CitizenRAccess.class}
    )
    private YesOrNo stf24wCurrentStatusAutoGenerated;

    @CCD(
            label = "STF 24 Weeks was Yes",
            access = {LegalrepSolicitorCruSpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCruAccess.class, IacjudgeCrudAccess.class, CitizenRAccess.class}
    )
    private YesOrNo stf24wPreviousStatusWasYesAutoGenerated;

    @CCD(
            label = "Date the appeal was transferred into STF24W",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private LocalDate stf24wSetDate;

    @CCD(
            label = "Date the appeal was transferred out of STF24W",
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeCrudAccess.class}
    )
    private LocalDate stf24wRemoveDate;

    @CCD(
            label = "Is this refusal in respect to a 24 week removal decision?",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerCitizenIacjudgeWaTaskConfigurationCruAccess.class, AdmofficerCruAccess.class, SystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private YesOrNo isRemovalOf24wApplicationRefused;

    @CCD(
            label = "Reason for removal of 24 week timeframe",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerCitizenIacjudgeWaTaskConfigurationCruAccess.class, AdmofficerCruAccess.class, SystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String removalOf24wDecisionReason;

    @CCD(
            label = "Name of decision maker",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, CaseofficerCitizenIacjudgeWaTaskConfigurationCruAccess.class, AdmofficerCruAccess.class, SystemCrudAccess.class, SpecificAccessCtscProfileRAccess.class}
    )
    private String removalOf24wDecisionDecisionMaker;

    @CCD(
            label = " ",
            searchable = false,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, HomeofficeapcHomeofficelartHomeofficepouRespondentofficerCrudAccess.class, SpecificAccessAdminCtscJudgeLegalOpsProfileCrudAccess.class, LegalrepSolicitorWaTaskConfigurationCrudAccess.class}
    )
    private String saveNotificationsToDataDate;

    @CCD(
            label = "Source of appeal",
            searchable = false,
            access = {SpecificAccessAdminJudgeLegalOpsProfileRAccess.class, AdmofficerCaseofficerSystemCrudAccess.class, IacjudgeLegalrepSolicitorCrudAccess.class, CitizenRAccess.class}
    )
    private SourceOfAppeal sourceOfAppeal;

    @CCD(
            label = "Selecting submit will restart the following services, which will be triggered from the next event. \r\n"
                            + "- Notifications\r\n"
                            + "- Work allocation tasks\r\n"
                            + "- Document generation\r\n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerSystemCrudAccess.class, WaTaskConfigurationCrudAccess.class}
    )
    private String turnOnNotificationsWATasksLabel;

    @CCD(
            label = "This event will move the appeal to the decided state\n"
                            + "\r\n"
                            + "\rBefore continuing, please ensure that case notes have been updated and any relevant documents relating to the decision have been updated",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerRAccess.class}
    )
    private String moveToDecidedDateLabel1;

    @CCD(
            label = "### What is your decision?\n"
                            + "\r\n"
                            + "\rIf the decicion is dismissed on all grounds, select 'Dismissed on all grounds'. If the appeal is allowed on any grounds, you must select 'Allowed'",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerRAccess.class}
    )
    private String moveToDecidedDecisionLabel1;

    @CCD(
            label = "This event will move the appeal to the FTPA decided state.\n"
                            + "\r\n"
                            + "\rBefore continuing, please ensure that case notes have been updated and any relevant documents relating to the decision have been updated.",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {AdmofficerRAccess.class}
    )
    private String moveToFtpaDecidedDateLabel1;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label, access = {AdmofficerRAccess.class})
    private String moveToFtpaDecidedApplicantLabel1;

    @CCD(
            label = "Email address to send PIP to",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {AdmofficerCaseofficerSystemCrudAccess.class, InternalCrudAccess.class}
    )
    private String generatePipEmail;

    @CCD(
            label = "Internal flag managed by API to allow recording if a case has been entered the decided state",
            searchable = false,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private YesOrNo hasBeenDecided;

    @CCD(
            label = "Internal flag managed by API to allow recording if a case has been entered the FTPA decided state",
            searchable = false,
            access = {AdmofficerSystemCrudAccess.class}
    )
    private YesOrNo hasBeenFtpaDecided;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
    private String cuiHomeOfficeReferenceNumber;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
    private String cuiGwfReferenceNumber;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
    private String cuiAppellantName;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
    private String cuiAppellantDob;

    @CCD(
            label = "Location",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "hearingVenue",
            access = {SystemCrudAccess.class}
    )
    private HearingVenue cmrHearingCentre;

    @CCD(
            label = "Hearing Centre address",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {SystemCrudAccess.class}
    )
    private String cmrHearingCentreAddress;

    @CCD(label = "Length", access = {SystemCrudAccess.class})
    private HoursMinutes cmrHearingLength;

    @CCD(label = "Date and time", access = {SystemCrudAccess.class})
    private LocalDateTime cmrHearingDate;

    @CCD(
            label = "What hearing channel type is required?",
            typeOverride = FieldType.DynamicRadioList,
            access = {SystemCrudAccess.class}
    )
    private String cmrHearingChannel;

    @CCD(label = "Will the hearing be held remotely?", access = {SystemCrudAccess.class})
    private YesOrNo cmrIsRemoteHearing;

    @CCD(label = "List of hearing IDs associated with case", access = {SystemCrudAccess.class})
    private List<ListValue<String>> cmrHearingIdList;
}
