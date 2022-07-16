# Starting up and trying the whole thing
* **Author:** Damir Murat
* **Created:** 26.05.2020.
* **Updated:** 16.07.2022.

Environment:
- OSX (should work with any desktop Linux distro and with Windows with appropriate bash-shell like git-bash)
- JDK 11 (should work with JDK 8)
- Gradle 7.0.2 (exact version)
- IDEA Community/Ultimate 2022.2
- Docker
- Postman

## Cloning
To **clone** the klokwrk project, open your shell (shell-1) at the desired location and execute the following command:

    git clone https://github.com/croz-ltd/klokwrk-project.git

You will end up with the new `klokwrk-project` directory created. Change your directory location into the `klokwrk-project`, which is, from now on, considered as a **root project directory** in the
rest of this document.

### Gradle wrapper helper
Klokwrk uses Gradle as a build system. In particular, executing each Gradle command assumes you are using the Gradle Wrapper (contained in the project's root `gradle` directory). You should not use
any other Gradle version for building the klokwrk.

However, the Gradle Wrapper can be cumbersome to use at times. Therefore, it is recommended to use one of the available Gradle Wrapper helper tools, like [gng](https://github.com/gdubw/gng) (used by
the klokwrk team) or [gum](https://github.com/kordamp/gm).

In the rest of the document, I will use the `gw` command as a convenient helper over the original `./gradlew` command. If you don't want to use such a helper, just substitute `gw` with `./gradlew`
and you should be fine.

## Compile and test
### Application artifacts
For **compiling classes and tests**, from the project root directory, execute the following command (when executed for the first time, this can take a while):

    gw assemble testClasses testIntegrationClasses testComponentClasses --parallel -x groovydoc

When the command finishes, we have all the necessary artifacts for running applications. Therefore, just for running applications, the following commands are not required, but you will need them to
develop or contribute to the `klokwrk-project`.

### Tests and other checks (optional)
For **running all tests**, execute the following command. Note that Docker daemon must be running to perform integration and component tests:

    gw test --parallel && gw bootBuildImage && gw testIntegration -PdisableTestRetry --parallel && gw testComponent -PdisableTestRetry --parallel

To **verify code conventions** compliance, execute the following command:

    gw aggregateCodenarc

To **generate and see the cumulative Groovydoc documentation**, execute the following commands:

    gw aggregateGroovydoc
    open build/docs/aggregate-groovydoc/index.html

To **generate and see the cumulative JaCoCo code coverage report**, execute the following commands:

    gw aggregateJacocoReport
    open build/reports/jacoco/aggregate/html/index.html

### Exploring the latest changelog (optional)
After cloning the klokwrk, you have likely checked out a snapshot version. Unfortunately, GitHub does not make public published changelogs for non-released versions. Therefore, you can not just
simply go to the website and explore what is happening lately.

However, if you are interested, there is a way to generate a changelog in a local environment:
- install the [jreleaser](https://jreleaser.org/) tool locally. The simplest way is to use [SdkMan](https://sdkman.io/):

      sdk install jreleaser

- execute the following commands:

      env JRELEASER_PROJECT_VERSION=1.1.0 JRELEASER_GITHUB_TOKEN=1
      jreleaser changelog --basedir=. --config-file=./support/jreleaser/jreleaser-draft.yml --debug

      open out/jreleaser/release/CHANGELOG.md

The changelog will not be rendered as lovely as on GitHub, but you can see the latest updates on the project.

## Importing the klokwrk in IDEA
This step is not mandatory but can be very useful if you want to explore the details.

From the welcome screen of IntelliJ IDEA, click on the Open button (upper right) and navigate to the `klokwrk-project` directory that you've checked out previously. Next, select the `klokwrk-project`
directory and click Open. You should wait until IDEA completes the import.

At the root level, klokwrk is pretty compact compared to what you might have seen in some other projects. This is because all code artifacts are organized under a single directory - `modules`. If you
start exploring, you will undoubtedly notice a very noisy way IDEA uses by default for rendering module names with so-called qualifiers.

![Module names with noisy qualifiers](images/01-module-names-with-noisy-qualifiers.jpg "Module names with noisy qualifiers")

Luckily, there is an undocumented way to get rid of that noise. From IDEA, open the file `.idea/gradle.xml` in the editor. In that XML file, locate `project/component/option/GradleProjectSettings`
node. Now add the `useQualifiedModuleNames` option beneath it as shown in the following listing:

    <?xml version="1.0" encoding="UTF-8"?>
    <project version="4">
      ...
      <component name="GradleSettings">
        <option name="linkedExternalProjectsSettings">
          <GradleProjectSettings>
            <option name="useQualifiedModuleNames" value="false" />
            ...

After saving `.idea/gradle.xml`, you must reload all Gradle projects (use the Gradle Tool window and its Reload All Gradle Projects button), and unfortunate qualifiers will be gone.

![Module names without noisy qualifiers](images/02-module-names-without-noisy-qualifiers.jpg "Module names without noisy qualifiers")

## Running and exercising applications
As in most distributed systems, we have multiple applications to run. Some of them are functional, while others have a supportive role. In addition, we also have required infrastructural pieces.

### Starting applications
In our case, infrastructure comprises Axon Server and PostgreSQL database. To **start those infrastructural components**, open the new shell (shell-2) at the project root and execute the following
commands:

    cd support/docker
    ./dockerComposeInfrastructureUp.sh

Open the next shell (shell-3) at the root of the project. Before we run applications, we must **execute a database schema management application** first. It will migrate the database schema to the
state expected by other applications (it wraps [flyway](https://flywaydb.org/) for implementing database migrations):

    gw :cargotracker-booking-rdbms-management-app:bootRun

or

    gw -p modules/cargotracker/booking/app/cargotracker-booking-rdbms-management-app bootRun

Alternatively, you can use corresponding shell script:

    cd support/docker
    ./dockerComposeRdbmsMigration.sh

Now we are ready for **running functional applications**. First, please make sure you are at the root of the project, and then execute the following commands, each one from a separate shell (shell-3,
shell-4, and shell-5 where shell-3 is reused from the previous step). Wait until applications are fully started:

    gw :cargotracker-booking-commandside-app:bootRun
    gw :cargotracker-booking-queryside-projection-rdbms-app:bootRun
    gw :cargotracker-booking-queryside-view-app:bootRun

or

    gw -p modules/cargotracker/booking/app/cargotracker-booking-commandside-app bootRun
    gw -p modules/cargotracker/booking/app/cargotracker-booking-queryside-projection-rdbms-app bootRun
    gw -p modules/cargotracker/booking/app/cargotracker-booking-queryside-view-app bootRun

If you prefer, you might want to run applications from IDE. In that case, select the application's `bootRun` Gradle task as is shown in the picture for the `cargotracker-booking-commandside-app`
application.

![Starting commandside application from IDE](images/03-commandside-bootRun.jpg "Starting commandside application from IDE")

There is also an additional, more convenient, and easier way via the IDEA Run toolbar, which has been populated with appropriate run configurations during klokwrk import.

![Prepared run configurations](images/04-prepared-run-configurations.jpg "Prepared run configurations")

### Stopping applications
Once experimenting is finished, you will want to stop applications. But, of course, do not do this yet if you intend to read the sections below.

If you've started applications from IDEA, you can stop them with the `CMD+F2` keyboard shortcut or via IDEA's Run tool window. For applications started from CLI, use the `CTRL+C` shortcut.

Since we started infrastructural components from CLI, `CTRL+C` will be handy again. However, this will only stop docker-compose log tailing. For a full stop of infrastructure components and cleaning
them up properly, execute the following shell script (from shell-2):

    ./dockerComposeInfrastructureDown.sh

### Executing HTTP requests via Postman
For executing HTTP requests, we will use [Postman](https://www.postman.com/). Please [download](https://www.postman.com/downloads/) a free local application for your OS and install it if you don't
have it already.

First, you have to import `support/http-request/postman/klokwrk-workspace/cargotracker-booking.postman_collection.json` collection.

> <br/>
> Note: Every time the collection source file changes, the collection must be deleted and reimported in Postman. <br/>
> <br/>

Inside your Postman workspace, click on `Import` button, select `File` tab in `Import` dialog, and click on `Upload Files` button:

![Postman Import dialog](images/05-postmanImportDialog.jpg "Postman Import dialog")

Navigate to the `support/http-request/postman/klokwrk-workspace/cargotracker-booking.postman_collection.json` collection and open it. Then, in the `Import` dialog, click the `Import` button to finish
the process. Now, you should have your `cargotracker-booking` collection available.

![Imported Postman collection](images/06-importedPostmanCollection.jpg "Imported Postman collection")

#### Commandside requests
For executing some command requests, expand the collection and navigate to `cargotracker-booking/individual-requests/commandside/booking-offer/create-booking-offer`. Here `create-booking-offer`
folder corresponds to multiple variations of the `CreateBookingOfferCommand` command from the `cargotracker-booking-commandside-app` application.

For example, select the `ok, en` request and click the `Send` button. You should get the appropriate response:

![Execute ok,en command request](images/07-executeOkEnCommandRequest.jpg "Execute ok,en command request")

The previous command request returns a response containing the **booking offer identifier** in the payload (`$.payload.bookingOfferId.identifier`). Utilizing some Postman scripting features, that
identifier is remembered and made available for subsequent queryside requests.

There are more commandside requests available. Feel free to experiment with them.

#### Queryside requests
Before executing queryside requests, remember they rely on identifiers returned from previously performed commandside requests.

Otherwise, queryside request execution is very similar to the commandside. In this case, navigate to the
`cargotracker-booking/individual-requests/queryside-view/booking-offer/booking-offer-summary-find-by-id` folder and pick and execute the request. Again, the results should be similar to commandside
requests.

#### Scenarios
Besides individual request execution, Postman also supports the execution of scenarios. In that way, we can organize sequences of dependent requests to exercise complete use cases of the system.

To execute a scenario from our collection, navigate to `cargotracker-booking/scenarios`, click a folder corresponding to the scenario, and click on the `Run` button in the scenario's tab:

![Execute scenario, step 1](images/08-executeScenarioStep1.jpg "Execute scenario, step 1")

This will open the `Runner` tab, where you should click the `Run collection-name` button to execute it:

![Execute scenario, step 2](images/09-executeScenarioStep2.jpg "Execute scenario, step 2")

Each request from the scenario contains tests that verify if a particular request was successful or not:

![Execute scenario, step 3](images/10-executeScenarioStep3.jpg "Execute scenario, step 3")

### Exploring Wavefront integration
Since the 2.3.0 version, Spring Boot provides out-of-the-box free integration with Wavefront observability service. It offers zero-setup and feature-rich alternative to the standard observability
solutions like Prometheus (metrics collection), Zipkin (distributed tracing) and Grafana (visualization) combo. Wavefront is very convenient and effortless to use from a development environment.

Here is a very brief overview of Wavefront usage for a `klokwrk-project`:
- Start all applications as described previously. From the output of any application, copy the link to the Wavefront service.

  ![Wavefront link](images/11-wavefront-link.jpg "Wavefront link")

- Execute a dozen of commandside and queryside requests as described above, to provide some data to the Wavefront.
- Open previously copied Wavefront link and start exploring. The following resources will get you quickly up to speed: <br/>
  - [Tanzu Observability by Wavefront for Spring Boot Applications](https://www.youtube.com/watch?v=Jxwf-Iw-3T8) <br/>
  - [Wavefront for Spring Boot](https://docs.wavefront.com/wavefront_springboot.html) <br/>
  - [Wavefront for Spring Boot Tutorial](https://docs.wavefront.com/wavefront_springboot_tutorial.html)

### Supportive Gradle tasks
While working on a project, a developer often needs access to various pieces of information about the current state of a project. These reports might provide beneficial information about code quality
and can point to the areas which require some attention and improvements. Project Klokwrk has a dozen of Gradle tasks that provide such information. They can be run for each individual module, or
from the project's root.

> <br/>
> Note: Before executing any of commands bellow, position your terminal prompt at the project's root. <br/>
> <br/>

- `gw test --parallel`

  Executes all unit tests while providing convenient colored CLI output.

- `gw testIntegration --parallel`

  Executes all Docker containerized integration tests (for more details, take a look at [ADR-0010 - Integration Testing with Containerized Infrastructure](../../adr/content/0010-integration-testing-with-containerized-infrastructure.md)).

- `gw testComponent --parallel`

  Executes all Docker containerized component tests (for more details, take a look at [ADR-0011 - Component Testing](../../adr/content/0011-component-testing.md)).

- `gw allTestReports`

  Creates a cumulative report of all unit, integration and component tests for all subprojects. You can open it from CLI with

      open build/reports/allTestReports/index.html

- `gw allTestUnitReports`, `gw allTestIntegrationReports`, `gw allTestComponentReports`

  These commands create cumulative reports for each supported test type.

- `gw aggregateJacocoReport`

  Creates a cumulative code coverage report accessible at `build/reports/jacoco/aggregate/html/index.html`

- `gw aggregateCodenarc`

  Creates a cumulative CodeNarc report accessible at `build/reports/codenarc/aggregate.html`.

- `gw aggregateGroovydoc`

  Creates a cumulative documentation for the whole project accessible at `build/docs/aggregate-groovydoc/index.html`.
