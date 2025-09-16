## How to setup mirrord

# Azure login
 Before any mirrord usage, login to azure with:
    > az login
    > az aks get-credentials --resource-group cft-preview-01-rg --name cft-preview-01-aks --subscription DCD-CFTAPPS-DEV --overwrite

# Get Pods name
    Get the pod name with:
        > kubectl get pods -n ia | grep 2711
        where 2711 is the PR number

# Installation and execution of mirrord in intellij
    * install mirrord plugin for intellij CE.
    No need of intalling mirrord localy
    * use the mirrod button on intellij, the first time it will create a .mirrord/mirrord.json
    * Have the vpn on and have the application deployed in preview
    * get the pod name with kubectl get pods -n ia | grep 2711
    * update the .mirrord/mirrord.json with something like this:
    {
        "target": {
            "path": "pod/ia-case-api-pr-2711-java-559cb9c7c7-t5lrs",
            "namespace": "ia"
        },
        "agent": {
        "startup_timeout": 300,
        "namespace": "ia"
        }
    }
Path is the java kubernete pod name
* start mirrord with the mirrord button
* then on the right menu, click on gradle and choose the task bootRun, right click to debug

# Intellij Kotlin issue
If issue with kotlin dependency, add this in the build.gradle in dependencies section:
in dependencies {
    modules {
// Add Kotlin runtime for mirrord compatibility
runtimeOnly 'org.jetbrains.kotlin:kotlin-stdlib:1.9.22'
runtimeOnly 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
runtimeOnly 'org.jetbrains.kotlin:kotlin-reflect:1.9.22'

# check if the agent has been created in a cluster
kubectl get pods -n ia | grep mirrord

# Failed to create mirrord-agent issue
If this message:Failed to create mirrord-agent: Timeout waiting for agent to be ready
Check the pod name in mirrord.json is same as returned by kubectl get pods -n ia | grep 2711
If necessary update the name in mirrord.json
Sometime jenkins build need to be rerun to get a new pod name working

# Invalidate caches in intellij
some time it can help to invalidate caches in intellij
go in intellij menu to File -> Invalidate Caches / Restart -> Invalidate and Restart

# Docker
Not need it. but sometime start it an stop fix some issues ???

# installation with Visual Studio Code
* Mirrord extension not working correctly instead vsc will the local mirrord
* Install mirrord with homebrew
    > brew install mirrord
* Setup settings in vcs. It's better to explecitly set the jdk that will be used by VSC.
So in .vscode/settings.json add
{
  "java.jdt.ls.java.home": "/Library/Java/JavaVirtualMachines/jdk-17.0.5.jdk/Contents/Home",
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "/Library/Java/JavaVirtualMachines/jdk-17.0.5.jdk/Contents/Home",
      "default": true
    }
  ],
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic"
}
in .vscode/tasks.json add
{
  "version": "2.0.0",
  "tasks": [
  {
      "label": "mirrord-gradle-bootrun-with-debug",
      "type": "shell",
      "command": "mirrord",
      "args": [
        "exec",
        "--config-file",
        ".mirrord/mirrord.json",
        "--",
        "./gradlew",
        "bootRun",
        "--debug-jvm",
        "--no-daemon",
        "--no-parallel",
        "--max-workers=1"
      ],
      "options": {
        "env": {
          "JAVA_HOME": "/Library/Java/JavaVirtualMachines/jdk-17.0.5.jdk/Contents/Home",
          "GRADLE_OPTS": "-Dorg.gradle.daemon=false"
        }
      },
      "group": "build",
      "isBackground": true,
      "problemMatcher": {
        "pattern": {
          "regexp": "."
        },
        "background": {
          "activeOnStart": true,
          "beginsPattern": "^.*Tomcat initialized.*",
          "endsPattern": "^.*Started.*Application.*"
        }
      },
      "presentation": {
        "echo": true,
        "reveal": "always",
        "focus": false,
        "panel": "shared"
      }
    }
  ]
}
Add in .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Mirrord With Debug",
      "request": "attach",
      "hostName": "localhost",
      "port": 5005,
      "preLaunchTask": "mirrord-gradle-bootrun-with-debug"
    }
  ]
}
* setup mirrord with steal mode to having breakpoints blocking the application in preview. The other mode is mirror that just mirror the traffic and do not block it (very useful for mirroring production traffic)
in .mirrord/mirrord.json
add 
{
  "target": {
    "path": "pod/ia-case-api-pr-2727-java-f49d45d6d-5ffhd",
    "namespace": "ia"
  },
  "agent": {
    "startup_timeout": 300,
    "namespace": "ia"
  },
  "feature": {
    "network": {
      "incoming": {
        "mode": "steal"
      },
      "outgoing": {
        "filter": {
          "local": ["127.0.0.1", "localhost"]
        }
      }
    }
  }
}

## Using npx and bin scripts
As an alternative for a development environment there is a procedure in place where after running the command below the required services are created in Preview under the developer's name, so these will be exclusively for the named developer use.

While connected to the VPN run one of the below command from your project's (ia-case-api) folder:
Note: be sure to have Docker running

```shell
npx @hmcts/dev-env@latest && ./bin/setup-devuser-preview-env.sh
```

Then to check that the environment is up in preview

```shell
// point kubectl at preview
az aks get-credentials --resource-group cft-preview-01-rg --name cft-preview-01-aks --subscription DCD-CFTAPPS-DEV --overwrite

// list preview dev enviroment pods, substitute mike for your mac usersname 
kubectl -n ia get pods | grep mike
```

The above should list roughly 30 pods. Wait until all pods, but specifically "ia-case-api-mike-ccd-definition-store" , are up and running and look like the below

```shell
ia-case-api-mike-ccd-definition-store-55fc7d9695-r945t                   1/1     Running
```

You should now be able to connect via Mirrord. First install the Intellij plugin (older versions of Intellij seem to not work with the latest plugin which is required, at time of writing Intellij 2024.3.2.1 plugin 3.66.0)
https://mirrord.dev

After the plugin is installed there should be a Mirror icon near the top right of Intellij, click that and debug the app. This should display some quick dialog box saying roughly "waiting for pod to be ready". Then in the Intellij console you should see the app starting up with the usually spring boot stuff.
Break points and debug should now behave as normal.

e.g. the method getCcdEventAuthorizor() in SecurityConfiguration line 93 is a good place to test things are working as this point in the code should get executed as the app inializes

If you want to clean up the environment just run:

```shell
npx @hmcts/dev-env@latest --delete
```