+++
date = '2025-06-05T23:19:47+02:00'
title = 'Local'
weight = 4
+++

> You prefer to run your environment locally and install all tools.

You **MUST** have set up these tools:

* [Java 21+](https://adoptium.net/temurin/releases/?version=21)
* [Gradle 8.7+](https://gradle.org/)
* [Docker](https://docs.docker.com/) & [Docker compose](https://docs.docker.com/compose/)
* Any
  IDE ([IntelliJ IDEA](https://www.jetbrains.com/idea), [VSCode](https://code.visualstudio.com/), [Netbeans](https://netbeans.apache.org/),...)
  you want
* [cURL](https://curl.se/), [jq](https://stedolan.github.io/jq/), [HTTPie](https://httpie.io/) or any tool to call your
  REST APIs

🛠️ Here are commands to validate your environment:

**Java**

```bash
$ java -version

openjdk version "21.0.3" 2024-04-16 LTS
OpenJDK Runtime Environment Temurin-21.0.3+9 (build 21.0.3+9-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.3+9 (build 21.0.3+9-LTS, mixed mode, sharing)
```

**Gradle**

🛠️ If you use the wrapper, you won't have troubles. Otherwise...:

```bash
$ gradle -version

------------------------------------------------------------
Gradle 8.7
------------------------------------------------------------

Build time:   2024-03-22 15:52:46 UTC
Revision:     650af14d7653aa949fce5e886e685efc9cf97c10

Kotlin:       1.9.22
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
JVM:          21.0.3 (Eclipse Adoptium 21.0.3+9-LTS)
OS:           Linux 5.15.146.1-microsoft-standard-WSL2 amd64
```

**Docker Compose**

``` bash
$ docker compose version
    
Docker Compose version v2.24.7
```