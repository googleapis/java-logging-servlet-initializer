# Google Cloud Logging Servlet Initializer for Java

Java EE idiomatic servlet container initializer to capture Http request context.

[![Maven][maven-version-image]][maven-version-link]
![Stability][stability-image]

- [Client Library Documentation][javadocs]

> **NOTE:** This client is a work-in-progress, and may occasionally
> make backwards-incompatible changes.

## Quickstart

If you are using Maven, add this to your pom.xml file:

```xml
<dependency>
  <groupId>com.google.cloud</groupId>
  <artifactId>google-cloud-logging-servlet-initializer</artifactId>
</dependency>
```

If you are using Gradle without BOM, add this to your dependencies

```Groovy
implementation 'com.google.cloud:google-cloud-logging-servlet-initializer'
```

If you are using SBT, add this to your dependencies

```Scala
libraryDependencies += "com.google.cloud" % "google-cloud-logging-servlet-initializer"
```

## Getting Started

### Installation and setup

You'll need to obtain the `google-cloud-logging-servlet-initializer` library. See the [Quickstart](#quickstart) section
to add `google-cloud-logging-servlet-initializer` as a dependency in your code.

## About Cloud Logging Servlet Initializer

Cloud Logging Servlet Initializer saves you an effort to capture the logging context of the servlet request.
The captured context includes [Http request](https://github.com/googleapis/java-logging/blob/86223ff36f9c4b147f322ba646607727b92fbe7b/google-cloud-logging/src/main/java/com/google/cloud/logging/HttpRequest.java) and tracing information such as trace and span Ids.
The initializer is registered using [Service Provider Interface](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) with the Java EE Servlet Container and adds a listener for servlet request events to capture the current context of the servlet requests.

Using Cloud Logging Servlet Initializer developers can enrich logs they write to Cloud Logging with Http request and tracing information without adding a single line of code.

### Usage

Add the library to your pom.xml in addition to `google-cloud-logging` or `google-cloud-logging-logback`.
If you use asynchronous execution to process servlet requests and need to propogate the context to child threads (threads that are forked from the request handler's thread) you would need to set the `useInheritedContext` property in the logging configuration to `true`. Remember to provide the path to the configuration file as a system property: `-Djava.util.logging.config.file=/path/to/logging.properties`.

```text
com.google.cloud.logging.ContextHandler.useInheritedContext=true
```

When the `useInheritedContext` property is set to `true`, the context stored for the request handler's thread will be inherited by all child threads that the thread creates.
> **NOTE:** If child threads are not disposed at the end of the request handling, consider avoiding context inheritance.
> Context clean up removes the context for the request handler's thread _ONLY_.

By default the context is saved for the request handler's thread only. If you do not use the `useInheritedContext` property, you can set the context for other threads explicitly using the [`com.google.cloud.logging.ContextHandler`][context-handler-class] class.

## Samples

Samples are in the [`samples/`](https://github.com/googleapis/java-logging-servlet-initializer/tree/main/samples) directory.

## Troubleshooting

To get help, follow the instructions in the [shared Troubleshooting document][troubleshooting].

## Supported Java Versions

Java 8 or above is required for using this client.

Google's Java client libraries,
[Google Cloud Client Libraries][cloudlibs]
follow the
[Oracle Java SE support roadmap][oracle]
(see the Oracle Java SE Product Releases section).

## Other dependencies

The library uses Jakarta Servlet package version 4.0.4. Older versions of `javax.servlet` are not supported.

### For new development

In general, new feature development occurs with support for the lowest Java
LTS version covered by  Oracle's Premier Support (which typically lasts 5 years
from initial General Availability). If the minimum required JVM for a given
library is changed, it is accompanied by a [semver][semver] major release.

Java 11 and (in September 2021) Java 17 are the best choices for new
development.

### Keeping production systems current

Google tests its client libraries with all current LTS versions covered by
Oracle's Extended Support (which typically lasts 8 years from initial
General Availability).

#### Legacy support

Google's client libraries support legacy versions of Java runtimes with long
term stable libraries that don't receive feature updates on a best efforts basis
as it may not be possible to backport all patches.

Google provides updates on a best efforts basis to apps that continue to use
Java 7, though apps might need to upgrade to current versions of the library
that supports their JVM.

#### Where to find specific information

The latest versions and the supported Java versions are identified on
the individual GitHub repository `github.com/GoogleAPIs/java-SERVICENAME`
and on [google-cloud-java][g-c-j].

## Versioning

This library follows [Semantic Versioning](http://semver.org/).

It is currently in major version zero (``0.y.z-alpha``), which means that anything may change at any time
and the public API should not be considered stable.

## Contributing

Contributions to this library are always welcome and highly encouraged.

See [CONTRIBUTING][contributing] for more information how to get started.

Please note that this project is released with a Contributor Code of Conduct. By participating in
this project you agree to abide by its terms. See [Code of Conduct][code-of-conduct] for more
information.

## License

Apache 2.0 - See [LICENSE][license] for more information.

Java is a registered trademark of Oracle and/or its affiliates.

[javadocs]: https://cloud.google.com/java/docs/reference/google-cloud-logging-servlet-initializer/latest/history
[stability-image]: https://img.shields.io/badge/stability-alpha-orange
[maven-version-image]: https://img.shields.io/maven-central/v/com.google.cloud/google-cloud-logging-servlet-initializer.svg
[maven-version-link]: https://search.maven.org/search?q=g:com.google.cloud%20AND%20a:google-cloud-logging-servlet-initializer&core=gav
[authentication]: https://github.com/googleapis/google-cloud-java#authentication
[auth-scopes]: https://developers.google.com/identity/protocols/oauth2/scopes
[predefined-iam-roles]: https://cloud.google.com/iam/docs/understanding-roles#predefined_roles
[iam-policy]: https://cloud.google.com/iam/docs/overview#cloud-iam-policy
[developer-console]: https://console.developers.google.com/
[create-project]: https://cloud.google.com/resource-manager/docs/creating-managing-projects
[cloud-sdk]: https://cloud.google.com/sdk/
[troubleshooting]: https://github.com/googleapis/google-cloud-common/blob/main/troubleshooting/readme.md#troubleshooting
[contributing]: https://github.com/googleapis/java-logging-servlet-initializer/blob/main/CONTRIBUTING.md
[code-of-conduct]: https://github.com/googleapis/java-logging-servlet-initializer/blob/main/CODE_OF_CONDUCT.md#contributor-code-of-conduct
[license]: https://github.com/googleapis/java-logging-servlet-initializer/blob/main/LICENSE

[enable-api]: https://console.cloud.google.com/flows/enableapi?apiid=logging.googleapis.com
[libraries-bom]: https://github.com/GoogleCloudPlatform/cloud-opensource-java/wiki/The-Google-Cloud-Platform-Libraries-BOM
[shell_img]: https://gstatic.com/cloudssh/images/open-btn.png

[semver]: https://semver.org/
[cloudlibs]: https://cloud.google.com/apis/docs/client-libraries-explained
[apilibs]: https://cloud.google.com/apis/docs/client-libraries-explained#google_api_client_libraries
[oracle]: https://www.oracle.com/java/technologies/java-se-support-roadmap.html
[g-c-j]: http://github.com/googleapis/google-cloud-java
[java-logging]: https://github.com/googleapis/java-logging
[context-handler-class]: https://github.com/googleapis/java-logging/blob/main/google-cloud-logging/src/main/java/com/google/cloud/logging/ContextHandler.java
