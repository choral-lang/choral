# Choral

The source code of the Choral programming language. See [https://choral-lang.org/](https://choral-lang.org/).

To build the project locally, install [Maven](https://maven.apache.org/) and run

```shell
mvn clean install
```

This will generate `dist/target/choral-standalone.jar`, which can be run with

```shell
java -jar ./dist/target/choral-standalone.jar
```

Remember to update the PATH and CHORAL_HOME environment variables:
```
export PATH="PATH_TO_CHORAL/scripts:$PATH"

export CHORAL_HOME="PATH_TO_CHORAL/dist/target"
```

This will allow you to compile choral files using the following command instead:
```
choral epp HelloRoles
```

Notice, that when compiling a choral file, it is the name of the choral class you wish to compile, that you give as argument to the `epp` command, not the name of the file containing the class.

Example of what not to do: \
```choral epp HelloRoles.ch```

Example of what to do: \
```choral epp HelloRoles``` 

When using the choral compiler, remember to also not compile things from root, as this will cause the choral compiler to attempt to compile all choral files found in subdirectories, some of which might be broken at a given time. The ```scripts``` folder works well for this purpose. 

## Issuing a new release

To issue a new version of Choral:

1. Bump the version number under `project.properties.revision` in the root [`pom.xml`](/pom.xml) file.
2. Make a new commit with the change and push it to Github.
3. Go to [releases](https://github.com/choral-lang/choral/releases) and create a new release. Make sure that the tag follows the exact format `v#.#.#`, where `#.#.#` is the new version number from step 1.
4. After the release has been created, a new [action](https://github.com/choral-lang/choral/actions) will have started. When it finishes, download the `choral-#.#.#.zip` artifact and upload it to the release.

Following these steps, ensures that the installation script and maven package is updated properly.
