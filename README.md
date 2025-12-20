# Choral

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

## Issuing a new release

To issue a new version of Choral:

1. Bump the version number under `project.properties.revision` in the root [`pom.xml`](/pom.xml) file.
2. Make a new commit with the change and push it to Github.
3. Go to [releases](https://github.com/choral-lang/choral/releases) and create a new release. Make sure that the tag follows the exact format `v#.#.#`, where `#.#.#` is the new version number from step 1.
4. After the release has been created, a new [action](https://github.com/choral-lang/choral/actions) will have started. When it finishes, download the `choral-#.#.#.zip` artifact and upload it to the release.

Following these steps, ensures that the installation script and maven package is updated properly.
