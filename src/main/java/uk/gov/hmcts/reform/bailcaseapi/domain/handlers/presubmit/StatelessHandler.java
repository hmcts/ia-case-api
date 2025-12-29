package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_NATIONALITY;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class StatelessHandler implements PreSubmitCallbackHandler<BailCase> {
    private static final String IS_STATELESS = "STATELESS";

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.START_APPLICATION
                   || callback.getEvent() == Event.EDIT_BAIL_APPLICATION
                   || callback.getEvent() == Event.MAKE_NEW_APPLICATION
                   || callback.getEvent() == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT);
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback.getCaseDetails()
                .getCaseData();

        final String stateless = bailCase
            .read(APPLICANT_NATIONALITY, String.class).orElse("");

        if (stateless.equals(IS_STATELESS)) {
            bailCase.clear(APPLICANT_NATIONALITIES);

            List<IdValue<NationalityFieldValue>> list = new ArrayList<>();
            list.add(new IdValue<>("1", new NationalityFieldValue("Stateless")));
            bailCase.write(APPLICANT_NATIONALITIES, list);
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
