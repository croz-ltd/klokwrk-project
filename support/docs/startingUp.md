## Starting up and trying the whole thing
Environment:
- OSX (should work with any desktop Linux distro and with Windows with appropriate bash-shell like git-bash)
- JDK 11 (should work with JDK 8)
- Gradle 6.4.1
- IDEA Ultimate 2020.1.1 (should work with IDEA Community except for http client which is part of the Ultimate edition)
- Docker
- httpie

### Running from the shell
Open your shell at the project root and execute following commands (shell-1):

    cd support/docker
    ./dockerComposeUpInfrastructure.sh

Open another shell (shell-2) at the project root and execute following command (generating Groovydoc is skipped to speed things up)

    ./gradlew clean assemble -x groovydocJar

There are three apps that needs to be run, and for each open a new shell at the project root (shell-3, shell-4 and shell-5)

    ./gradlew -p cargotracker/cargotracker-booking-commandside-app bootRun
    ./gradlew -p cargotracker/cargotracker-booking-queryside-rdbms-projection-app bootRun
    ./gradlew -p cargotracker/cargotracker-booking-queryside-app bootRun

Going back to shell-2, some HTTP requests can be tried via `httpie`:
- commandside

      http POST http://localhost:8080/cargotracker-booking-commandside/cargo-booking-command/cargo-book \
        Content-Type:application/json Accept:application/json Accept-Charset:utf-8 Accept-Language:hr-HR \
        originLocation=HRRJK \
        destinationLocation=HRZAG

      http POST http://localhost:8080/cargotracker-booking-commandside/cargo-booking-command/cargo-book \
        Content-Type:application/json Accept:application/json Accept-Charset:utf-8 Accept-Language:en \
        originLocation=HRRJK \
        destinationLocation=HRZAG

- queryside

    To be able to execute queryside request, from the previous commandside output we need to take the value of `aggregateIdentifier` element, and then try some queryside requests. For example:

      http POST http://localhost:8084/cargotracker-booking-queryside/cargo-booking-query/cargo-summary-query \
        Content-Type:application/json Accept:application/json Accept-Charset:utf-8 Accept-Language:hr-HR \
        aggregateIdentifier=9e4a13c8-cb74-4a01-9717-f41aaba5428d

      http POST http://localhost:8084/cargotracker-booking-queryside/cargo-booking-query/cargo-summary-query \
        Content-Type:application/json Accept:application/json Accept-Charset:utf-8 Accept-Language:en \
        aggregateIdentifier=9e4a13c8-cb74-4a01-9717-f41aaba5428d


When finished experimenting, applications can be stopped by `CTRL+C` (shell-3, shell-4, shell-5). For stopping infrastructural components (shell-1), first we need to stop docker-compose log tailing
by `CTRL+C`, and then do some cleanup with `./dockerComposeDownInfrastructure.sh`.

### Running from IDEA
Executing HTTP requests from CLI can be cumbersome, and many prefer some more user-friendly tool like Postman. If you develop from IDEA, you can also use IDEA's http client. Although it does not have
all niceties of Postman it can be really useful for storing series of requests as code artifacts in the project.

First, the project needs to be imported in IDEA as Gradle project. Next, either from the IDEA terminal or from external shell, infrastructural components needs to be started:

    cd support/docker
    ./dockerComposeUpInfrastructure.sh

Applications can also be started from CLI, but it might be preferable to use UI. Start applications exactly in listed order:
- Start commandside app by double-clicking on its `bootRun` Gradle task and wait until it is started up:

    ![Starting commandside application from IDE](images/startingUp/01-startup-01-commandside-bootRun.jpg "Starting commandside application from IDE")

- Start queryside projection app by double-clicking on its `bootRun` Gradle task and wait until it is started up:

    ![Starting projection application from IDE](images/startingUp/01-startup-02-queryside-projection-bootRun.jpg "Starting projection application from IDE")

- Start queryside app by double-clicking on its `bootRun` Gradle task and wait until it is started up:

    ![Starting queryside application from IDE](images/startingUp/01-startup-03-queryside-bootRun.jpg "Starting queryside application from IDE")

When all applications are started up, we can try executing some requests:
- Find and open `support/http-request/commandsideRequests.http`.
- Click on `Run All Requests in File` and select `Run with 'development' environment`.

    ![Running IDEA HTTP client for executing commands](images/startingUp/01-startup-04-commandside-httpClient.jpg "Running IDEA HTTP client for executing commands")

- Repeat the same with `support/http-request/querysideRequests.http`.

When finished experimenting, applications can be stopped by `CMD+F2` shortcut or via IDEA "Run" tool window. For stopping infrastructural components, stop docker-compose log tailing with `CTRL+C`,
and then do some cleanup with `./dockerComposeDownInfrastructure.sh`.

### Supportive Gradle tasks
While working on a project, a developer often needs access to various pieces of information about the current state of a project. These reports might provide beneficial information about code quality
and can point to the areas which require some attention and improvements. Project Klokwrk has a dozen of Gradle tasks that provide such information. They can be run for each individual project, or
for a project root.

Before executing any of commands bellow, position your terminal prompt at the project's root:


- `./gradlew check -x codenarcMain -x codenarcTest`

  Executes all tests while providing convenient colored CLI output. For speeding things up, the creation of CodeNarc reports for individual projects is turned off. We will create aggregate CodeNarc
  reports instead.

- `./gradlew aggregateAllTestReports`

  Creates a cumulative report of all tests for all individual projects. Report can be accessed via `klokwrk-project/build/reports/aggregate-all-tests/index.html`. It is best to open it from IDEA
  using `Open in Browser` action.

  ![Opening All Tests report from IDEA](images/startingUp/01-startup-05-open-allTests-report.jpg "Opening All Tests report from IDEA")

  Behind the scenes, IDEA uses an internal HTTP server, which is very convenient for looking at statically generated pages like these.

- `./gradlew aggregateJacocoReport`

  Creates a cumulative code coverage report accessible at `klokwrk-project/build/reports/jacoco/aggregate/html/index.html`

- `./gradlew aggregateCodenarcMain`<br/>
  `./gradlew aggregateCodenarcTest`

  These commands create a cumulative CodeNarc reports for `main` and `test` source sets, respectively. Reports can be accessed via `klokwrk-project/build/reports/codenarc/aggregateCodenarcMain.html`
  and `klokwrk-project/build/reports/codenarc/aggregateCodenarcTest.html`.

- `./gradlew aggregateGroovydoc`

  Creates a cumulative documentation for the whole project accessible at `klokwrk-project/build/docs/aggregate-groovydoc/index.html`.
