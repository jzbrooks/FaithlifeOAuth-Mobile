# FaithlifeOAuth-Mobile

[![Build](https://github.com/Faithlife/FaithlifeOAuth-Mobile/workflows/build/badge.svg)](https://github.com/Faithlife/FaithlifeOAuth-Mobile/actions?query=workflow%3Abuild)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.faithlife/faithlife-oauth-android/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.faithlife/faithlife-oauth-android)

Generates OAuth 1.0 signatures for web service requests on iOS and Android.

[changelog](changelog.md)

### Download

#### Android

Download [the latest AAR](https://search.maven.org/remote_content?g=com.faithlife&a=faithlife-oauth-android&v=LATEST) or download

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
implementation("com.faithlife:faithlife-oauth-android:$version")
```
#### iOS

XCFrameworks can be downloaded from the release page.

## Build instructions

This project uses the Gradle build system.

To build the library: `/.gradlew build`
To see all available tasks: `./gradlew tasks`
