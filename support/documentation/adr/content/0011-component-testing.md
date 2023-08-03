# ADR-0011 - Component Testing
* **Status: accepted**
* Dates: proposed - 2020-11-05
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
When developing application monoliths, it is common to have a relatively small number of end-to-end tests (a.k.a. [broad-stack tests](https://martinfowler.com/bliki/BroadStackTest.html) or system
tests). They are usually run on a full system, including necessary infrastructure, either by exercising UI or public system API.

When developing a microservices based system, instead of a single, we have many runnable components. They are usually grouped into corresponding bounded contexts, where each bounded context
contains a group of closely related microservices. Further, depending on technology choices, each logical microservice might be split into multiple runnable components.

In an environment like that, spawning a whole system for testing might be challenging to set up and time-consuming to execute. Also, broad-stack tests are not focused on a single logical
microservice, which can bring in a whole range of issues, from responsibility for development and maintenance to the expected but not yet developed system features.

For testing microservices, we need tests that have a very different focus than broad-stack tests.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We will use [component tests](https://martinfowler.com/bliki/ComponentTest.html) for exercising running instances of a single or multiple logical microservices inside a single bounded context.**

The technical implementation of component tests is very similar to broad-stack tests. Component tests are usually run through the public API of the component. If we have micro-frontends based UI,
it can also be used. Therefore, the applied scope of tests is the major difference from broad-stack testing. Component tests are scoped and focused at bounded context only.

Implementation of component tests will rely on the Testcontainers library. In contrast with [integration tests](0010-integration-testing-with-containerized-infrastructure.md), component tests do not
only containerize infrastructure but also logical microservices under the test. With such a setup, component tests usually leverage an external client for exercising the public API of microservices.

As an example of component tests, take a look at the `klokwrk-project` module `cargotracking-booking-test-component`.

## Consequences
### Positive
* Testing a realistic running microservice through external API as it will be used in production.
* All realistic integrations of a microservice are exercised.

### Negative
* More tests to write and maintain.
* Component tests are usually slower than focused, realistic integration tests.

### Neutral
* Only the main successful and exceptional scenarios are tested.

## Considered Options
* Testing the whole system with broad-stack tests.
* Do not test microservices under realistic conditions.

## References
* [Testing Strategies in a Microservice Architecture](https://martinfowler.com/articles/microservice-testing/)
* [Component Testing](https://martinfowler.com/articles/microservice-testing/#testing-component-introduction)
* [Broad-stack tests](https://martinfowler.com/bliki/BroadStackTest.html)
* [Component tests](https://martinfowler.com/bliki/ComponentTest.html)
* [ADR-0010 - Integration Testing with Containerized Infrastructure](0010-integration-testing-with-containerized-infrastructure.md)
