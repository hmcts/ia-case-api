package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

@Component
public class FinancialConditionSupporterContactPreferenceMidEventHandler implements PreSubmitCallbackHandler<BailCase> {

    private static final String SUPPORTER_1_CONTACT_PREF_PAGE_ID = "supporterContactDetails";
    private static final String SUPPORTER_2_CONTACT_PREF_PAGE_ID = "supporter2ContactDetails";
    private static final String SUPPORTER_3_CONTACT_PREF_PAGE_ID = "supporter3ContactDetails";
    private static final String SUPPORTER_4_CONTACT_PREF_PAGE_ID = "supporter4ContactDetails";
    private static final String EMAIL_REQUIRED_ERROR = "Email is required.";
    private static final String PHONE_REQUIRED_ERROR = "At least one phone type is required.";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        List<Event> listOfEvents = List.of(START_APPLICATION, EDIT_BAIL_APPLICATION,
            EDIT_BAIL_APPLICATION_AFTER_SUBMIT, MAKE_NEW_APPLICATION);
        List<String> listOfPages = List.of(SUPPORTER_1_CONTACT_PREF_PAGE_ID, SUPPORTER_2_CONTACT_PREF_PAGE_ID,
            SUPPORTER_3_CONTACT_PREF_PAGE_ID, SUPPORTER_4_CONTACT_PREF_PAGE_ID);

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && listOfEvents.contains(callback.getEvent())
               && listOfPages.contains(callback.getPageId());
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);

        String pageId = callback.getPageId();
        String error = "";
        Optional<List<ContactPreference>> supporterContactPreferences = Optional.empty();

        switch (pageId) {
            case SUPPORTER_1_CONTACT_PREF_PAGE_ID -> supporterContactPreferences = bailCase.read(SUPPORTER_CONTACT_DETAILS);
            case SUPPORTER_2_CONTACT_PREF_PAGE_ID -> supporterContactPreferences = bailCase.read(SUPPORTER_2_CONTACT_DETAILS);
            case SUPPORTER_3_CONTACT_PREF_PAGE_ID -> supporterContactPreferences = bailCase.read(SUPPORTER_3_CONTACT_DETAILS);
            case SUPPORTER_4_CONTACT_PREF_PAGE_ID -> supporterContactPreferences = bailCase.read(SUPPORTER_4_CONTACT_DETAILS);
            default -> {
                // do nothing
            }
        }

        if (supporterContactPreferences.isPresent()) {
            if (supporterContactPreferences.stream().noneMatch(list -> list.contains(EMAIL))) {
                error = EMAIL_REQUIRED_ERROR;
            } else if (supporterContactPreferences.stream().noneMatch(list -> list.contains(MOBILE)
                                                                              || list.contains(TELEPHONE))) {
                error = PHONE_REQUIRED_ERROR;
            }
        }

        if (!error.isEmpty()) {
            response.addError(error);
        }
        return response;
    }
}
