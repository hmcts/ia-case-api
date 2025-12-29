package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HMC_REQUEST_SUCCESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CallbackApiDelegator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ServiceResponseException;

@Slf4j
@Service
public class IaHearingsApiService {

    private final String hearingsApiEndpoint;
    private final String aboutToStartPath;
    private final String midEventPath;
    private final String aboutToSubmitPath;

    CallbackApiDelegator callbackApiDelegator;

    public IaHearingsApiService(
        CallbackApiDelegator callbackApiDelegator,
        @Value("${hearingsApi.endpoint}") String hearingsApiEndpoint,
        @Value("${hearingsApi.aboutToStartPath}") String aboutToStartPath,
        @Value("${hearingsApi.midEventPath}") String midEventPath,
        @Value("${hearingsApi.aboutToSubmitPath}") String aboutToSubmitPath
    ) {
        this.callbackApiDelegator = callbackApiDelegator;
        this.hearingsApiEndpoint = hearingsApiEndpoint;
        this.aboutToStartPath = aboutToStartPath;
        this.midEventPath = midEventPath;
        this.aboutToSubmitPath = aboutToSubmitPath;
    }

    public AsylumCase aboutToStart(Callback<AsylumCase> callback) {
        return callbackApiDelegator.delegate(callback, hearingsApiEndpoint + aboutToStartPath);
    }

    public AsylumCase midEvent(Callback<AsylumCase> callback) {
        return callbackApiDelegator.delegate(callback, hearingsApiEndpoint + midEventPath);
    }

    public AsylumCase aboutToSubmit(Callback<AsylumCase> callback) {
        return callbackApiDelegator.delegate(callback, hearingsApiEndpoint + aboutToSubmitPath);
    }

    public AsylumCase updateHearing(Callback<AsylumCase> callback) {

        callback.getCaseDetails().getCaseData().clear(UPDATE_HMC_REQUEST_SUCCESS);
        callback.getCaseDetails().getCaseData().clear(MANUAL_UPDATE_HEARING_REQUIRED);
        AsylumCase asylumCase;

        try {

            asylumCase = callbackApiDelegator.delegate(
                callback, hearingsApiEndpoint + aboutToSubmitPath);

            if (asylumCase.read(UPDATE_HMC_REQUEST_SUCCESS, YesOrNo.class).isEmpty()) {
                asylumCase.write(UPDATE_HMC_REQUEST_SUCCESS, YES);
            }

        } catch (ServiceResponseException e) {

            log.error("Failed to update hearing for case ID {} during event {} with error: {}",
                callback.getCaseDetails().getId(),
                callback.getEvent().toString(),
                e.getMessage());

            asylumCase = callback.getCaseDetails().getCaseData();
            asylumCase.write(UPDATE_HMC_REQUEST_SUCCESS, NO);
            asylumCase.write(MANUAL_UPDATE_HEARING_REQUIRED, YES);
        }

        return asylumCase;
    }

    public AsylumCase deleteHearing(Callback<AsylumCase> callback) {

        callback.getCaseDetails().getCaseData().clear(MANUAL_CANCEL_HEARINGS_REQUIRED);
        AsylumCase asylumCase;
        try {

            asylumCase = callbackApiDelegator.delegate(
                callback, hearingsApiEndpoint + aboutToSubmitPath);

            if (asylumCase.read(MANUAL_CANCEL_HEARINGS_REQUIRED, YesOrNo.class).isEmpty()) {
                asylumCase.write(MANUAL_CANCEL_HEARINGS_REQUIRED, NO);
            }

        } catch (ServiceResponseException e) {

            log.error("Failed to cancel hearing for case ID {} during event {} with error: {}",
                callback.getCaseDetails().getId(),
                callback.getEvent().toString(),
                e.getMessage());

            asylumCase = callback.getCaseDetails().getCaseData();
            asylumCase.write(MANUAL_CANCEL_HEARINGS_REQUIRED, YES);

        }

        return asylumCase;
    }

    public AsylumCase createHearing(Callback<AsylumCase> callback) {

        callback.getCaseDetails().getCaseData().clear(MANUAL_CREATE_HEARING_REQUIRED);
        AsylumCase asylumCase;
        try {

            asylumCase = callbackApiDelegator.delegate(
                callback, hearingsApiEndpoint + aboutToSubmitPath);

            if (asylumCase.read(MANUAL_CREATE_HEARING_REQUIRED, YesOrNo.class).isEmpty()) {
                asylumCase.write(MANUAL_CREATE_HEARING_REQUIRED, NO);
            }

        } catch (ServiceResponseException e) {

            log.error("Failed to auto create hearing for case ID {} during event {} with error: {}",
                callback.getCaseDetails().getId(),
                callback.getEvent().toString(),
                e.getMessage());

            asylumCase = callback.getCaseDetails().getCaseData();
            asylumCase.write(MANUAL_CREATE_HEARING_REQUIRED, YES);

        }

        return asylumCase;
    }
}
