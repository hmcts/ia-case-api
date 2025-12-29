package uk.gov.hmcts.reform.bailcaseapi.infrastructure;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
@Setter
public class ConfigValidatorAppListener implements ApplicationListener<ContextRefreshedEvent> {

    protected static final String CLUSTER_NAME = "CLUSTER_NAME";

    @Autowired
    private Environment environment;

    @Value("${ia.config.validator.secret}")
    private String iaConfigValidatorSecret;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        breakOnMissingIaConfigValidatorSecret();
    }

    void breakOnMissingIaConfigValidatorSecret() {
        String clusterName = environment.getProperty(CLUSTER_NAME);
        log.info("{} value: {}", CLUSTER_NAME, clusterName);
        if (StringUtils.isBlank(clusterName)) {
            log.warn("{} is null or empty. Skipping this check. This is allowed on a developer's local environment "
                + "but not on a cluster. If you are in a cluster, this warning is going to be a problem.",
                     CLUSTER_NAME);
            return;
        }

        if (StringUtils.isBlank(iaConfigValidatorSecret)) {
            throw new IllegalArgumentException("ia.config.validator.secret is null or empty."
                + " This is not allowed and it will break production. This is a secret value stored in a vault"
                + " (unless running locally). Check application.yaml for further information.");
        }
    }

}
