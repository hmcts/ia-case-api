package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggleService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@Component
@RequiredArgsConstructor
public class DeriveHearingCentreHandler implements PreSubmitCallbackHandler<BailCase> {

    private final HearingCentreFinder hearingCentreFinder;
    private final LocationRefDataService locationRefDataService;
    private final FeatureToggleService featureToggleService;


    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.START_APPLICATION
                   || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
                   || callback.getEvent() == Event.MAKE_NEW_APPLICATION
                   || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT);
    }

    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        setHearingCentreFromDetentionFacilityName(bailCase);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    public void setHearingCentreFromDetentionFacilityName(BailCase bailCase) {
        final String prisonName = bailCase.read(PRISON_NAME, String.class).orElse("");

        final String ircName = bailCase.read(IRC_NAME, String.class).orElse("");

        if (prisonName.isEmpty() && ircName.isEmpty()) {
            throw new RequiredFieldMissingException("Prison name and IRC name missing");

        } else {
            String detentionFacilityName = !prisonName.isEmpty() ? prisonName : ircName;

            HearingCentre hearingCentre = hearingCentreFinder.findByDetentionFacility(detentionFacilityName);
            bailCase.write(HEARING_CENTRE, hearingCentre);

            if (locationRefDataEnabled(bailCase)) {

                setHearingCentreRefData(bailCase, hearingCentre);
                bailCase.write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YES);
            } else {

                bailCase.write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, NO);
            }
            bailCase.write(DESIGNATED_TRIBUNAL_CENTRE, hearingCentre);
        }

    }

    private void setHearingCentreRefData(BailCase bailCase, HearingCentre hearingCentre) {

        DynamicList locationRefDataDynamicList = locationRefDataService.getCaseManagementLocationsDynamicList();
        Value currentHearingCentreValue = locationRefDataDynamicList.getListItems().stream().filter(value -> Objects.equals(
            value.getCode(),
            hearingCentre.getEpimsId()
        )).findFirst().orElse(null);

        if (currentHearingCentreValue != null) {
            DynamicList hearingCentreRefData = new DynamicList(
                currentHearingCentreValue, locationRefDataDynamicList.getListItems());

            //the value of this case field is used for searching tribunal centre
            bailCase.write(SELECTED_HEARING_CENTRE_REF_DATA, currentHearingCentreValue.getLabel());

            bailCase.write(HEARING_CENTRE_REF_DATA, hearingCentreRefData);
        }
    }

    private boolean locationRefDataEnabled(BailCase bailCase) {

        return bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED_FT, YesOrNo.class)
            .map(enabled -> YES == enabled)
            .orElseGet(featureToggleService::locationRefDataEnabled);
    }

}
