package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MakeAnApplicationAppenderTest {

    @Mock private UserDetails userDetails;
    @Mock private DateProvider dateProvider;
    @Mock private IdValue<MakeAnApplication> makeAnApplicationById1;
    @Mock private IdValue<MakeAnApplication> makeAnApplicationById2;

    private String newMakeAnApplicationType = "newMakeAnApplicationType";
    private String newMakeAnApplicationDesc = "newMakeAnApplicationDesc";
    private List<IdValue<Document>> newMakeAnApplicationEvidence = Collections.emptyList();
    private String newMakeAnApplicationDecision = "newMakeAnApplicationDecision";
    private String newMakeAnApplicationState = "newState";

    private MakeAnApplicationAppender makeAnApplicationAppender;

    @BeforeEach
    public void setUp() {

        makeAnApplicationAppender =
            new MakeAnApplicationAppender(userDetails, dateProvider);
    }

    @Test
    void should_append_the_new_application_in_first_position() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);
        when(userDetails.getRoles()).thenReturn(newArrayList(UserRole.HOME_OFFICE_APC.toString()));

        MakeAnApplication existingMakeAnApplication1 = mock(MakeAnApplication.class);
        when(makeAnApplicationById1.getValue()).thenReturn(existingMakeAnApplication1);

        MakeAnApplication existingMakeAnApplication2 = mock(MakeAnApplication.class);
        when(makeAnApplicationById2.getValue()).thenReturn(existingMakeAnApplication2);

        List<IdValue<MakeAnApplication>> existingMakeAnApplications =
            Arrays.asList(makeAnApplicationById1, makeAnApplicationById2);
        List<IdValue<MakeAnApplication>> allMakeAnApplications = makeAnApplicationAppender.append(
            existingMakeAnApplications, newMakeAnApplicationType,
            newMakeAnApplicationDesc, newMakeAnApplicationEvidence, newMakeAnApplicationDecision,
            newMakeAnApplicationState);

        assertNotNull(allMakeAnApplications);
        assertEquals(3, allMakeAnApplications.size());

        assertEquals("3", allMakeAnApplications.get(0).getId());
        assertEquals("2", allMakeAnApplications.get(1).getId());
        assertEquals("1", allMakeAnApplications.get(2).getId());

        assertEquals(newMakeAnApplicationType, allMakeAnApplications.get(0).getValue().getType());
        assertEquals(newMakeAnApplicationDesc, allMakeAnApplications.get(0).getValue().getDetails());
        assertEquals(newMakeAnApplicationEvidence, allMakeAnApplications.get(0).getValue().getEvidence());
        assertEquals(newMakeAnApplicationDecision, allMakeAnApplications.get(0).getValue().getDecision());
        assertEquals(newMakeAnApplicationState, allMakeAnApplications.get(0).getValue().getState());
        assertEquals(UserRole.HOME_OFFICE_APC.toString(), allMakeAnApplications.get(0).getValue().getApplicantRole());

        assertEquals(existingMakeAnApplication1, allMakeAnApplications.get(1).getValue());
        assertEquals(existingMakeAnApplication2, allMakeAnApplications.get(2).getValue());
    }

    @Test
    void should_return_new_application_if_no_existing_applications() {

        when(dateProvider.now()).thenReturn(LocalDate.MAX);
        when(userDetails.getRoles()).thenReturn(newArrayList(UserRole.LEGAL_REPRESENTATIVE.toString()));

        List<IdValue<MakeAnApplication>> existingMakeAnApplications = Collections.emptyList();

        List<IdValue<MakeAnApplication>> allMakeAnApplications = makeAnApplicationAppender.append(
            existingMakeAnApplications, newMakeAnApplicationType,
            newMakeAnApplicationDesc, newMakeAnApplicationEvidence, newMakeAnApplicationDecision,
            newMakeAnApplicationState);

        assertNotNull(allMakeAnApplications);
        assertEquals(1, allMakeAnApplications.size());

        assertEquals(newMakeAnApplicationType, allMakeAnApplications.get(0).getValue().getType());
        assertEquals(newMakeAnApplicationDesc, allMakeAnApplications.get(0).getValue().getDetails());
        assertEquals(newMakeAnApplicationEvidence, allMakeAnApplications.get(0).getValue().getEvidence());
        assertEquals(newMakeAnApplicationDecision, allMakeAnApplications.get(0).getValue().getDecision());
        assertEquals(newMakeAnApplicationState, allMakeAnApplications.get(0).getValue().getState());
        assertEquals(UserRole.LEGAL_REPRESENTATIVE.toString(),
            allMakeAnApplications.get(0).getValue().getApplicantRole());
    }

    @Test
    void should_not_allow_null_values() {

        List<IdValue<MakeAnApplication>> existingMakeAnApplications = Collections.emptyList();

        assertThatThrownBy(() -> makeAnApplicationAppender.append(
            null, newMakeAnApplicationType,
            newMakeAnApplicationDesc, newMakeAnApplicationEvidence, newMakeAnApplicationDecision,
            newMakeAnApplicationState))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationAppender.append(
            existingMakeAnApplications, null,
            newMakeAnApplicationDesc, newMakeAnApplicationEvidence, newMakeAnApplicationDecision,
            newMakeAnApplicationState))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationAppender.append(
            existingMakeAnApplications, newMakeAnApplicationType,
            null, newMakeAnApplicationEvidence, newMakeAnApplicationDecision, newMakeAnApplicationState))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationAppender.append(
            existingMakeAnApplications, newMakeAnApplicationType,
            newMakeAnApplicationDesc, null, newMakeAnApplicationDecision, newMakeAnApplicationState))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationAppender.append(
            existingMakeAnApplications, newMakeAnApplicationType,
            newMakeAnApplicationDesc, newMakeAnApplicationEvidence, null, newMakeAnApplicationState))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> makeAnApplicationAppender.append(
            existingMakeAnApplications, newMakeAnApplicationType,
            newMakeAnApplicationDesc, newMakeAnApplicationEvidence, newMakeAnApplicationDecision, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
