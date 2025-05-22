# Contributing

Thanks for your interest in contributing! Please take a moment to review this document **before submitting a pull request**.

## Feature Request

**we request that contributors create a feature request[add_url] to first discuss any new ideas. Your ideas and suggestions are welcome!**

## Where do I go from here?

For any questions, support, or ideas, etc. [please create a GitHub discussion](add_url). 
If you've noticed a bug, [please submit an issue][add_new_issue_link].

### Fork and create a branch

If this is something you think you can fix, then [add_repo_url] and create
a branch with a descriptive name. 


###  Getting Started

Add details

### Prerequisites

* [Java jdk 11+](https://jdk.java.net/archive/)
* [Maven 3.6](https://maven.apache.org/)
* [Spring-Boot 2.7.10](https://spring.io/blog/2023/03/23/spring-boot-2-7-10-available-now)

#### Ignite Dependencies

* parent pom:
  version of other modules and 3rd-party library used in notification center

* streambase jar

```xml
 <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>streambase</artifactId>
    <scope>test</scope>
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
    <groupId>org.eclipse.ecsp.platform</groupId>
    <artifactId>redis-cache</artifactId>
 </dependency>
```

* ignite redis

```xml
 <dependency>
    <groupId>org.eclipse.ecsp.platform</groupId>
    <artifactId>redis-jar</artifactId>
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

*ignite transformer

```xml
  <dependency>
    <groupId>org.eclipse.ecsp</groupId>
    <artifactId>ignite-transformers</artifactId>
  </dependency>
```

*sp/pom:

*services-commons

```xml
  <dependency>
   	<groupId>org.eclipse.ecsp.platform.services</groupId>
  	<artifactId>services-common</artifactId>
  </dependency>
```

*api/pom:

*services-commons

```xml
  <dependency>
   	<groupId>org.eclipse.ecsp</groupId>
    <artifactId>api-common</artifactId>
  </dependency>
```
*common/pom:

*ignite-security

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

Plugin:
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

## Test Guidelines

Apart from unit test we can run the system tests to ensure none of the flows are broken.

## Create a Pull Request

A PR must be created with a clear title and description against the `master` branch. 
All tests must be passing before we will review the PR.

## Merging a PR and Shipping a release (maintainers only)

* A PR can only be merged into master by a maintainer if: CI is passing, approved by another maintainer and is up-to-date with the default branch.
* Any maintainer is allowed to merge a PR if all of these conditions are met.
* The generated changelog in the PR should include all user visible changes you intend to ship.

## Code Reviews
If you can, please look at open PRs and review them. Give feedback and help us merge these PRs much faster! 
If you don't know how, GitHub has some <a href="https://help.github.com/articles/about-pull-request-reviews/">great information on how to review a Pull Request.</a>

## Community
Add details for any other channels created to discuss contributions,
List the author, maintainers, and/or contributors here, or set expectations for response time.