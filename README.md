 
# ia-case-api

Immigration &amp; Asylum case API
 
## Purpose

Immigration &amp; Asylum case API is a Spring Boot based application to manage cases for Immigration & Asylum Appellants and Legal Representatives.

### Prerequisites

To run the project you will need to have the following installed:

* Java 17
* Docker (optional)

For information about the software versions used to build this API and a complete list of it's dependencies see build.gradle

### Building the application

To compile (at the command-line):

```
./gradlew clean compileJava
```

To build and run local tests:

```
./gradlew clean build
```

### Running the application

To run the API quickly use the docker helper script as follows: (make sure to have the required environment variables set as under functional tests section)

```
./bin/run-in-docker.sh --clean --install
```

Alternatively, you can start the application from the current source files using Gradle as follows:

```
./gradlew clean bootRun
```

**Note**: this will only work if you have the dependent services running as well.

If required, to run with a low memory consumption, the following can be used:

```
./gradlew --no-daemon assemble && java -Xmx384m -jar build/libs/ia-case-*.jar
```

### Using the application

To understand if the application is working, you can call its health endpoint:

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

In order for these tests to run successfully you will need the dependent services to be running.  Firstly, CCD will need to be running, a dockerised version of it and its dependencies along with instructions for running locally can be found at https://github.com/hmcts/ccd-docker.

Secondly, the `ia-case-documents-api` needs to be running, this api along with instructions to run locally can be found at https://github.com/hmcts/ia-case-documents-api. 

And lastly the `ia-notifications-api` needs to be running.  This api along with instructions to run locally can be found at https://github.com/hmcts/ia-case-notifications-api.

To successfully interact with the above dependencies, a few environment variables need to be set as below. The examples (the values below are not real; replace them with values matching those in the latest CCD Definition spreadsheet):

| Environment Variable | *Example values*  |
|----------------------|----------|
| TEST_ADMINOFFICER_USERNAME        |  ia-adminofficer@example.com          |
| TEST_ADMINOFFICER_PASSWORD        |  password                             |
| TEST_HOMEOFFICE_APC_USERNAME      |  ia-respondentapc@example.com         |
| TEST_HOMEOFFICE_APC_PASSWORD      |  password                             |
| TEST_HOMEOFFICE_LART_USERNAME     |  ia-respondentlart@example.com        |
| TEST_HOMEOFFICE_LART_PASSWORD     |  password                             |
| TEST_HOMEOFFICE_POU_USERNAME      |  ia-respondentpou@example.com         |
| TEST_HOMEOFFICE_POU_PASSWORD      |  password                             |
| TEST_HOMEOFFICE_GENERIC_USERNAME  |  ia-respondentgen@example.com         |
| TEST_HOMEOFFICE_GENERIC_PASSWORD  |  password                             |
| TEST_CASEOFFICER_USERNAME         |  ia-caseofficer@example.com           |
| TEST_CASEOFFICER_PASSWORD         |  password                             |
| TEST_JUDICIARY_USERNAME           |  ia-judiciary@example.com             |
| TEST_JUDICIARY_PASSWORD           |  password                             |
| TEST_LAW_FIRM_A_USERNAME          |  ia-law-firm-a@example.com            |
| TEST_LAW_FIRM_A_PASSWORD          |  password                             |
| TEST_LAW_FIRM_B_USERNAME          |  ia-system-user@example.com           |
| TEST_LAW_FIRM_B_PASSWORD          |  password                             |
| IA_SYSTEM_USERNAME                |  ia-system-user@example.com           |
| IA_SYSTEM_PASSWORD                |  password                             |
| IA_IDAM_CLIENT_ID                 |  some-idam-client-id                  |
| IA_IDAM_SECRET                    |  some-idam-secret                     |
| IA_IDAM_REDIRECT_URI              |  http://localhost:3451/oauth2redirect |
| IA_S2S_SECRET                     |  some-s2s-secret                      |
| IA_S2S_MICROSERVICE               |  some-s2s-gateway                     |
| IA_CCD_DIR                        |  ../ia-ccd-definitions/               |
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

