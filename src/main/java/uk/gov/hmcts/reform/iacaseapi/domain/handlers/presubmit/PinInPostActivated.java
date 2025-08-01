package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.ChangeRepresentationConfirmation.revokeAppellantAccessToCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SubscriberType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RoleAssignmentService;

@Component
public class PinInPostActivated implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final UserDetailsProvider userDetailsProvider;
    private final RoleAssignmentService roleAssignmentService;

    public PinInPostActivated(UserDetailsProvider userDetailsProvider,
                              RoleAssignmentService roleAssignmentService) {
        this.userDetailsProvider = userDetailsProvider;
        this.roleAssignmentService = roleAssignmentService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.PIP_ACTIVATION;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback,
                                                        PreSubmitCallbackResponse<AsylumCase> callbackResponse) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        YesOrNo isAipTransfer = asylumCase.read(AsylumCaseFieldDefinition.IS_AIP_TRANSFER, YesOrNo.class)
            .orElse(YesOrNo.NO);

        if (isAipTransfer.equals(YesOrNo.YES)) {
            revokeAppellantAccessToCase(roleAssignmentService, String.valueOf(callback.getCaseDetails().getId()));
        } else {
            updateJourneyType(asylumCase);
            removeLegalRepDetails(asylumCase);
            updateReasonForAppeal(asylumCase);
            updatePaymentOption(asylumCase);
        }
        updateSubscription(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase, updatedState(callback.getCaseDetails().getState()));
    }

    private State updatedState(State currentState) {
        if (currentState == State.CASE_BUILDING) {
            return State.AWAITING_REASONS_FOR_APPEAL;
        }
        if (currentState == State.CASE_UNDER_REVIEW) {
            return State.REASONS_FOR_APPEAL_SUBMITTED;
        }
        return currentState;
    }

    private void updateJourneyType(AsylumCase asylumCase) {
        asylumCase.write(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.AIP);
        asylumCase.write(AsylumCaseFieldDefinition.PREV_JOURNEY_TYPE, JourneyType.REP);
    }

    private void updatePaymentOption(AsylumCase asylumCase) {
        Optional<String> paymentOption = asylumCase.read(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION);
        if (paymentOption.isPresent()) {
            asylumCase.write(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION,
                    "payNow".equals(paymentOption.get()) ? "payNow" : "payLater");
            asylumCase.clear(AsylumCaseFieldDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION);
        }
    }

    private void removeLegalRepDetails(AsylumCase asylumCase) {
        asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_NAME);
        asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_NAME);
        asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS);
        asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY);
        asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_NAME);
        asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_ADDRESS);
        asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_REFERENCE_NUMBER);
        asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID);
        asylumCase.clear(AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID);
    }

    private void updateSubscription(AsylumCase asylumCase) {
        List<IdValue<Subscriber>> existingSubscriptions = fetchSubscriptions(asylumCase);
        if (existingSubscriptions.isEmpty()) {
            buildSubscriptions(asylumCase);
        } else {
            updateExistingSubscriptions(asylumCase, existingSubscriptions);
        }
    }

    private List<IdValue<Subscriber>> fetchSubscriptions(AsylumCase asylumCase) {
        Optional<List<IdValue<Subscriber>>> subscriptionsOptional = asylumCase.read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);
        return subscriptionsOptional.orElse(Collections.emptyList());
    }

    private void buildSubscriptions(AsylumCase asylumCase) {
        Optional<ContactPreference> contactPreference = asylumCase.read(AsylumCaseFieldDefinition.CONTACT_PREFERENCE);
        Optional<String> mobileNumber = asylumCase.read(AsylumCaseFieldDefinition.MOBILE_NUMBER);

        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT,
            userDetailsProvider.getUserDetails().getEmailAddress(),
            contactPreference.filter(p -> p.equals(ContactPreference.WANTS_EMAIL)).map(p -> YesOrNo.YES).orElse(YesOrNo.NO),
            mobileNumber.orElse(null),
            contactPreference.filter(p -> p.equals(ContactPreference.WANTS_SMS)).map(p -> YesOrNo.YES).orElse(YesOrNo.NO));

        asylumCase.write(AsylumCaseFieldDefinition.SUBSCRIPTIONS, Arrays.asList(
            new IdValue<>(userDetailsProvider.getUserDetails().getId(), subscriber)));

        asylumCase.clear(AsylumCaseFieldDefinition.EMAIL);
        asylumCase.clear(AsylumCaseFieldDefinition.MOBILE_NUMBER);
        asylumCase.clear(AsylumCaseFieldDefinition.CONTACT_PREFERENCE);
        asylumCase.clear(AsylumCaseFieldDefinition.CONTACT_PREFERENCE_DESCRIPTION);
    }

    private void updateExistingSubscriptions(AsylumCase asylumCase, List<IdValue<Subscriber>> existingSubscriptions) {
        Subscriber existingSubscriber = existingSubscriptions.get(0).getValue();
        String existingSubscriberEmail = existingSubscriber.getEmail();
        String authUserEmail = userDetailsProvider.getUserDetails().getEmailAddress();
        if (existingSubscriber.getWantsEmail() == YesOrNo.YES && !existingSubscriberEmail.equals(authUserEmail)) {
            asylumCase.clear(AsylumCaseFieldDefinition.SUBSCRIPTIONS);

            Subscriber updatedSubscriber = new Subscriber(
                existingSubscriber.getSubscriber(),
                userDetailsProvider.getUserDetails().getEmailAddress(),
                existingSubscriber.getWantsEmail(),
                existingSubscriber.getMobileNumber(),
                existingSubscriber.getWantsSms()
            );

            asylumCase.write(AsylumCaseFieldDefinition.SUBSCRIPTIONS, Arrays.asList(
                new IdValue<>(userDetailsProvider.getUserDetails().getId(), updatedSubscriber)
            ));
        }
    }

    private void updateReasonForAppeal(AsylumCase asylumCase) {
        if (asylumCase.read(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION).isEmpty()) {
            Optional<List<IdValue<DocumentWithMetadata>>> legalRepDocumentsOptional =
                asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS);
            List<IdValue<DocumentWithMetadata>> caseArgumentDocuments = legalRepDocumentsOptional.orElse(emptyList()).stream()
                .filter(documentWithMetadata -> documentWithMetadata.getValue().getTag() == DocumentTag.CASE_ARGUMENT)
                .collect(Collectors.toList());

            if (!caseArgumentDocuments.isEmpty()) {
                asylumCase.write(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DECISION, caseArgumentDocuments.get(0).getValue().getDescription());
                asylumCase.write(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DATE_UPLOADED, caseArgumentDocuments.get(0).getValue().getDateUploaded());
                asylumCase.write(AsylumCaseFieldDefinition.REASONS_FOR_APPEAL_DOCUMENTS, caseArgumentDocuments);
            }
        }
    }
}
