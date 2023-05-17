package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OUT_OF_COUNTRY_DECISION_TYPE;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;


@Component
public class HomeOfficeReferenceFormatter implements PreSubmitCallbackHandler<AsylumCase> {

    public static final int REQUIRED_CID_REF_LENGTH = 9;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.START_APPEAL
                   || callback.getEvent() == Event.EDIT_APPEAL
                    || callback.getEvent() == Event.CREATE_DLRM_CASE)
               && HandlerUtils.isRepJourney(callback.getCaseDetails().getCaseData());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        if (!asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class).map(
            value -> (OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS.equals(value)
            || OutOfCountryDecisionType.REFUSE_PERMIT.equals(value))).orElse(false)) {
            String homeOfficeReferenceNumber = asylumCase
                .read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("homeOfficeReferenceNumber is missing"));

            if (homeOfficeReferenceNumber.length() < REQUIRED_CID_REF_LENGTH) {
                asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER,
                    String.format("%09d", Integer.parseInt(homeOfficeReferenceNumber)));
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
