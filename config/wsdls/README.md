### Introduction

This is the [Gradle](http://www.gradle.org) project for generating and copying the necessary WSDL jars for the Force.com IDE.

### Usage

1. Ensure that you have a 1.8 version of the JDK installed.
2. Place the latest versions of the WSDL files into src/main/resources directory
3. If a particular WSDL needs any special configurations, modify build.gradle
4. Execute `gradlew` from this directory. It will generate the files and copy them over.


