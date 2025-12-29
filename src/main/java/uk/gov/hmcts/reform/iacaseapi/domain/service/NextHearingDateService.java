package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ServiceResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class NextHearingDateService {

    private final IaHearingsApiService iaHearingsApiService;
    private final FeatureToggler featureToggler;

    public boolean enabled() {
        return featureToggler.getValue("nextHearingDateEnabled", false);
    }

    public NextHearingDetails calculateNextHearingDateFromHearings(
            Callback<AsylumCase> callback,
            PreSubmitCallbackStage callbackStage) {
        NextHearingDetails nextHearingDetails = null;

        try {
            AsylumCase asylumCase = callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                    ? iaHearingsApiService.aboutToStart(callback)
                    : iaHearingsApiService.aboutToSubmit(callback);
            nextHearingDetails = asylumCase.read(NEXT_HEARING_DETAILS, NextHearingDetails.class)
                .orElse(null);
        } catch (ServiceResponseException e) {
            log.error("Setting next hearing date from hearings failed: ", e);
        }

        long caseId = callback.getCaseDetails().getId();
        if (nextHearingDetails == null) {
            log.error("Failed to calculate Next hearing date from hearings for case ID {}", caseId);
            return calculateNextHearingDateFromCaseData(callback);
        } else {
            log.info("Next hearing date successfully calculated from hearings for case ID {}", caseId);
            return nextHearingDetails;
        }
    }

    public NextHearingDetails calculateNextHearingDateFromCaseData(Callback<AsylumCase> callback) {
        log.info("Getting next hearing date from case data for case ID {}", callback.getCaseDetails().getId());

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String listCaseHearingDate = asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse("");

        return NextHearingDetails.builder()
            .hearingId("999").hearingDateTime(listCaseHearingDate).build();
    }

    public void clearHearingDateInformation(AsylumCase asylumCase) {
        asylumCase.clear(LIST_CASE_HEARING_DATE);
        NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
            .hearingId(null).hearingDateTime(null).build();
        asylumCase.write(NEXT_HEARING_DETAILS, nextHearingDetails);
    }
}
