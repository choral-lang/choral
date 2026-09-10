# Choral [![Build Status](https://github.com/choral-lang/choral/actions/workflows/maven-tests.yml/badge.svg?event=push)](https://github.com/choral-lang/choral/actions/workflows/maven-tests.yml)

A compiler for the Choral programming language. Learn more at [https://choral-lang.org/](https://choral-lang.org/).

To build the project, install [Maven](https://maven.apache.org/) and run the command `mvn install`.
Run the command `mvn test` to test the compiler.

To use the `choral` executable, you'll need to update your `PATH` and 
`CHORAL_HOME` environment variables:
```
export PATH="PATH_TO_CHORAL/scripts:$PATH"
export CHORAL_HOME="PATH_TO_CHORAL/dist/target"
```
To compile a Choral class called `HelloRoles`, run the command `choral epp HelloRoles`.

## Testing

When running the command `mvn test`, the option `-DliftVerbose` is available if one wishes to see verbose warnings from the ClassLifter. This is disabled by default. 

## Issuing a new release

To issue a new version of Choral, go to [releases](https://github.com/choral-lang/choral/releases) and publish a release for that commit. The tag must use the exact format `v#.#.#`, for example `v0.1.13`.

Publishing the release triggers a [GitHub workflow](https://github.com/choral-lang/choral/actions/workflows/maven-publish.yml) that validates the tag, tests Choral, publishes the libraries to Maven Central, and attaches `choral-#.#.#.zip` to the GitHub release.

The workflow requires valid `CENTRAL_TOKEN_USERNAME`, `CENTRAL_TOKEN_PASSWORD`, `GPG_SIGNING_KEY`, and `GPG_SIGNING_KEY_PASSWORD` repository secrets. Maven Central release versions and Git tags are immutable, so never reuse a released version.
