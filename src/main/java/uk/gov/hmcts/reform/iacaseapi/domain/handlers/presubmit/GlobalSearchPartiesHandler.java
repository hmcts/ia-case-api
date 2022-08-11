package uk.gov.hmcts.reform.AsylumCaseapi.domain.handlers.presubmit;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;


import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.GlobalSearchParties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;


@Component
public class GlobalSearchPartiesHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public GlobalSearchPartiesHandler(
    ) {

    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(START_APPEAL);
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

        String appellantTitle = asylumCase
                .read(APPELLANT_TITLE, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantTitle is not present"));

        String appellantGivenNames = asylumCase
                .read(APPELLANT_GIVEN_NAMES, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantGivenNames is not present"));

        String appellantFamilyName = asylumCase
                .read(APPELLANT_FAMILY_NAME, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantFamilyName is not present"));

        String appellantAddress = asylumCase
                .read(APPELLANT_ADDRESS, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantAddress is not present"));

        String appellantEmailAddress = asylumCase
                .read(APPELLANT_EMAIL_ADDRESS, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantEmailAddress is not present"));

        String appellantDateOfBirth = asylumCase
                .read(APPELLANT_DATE_OF_BIRTH, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantDateOfBirth is not present"));


        final GlobalSearchParties newGlobalSearchParties = new GlobalSearchParties(
                appellantTitle,
                appellantGivenNames,
                appellantFamilyName,
                appellantAddress,
                appellantEmailAddress,
                appellantDateOfBirth
        );

        asylumCase.write(GLOBAL_SEARCH_PARTIES, newGlobalSearchParties);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
