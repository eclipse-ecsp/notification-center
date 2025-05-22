<div align="center">
  <img src="./images/logo.png" width="300" height="150"/>
</div>

# Notification Center

[![Maven Build & Sonar Analysis](https://github.com/eclipse-ecsp/notification-center/actions/workflows/maven-build.yml/badge.svg)](https://github.com/eclipse-ecsp/notification-center/actions/workflows/maven-build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=eclipse-ecsp_notification-center&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=eclipse-ecsp_notification-center)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=eclipse-ecsp_notification-center&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=eclipse-ecsp_notification-center)
[![License Compliance](https://github.com/eclipse-ecsp/notification-center/actions/workflows/licence-compliance.yaml/badge.svg)](https://github.com/eclipse-ecsp/notification-center/actions/workflows/licence-compliance.yaml)
[![Latest Release](https://img.shields.io/github/v/release/eclipse-ecsp/notification-center?sort=semver)](https://github.com/eclipse-ecsp/notification-center/releases)


Notification Center is an independently deployable component(microservice) intended for sending various types of notifications.It provides a common abstraction layer on top of the various notification types and tracks the status of the notifications.

# Table of Contents
* [Getting Started](#getting-started)
* [Usage](#usage)
* [How to contribute](#how-to-contribute)
* [Built with Dependencies](#built-with-dependencies)
* [Code of Conduct](#code-of-conduct)
* [Authors](#authors)
* [Security Contact Information](#security-contact-information)
* [Support](#support)
* [Troubleshooting](#troubleshooting)
* [License](#license)
* [Announcements](#announcements)
* [Acknowledgments](#acknowledgments)


## Getting Started

Notification center consists of below two components which are used for sending and managing the notification & alerts.
* notification-api
* notification-sp

To build the project in the local working directory after the project has been cloned/forked, run:

```mvn clean install```

from the command line interface.

### Prerequisites

The list of tools required to build and run the project:
- Java 17
- Maven
- Container environment

### Installation

- [Install Java 17](https://www.azul.com/downloads/?version=java-17-lts&package=jdk#zulu)

- [How to set up Maven](https://maven.apache.org/install.html)

- Install Docker on your machine by referring to official Docker documnentation to have a Container environment.

### Coding style check configuration

[checkstyle.xml](./checkstyle.xml) is the coding standard to follow while writing new/updating existing code.

Checkstyle plugin [maven-checkstyle-plugin:3.2.1](https://maven.apache.org/plugins/maven-checkstyle-plugin/) is integrated in [pom.xml](./pom.xml) which runs in the validate phase and check goal of the maven lifecycle and fails the build if there are any checkstyle errors in the project.

To run checkstyle plugin explicitly, run the following command:

```mvn checkstyle:check```

### Running the tests

To run the tests for this system run the below maven command.

```mvn test```

Or run a specific test

```mvn test -Dtest="TheFirstUnitTest"```

To run a method from within a test

```mvn test -Dtest="TheSecondUnitTest#whenTestCase2_thenPrintTest2_1"```

### Deployment

The component can be deployed as a Kubernetes pod by installing Notification Center charts.
Link: [Charts](../../../ecsp-helm-charts/tree/main/notification-center)

## Architecture

Sequence diagram of Notification Center:

## Usage

Notification Center component will be responsible for the below features,

1. As Service, I want to provision notification templates.

2. As Service or Core Enabler, I want to send notification (mobile push) to the user.

3. As Service or Core Enabler, I want to send email and sms notification to the user.

4. As Service or Core Enabler, I want to send a notification to the vehicle.

5. Create notification for campaign 

6. Notification History - List of notifications.

7. Notification History - Detail.

8. Notification Group Preferences

9. Notification User Preferences

10. As Service, I want to provision notification grouping/entitlement.

11. Call Center Integration.

12. As Service, I want to provision templates for notification.

13. Email attachments.

14. Quiet time.

15. Notification User Profile Using Webhook 2.0.Send notification using model/any vehicle attribute specific templates.

16. Send notification using custom placeholders.

17. SM : Date Format in Notification	


## Built With Dependencies

* [Spring](https://spring.io/projects/spring-framework) - Web framework used for building the application
* [Maven](https://maven.apache.org/) - Build tool used for dependency management
* [MongoDB](https://www.mongodb.com/) - NoSQL document database
* [Project Lombok](https://projectlombok.org/) - Auto-generates Java boilerplate code (e.g., getters, setters, builders)
* [Apache Common](https://commons.apache.org/proper/commons-lang/) - Java Library
* [Jackson](https://github.com/FasterXML) - Reading JSON Objects
* [Morphia](https://morphia.dev/landing/index.html) - A Java tool for mapping Java objects to MongoDB documents
* [Logback](https://logback.qos.ch/) - Concrete logging implementation used with SLF4J
* [slf4j](https://www.slf4j.org/) - Logging facade providing abstraction for various logging frameworks
* [Mockito](https://site.mockito.org/) - Mocking framework for testing
* [JUnit](https://junit.org/) - Unit testing framework

#### Dependencies Used

* parent pom:
  version of other modules and 3rd-party library used in notification center

* streambase jar

```xml
 <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>streambase</artifactId>
 </dependency>
```

* streambase test jar

```xml
 <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>streambase</artifactId>
    <type>test-jar</type>
    <scope>test</scope>
 </dependency>
```

* cache-enabler

```xml
 <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>cache-enabler</artifactId>
 </dependency>
```

* nosql-dao

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>nosql-dao</artifactId>
  </dependency>
```

* utils

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>utils</artifactId>
  </dependency>
```

* transformers

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>transformers</artifactId>
  </dependency>
```

* sp/pom:

* services-common

```xml
  <dependency>
   	<groupId>org.eclipse.ecsp</groupId>
  	<artifactId>services-common</artifactId>
  </dependency>
```

* api/pom:

* api-common

```xml
  <dependency>
   	<groupId>org.eclipse.ecsp</groupId>
    <artifactId>api-common</artifactId>
  </dependency>
```


## How to contribute

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on our contribution guidelines, and the process for submitting pull requests to us.

## Code of Conduct

Please read [CODE_OF_CONDUCT.md](./CODE_OF_CONDUCT.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Contributors

## Authors

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
	  <td align="center" valign="top" width="14.28%"><a href="https://github.com/Pankaj-Behere_harman"><img src="https://github.com/pbehe-harman.png" width="100px;" alt="Pankaj Behere"/><br /><sub><b>Pankaj Behere</b></sub></a><br /><a href="https://github.com/all-contributors/all-contributors/commits?author=Pankaj-Behere_harman" title="Code and Documentation">📖</a> <a href="https://github.com/all-contributors/all-contributors/pulls?q=is%3Apr+reviewed-by%3APankaj-Behere_harman" title="Reviewed Pull Requests">👀</a></td>
    </tr>
	<tr>
	  <td align="center" valign="top" width="14.28%"><a href="https://github.com/Lavin-Motwani_harman"><img src="https://github.com/LavinMotwani.png" width="100px;" alt="Lavin Motwani"/><br /><sub><b>Lavin Motwani</b></sub></a><br /><a href="https://github.com/all-contributors/all-contributors/commits?author=Lavin-Motwani_harman" title="Code and Documentation">📖</a> <a href="https://github.com/all-contributors/all-contributors/pulls?q=is%3Apr+reviewed-by%3ALavin-Motwani_harman" title="Reviewed Pull Requests">👀</a></td>
    </tr>
  </tbody>
</table>

The list of [contributors](../../graphs/contributors) who participated in this project.

## Security Contact Information

Please read [SECURITY.md](./SECURITY.md) to raise any security related issues.

## Support

Contact the project developers via the project's "dev" list - [ecsp-dev](https://accounts.eclipse.org/mailing-list/)

## Troubleshooting

Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on how to raise an issue and submit a pull request to us.

## License

This project is licensed under the Apache-2.0 License - see the [LICENSE](./LICENSE) file for details.

## Announcements

All updates to this component are present in our [releases page](../../releases).
For the versions available, see the [tags on this repository](../../tags).

