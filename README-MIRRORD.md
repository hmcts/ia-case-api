# How to setup mirrord for local debugging 

This document explains how to setup mirrord for:
 * Visual Studio Code with launch.json file
 * IntelliJ Community Edition with mirrord plugin
 * IntelliJ Ultimate Edition with mirrord plugin and npx scripts

# Mirrord in Steal mode vs Mirror mode
Mirrord is a tool that allows you to debug locally an application deployed in a Kubernetes cluster. It has 2 modes: mirror and steal. For local debugging with preview, it must be used in steal mode.

# Azure login
Before any mirrord usage, login to Azure with:
```bash
az login
az aks get-credentials --resource-group cft-preview-01-rg --name cft-preview-01-aks --subscription DCD-CFTAPPS-DEV --overwrite
```

# Get Pods name
  You local mirrord configuration need to know the pod name of the application you want to debug.
  Get the pod name with:
  > kubectl get pods -n ia | grep <PRNUMBER>
  
  Look for the pod with -java- in the name
  Example: ia-case-api-pr-2711-java-559cb9c7c7-t5lrs

# check if the agent has been created in a cluster
When mirrord is started, it will create a mirrord-agent in the same namespace as the pod you want to debug.
Check if the agent is created with:
kubectl get pods -n ia | grep mirrord  

# Installation with Visual Studio Code (Tested on 15/09/2025)

The Mirrord VS Code plugin does not work correctly, so we'll use local mirrord and launch VS Code with launch.json and tasks.json.

## Prerequisites
* Install mirrord with Homebrew:
```bash
brew install mirrord
```

* Setup settings in VS Code. It's better to explicitly set the JDK that will be used by VS Code.
Find your local JDK installation and set it in both settings.json and tasks.json. Replace `/Library/Java/JavaVirtualMachines/jdk-17.0.5.jdk/Contents/Home` with your actual JDK path.

Add the following to `.vscode/settings.json`:
Add the following to `.vscode/settings.json`:
```json
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
```

in .vscode/tasks.json add
```json
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
```

Add in .vscode/launch.json
```json
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
```

* Setup mirrord with steal mode to enable breakpoints that block the application in preview.
Replace `<POD_NAME>` with the pod name you got with `kubectl get pods -n ia | grep <PRNUMBER>-j`
where  here <PRNUMBER> is the PR number of your branch.
example: > ia-case-api-pr-2711-java-559cb9c7c7-t5lrs
Create/update `.mirrord/mirrord.json`:
```json
{
  "target": {
    "path": "pod/<POD_NAME>",
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
```

* Restart VCS
* In the left menu, click on Run and Debug, select "Mirrord With Debug" and click on the green triangle to start debugging
* Set breakpoints in your code, when the breakpoint is reached, the application in preview will be blocked until you continue in VS Code. Initially setup breakpoint in every Controller to test it's all working fine.


# Installation and execution of mirrord in IntelliJ CE

## Prerequisites
* Install mirrord plugin for IntelliJ CE
* No need to install mirrord locally
* Have the VPN connected and the application deployed in preview

## Setup Steps
1. Use the mirrord button in IntelliJ. The first time it will create a `.mirrord/mirrord.json` file
2. Get the pod name with:
   ```bash
   kubectl get pods -n ia | grep <PRNUMBER>
    ```
3. update the .mirrord/mirrord.json with something like this:
```json    
    {
        "target": {
            "path": "pod/<POD_NAME>",
            "namespace": "ia"
        },
        "agent": {
        "startup_timeout": 300,
        "namespace": "ia"
        }
    }
```

* start mirrord with the mirrord button
* then on the right menu, click on gradle and choose the task bootRun, right click to debug

## Intellij CE Kotlin issue
If issue with kotlin dependency, add this in the build.gradle in dependencies section:
in dependencies 

```gradle
    modules {
// Add Kotlin runtime for mirrord compatibility
runtimeOnly 'org.jetbrains.kotlin:kotlin-stdlib:1.9.22'
runtimeOnly 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
runtimeOnly 'org.jetbrains.kotlin:kotlin-reflect:1.9.22'
    
```


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