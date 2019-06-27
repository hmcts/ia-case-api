FROM hmcts/cnp-java-base:openjdk-8u191-jre-alpine3.9-2.0.1

# Mandatory!
ENV APP ia-case-api.jar
ENV APPLICATION_TOTAL_MEMORY 1024M
ENV APPLICATION_SIZE_ON_DISK_IN_MB 75

# Optional
ENV JAVA_OPTS ""

COPY build/libs/$APP /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=12 CMD http_proxy="" wget -q --spider http://localhost:8090/health || exit 1

EXPOSE 8090

CMD [ "ia-case-api.jar" ]