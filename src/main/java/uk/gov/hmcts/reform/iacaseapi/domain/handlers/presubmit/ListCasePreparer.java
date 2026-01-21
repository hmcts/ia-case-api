package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isCaseUsingLocationRefData;

import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@Component
public class ListCasePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final LocationRefDataService locationRefDataService;

    public ListCasePreparer(FeatureToggler featureToggler, LocationRefDataService locationRefDataService) {
        this.featureToggler = featureToggler;
        this.locationRefDataService = locationRefDataService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.LIST_CASE
                && ((callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)
                        || (callbackStage == PreSubmitCallbackStage.MID_EVENT && callback.getPageId().equals("listCaseHearingFinalPage")));
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

        if (asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
            && asylumCase.read(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.NO)).orElse(true)) {

            final PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("You've made an invalid request. You cannot list the case until the hearing requirements have been reviewed.");
            return asylumCasePreSubmitCallbackResponse;
        }

        if (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
            && featureToggler.getValue("reheard-feature", false)) {
            asylumCase.clear(LIST_CASE_HEARING_CENTRE);
            asylumCase.clear(LIST_CASE_HEARING_CENTRE_ADDRESS);
            asylumCase.clear(LIST_CASE_HEARING_DATE);
            asylumCase.clear(LIST_CASE_HEARING_LENGTH);
            asylumCase.clear(LISTING_LENGTH);
        } else {
            Optional<HearingCentre> hearingCentreOptional =
                asylumCase.read(HEARING_CENTRE);
            hearingCentreOptional.ifPresent(hearingCentre -> {
                asylumCase.write(
                    LIST_CASE_HEARING_CENTRE,
                    hearingCentre == HearingCentre.GLASGOW ? HearingCentre.GLASGOW_TRIBUNALS_CENTRE : hearingCentre);
            });
        }

        if (isCaseUsingLocationRefData(asylumCase)) {
            asylumCase.write(LISTING_LOCATION, prepareLocationDynamicList(asylumCase));
        }

        boolean hasTransferredOutOfAda = asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)
            .map(field -> field.equals(YesOrNo.YES))
            .orElse(false);

        boolean listCaseWasTriggeredInAdaJourney = asylumCase.read(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.class)
            .map(field -> field.equals(YesOrNo.YES))
            .orElse(false);

        if (callback.getEvent().equals(Event.LIST_CASE)
            && hasTransferredOutOfAda
            && listCaseWasTriggeredInAdaJourney) {
            // direct user to use EDIT_CASE_LISTING instead of LIST_CASE if the appeal was transferred out of ADA after listing

            PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
            response.addError("Case was listed before being transferred out of ADA. Edit case listing instead.");
            return response;
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private DynamicList prepareLocationDynamicList(AsylumCase asylumCase) {
        DynamicList dynamicList = locationRefDataService.getHearingLocationsDynamicList();

        asylumCase.read(LISTING_LOCATION, DynamicList.class)
            .ifPresentOrElse(existingDynamicList -> {

                // find selected value if selection had already been made before
                Value selectedLocation = existingDynamicList.getValue();

                // set the value in the new list, as long as that the LoV still includes it
                if (dynamicList.getListItems().contains(selectedLocation)) {
                    dynamicList.setValue(selectedLocation);
                }
            },
                // if listingLocation empty, read selection from listCaseHearingCentre
                () -> asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                    .flatMap(hearingCentre -> dynamicList.getListItems()
                        .stream() // find selected epimsId in new dynamic list
                        .filter(value -> Objects.equals(value.getCode(), hearingCentre.getEpimsId()))
                        .findAny())
                    .ifPresent(dynamicList::setValue));

        return dynamicList;
    }
}
