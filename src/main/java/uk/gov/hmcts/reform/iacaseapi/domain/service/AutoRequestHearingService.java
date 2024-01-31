package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseServiceResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoRequestHearingService {

    private final IaHearingsApiService iaHearingsApiService;
    private final LocationBasedFeatureToggler locationBasedFeatureToggler;

    public boolean shouldAutoRequestHearing(AsylumCase asylumCase) {

        return HandlerUtils.isIntegrated(asylumCase)
               && (locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase) == YES);
    }

    public boolean shouldAutoRequestHearing(AsylumCase asylumCase, boolean canAutoRequest) {

        return shouldAutoRequestHearing(asylumCase) && canAutoRequest;
    }

    public AsylumCase makeAutoHearingRequest(Callback<AsylumCase> callback, AsylumCaseFieldDefinition requestStatusField) {

        try {

            return iaHearingsApiService.aboutToSubmit(callback);

        } catch (AsylumCaseServiceResponseException e) {

            log.error("Failure in call to IA-HEARINGS-API for case ID {} during event {} with error: {}",
                callback.getCaseDetails().getId(),
                callback.getEvent().toString(),
                e.getMessage());

            AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
            asylumCase.write(requestStatusField, YES);

            return asylumCase;

        }
    }

    public Map<String, String> buildAutoHearingRequestConfirmation(
        AsylumCase asylumCase, long caseId) {

        boolean hearingRequestSuccessful = asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)
            .map(manualCreateRequired -> NO == manualCreateRequired)
            .orElse(false);

        Map<String, String> confirmation = new HashMap<>();

        if (hearingRequestSuccessful) {
            String body = "#### What happens next\n\n"
                          + "The hearing request has been created and is visible on the [Hearings tab]"
                          + "(/cases/case-details/" + caseId + "/hearings)";
            confirmation.put("header", "# Hearing listed");
            confirmation.put("body", body);
        } else {
            String body = "![Hearing could not be listed](https://raw.githubusercontent.com/hmcts/"
                + "ia-appeal-frontend/master/app/assets/images/hearingCouldNotBeListed.png)"
                + "\n\n"
                + "#### What happens next\n\n"
                + "The hearing could not be auto-requested. Please manually request the "
                + "hearing via the [Hearings tab](/cases/case-details/" + caseId + "/hearings)";

            confirmation.put("header", "");
            confirmation.put("body", body);
        }

        return confirmation;
    }
}
