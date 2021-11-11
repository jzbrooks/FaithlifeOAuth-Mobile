# FaithlifeOAuth-Mobile

[![Build](https://github.com/Faithlife/FaithlifeOAuth-Mobile/workflows/build/badge.svg)](https://github.com/Faithlife/FaithlifeOAuth-Mobile/actions?query=workflow%3Abuild)

Generates OAuth 1.0 signatures for web service requests on iOS and Android.

[changelog](changelog.md)

### Download

#### Android

via Maven:

```xml
<dependency>
  <groupId>com.faithlife</groupId>
  <artifactId>faithlife-oauth-android</artifactId>
  <version>$version</version>
</dependency>
```

via Gradle:

```kotlin
implementation("com.faithlife:android-lint:$version")
```
#### iOS

XCFrameworks can be downloaded from the release page.

## Build instructions

This project uses the Gradle build system.

To build the library: `/.gradlew build`
To see all available tasks: `./gradlew tasks`
