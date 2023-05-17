package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.*;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.NationalityFieldValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Service
public class StatelessHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private static final String IS_STATELESS = "isStateless";

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && (callback.getEvent() == Event.START_APPEAL
                || callback.getEvent() == Event.EDIT_APPEAL
                || callback.getEvent() == Event.CREATE_DLRM_CASE
                || callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
                callback.getCaseDetails()
                        .getCaseData();

        final String stateless = asylumCase
                .read(APPELLANT_STATELESS, String.class).orElse("");

        if (stateless.equals(IS_STATELESS)) {
            asylumCase.clear(APPELLANT_NATIONALITIES);

            List<IdValue<NationalityFieldValue>> list = new ArrayList<>();
            list.add(new IdValue<>("1", new NationalityFieldValue("ZZ")));
            asylumCase.write(APPELLANT_NATIONALITIES, list);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
