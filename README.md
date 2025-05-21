[<img src="./images/logo.png" width="400" height="200"/>](./images/logo.png)

[![Build Status](replace with badge.svg)]( add url)
[![Artifactory](replace with badge.svg)](add url)
[![GitHub contributors](replace with badge.svg)](add url)
[![Coverage](replace with badge.svg)](add url)
[![License](replace with badge.svg)](#license)

# Notification Center
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

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 
Add details

### Prerequisites

* [Java jdk 11+](https://jdk.java.net/archive/)
* [Maven 3.6](https://maven.apache.org/)
* [Spring-Boot 2.7.10](https://spring.io/blog/2023/03/23/spring-boot-2-7-10-available-now)

#### Ignite Dependencies

* parent pom:
  version of other modules and 3rd-party library used in notification center

* stream-base jar

```xml
 <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>streambase</artifactId>
 </dependency>
```

* stream-base test jar

```xml
 <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>streambase</artifactId>
    <type>test-jar</type>
    <scope>test</scope>
 </dependency>
```

* ignite cache

```xml
 <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>cache-enabler</artifactId>
 </dependency>
```

* ignite dao

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>nosql-dao</artifactId>
  </dependency>
```

* ignite utils

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>utils</artifactId>
  </dependency>
```

* ignite transformer

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>transformers</artifactId>
  </dependency>
```

* sp/pom:

* services-commons

```xml
  <dependency>
   	<groupId>org.eclipse.ecsp</groupId>
  	<artifactId>services-common</artifactId>
  </dependency>
```

* api/pom:

* services-commons

```xml
  <dependency>
   	<groupId>org.eclipse.ecsp</groupId>
    <artifactId>api-common</artifactId>
  </dependency>
```
* common/pom:

* ignite-security

```xml
  <dependency>
   	<groupId>org.eclipse.ecsp</groupId>
    <artifactId>ignite-security</artifactId>
  </dependency>
```

### Installation

A step by step series of examples that tell you how to get a  local development env running
Add details

```
Local dev - The script ./build-notification build the notification components and import all of their dependencies.
The script arguments:
'--component' - the component project to be built. Possible values sp or api
'--no-push' - flag indicating that docker image should be built and pushed to registry. By default is true.
'--run-test' - flag indicates whether maven build should run the tests. By default is false.

```

### Coding style check configuration

Explain the details for configuring style check in the IDE

* Eclipse: You can install the tool via the marketplace for Checkstyle Marketplace entry Or you can install into via
  Help Install new Software menu entry by using the following update site: https://checkstyle.org/eclipse-cs-update-site
* IntelliJ: Checkstyle plugin in IntelliJ provides both real-time and on-demand scanning of Java files with Checkstyle
  from within IDEA.

#### Using Checkstyle in the Eclipse IDE

1. Right-click on your project and select Checkstyle Check code with Checkstyle.<br/>
2. Afterwards open the checkstyle views to see the reported issues via Window Show View Others Checkstyle menu
   entry.<br/>
3. You can open the Checkstyle violations view to see the details or the Checkstyle violations chart to get a graphical
   overview of the issues.<br/>
4. In the Checkstyle violations view double-click on a violation to jump to the individual issues. Double-click on an
   individual issue to jump to it. Use the btn:Back[] button to navigate back.<br/>

####    

1. after installing checkstype plugin and restart of the IDE, you can see checkstyle in bottom tab
2. To add custom checkstyle.xml, please click on + sign in File → Settings → Tools → Checkstyle
3. Select dir icon to run for entire project or Run with the play button for single opened file in the tab<br>

#### Checkstyle can also be run during a Maven build.

The maven-checkstyle-plugin can generate reports about Checkstyle violations or can also be a part of the build and
cause a build failure when the rules defined in the checkstyle.xml are violated.

```xml
<properties>
    <checkstyle.config.location>${project.basedir}/checkstyle.xml</checkstyle.config.location>
</properties>

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-checkstyle-plugin</artifactId>
  <version>3.3.1</version>
  <configuration>
      <consoleOutput>true</consoleOutput>
      <failsOnError>true</failsOnError>
      <outputFileFormat>xml</outputFileFormat>
      <failsOnError>true</failsOnError>
      <includeTestSourceDirectory>true</includeTestSourceDirectory>
      <includeResources>true</includeResources>
      <includeTestResources>true</includeTestResources>
      <linkXRef>false</linkXRef>
  </configuration>
  <executions>
      <execution>
          <id>validate</id>
          <phase>validate</phase>
          <goals>
              <goal>checkstyle</goal>
          </goals>
      </execution>
   </executions>
   <dependencies>
      <dependency>
          <groupId>com.puppycrawl.tools</groupId>
          <artifactId>checkstyle</artifactId>
          <version>10.13.0</version>
      </dependency>
    </dependencies>
</plugin>
```

Note:  This plugin will generate HTML file under target/site/checkstyle.html. This HTML represent the issues found by
check style.

Link to
download [google_checks.xml](https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/google_checks.xml)

### Running the tests

Execute the Unit Test cases

```shell
mvn test
```
### Deployment

Steps for manual Deployment:

* Push the charts to the environment from the local.

* Execute helm install deployment command

```shell
helm install . -n notification -f values.yaml 
```

## Usage

	- As Service, I want to provision notification templates	
	- As Service or Core Enabler, I want to send notification (mobile push) to the user	
	- As Service or Core Enabler, I want to send email and sms notification to the user	
	- As Service or Core Enabler, I want to send a notification to the vehicle	
	- Create notification for campaign	
	- Notification History - List of notifications	
	- Notification History - Detail	
	- Notification Group Preferences	
	- Notification User Preferences		
	- As Service, I want to provision notification grouping/entitlement.	
	- Call Center Integration	
	- As Service I want to provision templates for notification	
	- Email attachments	
	- Quiet time	
	- Notification User Profile Using Webhook 2.0	
	- Send notification using model/any vehicle attribute specific templates	
	- Send notification using custom placeholders	
	- SM : Date Format in Notification	


## Built With Dependencies

* [Spring Boot](https://spring.io/projects/spring-boot/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [Java jdk 11+](https://jdk.java.net/archive/)

## How to contribute

Please read [CONTRIBUTING.md](https://github.com/your/project/CONTRIBUTING.md) for details on our contribution guidelines, and the process for submitting pull requests to us.

## Code of Conduct

Please read [CODE_OF_CONDUCT.md](https://github.com/your/project/CODE_OF_CONDUCT.md) for details on our code of conduct, and the process for submitting pull requests to us.


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

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## Security Contact Information

Add details on where to report a security vulnerability

## Support
Add support group details

## Troubleshooting

* Add link to any troubleshooting guideline
* Details on where to open issues or raise ticket for support

## License

This project is licensed under the MIT License - see the [LICENSE.md](https://github.com/your/project/LICENSE.md) file for details

## Announcements

All updates to this library are documented in our [CHANGELOG.md](https://github.com/your/project/CHANGELOG.md) and [releases](https://github.com/your/project/releases).
For the versions available, see the [tags on this repository](https://github.com/your/project/tags).

## Acknowledgments

* Acknowledgement 1
* Acknowledgement 2

