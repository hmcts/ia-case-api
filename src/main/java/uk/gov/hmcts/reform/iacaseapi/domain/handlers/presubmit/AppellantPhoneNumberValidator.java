package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber.CountryCodeSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

@Slf4j
@Component
public class AppellantPhoneNumberValidator implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String CONTACT_PREFERENCE_PAGE_ID = "appellantContactPreference";
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && callback.getPageId().equals(CONTACT_PREFERENCE_PAGE_ID)
            && Arrays.asList(Event.START_APPEAL,
            Event.EDIT_APPEAL,
            Event.EDIT_APPEAL_AFTER_SUBMIT).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        Optional<String> phoneNumber = asylumCase.read(MOBILE_NUMBER, String.class);
        if (phoneNumber.isPresent()) {
            try {
                PhoneNumber phone = phoneNumberUtil.parse(phoneNumber.get(), "GB");
                if (!phoneNumberUtil.isValidNumber(phone)) {
                    log.warn("Phone number " + phoneNumber.get() + " is not a valid UK number");
                    if (phoneNumberUtil.isPossibleNumber(phoneNumber.get(), "GB")) {
                        throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER,
                            "Phone number is UK format but invalid UK number.");
                    }
                    phoneNumberUtil.parse(phoneNumber.get(), CountryCodeSource.UNSPECIFIED.name());
                }
            } catch (NumberParseException e) {
                log.warn("Phone number " + phoneNumber.get() + " is invalid: " + e.getMessage());
                String errorMessage = switch (e.getErrorType()) {
                    case INVALID_COUNTRY_CODE -> "International number must begin with + followed by region code.";
                    case NOT_A_NUMBER -> "Phone number is invalid.";
                    default -> e.getMessage();
                };
                response.addError(errorMessage);
            }
        }
        return response;
    }
}
