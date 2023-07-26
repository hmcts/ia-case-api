 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.13
# Application image
FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/ia-bail-case-api.jar /opt/app/

EXPOSE 4550

CMD [ "ia-bail-case-api.jar" ]
