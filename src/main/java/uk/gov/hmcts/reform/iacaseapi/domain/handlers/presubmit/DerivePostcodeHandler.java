package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;


@Component
public class DerivePostcodeHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
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

        if (!asylumCase.read(SEARCH_POSTCODE, String.class).isPresent()) {
            getAppellantPostcode(asylumCase).ifPresent(postcode -> asylumCase.write(SEARCH_POSTCODE, postcode));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Optional<String> getAppellantPostcode(
        AsylumCase asylumCase
    ) {
        if (asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)
                .orElse(NO) == YES) {

            Optional<AddressUk> optionalAppellantAddress = asylumCase.read(APPELLANT_ADDRESS);

            if (optionalAppellantAddress.isPresent()) {

                AddressUk appellantAddress = optionalAppellantAddress.get();

                return appellantAddress.getPostCode();
            }
        }

        return Optional.empty();
    }

}
