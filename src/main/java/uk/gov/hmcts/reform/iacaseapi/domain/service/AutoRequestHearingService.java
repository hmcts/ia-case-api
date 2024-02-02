package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HMC_REQUEST_SUCCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
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

    public AsylumCase autoCreateHearing(Callback<AsylumCase> callback) {

        try {

            return iaHearingsApiService.aboutToSubmit(callback);

        } catch (AsylumCaseServiceResponseException e) {

            log.error("Failure in call to IA-HEARINGS-API for case ID {} during event {} with error: {}",
                callback.getCaseDetails().getId(),
                callback.getEvent().toString(),
                e.getMessage());

            AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
            asylumCase.write(MANUAL_CREATE_HEARING_REQUIRED, YES);

            return asylumCase;

        }
    }

    public AsylumCase autoUpdateHearing(Callback<AsylumCase> callback) {

        try {

            return iaHearingsApiService.aboutToSubmit(callback);

        } catch (AsylumCaseServiceResponseException e) {

            log.error("Failure in call to IA-HEARINGS-API for case ID {} during event {} with error: {}",
                callback.getCaseDetails().getId(),
                callback.getEvent().toString(),
                e.getMessage());

            AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
            asylumCase.write(UPDATE_HMC_REQUEST_SUCCESS, NO);

            return asylumCase;

        }
    }

    public AsylumCase autoCancelHearing(Callback<AsylumCase> callback) {

        try {

            return iaHearingsApiService.aboutToSubmit(callback);

        } catch (AsylumCaseServiceResponseException e) {

            log.error("Failure in call to IA-HEARINGS-API for case ID {} during event {} with error: {}",
                callback.getCaseDetails().getId(),
                callback.getEvent().toString(),
                e.getMessage());

            AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
            asylumCase.write(MANUAL_CANCEL_HEARINGS_REQUIRED, YES);

            return asylumCase;

        }
    }

    public PostSubmitCallbackResponse buildAutoHearingRequestConfirmation(
        AsylumCase asylumCase, String header, long caseId) {

        boolean hearingRequestSuccessful = asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class)
            .map(manualCreateRequired -> NO == manualCreateRequired)
            .orElse(false);

        PostSubmitCallbackResponse postSubmitResponse = new PostSubmitCallbackResponse();

        if (hearingRequestSuccessful) {
            String body = "#### What happens next\n\n"
                          + "The hearing request has been created and is visible on the [Hearings tab]"
                          + "(/cases/case-details/" + caseId + "/hearings)";
            postSubmitResponse.setConfirmationHeader(header);
            postSubmitResponse.setConfirmationBody(body);
        } else {
            String body = "![Hearing could not be listed](https://raw.githubusercontent.com/hmcts/"
                + "ia-appeal-frontend/master/app/assets/images/hearingCouldNotBeListed.png)"
                + "\n\n"
                + "#### What happens next\n\n"
                + "The hearing could not be auto-requested. Please manually request the "
                + "hearing via the [Hearings tab](/cases/case-details/" + caseId + "/hearings)";

            postSubmitResponse.setConfirmationHeader("");
            postSubmitResponse.setConfirmationBody(body);
        }

        return postSubmitResponse;
    }
}
