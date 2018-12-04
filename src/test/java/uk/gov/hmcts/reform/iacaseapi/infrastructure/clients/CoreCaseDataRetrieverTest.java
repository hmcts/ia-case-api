package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import org.junit.Test;

public class CoreCaseDataRetrieverTest {

    private final AsylumCasesRetriever asylumCasesRetriever = mock(AsylumCasesRetriever.class);
    private final CoreCaseDataRetriever underTest = new CoreCaseDataRetriever(asylumCasesRetriever);

    @Test
    public void gets_all_pages() {

        when(asylumCasesRetriever.getNumberOfPages()).thenReturn(10);

        underTest.retrieveAllAppealCases();

        verify(asylumCasesRetriever, times(1)).getNumberOfPages();

        verify(asylumCasesRetriever, times(1)).getAsylumCasesPage("1");
        verify(asylumCasesRetriever, times(1)).getAsylumCasesPage("2");
        verify(asylumCasesRetriever, times(1)).getAsylumCasesPage("3");
        verify(asylumCasesRetriever, times(1)).getAsylumCasesPage("4");
        verify(asylumCasesRetriever, times(1)).getAsylumCasesPage("5");
        verify(asylumCasesRetriever, times(1)).getAsylumCasesPage("6");
        verify(asylumCasesRetriever, times(1)).getAsylumCasesPage("7");
        verify(asylumCasesRetriever, times(1)).getAsylumCasesPage("8");
        verify(asylumCasesRetriever, times(1)).getAsylumCasesPage("9");
        verify(asylumCasesRetriever, times(1)).getAsylumCasesPage("10");

        verifyNoMoreInteractions(asylumCasesRetriever);
    }

    @Test
    public void maps_new_exception_message_when_get_number_of_pages_throws() {

        when(asylumCasesRetriever.getNumberOfPages())
            .thenThrow(mock(AsylumCaseRetrievalException.class));

        assertThatThrownBy(underTest::retrieveAllAppealCases)
            .hasMessage("Couldn't retrieve appeal cases from Ccd")
            .isExactlyInstanceOf(AsylumCaseRetrievalException.class);
    }

    @Test
    public void returns_empty_list_when_there_are_no_pages() {

        when(asylumCasesRetriever.getNumberOfPages()).thenReturn(0);

        List<Map> cases = underTest.retrieveAllAppealCases();

        assertThat(cases).isEmpty();
    }
}
