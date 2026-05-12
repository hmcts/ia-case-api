package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.MakeAnApplicationTypesProvider;

@Component
public class MakeAnApplicationMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    private final MakeAnApplicationTypesProvider makeAnApplicationTypesProvider;

    public MakeAnApplicationMidEvent(MakeAnApplicationTypesProvider makeAnApplicationTypesProvider) {
        this.makeAnApplicationTypesProvider = makeAnApplicationTypesProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage,
                             Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.MAKE_AN_APPLICATION;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        DynamicList maybeMakeAnApplicationType = asylumCase.read(MAKE_AN_APPLICATION_TYPES, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("MakeAnApplicationTypes is not present"));

        String makeAnApplicationTypeCode = maybeMakeAnApplicationType.getValue().getCode();
        String makeAnApplicationTypeLabel = MakeAnApplicationTypes.valueOf(makeAnApplicationTypeCode).toString();

        List<Value> existingMakeAnApplicationTypes = makeAnApplicationTypesProvider
            .getMakeAnApplicationTypes(callback)
            .getListItems();

        DynamicList newMakeAnApplicationTypes =
            new DynamicList(
                new Value(makeAnApplicationTypeCode, makeAnApplicationTypeLabel),
                existingMakeAnApplicationTypes
            );

        setMakeAnApplicationDescriptionLabel(makeAnApplicationTypeCode, asylumCase);

        asylumCase.write(MAKE_AN_APPLICATION_TYPES, newMakeAnApplicationTypes);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void setMakeAnApplicationDescriptionLabel(String applicationType, AsylumCase asylumCase) {
        switch (MakeAnApplicationTypes.valueOf(applicationType)) {
            case ADJOURN:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Explain why you need an adjournment and for how long you need it.");
                break;
            case EXPEDITE:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Explain why you need to expedite the appeal. Include the latest date you would like the appeal to be decided "
                    + "by and state if you are willing for the appeal to be decided without a hearing.");
                break;
            case LINK_OR_UNLINK:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Explain why you want to link or unlink this appeal. You must include the appellant name and HMCTS appeal reference "
                    + "of each appeal you want to link to or unlink from.");
                break;
            case JUDGE_REVIEW:
            case JUDGE_REVIEW_LO:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Tell us which application decision you want to be reviewed by a Judge and explain why you think the original decision "
                    + "was wrong.");
                break;
            case REINSTATE:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Explain why you believe the Tribunal should reinstate this appeal.");
                break;
            case TIME_EXTENSION:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Tell us which task you need more time to complete, explain why you need more time and include how much more time you will need.");
                break;
            case TRANSFER:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Tell us which hearing centre you want to transfer the appeal to and why.");
                break;
            case UPDATE_APPEAL_DETAILS:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Tell us which appeal details you want to update and explain why the changes are necessary.");
                break;
            case UPDATE_HEARING_REQUIREMENTS:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Tell us which hearing requirements you want to update and explain why the changes are necessary.");
                break;
            case WITHDRAW:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Explain why you want to withdraw the appeal.");
                break;
            case TRANSFER_OUT_OF_ACCELERATED_DETAINED_APPEALS_PROCESS:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Explain why this appeal should be transferred out of the accelerated detained appeal process.");
                break;
            case OTHER:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Describe the application you are making and explain the reasons for the application.");
                break;
            case CHANGE_DECISION_TYPE:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Explain how the appellant now wants the appeal to be decided and why they want to change");
                break;
            case SET_ASIDE_A_DECISION:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Explain why the decision should be set aside.");
                break;
            case APPLICATION_UNDER_RULE_31_OR_RULE_32:
                asylumCase.write(MAKE_AN_APPLICATION_DETAILS_LABEL,
                    "Explain why the decision should be set aside or changed.");
                break;
            default:
                break;
        }
    }
}
