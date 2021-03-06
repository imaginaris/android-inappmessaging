[![Maven Central](https://img.shields.io/maven-central/v/io.github.rakutentech.inappmessaging/inappmessaging)](https://search.maven.org/artifact/io.github.rakutentech.inappmessaging/inappmessaging)
[![CircleCI](https://circleci.com/gh/rakutentech/android-inappmessaging.svg?style=svg)](https://circleci.com/gh/rakutentech/android-inappmessaging)
[![codecov](https://codecov.io/gh/rakutentech/android-inappmessaging/branch/master/graph/badge.svg)](https://codecov.io/gh/rakutentech/android-inappmessaging)

# In-App Messaging SDK for Android

Provides in-app messaging for Android applications. See the [User Guide](./inappmessaging/USERGUIDE.md) for instructions on implementing in an android application.

## How to build

This repository uses submodules for some configuration, so they must be initialized first.

```bash
$ git submodule init
$ git submodule update
$ ./gradlew assemble
```

## How to test the Sample app

You must first define your API config url and subscription key as either environment variables or as gradle properties (such as in your global `~/.gradle/gradle.properties` file).

```
IAM_SUBSCRIPTION_KEY=your_subscription_key
CONFIG_URL=https://www.example.com/
```

## How to use it

Currently we do not host any public APIs but you can create your own APIs and configure the SDK to use those.

## Contributing

See [Contribution guidelines](./CONTRIBUTING.md)
