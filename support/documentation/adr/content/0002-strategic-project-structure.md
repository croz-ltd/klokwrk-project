# ADR-0002 - Strategic Project Structure
* **Status: accepted**
* Dates:
  * proposed - 2020-10-28
  * updated - 2023-09-01
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
Excluding the simplest hello-world-like cases, any useful project typically contains several modules. The traditional way to organize project modules is just to put them under the project root.
We can call that structure simply **flat structure**.

While the flat structure is appropriate and sufficient for simpler projects, when the project grows and the number of modules increases, the flat structure starts suffering from many drawbacks:
* Flat structure does not scale well when the number of modules grows.
* Flat structure is difficult and confusing to navigate with numerous modules at the same hierarchy level.
* Flat structure does not suggest a direction of dependencies between modules.
* Flat structure does not suggest abstraction levels of modules.
* Flat structure does not suggest where are the system's entry points.
* Flat structure can use only module names to provide hints about relations between modules. Unfortunately, even that possibility is rarely leveraged.
* Flat structure does not use any high-level constructs that may suggest how modules are organized and related.
* Negative usage aspects are getting worse and worse as we add additional modules.
* Flat structure often requires extracting modules in separate repositories just because confusion becomes unbearable with a larger number of modules.
* When using microservices, the flat structure practically forces us to use one project per microservice.

> Note: Terms **flat structure** and **strategic structure** (see below) are ad-hoc terms introduced just for this document. However, in the `klokwrk-project`, we may use them in other places for
> convenience.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We'll organize project modules around strategic DDD (Domain Driven Design) constructs of bounded context and subdomains.**

Our project organization will follow principles and recommendations of **strategic structure** as defined below.

### Decision Details
We'll start with a concrete example of the strategic structure used in the klokwrk at the time of writing this document. As a follow-up, we'll present a general scheme for creating the strategic
structure focusing on the differences to the given concrete example.

#### Strategic structure in klokwrk
The current project layout in the klokwrk looks like this:

    klokwrk-project
    ├── ... (other files or directories)
    ├── modules
    │   ├── bc
    │   │   └── cargotracking
    │   │       ├── asd
    │   │       │   └── booking
    │   │       │       ├── app
    │   │       │       │       cargotracking-booking-app-commandside
    │   │       │       │       cargotracking-booking-app-queryside-projection-rdbms
    │   │       │       │       cargotracking-booking-app-queryside-view
    │   │       │       │       cargotracking-booking-app-rdbms-management
    │   │       │       └── lib
    │   │       │               cargotracking-booking-lib-boundary-web
    │   │       │               cargotracking-booking-lib-out-customer
    │   │       │               cargotracking-booking-lib-queryside-model-rdbms-jpa
    │   │       │               cargotracking-booking-test-component
    │   │       │               cargotracking-booking-test-support-queryside
    │   │       │               cargotracking-booking-test-support-testcontainers
    │   │       │
    │   │       ├── domain-model
    │   │       │       cargotracking-domain-model-aggregate
    │   │       │       cargotracking-domain-model-command
    │   │       │       cargotracking-domain-model-event
    │   │       │       cargotracking-domain-model-service
    │   │       │       cargotracking-domain-model-value
    │   │       │
    │   │       └── lib
    │   │               cargotracking-lib-axon-cqrs
    │   │               cargotracking-lib-axon-logging
    │   │               cargotracking-lib-boundary-api
    │   │               cargotracking-lib-boundary-query-api
    │   │               cargotracking-lib-domain-model-command
    │   │               cargotracking-lib-domain-model-event
    │   │               cargotracking-lib-web
    │   │               cargotracking-test-support
    │   │
    │   ├── lib
    │   │   ├── hi
    │   │   │       klokwrk-lib-hi-datasourceproxy-springboot
    │   │   │       klokwrk-lib-hi-jackson-springboot
    │   │   │       klokwrk-lib-hi-spring-context
    │   │   │       klokwrk-lib-hi-spring-data-jpa
    │   │   │       klokwrk-lib-hi-validation-springboot
    │   │   │
    │   │   ├── lo
    │   │   │       klokwrk-lib-lo-archunit
    │   │   │       klokwrk-lib-lo-datasourceproxy
    │   │   │       klokwrk-lib-lo-hibernate
    │   │   │       klokwrk-lib-lo-jackson
    │   │   │       klokwrk-lib-lo-uom
    │   │   │       klokwrk-lib-lo-validation-constraint
    │   │   │       klokwrk-lib-lo-validation-validator
    │   │   │
    │   │   └── xlang
    │   │           klokwrk-lib-xlang-groovy-base
    │   │           klokwrk-lib-xlang-groovy-contracts-match
    │   │           klokwrk-lib-xlang-groovy-contracts-simple
    │   │
    │   └── other
    │       ├── platform
    │       │       klokwrk-platform-base
    │       │       klokwrk-platform-micronaut
    │       │       klokwrk-platform-spring-boot
    │       │
    │       └── tool
    │               klokwrk-tool-gradle-source-repack
    ├── support
    │   └── ... (other files or directories)
    └── ... (other files or directories)

