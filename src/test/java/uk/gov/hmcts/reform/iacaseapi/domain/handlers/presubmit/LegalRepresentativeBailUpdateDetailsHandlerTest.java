package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousRepresentation;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousRepresentationAppender;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LegalRepresentativeBailUpdateDetailsHandlerTest {

    private final String legalRepName = "John";
    private final String legalRepFamilyName = "Doe";
    private final String legalRepEmailAddress = "john.doe@example.com";
    private final String legalRepMobilePhoneNumber = "01234123123";
    private final String legalRepReferenceNumber = "ABC-123";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private PreviousRepresentationAppender previousRepresentationAppender;

    private uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.LegalRepresentativeBailUpdateDetailsHandler legalRepresentativeBailUpdateDetailsHandler;

    @BeforeEach
    public void setUp() {
        legalRepresentativeBailUpdateDetailsHandler = new uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.LegalRepresentativeBailUpdateDetailsHandler(previousRepresentationAppender);

        when(callback.getEvent()).thenReturn(Event.UPDATE_LEGAL_REPRESENTATIVES_DETAILS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(UPDATE_LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(legalRepName));
        when(asylumCase.read(UPDATE_LEGAL_REP_FAMILY_NAME, String.class)).thenReturn(Optional.of(legalRepFamilyName));
        when(asylumCase.read(UPDATE_LEGAL_REP_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));
        when(asylumCase.read(UPDATE_LEGAL_REP_MOBILE_PHONE_NUMBER, String.class))
            .thenReturn(Optional.of(legalRepMobilePhoneNumber));
        when(asylumCase.read(UPDATE_LEGAL_REP_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(legalRepReferenceNumber));
    }

    @Test
    void prepare_fields_test() {
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeBailUpdateDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase).read(UPDATE_LEGAL_REP_NAME, String.class);
        verify(asylumCase).read(UPDATE_LEGAL_REP_FAMILY_NAME, String.class);
        verify(asylumCase).read(UPDATE_LEGAL_REP_EMAIL_ADDRESS, String.class);
        verify(asylumCase).read(UPDATE_LEGAL_REP_MOBILE_PHONE_NUMBER, String.class);
        verify(asylumCase).read(UPDATE_LEGAL_REP_REFERENCE_NUMBER, String.class);

        verify(asylumCase, times(1)).clear(eq(UPDATE_LEGAL_REP_COMPANY));
        verify(asylumCase, times(1)).clear(eq(UPDATE_LEGAL_REP_NAME));
        verify(asylumCase, times(1)).clear(eq(UPDATE_LEGAL_REP_FAMILY_NAME));
        verify(asylumCase, times(1)).clear(eq(UPDATE_LEGAL_REP_EMAIL_ADDRESS));
        verify(asylumCase, times(1)).clear(eq(UPDATE_LEGAL_REP_MOBILE_PHONE_NUMBER));
        verify(asylumCase, times(1)).clear(eq(UPDATE_LEGAL_REP_REFERENCE_NUMBER));

        verify(asylumCase, times(1)).write(eq(LEGAL_REP_NAME), eq(legalRepName));
        verify(asylumCase, times(1)).write(HAS_ADDED_LEGAL_REP_DETAILS, YesOrNo.YES);
        verify(asylumCase, times(1)).write(eq(LEGAL_REP_FAMILY_NAME), eq(legalRepFamilyName));
        verify(asylumCase, times(1)).write(eq(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS), eq(legalRepEmailAddress));
        verify(asylumCase, times(1)).write(eq(LEGAL_REP_MOBILE_PHONE_NUMBER), eq(legalRepMobilePhoneNumber));
        verify(asylumCase, times(1)).write(eq(LEGAL_REP_REFERENCE_NUMBER), eq(legalRepReferenceNumber));
        verify(asylumCase, times(1)).write(eq(LEGAL_REPRESENTATIVE_NAME), eq(legalRepName));

        verify(asylumCase, times(1)).clear(eq(CHANGE_ORGANISATION_REQUEST_FIELD));
    }

    @Test
    void should_not_write_to_previous_representations_when_change_organisation_request_is_missing() {
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeBailUpdateDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class)).thenReturn(Optional.empty());

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertTrue(asylumCase.read(PREVIOUS_REPRESENTATIONS).isEmpty());

        final List<IdValue<PreviousRepresentation>> allPreviousRepresentations = new ArrayList<>();

        verify(asylumCase, times(0)).write(PREVIOUS_REPRESENTATIONS, allPreviousRepresentations);
        verify(asylumCase, times(0)).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), anyString());
        verify(asylumCase, times(0)).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), anyString());
    }

    /*
    This is to ensure that legal representative related partyIds are generated when LR details
    are being updated because a new LR is taking over the case and not when existing details are
    simply getting updated
     */
    @Test
    void should_overwrite_legal_rep_party_ids_when_change_organisation_request_is_present() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());

        Value caseRole = new Value("[LEGALREPRESENTATIVE]", "Legal Representative");
        ChangeOrganisationRequest changeOrganisationRequest = new ChangeOrganisationRequest(
            new DynamicList(caseRole, newArrayList(caseRole)),
            LocalDateTime.now().toString(),
            "1"
        );
        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class))
            .thenReturn(Optional.of(changeOrganisationRequest));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeBailUpdateDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(eq(LEGAL_REP_INDIVIDUAL_PARTY_ID), anyString());
        verify(asylumCase, times(1)).write(eq(LEGAL_REP_ORGANISATION_PARTY_ID), anyString());
    }

    @Test
    void should_write_to_previous_representations_when_change_organisation_request_is_present() {
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            legalRepresentativeBailUpdateDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        final List<IdValue<PreviousRepresentation>> existingPreviousRepresentations = new ArrayList<>();

        final List<IdValue<PreviousRepresentation>> allPreviousRepresentations = new ArrayList<>();

        final Value caseRole =
            new Value("[LEGALREPRESENTATIVE]", "Legal Representative");

        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class))
            .thenReturn(Optional.of(
                new ChangeOrganisationRequest(
                    new DynamicList(caseRole, newArrayList(caseRole)),
                    LocalDateTime.now().toString(),
                    "1"
                )
            ));

        when(asylumCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.of("some company name"));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("some reference number"));

        legalRepresentativeBailUpdateDetailsHandler.writeToPreviousRepresentations(callback);

        verify(previousRepresentationAppender, times(1)).append(
            existingPreviousRepresentations,
            new PreviousRepresentation("some company name", "some reference number")
        );

        verify(asylumCase, times(1)).write(PREVIOUS_REPRESENTATIONS, allPreviousRepresentations);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> legalRepresentativeBailUpdateDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = legalRepresentativeBailUpdateDetailsHandler.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_LEGAL_REPRESENTATIVES_DETAILS
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepresentativeBailUpdateDetailsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> legalRepresentativeBailUpdateDetailsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
