package uk.gov.hmcts.reform.bailcaseapi.component.testutils;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import ru.lanwen.wiremock.config.WiremockConfigFactory;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.wiremock.DocumentsApiCallbackTransformer;
import uk.gov.hmcts.reform.bailcaseapi.component.testutils.wiremock.NotificationsApiCallbackTransformer;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

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
