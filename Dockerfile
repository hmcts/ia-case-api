 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=2.5.1-BETA
# Application image
FROM hmctspublic.azurecr.io/base/java:11-distroless

# Change to non-root privilege
USER hmcts

COPY lib/AI-Agent.xml /opt/app/
COPY build/libs/ia-case-api.jar /opt/app/

EXPOSE 8090

CMD [ "ia-case-api.jar" ]
