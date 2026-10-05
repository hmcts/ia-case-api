package uk.gov.hmcts.reform.iacaseapi.ccd.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppealGroundsDeprivationPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppealGroundsEuRefusalPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppealGroundsHumanRightsRefusalPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppealGroundsProtectionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppealGroundsRevocationPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppealReferenceNumberPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppealTypePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppealWasNotSubmittedMyHmCtsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppellantAddressAdminJPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppellantAddressPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppellantBailApplicationPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppellantBasicDetailsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppellantContactPreferencePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppellantNationalitiesPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealAppellantsRepresentationPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealBeforeYouStartPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealBeforeYouStartRehydrateAppealPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealChecklistPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealCustodialSentencePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealDepartureDateAdminJPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealDepartureDatePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealDeportationOrderPagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealDetentionFacilityPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealDetentionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealEntryClearanceDecisionLetterPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealExceptionalCircumstancesRemissionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealHasOtherAppealsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealHearingFeeDecisionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealHelpWithFeesPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealHomeOfficeDecisionLetterPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealHomeOfficeReferenceNumberPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealHomeOfficeWaiverPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealInternalContactDetailsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealIrcNamePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealLegalAidPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealLegalRepresentativeAddressPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealLegalRepresentativeDetailsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealLegalRepresentativeDetailsPaperJPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealNewMattersPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealOocAppellantAddressPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealOocHomeOfficeReferenceNumberPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealOtherAppealsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealOutOfCountryAppealAdminJPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealOutOfCountryDecisionTypePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealOutOfCountryPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealPaymentOptionsPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealPrisonNamePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealRemissionAsylumSupportPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealRemissionClaimPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealRemissionTypePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealRemovalDirectionsPagePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealRpDCAppealHearingOptionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealSection17Page;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealSection20Page;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealSponsorAddressPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealSponsorAuthorisationPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealSponsorContactPreferencePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealSponsorNamePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealSponsorPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealSubmissionOutOfTimePage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealTribunalReceivedAppealPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealUploadAppealFormPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.event.page.StartAppealUploadTheNoticeOfDecisionPage;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.CaseData;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.State;
import uk.gov.hmcts.reform.iacaseapi.ccd.model.UserRole;

/**
 * Generated by ccd-definition-converter — do not edit by hand.
 *
 * <p>Event {@code startAppeal} for case type {@code Asylum}.
 */
@Component
public class StartAppeal implements CCDConfig<CaseData, State, UserRole> {
    /**
     * The CCD event ID.
     */
    public static final String START_APPEAL = "startAppeal";

    /**
     * Registers event {@code startAppeal}.
     *
     * @param builder the config builder
     */
    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> builder) {
        var fields = builder.event(START_APPEAL)
            .initialState(State.appealStartedByAdmin)
            .name("Start the appeal")
            .displayOrder(1)
            .showSummary()
            .publishToCamunda()
            .ttlIncrement(90)
            .explicitGrants()
            .grant(Permission.CRUD, UserRole.CASEWORKER_IA_ADMOFFICER, UserRole.CASEWORKER_IA_LEGALREP_SOLICITOR, UserRole.CITIZEN)
            .fields();
        StartAppealBeforeYouStartPage.apply(fields);
        StartAppealBeforeYouStartRehydrateAppealPage.apply(fields);
        StartAppealAppealReferenceNumberPage.apply(fields);
        StartAppealTribunalReceivedAppealPage.apply(fields);
        StartAppealSubmissionOutOfTimePage.apply(fields);
        StartAppealAppellantsRepresentationPage.apply(fields);
        StartAppealAppealWasNotSubmittedMyHmCtsPage.apply(fields);
        StartAppealLegalRepresentativeDetailsPaperJPage.apply(fields);
        StartAppealLegalRepresentativeAddressPage.apply(fields);
        StartAppealChecklistPage.apply(fields);
        StartAppealOutOfCountryPage.apply(fields);
        StartAppealDetentionPage.apply(fields);
        StartAppealDetentionFacilityPage.apply(fields);
        StartAppealIrcNamePage.apply(fields);
        StartAppealPrisonNamePage.apply(fields);
        StartAppealCustodialSentencePage.apply(fields);
        StartAppealAppellantBailApplicationPage.apply(fields);
        StartAppealOutOfCountryDecisionTypePage.apply(fields);
        StartAppealOutOfCountryAppealAdminJPage.apply(fields);
        StartAppealDepartureDateAdminJPage.apply(fields);
        StartAppealDepartureDatePage.apply(fields);
        StartAppealHomeOfficeReferenceNumberPage.apply(fields);
        StartAppealOocHomeOfficeReferenceNumberPage.apply(fields);
        StartAppealAppellantBasicDetailsPage.apply(fields);
        StartAppealAppellantNationalitiesPage.apply(fields);
        StartAppealAppellantAddressPage.apply(fields);
        StartAppealOocAppellantAddressPage.apply(fields);
        StartAppealAppellantAddressAdminJPage.apply(fields);
        StartAppealAppellantContactPreferencePage.apply(fields);
        StartAppealInternalContactDetailsPage.apply(fields);
        StartAppealAppealTypePage.apply(fields);
        StartAppealAppealGroundsEuRefusalPage.apply(fields);
        StartAppealAppealGroundsHumanRightsRefusalPage.apply(fields);
        StartAppealAppealGroundsDeprivationPage.apply(fields);
        StartAppealAppealGroundsProtectionPage.apply(fields);
        StartAppealAppealGroundsRevocationPage.apply(fields);
        StartAppealHomeOfficeDecisionLetterPage.apply(fields);
        StartAppealEntryClearanceDecisionLetterPage.apply(fields);
        StartAppealUploadTheNoticeOfDecisionPage.apply(fields);
        StartAppealSponsorPage.apply(fields);
        StartAppealSponsorNamePage.apply(fields);
        StartAppealSponsorAddressPage.apply(fields);
        StartAppealSponsorContactPreferencePage.apply(fields);
        StartAppealSponsorAuthorisationPage.apply(fields);
        StartAppealDeportationOrderPagePage.apply(fields);
        StartAppealRemovalDirectionsPagePage.apply(fields);
        StartAppealNewMattersPage.apply(fields);
        StartAppealHasOtherAppealsPage.apply(fields);
        StartAppealOtherAppealsPage.apply(fields);
        StartAppealLegalRepresentativeDetailsPage.apply(fields);
        StartAppealRpDCAppealHearingOptionPage.apply(fields);
        StartAppealHearingFeeDecisionPage.apply(fields);
        StartAppealRemissionTypePage.apply(fields);
        StartAppealRemissionClaimPage.apply(fields);
        StartAppealRemissionAsylumSupportPage.apply(fields);
        StartAppealLegalAidPage.apply(fields);
        StartAppealSection17Page.apply(fields);
        StartAppealSection20Page.apply(fields);
        StartAppealHomeOfficeWaiverPage.apply(fields);
        StartAppealHelpWithFeesPage.apply(fields);
        StartAppealExceptionalCircumstancesRemissionPage.apply(fields);
        StartAppealPaymentOptionsPage.apply(fields);
        StartAppealUploadAppealFormPage.apply(fields);
    }
}
