# ADR-0004 - Hexagonal Architecture for Applications
* **Status: accepted**
* Dates: proposed - 2020-11-02
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
We want improved application design architecture that supports and promotes the following characteristics:
* An explicit promotion of application features as first-class citizens, instead of promoting technical aspects or database design.
* Explicit support for DDD tactical patterns.
* Is domain-centric with clear separation of the domain from infrastructure.
* Improves testability, maintainability, flexibility, extensibility, and adaptability.
* Discourages the use of any non-supported shortcuts.
* Is explicit enough to allow testing of architecture constraints and rules.
* Has sound structure and aesthetics where it is easy to find concrete artifacts.
* Allows parallel work on features.
* Is well-known and battle-tested already in the wild.

It is proven that traditional layer-centric designs fail on almost every characteristic above. Layer-centric application design architecture can work for simpler cases but often fails when the
feature set grows, and the domain becomes more complicated. We want architecture that can scale cleanly in terms of design and development.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We will use hexagonal architecture as a primary choice of architecture for designing applications.**

**We will provide concrete guidance for creating structure and concrete artifacts** (see ["Packaging for applications"](../../article/modules-and-packages/modulesAndPackages.md#packaging-for-applications) for more
details).

**We will adapt hexagonal architecture for efficient use with CQRS/ES component architecture** (see
["Applying hexagonal architecture"](../../article/modules-and-packages/modulesAndPackages.md#applying-hexagonal-architecture) for more details). This includes adding support for enforcing and testing the structure and
behavior of the architecture (see ["Behavior and architectural testing"](../../article/modules-and-packages/modulesAndPackages.md#behavior-and-architectural-testing) for more details).

## Consequences
### Positive
* Everything stated in ["Context"](#context)

### Negative
* It might be overkill for simpler cases.

### Neutral
* It requires some time for learning and adapting.

## Considered Options
* Traditional layered architecture.
* Clean and onion architecture.
  * Those are very similar to hexagonal architecture, but hexagonal architecture offers more concrete implementation guidance.

## References
* [Organizing modules and packages, section "Packaging for applications"](../../article/modules-and-packages/modulesAndPackages.md#packaging-for-applications)
* [Organizing modules and packages, section "Applying hexagonal architecture"](../../article/modules-and-packages/modulesAndPackages.md#applying-hexagonal-architecture)
* [Organizing modules and packages, section "Behavior and architectural testing"](../../article/modules-and-packages/modulesAndPackages.md#behavior-and-architectural-testing)
* ["Hexagonal Architecture with Java and Spring"](https://reflectoring.io/spring-hexagonal/) by Tom Hombergs
* ["Get your hands dirty on Clean Architecture"](https://reflectoring.io/book/) by Tom Hombergs
