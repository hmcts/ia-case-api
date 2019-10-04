ARG APP_INSIGHTS_AGENT_VERSION=2.4.1
FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.0

# Mandatory!
ENV APP ia-case-api.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 75

COPY lib/applicationinsights-agent-2.4.1.jar lib/AI-Agent.xml /opt/app/
COPY build/libs/$APP /opt/app/

EXPOSE 8090

CMD [ "ia-case-api.jar" ]