You can then publish your pact tests locally by first running the pact docker-compose:

```
docker-compose -f docker-pactbroker-compose.yml up

```

and then using it to publish your tests:

```
./gradlew pactPublish
```


### Running mutation tests tests:

If you have some time to spare, you can run the *mutation tests* as follows:

```
./gradlew pitest
```

As the project grows, these tests will take longer and longer to execute but are useful indicators of the quality of the test suite.

More information about mutation testing can be found here:
http://pitest.org/ 

#### Validate CCD definitions and ia-case-api compatibility

There is a need to check the compatibility of `ia-case-api` Pull Request code changes and existing CCD definitions imported to Production before every release. We can't release changes to `ia-case-api` where, for example, we might be writing to a non-existing case data field. Depending on the event scope, it could block case data progress for a particular event or even for all events.

The script has been prepared to identify approximately 95% of potential issues by scanning local `ia-case-api` changes and existing CCD definitions. The script can't eliminate breaking changes entirely, however. If you do complex refactoring, it is always good to ask your colleagues for advice.

Before running the script make sure you set up correct branches on your local:
- ia-ccd-definitions -> master branch
- ia-case-api -> RIA-* feature branch

Run the script
```
yarn validate
```

Standard output will show INFOs, WARNs and ERRORs logs. There is a need to check all WARNs as they are potential compatibility issues. Any ERROR tells that there is a need for intermediate CCD definitions which should include missing field definitions.

Intermediate CCD definitions must be imported to Production before any `ia-case-api` breaking changes are merged to `master`. Once it is done you can re-run the validation script.

There is an `IGNORED` array defined in `validate_case_api.js` script. If you think the validation script gives you false positives, please add a new entry to the array.

### WA local env integration

This is only for WA devs or devs who work with the WA local env and this service. 

To integrate this service with the WA local env we have to set the wa-local profile.
To set the profile pick one of the options: 

1. From the terminal run: 
```shell
./gradlew --no-daemon assemble && java -Xmx384m -jar -Dspring.profiles.active=wa-local build/libs/ia-case-api.jar
```

2. From IntelliJ: Edit the Run/Debug configuration dialog and add the profile.


## Adding Git Conventions

### Include the git conventions.
* Make sure your git version is at least 2.9 using the `git --version` command
* Run the following command:
```
git config --local core.hooksPath .git-config/hooks
```
Once the above is done, you will be required to follow specific conventions for your commit messages and branch names.

If you violate a convention, the git error message will report clearly the convention you should follow and provide
additional information where necessary.

*Optional:*
* Install this plugin in Chrome: https://github.com/refined-github/refined-github

  It will automatically set the title for new PRs according to the first commit message, so you won't have to change it manually.

  Note that it will also alter other behaviours in GitHub. Hopefully these will also be improvements to you.

*In case of problems*

1. Get in touch with your Technical Lead and inform them, so they can adjust the git hooks accordingly
2. Instruct IntelliJ not to use Git Hooks for that commit or use git's `--no-verify` option if you are using the command-line
3. If the rare event that the above is not possible, you can disable enforcement of conventions using the following command

   `git config --local --unset core.hooksPath`

   Still, you shouldn't need to do this so make sure you get in touch with a Technical Lead soon afterward.

## Creating AiP users using the scripts
To create AiP citizen users, run `az login` and navigate to `/bin/utils/aip_scripts` and run `export IDAM_TESTING_ACCESS_TOKEN=$(zsh ./get_idam_token.zsh <environment>)`

Then run `zsh ./create-test-user.zsh <environment> <email (optional)>` where `<environment>` is either `aat`, `demo`, `perftest`, `ithc` and `<email (optional)>` is an optional email address, if not entered then it will generate one automatically in the format of `citizen-SOME_UUID@mailnesia.com`.

NB. The token will expire every 8 hours, so if the create-test-user doesn't return an email address, it's likely that the first step will need to be re-run.
NB2. Use aat env variable for preview environment user creation as it uses aat's IDAM instance.

## Development / Debugging Environment - Preview with Mirrord

Check the documentation in ./README-MIRRORD.md

