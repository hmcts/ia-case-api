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
import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.XUI_BANNER_TEXT;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BannerTextServiceTest {
    public static final String SOME_TEXT = "SOME TEXT";
    public static final String SOME_TEXT_A = "SOME TEXTA";
    public static final String SOME_TEXT_B = "SOME TEXTB";
    private static final int ONE = 1;
    private static final int ZERO = 0;

    @Mock
    private AsylumCase asylumCase;

    private BannerTextService subject;

    @BeforeEach
    public void setUp() {
        subject = new BannerTextService();
    }

    @Test
    void shouldAddBannerTextToTheCase() {
        subject.addToBannerText(asylumCase, SOME_TEXT);
        verify(asylumCase, times(ONE)).write(XUI_BANNER_TEXT, SOME_TEXT);
    }

    @Test
    void shouldAddGivenBannerTextToTheCase() {
        when(asylumCase.read(XUI_BANNER_TEXT)).thenReturn(Optional.empty());
        subject.addToBannerText(asylumCase, SOME_TEXT);
        verify(asylumCase, times(ONE)).write(XUI_BANNER_TEXT, SOME_TEXT);
    }

    @Test
    void shouldAddGivenBannerTextToExistingBannerTextOfTheCase() {
        when(asylumCase.read(XUI_BANNER_TEXT)).thenReturn(Optional.of(SOME_TEXT_A));
        subject.addToBannerText(asylumCase, SOME_TEXT);
        verify(asylumCase, times(ONE)).write(XUI_BANNER_TEXT, SOME_TEXT_A + SPACE + SOME_TEXT);
    }

    @Test
    void shouldNotAddGivenBannerTextIfAlreadyExits() {
        when(asylumCase.read(XUI_BANNER_TEXT)).thenReturn(Optional.of(SOME_TEXT));
        subject.addToBannerText(asylumCase, SOME_TEXT);
        verify(asylumCase, times(ZERO)).write(XUI_BANNER_TEXT, SOME_TEXT);
    }

    @Test
    void shouldRemoveGivenTextFromTheBannerText() {
        when(asylumCase.read(XUI_BANNER_TEXT)).thenReturn(Optional.of(SOME_TEXT_A));
        subject.removeFromBannerText(asylumCase, SOME_TEXT);
        verify(asylumCase, times(ONE)).write(XUI_BANNER_TEXT, "A");
    }

    @Test
    void shouldNotRemoveGivenTextFromTheBannerText() {
        when(asylumCase.read(XUI_BANNER_TEXT)).thenReturn(Optional.of(SOME_TEXT_A));
        subject.removeFromBannerText(asylumCase, SOME_TEXT_B);
        verify(asylumCase, times(ZERO)).write(XUI_BANNER_TEXT, SOME_TEXT_A);
    }

    @Test
    void shouldThrowExceptionIfTextIsEmpty() {
        assertThatThrownBy(() -> subject.addToBannerText(asylumCase, EMPTY))
                .hasMessage("Banner text can not be null or empty")
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }

}