At the top of the hierarchy, we have a project folder  - `klokwrk-project`. It is the equivalent of the whole system. In the strategic structure, the system name appears in the names of artifacts
considered to be conceptually at the level of a system.

Right below the root, we have `modules` and `support` folders. These should be the area of 99% of everyday work, with the `modules` folder taking a vast majority of that percentage.

The `support` folder houses all kinds of supportive files like scripts, documentation, git hooks, etc. The `support` folder is free-form, and the strategic structure does not impose any
recommendations or rules on its content. On the contrary, the strategic structure is applied to the content of the `modules` directory - the home of all source code modules in the system.

At the 1st level of strategic structure - the system level, we have the content of the `modules` directory. It is divided into three subdirectories: `bc` (bounded context modules),
`lib` (system-level libraries), and `other` (miscellaneous helper modules).

At the 2nd level - the bounded context level, we have the content of the `modules/bc` directory that is further organized into three parts, `asd` (asd stands for **A** **S**ub**D**omain),
`domain-model` (bounded context domain model), and `lib` (bounded context libraries).

At the 3rd level of a hierarchy, we have the content of the `modules/bc/[bounded-context-name]/asd` directory that holds all bounded context's subdomains. The modules for each subdomain are further
divided into `app` and `lib`. The `modules/bc/[bounded-context-name]/asd/[subdomain-name]/app` directory contains the **subdomain applications** responsible for implementing concrete subdomain
scenarios. From the abstraction level and dependency perspectives, subdomain applications are at the top of the hierarchy. Subdomain applications speak the language of domain - the bounded context's
ubiquitous language. They even contribute to it through the naming and meaning of use cases.

