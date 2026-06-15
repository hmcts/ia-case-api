 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.8
# Application image
FROM hmctsprod.azurecr.io/base/java:21-distroless

# Change to non-root privilege
USER hmcts

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/ia-bail-case-api.jar /opt/app/

EXPOSE 4550

CMD [ "ia-bail-case-api.jar" ]
