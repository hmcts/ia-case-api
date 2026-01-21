package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.internal.verification.VerifyNoMoreInteractions.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_HEARING_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ID_LIST;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HearingIdBailListProcessorTest {
    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private AsylumCase asylumCase;

    @Captor
    private ArgumentCaptor<List<IdValue<String>>> newHearingIdListArgumentCaptor;

    private HearingIdBailListProcessor hearingIdBailListProcessor;

    @BeforeEach
    void setUp() {
        hearingIdBailListProcessor = new HearingIdBailListProcessor();
    }

    @Test
    void should_add_hearing_id_list_if_does_not_exist() {
        // given
        when(asylumCase.read(CURRENT_HEARING_ID, String.class)).thenReturn(Optional.of("12345"));
        when(asylumCase.read(HEARING_ID_LIST)).thenReturn(Optional.empty());

        // when
        hearingIdBailListProcessor.processHearingIdList(asylumCase);

        // then
        verify(asylumCase).write(eq(HEARING_ID_LIST), newHearingIdListArgumentCaptor.capture());
        List<IdValue<String>> hearingIdList = newHearingIdListArgumentCaptor.getValue();
        assertEquals(1, hearingIdList.size());
        assertEquals("1", hearingIdList.get(0).getId());
        assertEquals("12345", hearingIdList.get(0).getValue());
    }

    @Test
    void should_add_hearing_id_to_existing_list() {
        // given
        when(asylumCase.read(CURRENT_HEARING_ID, String.class)).thenReturn(Optional.of("12345"));
        IdValue<String> idValue1 = new IdValue<>("1", "23456");
        IdValue<String> idValue2 = new IdValue<>("2", "34567");
        List<IdValue<String>> existingHearingIdList = Arrays.asList(idValue1, idValue2);
        when(asylumCase.read(HEARING_ID_LIST)).thenReturn(Optional.of(existingHearingIdList));

        // when
        hearingIdBailListProcessor.processHearingIdList(asylumCase);

        // then
        verify(asylumCase).write(eq(HEARING_ID_LIST), newHearingIdListArgumentCaptor.capture());
        List<IdValue<String>> newHearingIdList = newHearingIdListArgumentCaptor.getValue();
        assertEquals(3, newHearingIdList.size());
        assertEquals("1", newHearingIdList.get(0).getId());
        assertEquals("23456", newHearingIdList.get(0).getValue());
        assertEquals("2", newHearingIdList.get(1).getId());
        assertEquals("34567", newHearingIdList.get(1).getValue());
        assertEquals("3", newHearingIdList.get(2).getId());
        assertEquals("12345", newHearingIdList.get(2).getValue());
    }


    @Test
    void should_not_add_hearing_id_if_already_exists() {
        // given
        when(asylumCase.read(CURRENT_HEARING_ID, String.class)).thenReturn(Optional.of("12345"));
        IdValue<String> idValue1 = new IdValue<>("1", "23456");
        IdValue<String> idValue2 = new IdValue<>("2", "12345");
        List<IdValue<String>> existingHearingIdList = Arrays.asList(idValue1, idValue2);
        when(asylumCase.read(HEARING_ID_LIST)).thenReturn(Optional.of(existingHearingIdList));

        // when
        hearingIdBailListProcessor.processHearingIdList(asylumCase);

        // then
        verify(asylumCase).read(eq(CURRENT_HEARING_ID), eq(String.class));
        verify(asylumCase).read(eq(HEARING_ID_LIST));
        verifyNoMoreInteractions(asylumCase);
    }
}
