package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE_PREVIOUS_SELECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK_PREVIOUS_SELECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NATIONALITIES_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_OUT_OF_COUNTRY_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_STATELESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_WITH_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RP_DC_APPEAL_HEARING_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEARCH_POSTCODE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Slf4j
@Component
public class AppealOutOfCountryEditAppealAipHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public AppealOutOfCountryEditAppealAipHandler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Optional<JourneyType> journeyTypeOptional =
            callback.getCaseDetails()
                .getCaseData()
                .read(JOURNEY_TYPE);

        boolean isAipJourney =
            journeyTypeOptional
                .map(journeyType -> journeyType == JourneyType.AIP)
                .orElse(false);

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.EDIT_APPEAL || callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT)
               && featureToggler.getValue("aip-ooc-feature", false) && isAipJourney;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final long caseId = callback.getCaseDetails().getId();
        boolean holdFieldsForInUkChange = true;
        boolean holdFieldsForAppealTypeChange = true;

        Optional<YesOrNo> optionalAppellantInUk =
            asylumCase.read(APPELLANT_IN_UK, YesOrNo.class);

        Optional<AppealType> optionalAppealType =
            asylumCase.read(APPEAL_TYPE, AppealType.class);

        if (optionalAppellantInUk.isPresent()) {
            Optional<YesOrNo> optionalAppellantInUkPreviousSelection = Optional.of(
                asylumCase.read(APPELLANT_IN_UK_PREVIOUS_SELECTION, YesOrNo.class)
                    .orElse(optionalAppellantInUk.get()));

            asylumCase.write(APPELLANT_IN_UK_PREVIOUS_SELECTION, optionalAppellantInUkPreviousSelection);
            holdFieldsForInUkChange = optionalAppellantInUk.get().equals(optionalAppellantInUkPreviousSelection.get());
        }

        if (optionalAppealType.isPresent()) {
            Optional<AppealType> optionalAppealTypePreviousSelection = Optional.of(
                asylumCase.read(APPEAL_TYPE_PREVIOUS_SELECTION, AppealType.class)
                    .orElse(optionalAppealType.get()));

            asylumCase.write(APPEAL_TYPE_PREVIOUS_SELECTION, optionalAppealTypePreviousSelection);
            holdFieldsForAppealTypeChange = optionalAppealType.get().equals(optionalAppealTypePreviousSelection.get());
        }

        updatePreviousInUkAndAppealTypeSelections(
            asylumCase,
            holdFieldsForInUkChange,
            holdFieldsForAppealTypeChange,
            optionalAppellantInUk,
            optionalAppealType
        );

        verifyInUkChange(
            optionalAppellantInUk,
            asylumCase,
            holdFieldsForInUkChange,
            caseId
        );

        if (optionalAppealType.isPresent() && !holdFieldsForAppealTypeChange) {
            log.info("Clearing fields for Appeal Type change for AIP case Id [{}]", caseId);
            clearAipFieldsForAppealType(asylumCase, false);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void updatePreviousInUkAndAppealTypeSelections(
        AsylumCase asylumCase,
        boolean holdFieldsForInUkChange,
        boolean holdFieldsForAppealTypeChange,
        Optional<YesOrNo> optionalAppellantInUk,
        Optional<AppealType> optionalAppealType
    ) {
        if (!holdFieldsForInUkChange) {
            asylumCase.write(APPELLANT_IN_UK_PREVIOUS_SELECTION, optionalAppellantInUk);
        }
        if (!holdFieldsForAppealTypeChange) {
            asylumCase.write(APPEAL_TYPE_PREVIOUS_SELECTION, optionalAppealType);
        }
    }

    private void verifyInUkChange(
        Optional<YesOrNo> optionalAppellantInUk,
        AsylumCase asylumCase,
        boolean holdFieldsForInUkChange,
        long caseId
    ) {
        if (optionalAppellantInUk.isPresent()) {
            YesOrNo appellantInUk = optionalAppellantInUk.get();

            if (appellantInUk.equals(YES)) {
                asylumCase.write(APPEAL_OUT_OF_COUNTRY, NO);
                if (!holdFieldsForInUkChange) {
                    log.info("Clearing Out Of Country fields for an In Country AIP Appeal with case Id [{}]", caseId);
                    clearAipFieldsForAppealType(asylumCase, true);
                }
            }

            if (appellantInUk.equals(NO)) {
                asylumCase.write(APPEAL_OUT_OF_COUNTRY, YES);
                if (!holdFieldsForInUkChange) {
                    log.info("Clearing In Country fields for Out Of Country AIP Appeal with case Id [{}]", caseId);
                    clearAipFieldsForAppealType(asylumCase, true);
                }
            }
        }
    }

    private void clearAipFieldsForAppealType(AsylumCase asylumCase, boolean clearAppealType) {
        if (clearAppealType) {
            clearAipAppealTypeDetails(asylumCase);
        }
        clearAipHomeOfficeDetails(asylumCase);
        clearAipPersonalDetails(asylumCase);
        clearAipContactDetails(asylumCase);
        clearAipDecisionType(asylumCase);
    }

    private void clearAipAppealTypeDetails(AsylumCase asylumCase) {
        asylumCase.clear(APPEAL_TYPE);
    }

    private void clearAipHomeOfficeDetails(AsylumCase asylumCase) {
        asylumCase.clear(HOME_OFFICE_REFERENCE_NUMBER);
        asylumCase.clear(HOME_OFFICE_DECISION_DATE);
        asylumCase.clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);
        asylumCase.clear(UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION);
    }

    private void clearAipPersonalDetails(AsylumCase asylumCase) {
        asylumCase.clear(APPELLANT_GIVEN_NAMES);
        asylumCase.clear(APPELLANT_FAMILY_NAME);
        asylumCase.clear(APPELLANT_DATE_OF_BIRTH);
        asylumCase.clear(APPELLANT_NATIONALITIES);
        asylumCase.clear(APPELLANT_NATIONALITIES_DESCRIPTION);
        asylumCase.clear(APPELLANT_STATELESS);
        asylumCase.clear(APPELLANT_HAS_FIXED_ADDRESS);
        asylumCase.clear(APPELLANT_ADDRESS);
        asylumCase.clear(SEARCH_POSTCODE);
        asylumCase.clear(APPELLANT_OUT_OF_COUNTRY_ADDRESS);
    }

    private void clearAipContactDetails(AsylumCase asylumCase) {
        asylumCase.clear(SUBSCRIPTIONS);
    }

    private void clearAipDecisionType(AsylumCase asylumCase) {
        asylumCase.clear(RP_DC_APPEAL_HEARING_OPTION);
        asylumCase.clear(DECISION_WITH_HEARING);
        asylumCase.clear(DECISION_WITHOUT_HEARING);
    }
}
