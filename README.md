# Choral [![Build Status](https://github.com/choral-lang/choral/actions/workflows/maven-tests.yml/badge.svg?event=push)](https://github.com/choral-lang/choral/actions/workflows/maven-tests.yml)

Choral is a [choreographic programming language](https://en.wikipedia.org/wiki/Choreographic_programming) that compiles to Java. Try out the [VS Code extension](https://marketplace.visualstudio.com/items?itemName=choral.vscode-choral), look at some [examples](https://github.com/choral-lang/examples), and use it in your own project with the [Maven plugin](https://github.com/choral-lang/choral-maven-plugin). For a guided tour, check out the [official documentation](https://www.choral-lang.org).

## Local Development

### Installation

To build the compiler from source, install [Maven](https://maven.apache.org/) and run the command `mvn install`.
Use `mvn test` to run the test suite.

To use the `choral` executable, you'll need to update your `PATH` and 
`CHORAL_HOME` environment variables:
```
export PATH="PATH_TO_CHORAL/scripts:$PATH"
export CHORAL_HOME="PATH_TO_CHORAL/dist/target"
```
To compile a Choral class called `HelloRoles`, run the command `choral epp HelloRoles`.

### Testing

When running the command `mvn test`, the option `-DliftVerbose` is available if one wishes to see verbose warnings from the ClassLifter. This is disabled by default. 

### Issuing a new release

To issue a new version of Choral, go to [releases](https://github.com/choral-lang/choral/releases) and publish a release for that commit. The tag must use the exact format `v#.#.#`, for example `v0.1.13`.

Publishing the release triggers a [GitHub workflow](https://github.com/choral-lang/choral/actions/workflows/maven-publish.yml) that validates the tag, tests Choral, publishes the libraries to Maven Central, and attaches `choral-#.#.#.zip` to the GitHub release.