The first thing that **subdomain libraries** (`modules/bc/[bounded-context-name]/asd/subdomain-name/lib)` can hold is infrastructural code related to the technological choices made for that
particular subdomain and are not reusable outside the subdomain. However, they can temporarily have infrastructural modules intended to be more reusable (either on the bounded context or system
levels) at the end. Still, for whatever reason, it was more convenient to hold them at the subdomain level for a limited time.

The second thing that can be found in subdomain libraries are business-related reusable modules that connect technological choices with the domain model. One characteristic example is the
`cargotracking-booking-lib-queryside-model-rdbms-jpa` module. Those kinds of modules do speak bounded context's ubiquitous language.

The bounded context's **domain model** is implemented in `modules/bc/[bounded-context-name]/domain-model`. Those modules contain the essence of the bounded context business logic. Implementation of
the domain model should be free of technology as much as possible and practical. Adding external libraries is not strictly forbidden, but each addition should be conscious and must be carefully
evaluated. It is best to have tests that monitor and control the dependencies of a domain model. The domain model implements the majority of code-level representation of the bounded context's
ubiquitous language and must be consistent across all bounded context's subdomains.

By default, the directory `modules/bc/[bounded-context-name]/lib` is the home of shareable **bounded context infrastructural libraries**. It contains modules with infrastructural code that is
reusable across the bounded context. Those modules are at a lower abstraction level than subdomain libraries. Bounded context infrastructural libraries do not speak domain language. However, they can
support the implementation of the domain model and other module groups higher in the hierarchy. Domain model should not generally depend on bounded context infrastructural libraries. Exceptions are
allowed but should be conscious and carefully managed.

Do note that another variant of bounded context libraries is also possible. It is a variant supporting the sharing of business logic at the bounded context level when necessary. In that case, instead
of a single `lib` directory, we would have `blib` and `ilib` directories. The `blib` directory would contain business-related modules that can depend on a domain model. On the contrary, the `ilib`
directory cannot use the domain model because it should contain infrastructural code only. The `ilib` directory role is the same as the role of `lib` directory from the default variant of bounded
context libraries.

Let's return to the `modules/lib` directory containing general **system-level libraries**. It is divided into `hi`, `lo`, and `xlang` subdirectories. All system-level libraries are at lower
dependency and abstraction levels than any bounded context module.

Although separation on the high (`hi`) and low-level (`lo`) system libraries is somewhat arbitrary, it is helpful in practice. The `hi` directory is intended to contain
**high-level system libraries**, which are general infrastructural modules closer to the high-level technological frameworks (something like Spring, Spring Boot, or Axon frameworks) used in the
system. They could contain some specifics of our system, but usually, they do not. In that later case, they are general enough to be reused even outside of our system.

The **low-level system libraries** from the `lo` directory deal with the customizations and extensions of widely used 3rd party libraries like Hibernate, Jackson, Java Bean validations, and similar.
Both types of system-level libraries should not be, in general, dependencies of a domain model.

At the lowest abstraction level, we have the **language extensions** (`modules/lib/xlang`). They focus on adding features to the programming language itself or its accompanying SDK (JDK in our case).
Language extensions can be used from everywhere, even from the domain model, without restrictions. Some of them are often written to ease the implementation of the domain model by making it more
expressive and concise.

#### Characteristics of strategic structure
The most important thing about strategic structure is not the structure itself but rather the distinguishing characteristics that it provides.

We already mentioned abstraction levels and dependencies between groups of modules. If you look again at the example, you will notice that both of them are constantly flowing top to bottom through
the strategic structure. For instance, subdomain applications depend on subdomain libraries. They both can depend on the domain model, which can depend on bounded context libraries and language
extensions. At the level of system libraries, high-level modules can depend on low-level modules, and they both can depend on the language extensions. However, none of the dependencies can come the
other way around. Dependencies are not allowed to flow from the bottom to the top.

We have managed to do this because we applied strategic DDD concepts of bounded context and subdomains to the project structure. They provide sense and meaningfulness by connecting our code to the
business. Without that business context, we will be left exclusively to the technical aspects, which are just insufficient. Technical aspects know nothing about the purpose of our system. They do not
know anything about the business context.

Described characteristics bring important benefits when trying to understand or navigate through the system's code. Finding the desired functionality is much easier because we usually know, at least
approximately, where we should look for it. This can greatly reduce cognitive load while exploring unfamiliar (or even familiar) codebases.

In addition, if you follow the proposed naming conventions for modules and their packages (see below), the same easy orientation can be applied at the package level or even if you pull out all
modules into the flat structure. You will always know where to look for.

#### Naming conventions
You have probably noticed that modules have very particular names reflecting their position in the strategic structure. The following table summarizes them as used in the example:

| Module group    | Naming scheme                                            | Example                                  |
|-----------------|----------------------------------------------------------|------------------------------------------|
| subdomain apps  | `[bounded-context-name]-[subdomain-name]-app-[app-name]` | `cargotracking-booking-app-commandside`  |
| subdomain libs  | `[bounded-context-name]-[subdomain-name]-lib-[lib-name]` | `cargotracking-booking-lib-boundary-web` |
| domain model    | `[bounded-context-name]-domain-model-[model-part-name]`  | `cargotracking-domain-model-aggregate`   |
| bc libs         | `[bounded-context-name]-lib-[lib-name]`                  | `cargotracking-lib-boundary-api`         |
| sys hi libs     | `[system-name]-lib-hi-[lib-name]`                        | `klokwrk-lib-hi-spring-context`          |
| sys lo libs     | `[system-name]-lib-lo-[lib-name]`                        | `klokwrk-lib-lo-jackson`                 |
| lang extensions | `[system-name]-lib-xlang-[lib-name]`                     | `klokwrk-lib-xlang-groovy-base`          |

Module naming conventions are essential because our modules are not always presented (i.e., try the Packages view in the IntelliJ IDEA's Project tool window) or used as a part of the hierarchy (think
of JAR names put in the same directory). For those reasons, our naming scheme closely follows the strategic structure hierarchy where parts of module names are directly pulled from corresponding
subdirectory names. That way, we can keep the match between alphabetical order and the direction of dependencies.

> Note: When you have multiple bounded contexts and/or multiple subdomains in the project, to get the exact match between alphabetical order and the direction of dependencies, you can use the `bc-`
> prefix in front of bounded context names and the `asd-` prefix for subdomain names.

The same naming principles should also be applied to packages. Here are a few examples of package names:

    org.klokwrk.cargotracking.booking.app.commandside.*
    org.klokwrk.cargotracking.booking.lib.boundary.web.*
    org.klokwrk.cargotracking.domain.model.aggregate.*
    org.klokwrk.cargotracking.lib.boundary.api.*
    org.klokwrk.lib.hi.spring.context.*
    org.klokwrk.lib.lo.jackson.*
    org.klokwrk.lib.xlang.groovy.base.*

With those naming conventions, we should be able to avoid naming collisions on the module and package levels.

#### The general scheme of strategic structure
In some circumstances, we may need additional elements in the strategic structure to deal with shared libraries at different levels. Examples of those, with sparse explanations, are given in the
general scheme of strategic structure below:

    modules
    ├── bc
    │   ├── my_food
    │   │   ├── asd
    │   │   │   ├── restaurant
    │   │   │   │   ├── app
    │   │   │   │   │       ... *
    │   │   │   │   └── lib
    │   │   │   │           ... *
    │   │   │   ├── menu_management
    │   │   │   │   ├── app
    │   │   │   │   │       ... *
    │   │   │   │   └── lib
    │   │   │   │           ... *
    │   │   │   └── zshared         // sharing code between subdomains if necessary
    │   │   │       └── lib
    │   │   │           ... *
    │   │   ├── domain-model
    │   │   │       ... *
    │   │   └── lib                 // bounded context libraries - default variant
    │   │           ... *           // Can be split into "blib" and "ilib" directories when the sharing of
    │   │                           // business logic is necessary at the level of a single bounded context
    │   ├── my_carrier
    │   │   ├── asd
    │   │   │   ├── app
    │   │   │   │       ... *
    │   │   │   └── lib
    │   │   │           ... *
    │   │   ├── domain-model
    │   │   │       ... *
    │   │   └── lib
    │   │           ... *
    │   └── zshared                 // shared code between multiple bounded contexts (if necessary).
    │       │                       // "z" prefix - funny reference to "zee Germans" from Snatch movie.
    │       │                       // Moves "zshared" at the last place alphabetically, which matches
    │       │                       // the proper place in terms of dependencies and abstraction levels.
    │       ├── domain-model
    │       │       ... *
    │       └── lib
    │               ... *
    ├── lib
    │   ├── hi
    │   │       ... *
    │   ├── lo
    │   │       ... *
    │   └── xlang
    │           ... *
    └── other            // supportive project's code for various "other" purposes
        ├── build
        │       ... *
        ├── tool
        │       ... *
        └── ...

#### Simplification - the case of bounded context boundaries matching 1:1 with subdomain
The one-to-one match between bounded context boundaries and corresponding subdomain is considered to be the "ideal" case, and it is relatively common in practice. When we know how a fully expanded
strategic structure works and looks like, it is relatively easy to come up with simplification for this particular case.

Here are "refactoring" steps and the example based on our concrete example from the beginning of this document:
- move subdomain applications to the bounded context level
- merge subdomain libraries with bounded context libraries
- split bounded context libraries into `blib` and `ilib` directories if necessary
- rename corresponding modules and packages

      klokwrk-project
      ├── ... (other files or directories)
      ├── modules
      │   ├── bc
      │   │   └── cargotracking
      │   │       ├── app
      │   │       │       cargotracking-app-commandside
      │   │       │       cargotracking-app-queryside-projection-rdbms
      │   │       │       cargotracking-app-queryside-view
      │   │       │       cargotracking-app-rdbms-management
      │   │       │
      │   │       ├── blib
      │   │       │       cargotracking-blib-out-customer
      │   │       │       cargotracking-blib-queryside-model-rdbms-jpa
      │   │       │
      │   │       ├── domain-model
      │   │       │       cargotracking-domain-model-aggregate
      │   │       │       cargotracking-domain-model-command
      │   │       │       cargotracking-domain-model-event
      │   │       │       cargotracking-domain-model-service
      │   │       │       cargotracking-domain-model-value
      │   │       │
      │   │       └── ilib
      │   │               cargotracking-ilib-axon-cqrs
      │   │               cargotracking-ilib-axon-logging
      │   │               cargotracking-ilib-boundary-api
      │   │               cargotracking-ilib-boundary-query-api
      │   │               cargotracking-ilib-boundary-web
      │   │               cargotracking-ilib-domain-model-command
      │   │               cargotracking-ilib-domain-model-event
      │   │               cargotracking-ilib-web
      │   │               cargotracking-test-component
      │   │               cargotracking-test-support
      │   │               cargotracking-test-support-queryside
      │   │               cargotracking-test-support-testcontainers
      │   │
      │   ├── lib
      │   │   ├── hi
      │   │   │       ... *
      │   │   ├── lo
      │   │   │       ... *
      │   │   └── xlang
      │   │           ... *
      │   └── other
      │           ... *
      ├── support
      │       ... *
      └── ... *

## Consequences
### Positive
* The strategic structure provides predicable way of navigation and orientation in complex projects.
* The strategic structure organizes modules into business-oriented categories.
* The strategic structure provides strict direction of dependencies between module categories.
* The strategic structure indicates abstraction levels of module categories.
* The strategic structure provides apparent entry points into the system (subdomain apps).
* With consistent naming of modules and packages, the strategic structure supports alternative perspectives on project artifacts (IDEA packages view, IDEA custom scopes).
* The strategic structure supports the smooth system growth.
* The strategic structure organizes modules around well know DDD concepts of bounded context and subdomains.
* The strategic structure provides reduced cognitive load when working or exploring complex codebases.
* The strategic structure improves the speed of development and maintenance.

### Negative
* The strategic structure is not appropriate for simple projects that will never grow beyond initial inception and vision.
* Build tooling might have problems with the custom structures that diverge from the most common case of a flat structure.
  * With flexible build tool, issues can be resolved with appropriate tool configuration, by 3rd party plugins, or by developing custom build tool plugins. `klokwrk-project` uses
    [kordamp-gradle-plugins](https://github.com/kordamp/kordamp-gradle-plugins) for this purpose.

### Neutral
* The development team must become familiar with the strategic structure, which requires some time and understanding.

## Considered Options
* Traditional flat project structure.

## References
* [Organizing modules and packages](../../article/modules-and-packages/modulesAndPackages.md)
* [kordamp-gradle-plugins](https://github.com/kordamp/kordamp-gradle-plugins)
