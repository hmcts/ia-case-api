package uk.gov.hmcts.reform.iacaseapi.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAdminJSponsorContactPreferencePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppealGroundsDeprivationPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppealGroundsEuRefusalPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppealGroundsHumanRightsRefusalPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppealGroundsProtectionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppealGroundsRevocationPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppealReferenceNumberPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppealTypePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppealWasNotSubmittedMyHmCtsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppellantAddressAdminJPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppellantAddressPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppellantBailApplicationPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppellantBasicDetailsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppellantContactPreferencePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppellantNationalitiesPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealAppellantsRepresentationPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealChecklistPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealCuiAppellantDobPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealCuiAppellantNamePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealCuiGwfReferenceNumberPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealCuiHomeOfficeReferenceNumberPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealCustodialSentencePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealDepartureDateAdminJPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealDepartureDatePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealDeportationOrderPagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealDetentionFacilityPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealDetentionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealEntryClearanceDecisionLetterPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealExceptionalCircumstancesRemissionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealHasOtherAppealsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealHearingFeeDecisionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealHelpWithFeesPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealHomeOfficeDecisionLetterPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealHomeOfficeReferenceNumberPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealHomeOfficeWaiverPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealInternalContactDetailsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealIrcNamePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealLegalAidPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealLegalRepresentativeAddressPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealLegalRepresentativeDetailsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealLegalRepresentativeDetailsPaperJPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealNewMattersPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealOocAppellantAddressPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealOocHomeOfficeReferenceNumberPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealOtherAppealsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealOutOfCountryAppealAdminJPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealOutOfCountryDecisionTypePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealOutOfCountryPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealPaymentOptionsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealPrisonNamePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealRemissionAsylumSupportPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealRemissionClaimPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealRemissionTypePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealRemovalDirectionsPagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealRpDCAppealHearingOptionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealSection17Page;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealSection20Page;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealSponsorAddressPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealSponsorAuthorisationPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealSponsorContactPreferencePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealSponsorNamePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealSponsorPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealSubmissionOutOfTimePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealTribunalReceivedAppealPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealUploadAppealFormPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.EditAppealUploadTheNoticeOfDecisionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.State;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.UserRole;

/**
 * Generated by ccd-definition-converter — do not edit by hand.
 *
 * <p>Event {@code editAppeal} for case type {@code Asylum}.
 */
@Component
public class EditAppeal implements CCDConfig<CaseData, State, UserRole> {
    /**
     * The CCD event ID.
     */
    public static final String EDIT_APPEAL = "editAppeal";

    /**
     * Registers event {@code editAppeal}.
     *
     * @param builder the config builder
     */
    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> builder) {
        var fields = builder.event(EDIT_APPEAL)
            .forStates(State.appealStarted, State.appealStartedByAdmin)
            .name("Edit appeal")
            .displayOrder(2)
            .showSummary()
            .ttlIncrement(90)
            .explicitGrants()
            .grant(Permission.CRUD, UserRole.CASEWORKER_IA_ADMOFFICER, UserRole.CASEWORKER_IA_LEGALREP_SOLICITOR, UserRole.CITIZEN)
            .fields();
        EditAppealAppealReferenceNumberPage.apply(fields);
        EditAppealTribunalReceivedAppealPage.apply(fields);
        EditAppealSubmissionOutOfTimePage.apply(fields);
        EditAppealAppellantsRepresentationPage.apply(fields);
        EditAppealAppealWasNotSubmittedMyHmCtsPage.apply(fields);
        EditAppealLegalRepresentativeDetailsPaperJPage.apply(fields);
        EditAppealLegalRepresentativeAddressPage.apply(fields);
        EditAppealChecklistPage.apply(fields);
        EditAppealOutOfCountryPage.apply(fields);
        EditAppealDetentionPage.apply(fields);
        EditAppealDetentionFacilityPage.apply(fields);
        EditAppealIrcNamePage.apply(fields);
        EditAppealPrisonNamePage.apply(fields);
        EditAppealCustodialSentencePage.apply(fields);
        EditAppealAppellantBailApplicationPage.apply(fields);
        EditAppealOutOfCountryDecisionTypePage.apply(fields);
        EditAppealOutOfCountryAppealAdminJPage.apply(fields);
        EditAppealDepartureDateAdminJPage.apply(fields);
        EditAppealDepartureDatePage.apply(fields);
        EditAppealHomeOfficeReferenceNumberPage.apply(fields);
        EditAppealCuiHomeOfficeReferenceNumberPage.apply(fields);
        EditAppealOocHomeOfficeReferenceNumberPage.apply(fields);
        EditAppealCuiGwfReferenceNumberPage.apply(fields);
        EditAppealAppellantBasicDetailsPage.apply(fields);
        EditAppealCuiAppellantNamePage.apply(fields);
        EditAppealCuiAppellantDobPage.apply(fields);
        EditAppealAppellantNationalitiesPage.apply(fields);
        EditAppealAppellantAddressPage.apply(fields);
        EditAppealOocAppellantAddressPage.apply(fields);
        EditAppealAppellantAddressAdminJPage.apply(fields);
        EditAppealAppellantContactPreferencePage.apply(fields);
        EditAppealInternalContactDetailsPage.apply(fields);
        EditAppealAppealTypePage.apply(fields);
        EditAppealAppealGroundsEuRefusalPage.apply(fields);
        EditAppealAppealGroundsHumanRightsRefusalPage.apply(fields);
        EditAppealAppealGroundsDeprivationPage.apply(fields);
        EditAppealAppealGroundsProtectionPage.apply(fields);
        EditAppealAppealGroundsRevocationPage.apply(fields);
        EditAppealHomeOfficeDecisionLetterPage.apply(fields);
        EditAppealEntryClearanceDecisionLetterPage.apply(fields);
        EditAppealUploadTheNoticeOfDecisionPage.apply(fields);
        EditAppealSponsorPage.apply(fields);
        EditAppealSponsorNamePage.apply(fields);
        EditAppealSponsorAddressPage.apply(fields);
        EditAppealSponsorContactPreferencePage.apply(fields);
        EditAppealAdminJSponsorContactPreferencePage.apply(fields);
        EditAppealSponsorAuthorisationPage.apply(fields);
        EditAppealDeportationOrderPagePage.apply(fields);
        EditAppealRemovalDirectionsPagePage.apply(fields);
        EditAppealNewMattersPage.apply(fields);
        EditAppealHasOtherAppealsPage.apply(fields);
        EditAppealOtherAppealsPage.apply(fields);
        EditAppealLegalRepresentativeDetailsPage.apply(fields);
        EditAppealRpDCAppealHearingOptionPage.apply(fields);
        EditAppealHearingFeeDecisionPage.apply(fields);
        EditAppealRemissionTypePage.apply(fields);
        EditAppealRemissionClaimPage.apply(fields);
        EditAppealRemissionAsylumSupportPage.apply(fields);
        EditAppealLegalAidPage.apply(fields);
        EditAppealSection17Page.apply(fields);
        EditAppealSection20Page.apply(fields);
        EditAppealHomeOfficeWaiverPage.apply(fields);
        EditAppealHelpWithFeesPage.apply(fields);
        EditAppealExceptionalCircumstancesRemissionPage.apply(fields);
        EditAppealPaymentOptionsPage.apply(fields);
        EditAppealUploadAppealFormPage.apply(fields);
    }
}
