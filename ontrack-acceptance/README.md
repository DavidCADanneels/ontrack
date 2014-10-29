Acceptance tests
================

## Standalone mode

For testing `ontrack` in real mode, with the application to test deployed on a remote machine, it is needed to be
able to run acceptance tests in standalone mode, without having to check the code out and to build it.

The acceptance tests are packaged as a standalone JAR, that contains all the dependencies.

To run the acceptance tests, you need a JDK8 and you have to run the JAR using:

    java -jar ontrack-acceptance-<version>-test.jar <options>

The options are:

* `--option.url=<url>` to specify the `<url>` where `ontrack` is deployed. It defaults to http://localhost:8080
* `--option.admin=<password>` to specify the administrator password. It defaults to `admin`.
* `--option.context=<context>` can be specified several time to define the context(s) the acceptance tests are running
in (like `--option.context=production` for example). According to the context, some test can be excluded from the run.

The results of the tests is written as a JUnit XML file, `ontrack-acceptance.xml`.

