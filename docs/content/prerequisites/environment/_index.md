+++
date = '2025-06-05T23:18:03+02:00'
title = 'Environment Setup'
weight = '2'
+++

You can start the workshop using a Cloud Development Environment, a DevContainer or a local setup.

## Cloud Development Environment (CDE)?

A **Cloud Development Environment** is an online platform that provides all the tools and resources needed for software development directly in the cloud. Instead of installing and configuring software on your local machine, you can access a ready-to-use development workspace through your web browser. This allows you to code, build, and test applications from anywhere, collaborate easily with others, and ensures a consistent setup for all team members.

{{% notice warning %}}
GitHub Codespaces does not work well on a shared public Wifi (you can use your mobile phone data plan to overcome this issue)
{{% /notice %}}

{{% notice tip %}}
Most of the CDE provides monthly free credits which are largely sufficient for this workshop.
{{% /notice %}}

## DevContainer

A **DevContainer** is a development environment defined by configuration files that specify the tools, extensions, and settings needed for a project. It uses containers (such as Docker) to create a consistent and isolated workspace, ensuring that everyone on the team works with the same setup regardless of their local machine. DevContainers are especially useful for onboarding, collaboration, and avoiding "it works on my machine" issues.

## Which environment?

* **CDE**: You don’t want to bother to setup your environment,
* **DevContainer**: You can’t use a CDE or prefer to have code available locally,
* **Local**: You prefer to master everything, we provide instructions to setup a local installation.

Follow the instructions which suits you the most:

{{% children containerstyle="div" style="h3" description=false %}}