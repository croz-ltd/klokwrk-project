# ADR-0003 - CQRS and Event Sourcing (ES) for Applications
* **Status: accepted**
* Dates: proposed - 2020-10-28
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
When building monoliths or microservices that contain non-trivial business logic, we want to have a high-level component architecture pattern that natively leverages a strategic Domain Driven
Design (DDD) concepts like ubiquitous language and bounded context. It should also support or allow, at least, the usage of application design architecture patterns like hexagonal architecture.

At the lower technical level, component architecture pattern should provide native support for tactical DDD concepts like aggregates, entities, value objects, and events.

When building microservices, the chosen component architecture pattern should natively support asynchronous communication through events.

It would be ideal to have all of these in a single coherent open-source framework and platform.

For anemic CRUD style applications, more traditional approaches, like layered architecture, would be just fine.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We will use the Axon framework and Axon Server as a mature open-source implementation of the CQRS/ES component architecture pattern.**

Especially as one of the major `klokwrk-project` goals is to demonstrate the creation of complex CQRS/ES microservice applications. We can use more traditional approaches like classic layered
architecture and anemic model for anemic CRUD style applications.

## Consequences
### Positive
* CQRS/ES naturally supports building microservices and distributed systems.
* CQRS/ES naturally supports asynchronous communication through events.
* Axon platform is a mature open-source library that provides all necessary building blocks to support CQRS/ES, DDD, testing, distribution, and scaling.

### Negative
* Axon and CQRS/ES, in general, have a steep learning curve.

## Considered Options
* Anemic CRUD-style applications.
* Classic layered applications written with DDD concepts in mind.

## References
[DDD, CQRS and Event Sourcing Explained](https://lp.axoniq.io/whitepaper-event-sourcing)
