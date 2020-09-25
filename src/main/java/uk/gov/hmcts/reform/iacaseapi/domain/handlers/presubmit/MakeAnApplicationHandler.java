package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.MakeAnApplicationAppender;

@Component
public class MakeAnApplicationHandler implements PreSubmitCallbackHandler<AsylumCase>  {

    private final MakeAnApplicationAppender makeAnApplicationAppender;
    private final FeatureToggler featureToggler;

    public MakeAnApplicationHandler(
        MakeAnApplicationAppender makeAnApplicationAppender,
        FeatureToggler featureToggler
    ) {
        this.makeAnApplicationAppender = makeAnApplicationAppender;
        this.featureToggler = featureToggler;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage,
                             Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.MAKE_AN_APPLICATION
               && featureToggler.getValue("make-an-application-feature", false);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final State currentState = callback.getCaseDetails().getState();

        String applicationType = asylumCase.read(MAKE_AN_APPLICATION_TYPES, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("MakeAnApplication type is not present"))
            .getValue()
            .getLabel();

        String applicationReason = asylumCase.read(MAKE_AN_APPLICATION_DETAILS, String.class)
            .orElseThrow(() -> new IllegalStateException("MakeAnApplication details is not present"));

        Optional<List<IdValue<Document>>> applicationEvidence = asylumCase.read(MAKE_AN_APPLICATION_EVIDENCE);

        Optional<List<IdValue<MakeAnApplication>>> maybeExistingMakeAnApplications =
            asylumCase.read(MAKE_AN_APPLICATIONS);

        final List<IdValue<MakeAnApplication>> existingMakeAnApplications =
            maybeExistingMakeAnApplications.orElse(Collections.emptyList());

        List<IdValue<MakeAnApplication>> allMakeAnApplications =
            makeAnApplicationAppender.append(
                existingMakeAnApplications,
                applicationType,
                applicationReason,
                applicationEvidence.orElse(Collections.emptyList()),
                "Pending",
                currentState.toString()
            );


        asylumCase.write(MAKE_AN_APPLICATIONS, allMakeAnApplications);
        asylumCase.write(HAS_APPLICATIONS_TO_DECIDE, YesOrNo.YES);

        asylumCase.clear(MAKE_AN_APPLICATION_TYPES);
        asylumCase.clear(MAKE_AN_APPLICATION_DETAILS);
        asylumCase.clear(MAKE_AN_APPLICATION_EVIDENCE);
        asylumCase.clear(MAKE_AN_APPLICATION_DETAILS_LABEL);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
