# ia-case-api

Immigration &amp; Asylum case API

## Purpose

Immigration &amp; Asylum case API is a Spring Boot based application to manage cases for Immigration & Asylum Appellants and Legal Representatives.

### Prerequisites

To run the project you will need to have the following installed:

* Java 8
* Docker (optional)

For information about the software versions used to build this API and a complete list of it's dependencies see build.gradle

### Running the application

To run the API quickly use the docker helper script as follows:

```
./bin/run-in-docker.sh install
```


Alternatively, you can start the application from the current source files using Gradle as follows:

```
./gradlew clean bootRun
```

If required, to run with a low memory consumption, the following can be used:

```
./gradlew --no-daemon assemble && java -Xmx384m -jar build/libs/ia-case-*.jar
```

### Using the application

To understand if the application is working, you can call it's health endpoint:

```
curl http://localhost:8090/health
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

In order for these tests to run successfully you will need its dependencies to be running.  Firstly, CCD will need to be running, a dockerised version of it and its dependencies along with instructions for running locally can be found at https://github.com/hmcts/ccd-docker

Secondly, the ia-case-documents-api needs to be running, this api along with instructions to run locally can be found at https://github.com/hmcts/ia-case-documents-api. 

And lastly the ia-notifications-api needs to be running.  This api along with instructions to run locally can be found at https://github.com/hmcts/ia-case-notifications-api.

To successfully interact with the above dependencies a few environment variables need to be set as below. The examples (the values below are not real, replace them with values matching those in the latest CCD Definition spreadsheet):

| Environment Variable | *Example values*  |
|----------------------|----------|
| TEST_CASEOFFICER_USERNAME  |  ia-caseofficer@example.com |
| TEST_CASEOFFICER_PASSWORD  |  password |
| TEST_JUDICIARY_USERNAME  |  ia-judiciary@example.com |
| TEST_JUDICIARY_PASSWORD  |  password |
| TEST_LAW_FIRM_A_USERNAME  |  ia-law-firm-a@example.com |
| TEST_LAW_FIRM_A_PASSWORD  |  password |
| TEST_LAW_FIRM_B_USERNAME  |  ia-system-user@example.com |
| TEST_LAW_FIRM_B_PASSWORD  |  password |
| IA_SYSTEM_USERNAME  |  ia-system-user@example.com |
| IA_SYSTEM_PASSWORD  |  password |
| IA_IDAM_CLIENT_ID  |  some-idam-client-id |
| IA_IDAM_SECRET  |  some-idam-secret |
| IA_IDAM_REDIRECT_URI  |  http://localhost:3451/oauth2redirect |
| IA_S2S_SECRET  |  some-s2s-secret |
| IA_S2S_MICROSERVICE  |  some-s2s-gateway |

If you want to run a specific scenario use this command:

```
./gradlew functional --tests CcdScenarioRunnerTest --info -Dscenario=RIA-697
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


### Running mutation tests tests:

If you have some time to spare, you can run the *mutation tests* as follows:

```
./gradlew pitest
```

As the project grows, these tests will take longer and longer to execute but are useful indicators of the quality of the test suite.

More information about mutation testing can be found here:
http://pitest.org/ 

