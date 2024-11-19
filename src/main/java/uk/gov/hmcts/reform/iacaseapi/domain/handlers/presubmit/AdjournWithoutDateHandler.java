package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADJOURN_HEARING_WITHOUT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NextHearingDateService;


@Component
@RequiredArgsConstructor
public class AdjournWithoutDateHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final NextHearingDateService nextHearingDateService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(ADJOURN_HEARING_WITHOUT_DATE);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        State currentState = callback.getCaseDetailsBefore().orElseThrow(() -> new IllegalStateException("cannot find previous state")).getState();

        String currentHearingDate = asylumCase.read(LIST_CASE_HEARING_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException("listCaseHearingDate is missing."));

        asylumCase.write(LIST_CASE_HEARING_DATE_ADJOURNED, "Adjourned");
        asylumCase.write(STATE_BEFORE_ADJOURN_WITHOUT_DATE, currentState.toString());
        asylumCase.write(DATE_BEFORE_ADJOURN_WITHOUT_DATE, currentHearingDate);

        asylumCase.write(DOES_THE_CASE_NEED_TO_BE_RELISTED, YesOrNo.NO);


        if (nextHearingDateService.enabled()) {
            if (!HandlerUtils.isIntegrated(asylumCase)) {
                asylumCase.clear(LIST_CASE_HEARING_DATE);
                NextHearingDetails nextHearingDetails = NextHearingDetails.builder()
                    .hearingId(null).hearingDateTime(null).build();
                asylumCase.write(NEXT_HEARING_DETAILS, nextHearingDetails);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
