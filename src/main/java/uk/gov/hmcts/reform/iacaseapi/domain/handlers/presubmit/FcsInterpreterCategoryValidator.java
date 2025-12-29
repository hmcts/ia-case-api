package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.*;

@Slf4j
@Component
public class FcsInterpreterCategoryValidator implements PreSubmitCallbackHandler<BailCase> {

    private static final Set<Event> eventsToHandle = Set.of(Event.START_APPLICATION,
                                               Event.EDIT_BAIL_APPLICATION,
                                               Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT,
                                               Event.MAKE_NEW_APPLICATION);

    private static final String ERROR_MESSAGE = "You must select at least one interpreter category";

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && eventsToHandle.contains(callback.getEvent())
               && callback.getPageId().equals("fcsInterpreterCategory");
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
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

        if (bailCase.read(FCS_INTERPRETER_YESNO, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            Optional<List<String>> maybeFcs1Category = bailCase.read(FCS1_INTERPRETER_LANGUAGE_CATEGORY);
            Optional<List<String>> maybeFcs2Category = bailCase.read(FCS2_INTERPRETER_LANGUAGE_CATEGORY);
            Optional<List<String>> maybeFcs3Category = bailCase.read(FCS3_INTERPRETER_LANGUAGE_CATEGORY);
            Optional<List<String>> maybeFcs4Category = bailCase.read(FCS4_INTERPRETER_LANGUAGE_CATEGORY);

            final boolean fcs1CategoryEmpty = maybeFcs1Category.isPresent() && maybeFcs1Category.get().isEmpty();
            final boolean fcs2CategoryEmpty = maybeFcs2Category.isPresent() && maybeFcs2Category.get().isEmpty();
            final boolean fcs3CategoryEmpty = maybeFcs3Category.isPresent() && maybeFcs3Category.get().isEmpty();
            final boolean fcs4CategoryEmpty = maybeFcs4Category.isPresent() && maybeFcs4Category.get().isEmpty();

            final boolean has4Fcs = bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES;
            final boolean has3Fcs = bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES;
            final boolean has2Fcs = bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES;
            final boolean hasFcs = bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES;

            //if has4Fcs then either of fcs1 or fcs2 or fcs3 or fcs4 should be present
            if (has4Fcs && has3Fcs && has2Fcs && hasFcs) {
                if (fcs1CategoryEmpty && fcs2CategoryEmpty && fcs3CategoryEmpty && fcs4CategoryEmpty) {
                    response.addError(ERROR_MESSAGE);
                }
                return response;
            }

            //if has3Fcs then either of fcs1 or fcs2 or fcs3 should be present
            if (has3Fcs && has2Fcs && hasFcs) {
                if (fcs1CategoryEmpty && fcs2CategoryEmpty && fcs3CategoryEmpty) {
                    response.addError(ERROR_MESSAGE);
                }
                return response;
            }

            //if has2Fcs then either of fcs1 or fcs2 should be present
            if (has2Fcs && hasFcs) {
                if (fcs1CategoryEmpty && fcs2CategoryEmpty) {
                    response.addError(ERROR_MESSAGE);
                }
                return response;
            }

            //if hasFcs then  fcs1 should be present
            if (hasFcs) {
                if (fcs1CategoryEmpty) {
                    response.addError(ERROR_MESSAGE);
                }
                return response;
            }


        }
        return response;
    }

}
