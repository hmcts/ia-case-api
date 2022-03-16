# ia-bail-case-api
Immigration &amp; Asylum Bail case API

[![Build Status](https://travis-ci.org/hmcts/spring-boot-template.svg?branch=master)](https://travis-ci.org/hmcts/spring-boot-template)

## Purpose

Immigration &amp; Asylum case API is a Spring Boot based application  for Legal Representatives to manage bail applications.

### Prerequisites

To run the project you will need to have the following installed:

* Java 8
* Docker (optional)

For information about the software versions used to build this API and a complete list of it's dependencies see build.gradle

### Running the application

To run the API quickly use the docker helper script as follows: (make sure to have the required environment variables set as under functional tests section)

```
./bin/run-in-docker.sh --clean --install
```


Alternatively, you can start the application from the current source files using Gradle as follows:

```
./gradlew clean bootRun
```

If required, to run with a low memory consumption, the following can be used:

```
./gradlew --no-daemon assemble && java -Xmx384m -jar build/libs/ia-bail-case-*.jar
```
### Using the application

To understand if the application is working, you can call it's health endpoint:

```
curl http://localhost:4550/health
```

If the API is running, you should see this response:

```
{"status":"UP"}
```

### Running integration tests:


You can run the *integration tests* as follows:

```
./gradlew integration
```

### Running functional tests:

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *functional tests* as follows:

```
./gradlew functional
```

### Running smoke tests:

If the API is running (either inside a Docker container or via `gradle bootRun`) you can run the *smoke tests* as follows:

```
./gradlew smoke
```

### Running contract or pact tests:

You can run contract or pact tests as follows:

```
./gradlew contract
```

You can then publish your pact tests locally by first running the pact docker-compose:

```
docker-compose -f docker-pactbroker-compose.yml up
```

and then using it to publish your tests:

```
./gradlew pactPublish
```
