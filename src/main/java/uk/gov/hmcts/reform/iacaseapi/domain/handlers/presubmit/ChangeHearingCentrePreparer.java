package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE_DYNAMIC_LIST;

@Component
@RequiredArgsConstructor
public class ChangeHearingCentrePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final LocationRefDataService locationRefDataService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && callback.getEvent() == Event.CHANGE_HEARING_CENTRE;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (HandlerUtils.isCaseUsingLocationRefData(asylumCase)) {

            Optional<DynamicList> optionalHearingCentreDynamicList =
                    asylumCase.read(HEARING_CENTRE_DYNAMIC_LIST, DynamicList.class);

            DynamicList refDataHearingCentreDynamicList
                    = locationRefDataService.getCaseManagementLocationDynamicList();

            optionalHearingCentreDynamicList.ifPresent(
                    dynamicList -> refDataHearingCentreDynamicList.setValue(dynamicList.getValue()));

            asylumCase.write(HEARING_CENTRE_DYNAMIC_LIST, refDataHearingCentreDynamicList);

        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
