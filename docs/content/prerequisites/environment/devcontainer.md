+++
date = '2025-06-05T23:19:44+02:00'
title = 'DevContainer'
weight  = 3
+++

> You want to execute this workshop on your desktop with [DevContainers](https://containers.dev/).

A configuration to set the project up in DevContainer is available. You can check it out in the project [``.devcontainer/devcontainer.json``](https://github.com/worldline/observability-workshop/tree/main/.devcontainer) file.

If you want to know more about DevContainers, you can check out this [documentation](https://containers.dev/).

You **MUST** have set up these tools first:

* [Docker](https://docs.docker.com/),
* An IDE: ([IntelliJ IDEA](https://www.jetbrains.com/idea) or [VSCode](https://code.visualstudio.com/)).

🛠️ You can validate your environment running these commands:

**Docker**

```jshelllanguage
$ docker version
    Client:
    Docker Engine -Community
    Version:
    27.4.1
    API version:1.47
    Go version:go1.22.10
    Git commit:b9d17ea
    Built:Tue Dec 17 15:45:46 2024
    OS/Arch:linux/amd64
    Context:default

```

{{% notice tip %}}
This workshop was tested with:
* [Rancher Desktop](https://rancherdesktop.io/) as Docker environment,
* IDEs: VSCode and JetBrains IntelliJ.
{{% /notice %}}