package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class PriorApplicationAppenderTest {

    @Mock
    private PriorApplication newPriorApplication;
    @Mock
    private PriorApplication oldPriorApplication;
    @Mock
    private PriorApplication oldestPriorApplication;

    private Appender<PriorApplication> priorApplicationAppender;
    private List<IdValue<PriorApplication>> oldPriorApplications = new ArrayList<>();

    @BeforeEach
    public void setUp() {

        oldPriorApplications.add(new IdValue<>(
            "2",
            oldPriorApplication));

        oldPriorApplications.add(new IdValue<>(
            "1",
            oldestPriorApplication));

        priorApplicationAppender = new Appender<>();
    }

    @Test
    void appends_case_note_to_empty_list() {

        List<IdValue<PriorApplication>> allPriorApplications =
            priorApplicationAppender.append(newPriorApplication, emptyList());

        assertThat(allPriorApplications)
            .extracting(IdValue::getValue)
            .containsOnly(newPriorApplication);

        assertThat(allPriorApplications)
            .extracting(IdValue::getId)
            .containsOnly("1");
    }

    @Test
    void appends_case_note_to_existing_case_notes() {

        List<IdValue<PriorApplication>> allPriorApplications = priorApplicationAppender.append(newPriorApplication,
                                                                                               oldPriorApplications
        );

        assertThat(allPriorApplications)
            .extracting(IdValue::getValue)
            .containsExactly(newPriorApplication, oldPriorApplication, oldestPriorApplication);

        assertThat(allPriorApplications)
            .extracting(IdValue::getId)
            .containsExactly("3", "2", "1");
    }

    @Test
    void throws_if_case_note_null() {

        assertThatThrownBy(() -> priorApplicationAppender.append(null, oldPriorApplications))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
