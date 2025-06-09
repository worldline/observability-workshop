+++
date = '2025-06-05T23:19:40+02:00'
title = 'CodeSandbox CDE'
weight = 1
+++

> You want to use CodeSandbox Cloud Development Environment to follow this workshop.

> [!IMPORTANT]
> A GitHub, Google or Apple account is required to register to CodeSandbox.

## Registration

* Go to [CodeSandbox.io](https://codesandbox.io) and click on the [Sign In](https://codesandbox.io/signin) link,
* Use your GitHub, Google or Apple account to register.

## Import the repository

* Click on the {{% button icon="github" color="black" %}}Import{{% /button %}} button (top right of your screen),
* In the `New Repository` view, select `Find by URL` and enter `worldline/observability-workshop`,
* Select the proposed repository and click on `+Import`

![Import Repository](../codesandbox_import.png)

* Click on the {{% button color="green" %}}Fork repository{{% /button %}} button.

After some minutes, CodeSandbox should have opened your code in a VSCode Web view.

![CodeSandbox VSCode View](../codesandbox_vscode.png)

> [!TIP]
> You have the choice to continue to work in the VSCode web view, or use the `Open in VS Code` button to use your Desktop instance instead.

## Virtual Machine settings

We recommend to use a stronger Virtual Machine for this workshop as it involves lot of services and components.

🛠️ Click on the square button on the top left of the screen and select Virtual Machine:

![CodeSandbox Virtual Machine property](../codesandbox_vm.png)

🛠️ Select the `Micro` sizing and click on {{% button color="green" %}}Apply{{% /button %}}

New Virtual Machine specifications should have been applied without restart.

> [!IMPORTANT]
> During the first startup, the gradle build is automatically started. Please wait until it is completely finished.

## Wait for startup!

It will take some time to the project start, so please be patient.

Before starting the infrastructure, you should wait that VSCode has correctly imported all the Java modules and packages.

![VSCode is ready](../vscode_ready.png)

### Troubleshooting: Gradle or Java error?

If Java state is in "Error" or see some Gradle issues, you can try the following VSCode commands (`CTRL-SHIFT-P`):

- `Gradle: Refresh Gradle Projects View`
- `Java: Clean Java Language Server Workspace`

### Environment Error: Failed to load DevContainer or Workspace?

You can reload your CodeSandbox environment with the following VSCode command (`CTRL-SHIFT-P`):

- `Developer: Reload Window`