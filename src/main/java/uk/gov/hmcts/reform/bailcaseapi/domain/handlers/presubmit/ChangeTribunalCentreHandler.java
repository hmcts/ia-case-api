package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.Optional;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.bailcaseapi.domain.utils.HearingCentreUtils;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;

@Component
@RequiredArgsConstructor
public class ChangeTribunalCentreHandler implements PreSubmitCallbackHandler<BailCase> {

    private final LocationRefDataService locationRefDataService;
    private final CaseManagementLocationService caseManagementLocationService;

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == Event.CHANGE_TRIBUNAL_CENTRE);
    }

    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage,
                                                      Callback<BailCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        Optional<HearingCentre> designatedTribunalCentre = bailCase.read(
            DESIGNATED_TRIBUNAL_CENTRE, HearingCentre.class);
        PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);

        if (designatedTribunalCentre.isPresent()) {
            HearingCentreUtils.setHearingCentre(
                bailCase,
                designatedTribunalCentre.get(),
                caseManagementLocationService,
                locationRefDataService
            );
        } else {
            response.addError("designatedTribunalCentre cannot be empty");
        }

        return response;
    }
}
