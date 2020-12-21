# ADR-0008 - Testing Architecture
* **Status: accepted**
* Dates: proposed - 2020-11-04
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
Application architecture is commonly expressed as a set of guidelines (written or not) that the development team needs to follow. Although essential, architectural guidelines are rarely reviewed or
enforced appropriately. The typical result is that architecture degrades through time.

In some cases, application use component ([ADR-0003 - CQRS and Event Sourcing (ES) for Applications](0003-cqrs-and-event-sourcing-for-applications.md)) and
design ([ADR-0004 - Hexagonal Architecture for Applications](0004-hexagonal-architecture-for-applications.md)) architectural patterns that promote architectural guidelines, but usually there is
nothing to verify them.

The hexagonal architecture provides a well-defined placeholder for every significant application artifact. But there are also some rules regarding dependencies between those artifacts. It is not
allowed that each class or interface access anything that it wants. When you add additional CQRS/ES aspects, there are even more rules to follow.

We want to ensure that rules will not be broken and that developers new to the hexagonal architecture and CQRS/ES can comfortably work with them without breaking anything. It will help if we
have in place tests that verify all architectural invariants.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We will use architectural testing for verifying architectural constraints.**

Building on top of the [ArchUnit](https://www.archunit.org/) library, Klokwrk provides DSL for specifying hexagonal architecture layers for CQRS/ES applications. There is support for several subtypes
of CQRS/ES flavored hexagonal architecture corresponding to the commandside, projections, and queryside aspects.

For more insight and details, take a look at ["Behavior and architectural testing"](../../article/modules-and-packages/modulesAndPackages.md#behavior-and-architectural-testing). There is also a video
["Project Klokwrk: how it helps defining software architecture and solves integration"](https://www.youtube.com/watch?v=35oUxjXWNYU) that, besides other things, talks about architectural testing in
`klokwrk-project`.

## Consequences
### Positive
* Architectural constraints are verified and enforced by unit tests.
* The architecture will not degrade over time.
* Architectural requirements are expresses in code, not only in the documentation.

### Neutral
* Learning ArchUnit and Klokwrk Architectural Testing DSL take some time.

## Considered Options
* Not to test architectural constraints.

## References
* [ADR-0003 - CQRS and Event Sourcing (ES) for Applications](0003-cqrs-and-event-sourcing-for-applications.md)
* [ADR-0004 - Hexagonal Architecture for Applications](0004-hexagonal-architecture-for-applications.md)
* [Behavior and architectural testing](../../article/modules-and-packages/modulesAndPackages.md#behavior-and-architectural-testing)
* [ArchUnit library](https://www.archunit.org/)
* [Project Klokwrk: how it helps defining software architecture and solves integration](https://www.youtube.com/watch?v=35oUxjXWNYU)
