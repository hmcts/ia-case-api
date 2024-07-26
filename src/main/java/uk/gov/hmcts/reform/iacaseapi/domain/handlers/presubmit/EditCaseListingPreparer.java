package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isCaseUsingLocationRefData;

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@Component
public class EditCaseListingPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final LocationRefDataService locationRefDataService;

    public EditCaseListingPreparer(LocationRefDataService locationRefDataService) {
        this.locationRefDataService = locationRefDataService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == EDIT_CASE_LISTING;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (isCaseUsingLocationRefData(asylumCase)) {
            asylumCase.write(LISTING_LOCATION, prepareLocationDynamicList(asylumCase));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private DynamicList prepareLocationDynamicList(AsylumCase asylumCase) {

        String existingSelectionEpimmsId = asylumCase.read(LISTING_LOCATION, DynamicList.class)
            .map(location -> location.getValue().getCode())
            .orElseGet(() -> asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .map(HearingCentre::getEpimsId).orElse(""));

        List<Value> refDataLocations = locationRefDataService.getHearingLocationsDynamicList().getListItems();

        Value existingSelectionValue = refDataLocations.stream()
            .filter(location -> Objects.equals(location.getCode(), existingSelectionEpimmsId))
            .findAny().orElse(new Value("", ""));

        return new DynamicList(existingSelectionValue, refDataLocations);
    }
}
