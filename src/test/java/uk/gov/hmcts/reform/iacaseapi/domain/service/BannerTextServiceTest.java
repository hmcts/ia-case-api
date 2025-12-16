package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.XUI_BANNER_TEXT;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BannerTextServiceTest {
    public static final String SOME_TEXT = "SOME TEXT";
    private static final int ONE = 1;
    @Mock
    private AsylumCase asylumCase;

    private BannerTextService subject;

    @BeforeEach
    public void setUp() {
        subject = new BannerTextService();
    }

    @Test
    void shouldReturnEmptyStringIfNoExistingBannerTextOfTheCase() {
        when(asylumCase.read(XUI_BANNER_TEXT)).thenReturn(Optional.empty());
        String bannerText = subject.getBannerText(asylumCase);
        assertThat(bannerText).isEqualTo(EMPTY);
        verify(asylumCase, times(ONE)).read(XUI_BANNER_TEXT);
    }

    @Test
    void shouldReturnExistingBannerTextOfTheCase() {
        when(asylumCase.read(XUI_BANNER_TEXT)).thenReturn(Optional.of(SOME_TEXT));
        String bannerText = subject.getBannerText(asylumCase);
        assertThat(bannerText).isEqualTo(SOME_TEXT);
        verify(asylumCase, times(ONE)).read(XUI_BANNER_TEXT);
    }

    @Test
    void shouldAddBannerTextToTheCase() {
        subject.addBannerText(asylumCase, SOME_TEXT);
        verify(asylumCase, times(ONE)).write(XUI_BANNER_TEXT, SOME_TEXT);
    }

    @Test
    void shouldThrowExceptionIfTextIsEmpty() {
        assertThatThrownBy(() -> subject.addBannerText(asylumCase, EMPTY))
                .hasMessage("Banner text can not be null or empty")
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

}