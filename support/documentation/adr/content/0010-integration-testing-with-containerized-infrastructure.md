# ADR-0010 - Integration Testing with Containerized Infrastructure
* **Status: accepted**
* Dates: proposed - 2020-11-05
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
Integration testing is commonly used in contemporary development. Frameworks like Spring and Spring Boot makes integration testing seamless, and it often looks like plain unit testing in terms of
simplicity and speed of execution.

However, the way that integration tests are used today commonly exercises only integration between internal application components (e.g., integration between controllers and services, integration
between services and domain objects, configuration correctness, etc.). If the tests require some external infrastructure, it is usually mocked out or replaced by alternative in-memory variants.
Internal application integration tests are important, but they do not cover integration with realistic external infrastructure.

As containerized solutions (e.g., Docker) are a norm today, there is an obvious opportunity to use containerized infrastructure in our integration tests. There is a lot of value in such tests since
the infrastructure is (almost) the same as it will be in the production.

The most obvious examples are databases. A typical pattern in integration tests is to use an in-memory database, for example, H2. Although very convenient, it is not a real database as it will be in
production. It will not behave like an actual database either in a functional (SQL dialect) or non-functional (security, performance) way. If our production uses, say, the PostgreSQL database, it
will be much better to use that same database in integration tests.

If we look at other non-database examples of infrastructure components, say some modern messaging system, we will probably encounter no in-memory equivalent exists. In these cases, infrastructure in
integration tests is usually mocked at something like the repository layer, and we end up with tests that do not exercise infrastructure at all. Not even inadequate in-memory replacement.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We will use the [Testcontainers](https://www.testcontainers.org/) library and Docker for implementing realistic integration tests whenever it makes sense.**

Testcontainers relies on Docker for spawning required infrastructure during integration testing. Therefore, Docker must be available on a system where tests execute. Depending on the concrete
external component, starting up the container can require non-negligible time, which can prolong test execution time.

For those reasons, we will use Testcontainers library only in scenarios when a realistic external structure is really required or when having it significantly simplifies tests (e.g., when mocking
external infrastructure is complex).

We must reuse containerized infrastructure whenever we can and run as much as possible test suites on the same container instances. It would be inefficient to start new containers, say, for each
related test class. We can achieve it with the Testcontainers library by creating abstract parent test classes that start up containers via static initializers.

Of course, when we already have Testcontainers based tests that spawn relevant infrastructure components, we can reuse it for tests that otherwise will be simple internal application integration
tests. Test execution time should not extend significantly.

Running unit tests and internal application component integration tests must be separate from running containerized integration tests. Each category of tests needs to have a different execution
command. That way, we can trigger containerized tests only when appropriate.

As an example, take a look at `AbstractCommandSideIntegrationSpecification` abstract class, and related test classes `BookingOfferCommandWebControllerIntegrationSpecification` and
`AbstractBookingOfferCommandApplicationServiceIntegrationSpecification`.

## Consequences
### Positive
* Integration tests use realistic infrastructure components.

### Negative
* Integration tests execution time is extended.

### Neutral
* Have to use containerized tests selectively, taking into account test execution time.

## Considered Options
* Mocked infrastructure.
* Alternative in-memory replacements of infrastructure components.

## References
* [Testcontainers](https://www.testcontainers.org/)
