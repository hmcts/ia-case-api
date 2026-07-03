package uk.gov.hmcts.reform.iacaseapi.domain.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_CLAIM_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE;
import static uk.gov.hmcts.reform.iacaseapi.utils.TestUtils.setupLogVerifier;
import static uk.gov.hmcts.reform.iacaseapi.utils.TestUtils.verifyLogsContainMessage;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HomeOfficeReferenceServiceTest {

    private static final String HO_REFERENCE = "HO123456";
    private final String homeOfficeSerialisedEncryptionKey = "test-encryption-key";
    private final String encryptedData = "someData";
    private final String decryptedData =
            "[{\"id\":\"1\",\"value\":{\"familyName\":\"Smith\"}}]";

    @Mock
    private HomeOfficeApi<AsylumCase> homeOfficeApi;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private AsylumCase asylumCaseWithApiData;

    @Mock
    private IdValue<HomeOfficeAppellant> appellant;

    private final MockedStatic<HandlerUtils> handlerUtilsMock = Mockito.mockStatic(HandlerUtils.class);

    private HomeOfficeReferenceService service;

    private List<IdValue<HomeOfficeAppellant>> appellants;

    @BeforeEach
    void setup() {
        service = new HomeOfficeReferenceService(homeOfficeApi, homeOfficeSerialisedEncryptionKey);
        appellants = Collections.singletonList(appellant);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(
                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
                String.class))
                .thenReturn(Optional.empty());
        handlerUtilsMock.when(
                        () -> HandlerUtils.encrypt(any(), eq(homeOfficeSerialisedEncryptionKey)))
                .thenReturn(encryptedData);
        handlerUtilsMock.when(
                        () -> HandlerUtils.decrypt(any(), eq(homeOfficeSerialisedEncryptionKey)))
                .thenReturn(decryptedData);
    }

    @AfterEach
    void tearDown() {
        handlerUtilsMock.close();
    }

    @Test
    void should_return_existing_appellants_without_calling_api_if_serialised_not_empty() {
        handlerUtilsMock
                .when(() -> HandlerUtils.encrypt(any(), eq(homeOfficeSerialisedEncryptionKey)))
                .thenReturn(encryptedData);

        when(asylumCase.read(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, String.class))
                .thenReturn(Optional.of(encryptedData));

        List<IdValue<HomeOfficeAppellant>> result =
                service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        assertFalse(result.isEmpty());

        HomeOfficeAppellant hoAppellant = new HomeOfficeAppellant();
        hoAppellant.setFamilyName("Smith");

        List<IdValue<HomeOfficeAppellant>> expected =
                Collections.singletonList(new IdValue<>("1", hoAppellant));

        assertEquals(expected, result);

        verify(homeOfficeApi, never()).midEvent(any());
    }

    @Test
    void should_catch_exception_if_deserialisation_fails_and_continue_with_api_call() {
        when(asylumCase.read(
                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
                String.class))
                .thenReturn(Optional.of(decryptedData));

        when(homeOfficeApi.midEvent(callback))
                .thenReturn(asylumCaseWithApiData);

        when(asylumCaseWithApiData.read(
                HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.class))
                .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.OK));

        when(asylumCaseWithApiData.read(HOME_OFFICE_APPELLANTS))
                .thenReturn(Optional.of(appellants));

        handlerUtilsMock.when(
                        () -> HandlerUtils.decrypt(any(), eq(homeOfficeSerialisedEncryptionKey)))
                .thenThrow(new RuntimeException("Decryption failed"));

        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceService.class);
        List<IdValue<HomeOfficeAppellant>> result =
                service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        verifyLogsContainMessage(listAppender, "Could not deserialise list of Home Office appellants from encrypted serialised string");
        assertFalse(result.isEmpty());

        verify(homeOfficeApi, times(1)).midEvent(callback);
    }

    @Test
    void should_call_api_and_store_data_when_status_ok() {
        when(asylumCase.read(
                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
                String.class))
                .thenReturn(Optional.empty());

        when(homeOfficeApi.midEvent(callback))
                .thenReturn(asylumCaseWithApiData);

        when(asylumCaseWithApiData.read(
                HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.class))
                .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.OK));

        when(asylumCaseWithApiData.read(HOME_OFFICE_APPELLANTS))
                .thenReturn(Optional.of(appellants));

        List<IdValue<HomeOfficeAppellant>> result =
                service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        assertFalse(result.isEmpty());
        assertEquals(appellants, result);

        verify(homeOfficeApi).midEvent(callback);
        verify(asylumCaseWithApiData).read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.class);

        verify(asylumCase).write(
                HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.OK);

        verify(asylumCaseWithApiData).read(HOME_OFFICE_APPELLANTS);
        verify(asylumCase).write(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, encryptedData);
        verify(asylumCase).write(eq(HOME_OFFICE_APPELLANT_CLAIM_DATE), any());
        verify(asylumCase).write(eq(HOME_OFFICE_APPELLANT_DECISION_DATE), any());
        verify(asylumCase).write(eq(HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE), any());
    }


    @Test
    void should_call_api_and_not_store_data_when_serialisation_fails() {
        when(asylumCase.read(
                HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
                String.class))
                .thenReturn(Optional.empty());

        when(homeOfficeApi.midEvent(callback))
                .thenReturn(asylumCaseWithApiData);

        when(asylumCaseWithApiData.read(
                HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.class))
                .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.OK));

        when(asylumCaseWithApiData.read(HOME_OFFICE_APPELLANTS))
                .thenReturn(Optional.of(appellants));
        handlerUtilsMock.when(
                        () -> HandlerUtils.encrypt(any(), eq(homeOfficeSerialisedEncryptionKey)))
                .thenThrow(new RuntimeException("Encryption failed"));

        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceService.class);
        List<IdValue<HomeOfficeAppellant>> result =
                service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        verifyLogsContainMessage(listAppender, "Could not serialise list of Home Office appellants: ");
        assertFalse(result.isEmpty());
        assertEquals(appellants, result);

        verify(homeOfficeApi).midEvent(callback);
        verify(asylumCaseWithApiData).read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.class);

        verify(asylumCase).write(
                HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.OK);

        verify(asylumCaseWithApiData).read(HOME_OFFICE_APPELLANTS);
        verify(asylumCase, never()).write(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, encryptedData);
        verify(asylumCase).write(eq(HOME_OFFICE_APPELLANT_CLAIM_DATE), any());
        verify(asylumCase).write(eq(HOME_OFFICE_APPELLANT_DECISION_DATE), any());
        verify(asylumCase).write(eq(HOME_OFFICE_APPELLANT_DECISION_LETTER_DATE), any());
    }

    @Test
    void should_handle_404() {

        when(homeOfficeApi.midEvent(callback))
                .thenReturn(asylumCaseWithApiData);

        when(asylumCaseWithApiData.read(
                HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.class))
                .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.NOT_FOUND));

        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceService.class);
        List<IdValue<HomeOfficeAppellant>> result =
                service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        ILoggingEvent loggingEvent = verifyLogsContainMessage(listAppender,
                service.buildLogMessage(HO_REFERENCE, HomeOfficeApiResponseStatusType.NOT_FOUND));
        assertEquals(Level.INFO, loggingEvent.getLevel());
        assertTrue(result.isEmpty());

        verify(homeOfficeApi).midEvent(callback);
        verify(asylumCaseWithApiData).read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.class);

        verify(asylumCase).write(
                HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.NOT_FOUND);

        verify(asylumCaseWithApiData, never()).read(HOME_OFFICE_APPELLANTS);
        verify(asylumCase, never()).write(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, encryptedData);
        verify(asylumCase, never()).write(eq(HOME_OFFICE_APPELLANT_CLAIM_DATE), any());
    }

    private static Stream<HomeOfficeApiResponseStatusType> getWarnStatusCodes() {
        return Arrays.stream(HomeOfficeApiResponseStatusType.values())
                .filter(statusCode -> statusCode.getStatusCode() <= 0 || statusCode.getStatusCode() >= 500);
    }

    private static Stream<HomeOfficeApiResponseStatusType> getErrorStatusCodes() {
        return Arrays.stream(HomeOfficeApiResponseStatusType.values())
                .filter(statusCode ->
                        statusCode.getStatusCode() > 0
                                && statusCode.getStatusCode() < 500
                                && statusCode != HomeOfficeApiResponseStatusType.NOT_FOUND
                                && statusCode != HomeOfficeApiResponseStatusType.OK);
    }

    @ParameterizedTest
    @MethodSource("getWarnStatusCodes")
    void should_log_warn_for_server_errors(HomeOfficeApiResponseStatusType statusCode) {
        when(homeOfficeApi.midEvent(callback))
                .thenReturn(asylumCaseWithApiData);

        when(asylumCaseWithApiData.read(
                HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.class))
                .thenReturn(Optional.of(statusCode));

        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceService.class);
        List<IdValue<HomeOfficeAppellant>> result =
                service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        ILoggingEvent loggingEvent = verifyLogsContainMessage(listAppender,
                service.buildLogMessage(HO_REFERENCE, statusCode));
        assertEquals(Level.WARN, loggingEvent.getLevel());
        assertTrue(result.isEmpty());

        verify(homeOfficeApi).midEvent(callback);
        verify(asylumCaseWithApiData).read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.class);
        verify(asylumCase).write(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, statusCode);
        verify(asylumCaseWithApiData, never()).read(HOME_OFFICE_APPELLANTS);
        verify(asylumCase, never()).write(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, encryptedData);
        verify(asylumCase, never()).write(eq(HOME_OFFICE_APPELLANT_CLAIM_DATE), any());
    }

    @ParameterizedTest
    @MethodSource("getErrorStatusCodes")
    void should_log_error_for_server_errors(HomeOfficeApiResponseStatusType statusCode) {

        when(homeOfficeApi.midEvent(callback))
                .thenReturn(asylumCaseWithApiData);

        when(asylumCaseWithApiData.read(
                HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.class))
                .thenReturn(Optional.of(statusCode));

        ListAppender<ILoggingEvent> listAppender = setupLogVerifier(HomeOfficeReferenceService.class);
        List<IdValue<HomeOfficeAppellant>> result =
                service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        ILoggingEvent loggingEvent = verifyLogsContainMessage(listAppender,
                service.buildLogMessage(HO_REFERENCE, statusCode));
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        assertTrue(result.isEmpty());

        verify(homeOfficeApi).midEvent(callback);
        verify(asylumCaseWithApiData).read(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
                HomeOfficeApiResponseStatusType.class);
        verify(asylumCase).write(HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS, statusCode);
        verify(asylumCaseWithApiData, never()).read(HOME_OFFICE_APPELLANTS);
        verify(asylumCase, never()).write(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY, encryptedData);
        verify(asylumCase, never()).write(eq(HOME_OFFICE_APPELLANT_CLAIM_DATE), any());

    }

    @ParameterizedTest
    @EnumSource(HomeOfficeApiResponseStatusType.class)
    void should_build_log_message_for_all_status_codes(HomeOfficeApiResponseStatusType statusCode) {
        String logMessage = service.buildLogMessage(HO_REFERENCE, statusCode);
        assertTrue(logMessage.contains(HO_REFERENCE));
        assertTrue(logMessage.contains(statusCode.getHoIntegrationErrorText(HO_REFERENCE)));
    }
}