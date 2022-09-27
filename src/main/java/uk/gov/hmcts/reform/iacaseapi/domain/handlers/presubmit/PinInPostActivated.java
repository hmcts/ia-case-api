package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;

import java.util.Arrays;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class PinInPostActivated implements PreSubmitCallbackHandler<AsylumCase> {

    private UserDetailsProvider userDetailsProvider;

    public PinInPostActivated(UserDetailsProvider userDetailsProvider) {
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.PIP_ACTIVATION;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        updateJourneyType(asylumCase);
        updateSubscription(asylumCase);
        updateReasonForAppeal(asylumCase);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void updateJourneyType(AsylumCase asylumCase) {
        asylumCase.write(AsylumCaseFieldDefinition.JOURNEY_TYPE, JourneyType.AIP);
    }

    private void updateSubscription(AsylumCase asylumCase) {
        Optional<ContactPreference> contactPreference = asylumCase.read(AsylumCaseFieldDefinition.CONTACT_PREFERENCE);
        Optional<String> mobileNumber = asylumCase.read(AsylumCaseFieldDefinition.MOBILE_NUMBER);
        Optional<String> email = asylumCase.read(AsylumCaseFieldDefinition.EMAIL);

        Subscriber subscriber = new Subscriber(
            SubscriberType.APPELLANT,
            email.orElse(null),
            contactPreference.filter(p -> p.equals(ContactPreference.WANTS_EMAIL)).map(p -> YesOrNo.YES).orElse(YesOrNo.NO),
            mobileNumber.orElse(null),
            contactPreference.filter(p -> p.equals(ContactPreference.WANTS_SMS)).map(p -> YesOrNo.YES).orElse(YesOrNo.NO));

        asylumCase.write(AsylumCaseFieldDefinition.SUBSCRIPTIONS, Arrays.asList(
            new IdValue<>(userDetailsProvider.getUserDetails().getId(), subscriber)));
    }

    private void updateReasonForAppeal(AsylumCase asylumCase) {
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
