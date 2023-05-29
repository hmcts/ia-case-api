package uk.gov.hmcts.reform.iacaseapi.component.testutils;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import ru.lanwen.wiremock.config.WiremockConfigFactory;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock.DocumentsApiCallbackTransformer;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.wiremock.NotificationsApiCallbackTransformer;

public class StaticPortWiremockFactory implements WiremockConfigFactory {

    public static final int WIREMOCK_PORT = 8990;
    private final DocumentsApiCallbackTransformer documentsApiCallbackTransformer =
        new DocumentsApiCallbackTransformer();

    private final NotificationsApiCallbackTransformer notificationsApiCallbackTransformer =
        new NotificationsApiCallbackTransformer();

    @Override
    public WireMockConfiguration create() {
        return options()
            .port(WIREMOCK_PORT)
            .notifier(new Slf4jNotifier(true))
            .extensions(documentsApiCallbackTransformer, notificationsApiCallbackTransformer);
    }

}
