# ADR-0002 - Strategic Project Structure
* **Status: accepted**
* Dates:
  * proposed - 2020-10-28
  * updated - 2022-07-18
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
Excluding the simplest hello-world-like cases, any useful project typically contains several modules. The traditional way of organizing project modules is just putting them under the project root.
We can call that structure - **traditional flat project structure**, or just **flat structure**.

While the flat structure is appropriate and sufficient for simpler projects, when the project grows and the number of modules increase, the flat structure starts suffering from many drawbacks:
* Flat structure does not scale when the project and number of modules grows.
* Flat structure is hard and confusing to navigate with numerous modules at the same hierarchy level.
* Flat structure relies only on module names to provide hints about relations between modules.
* Flat structure does not suggest the abstraction level of a specific module.
* Flat structure does not use any high-level constructs that might suggest how modules are organized and related.
* Flat structure often requires extracting modules in separate repositories just because confusion becomes unbearable with numerous modules.
* When using microservices, flat structure practically forces us to use one project per microservice.

> Note: Terms **traditional flat project structure** and **strategic DDD project structure** (see below) are ad-hoc terms introduced just for this document. However, in the `klokwrk-project`,
> we may use them in other places for convenience.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We'll organize project modules around strategic DDD (Domain Driven Design) constructs of domain and subdomain.**

We can call that structure - *strategic DDD project structure*, or just *strategic structure*.

### Decision Details
Concrete implementations of strategic structure can differ. So we'll cover here two variants that we used at the `klokwrk-project` at different moments in its evolution. We'll start with a simpler
structure used in the klokwrk at the beginning. Regarding the naming, let's call it a *simple strategic structure*.

#### Simple strategic structure
In this structure variant, all structure-related directories are placed directly under a project root (`klokwrk-project`) as shown in the following listing:

    klokwrk-project
    ├── ... (other files or directories)
    ├── cargotracker
    │   ├── cargotracking-booking-app-commandside
    │   ├── cargotracking-booking-app-queryside-projection-rdbms
    │   ├── cargotracking-booking-app-queryside-view
    │   ├── cargotracking-booking-app-rdbms-management
    │   ├── cargotracking-booking-lib-boundary-web
    │   ├── cargotracking-booking-lib-out-customer
    │   ├── cargotracking-booking-lib-queryside-model-rdbms-jpa
    │   ├── cargotracking-booking-test-component
    │   ├── cargotracking-booking-test-support-queryside
    │   ├── cargotracking-booking-test-support-testcontainers
    │   ├── cargotracking-domain-model-aggregate
    │   ├── cargotracking-domain-model-command
    │   ├── cargotracking-domain-model-event
    │   ├── cargotracking-domain-model-service
    │   ├── cargotracking-domain-model-value
    │   ├── cargotracking-lib-axon-cqrs
    │   ├── cargotracking-lib-axon-logging
    │   ├── cargotracking-lib-boundary-api
    │   ├── cargotracking-lib-boundary-query-api
    │   ├── cargotracking-lib-domain-model-command
    │   ├── cargotracking-lib-domain-model-event
    │   └── cargotracking-lib-web
    ├── ... (other files or directories)
    ├── lang
    │   ├── klokwrk-lib-xlang-groovy-base
    │   ├── klokwrk-lib-xlang-groovy-contracts-match
    │   └── klokwrk-lib-xlang-groovy-contracts-simple
    ├── ... (other files or directories)
    ├── lib
    │   ├── klokwrk-lib-hi-datasourceproxy-springboot
    │   ├── klokwrk-lib-hi-jackson-springboot
    │   ├── klokwrk-lib-hi-spring-context
    │   ├── klokwrk-lib-archunit
    │   ├── klokwrk-lib-datasourceproxy
    │   ├── klokwrk-lib-hibernate
    │   ├── klokwrk-lib-jackson
    │   ├── klokwrk-lib-spring-data-jpa
    │   ├── klokwrk-lib-validation-constraint
    │   ├── klokwrk-lib-validation-springboot
    │   └── klokwrk-lib-validation-validator
    ├── ... (other files or directories)
    ├── platform
    │   ├── klokwrk-platform-base
    │   ├── klokwrk-platform-micronaut
    │   └── klokwrk-platform-spring-boot
    ├── ... (other files or directories)
    ├── support
    ├── ... (other files or directories)
    ├── tool
    │   └── klokwrk-tool-gradle-source-repack
    └── ... (other files or directories)

We can think about the project root as equivalent to the whole system. One step below the system, we have organizational directories for each domain in our system (`cargotracker` in our example),
then the directory for **generic reusable libraries** (`lib`), and the directory for **language extensions** (`lang`).

Generic reusable libraries contain infrastructural code that often deals with nuances of integration with 3rd party libraries. We can use generic reusable libraries as dependencies of infrastructural
code in the whole system. The important distinction are pure domain modules, which should not have (in general) a dependency on any infrastructural code.

Language extensions contain code that extends the feature set of programming language or its SDK library. We can use language extensions across the whole system, even in pure domain modules.

Just bellow the root we have some additional directories like `tool` (containing custom-developed miscellaneous tools related to the project), `platform` (containing Gradle-related artifacts for
dependency management), and `support`. Directory `support` contains every other artifact (other than source code) necessary for various aspects of the project. Here we can find documentation,
supportive scripts, git hooks, etc.

Inside domain directories (`cargotracker` in our example), we have three different artifact types. At the lowest abstraction level are **domain libraries** that can be recognized by
`[domain-name]-lib-*` pattern in their name (i.e., `cargotracking-lib-axon-cqrs`, `cargotracking-lib-web`, etc.). Domain libraries contain code reusable across the domain.

Then we have **subdomain libraries**, recognized by `[domain-name]-[subdomain-name]-*` pattern. These contain code reusable only inside a single subdomain. In our example, the name of a subdomain
is `booking`, so `cargotracking-booking-lib-boundary-web`, `cargotracking-booking-lib-out-customer`, etc., are examples of subdomain libraries.

And finally, at the highest abstraction level, we have applications. Since, usually, applications belong to subdomains, their name will follow the `[domain-name]-[subdomain-name]-*-app` pattern
where the `app` suffix distinguishes them from subdomain libraries.

#### Elaborate strategic structure.
Although a significant improvement over traditional flat project layouts, a simple strategic structure has some shortcomings. Those issues may not be a real burden in the early project stages. But
the problems become more evident as the project grows, especially if it grows in the number of subdomains and corresponding modules.

For example, as the most valuable project artifacts, modules are not separated from all other artifacts in the project root. This certainly does not help when you try to navigate through the project
tree. Further, all domain and subdomain modules are crammed together in a single directory. Although this may work for a (very) small number of subdomains, it can become a heavy burden if the number
of subdomains grows.

The following listing shows the example of the elaborate strategic structure as is used today in the `klokwrk`:

    klokwrk-project
    ├── ... (other files or directories)
    ├── modules
    │   ├── cargotracker
    │   │   ├── booking
    │   │   │   ├── app
    │   │   │   │   ├── cargotracking-booking-app-commandside
    │   │   │   │   ├── cargotracking-booking-app-queryside-projection-rdbms
    │   │   │   │   ├── cargotracking-booking-app-queryside-view
    │   │   │   │   └── cargotracking-booking-app-rdbms-management
    │   │   │   └── lib
    │   │   │       ├── cargotracking-booking-lib-boundary-web
    │   │   │       ├── cargotracking-booking-lib-out-customer
    │   │   │       ├── cargotracking-booking-lib-queryside-model-rdbms-jpa
    │   │   │       ├── cargotracking-booking-test-component
    │   │   │       ├── cargotracking-booking-test-support-queryside
    │   │   │       ├── cargotracking-booking-test-support-testcontainers
    │   │   │       ├── cargotracking-domain-model-aggregate
    │   │   │       ├── cargotracking-domain-model-command
    │   │   │       ├── cargotracking-domain-model-event
    │   │   │       ├── cargotracking-domain-model-service
    │   │   │       └── cargotracking-domain-model-value
    │   │   └── lib
    │   │       ├── cargotracking-lib-axon-cqrs
    │   │       ├── cargotracking-lib-axon-logging
    │   │       ├── cargotracking-lib-boundary-api
    │   │       ├── cargotracking-lib-boundary-query-api
    │   │       ├── cargotracking-lib-domain-model-command
    │   │       ├── cargotracking-lib-domain-model-event
    │   │       └── cargotracking-lib-web
    │   ├── lang
    │   │   ├── klokwrk-lib-xlang-groovy-base
    │   │   ├── klokwrk-lib-xlang-groovy-contracts-match
    │   │   └── klokwrk-lib-xlang-groovy-contracts-simple
    │   ├── lib
    │   │   ├── klokwrk-lib-hi-datasourceproxy-springboot
    │   │   ├── klokwrk-lib-hi-jackson-springboot
    │   │   ├── klokwrk-lib-hi-spring-context
    │   │   ├── klokwrk-lib-archunit
    │   │   ├── klokwrk-lib-datasourceproxy
    │   │   ├── klokwrk-lib-hibernate
    │   │   ├── klokwrk-lib-jackson
    │   │   ├── klokwrk-lib-spring-data-jpa
    │   │   ├── klokwrk-lib-validation-constraint
    │   │   ├── klokwrk-lib-validation-springboot
    │   │   └── klokwrk-lib-validation-validator
    │   ├── platform
    │   │   ├── klokwrk-platform-base
    │   │   ├── klokwrk-platform-micronaut
    │   │   └── klokwrk-platform-spring-boot
    │   └── tool
    │       └── klokwrk-tool-gradle-source-repack
    ├── ... (other files or directories)
    ├── support
    └── ... (other files or directories)

At the top level, we have just two directories with predefined names: `modules` and `support`. All our development efforts will primarily focus on `modules`, while here and there, we'll add something
in the `support` directory. Therefore, 99% of the time, developers will focus only on `modules` and `support`, and the rest of the root content will no longer clutter with the most crucial project
artifacts.

The first level of the `modules` directory content is organized the same as it was with the simple strategic structure. However, there is a significant distinction under the domain directory
(`cargotracker` in our example). At the domain level, each subdomain has its own dedicated directory (`booking` in our example), and domain libraries are placed in the dedicated `lib` directory. At
the subdomain level, we further categorize subdomain libraries in the `lib` directory and applications in the `app` directory.

As a result, the elaborate strategic structure will leave little doubt about each module's context and abstraction level. Navigation should be easy and apparent.

More details about strategic structure can be found in "[Organizing modules and packages](../../article/modules-and-packages/modulesAndPackages.md)" article.

## Consequences
### Positive
* The strategic structure provides a means for organizing modules that will support future system growth.
* The strategic structure facilitates easier management of dependencies between modules and dependencies on 3rd party libraries.
* The strategic structure provides organizational artifacts that help with orientation and navigation.
* The strategic structure organizes modules around well know DDD concepts of domain and subdomain.
* The strategic structure provides a means for organizing generic reusable libraries and language extensions.
* The strategic structure provides a means for easier development of the large system under a single project.

### Negative
* The strategic structure is not appropriate for simple projects that will never grow beyond initial inception and vision.
* Build tooling might have problems with the custom structure, which diverges from the most common case.
  * With flexible enough build tool, issues can be resolved with appropriate tool configuration, by 3rd party plugins, or by developing custom build tool plugins. `klokwrk-project` uses
    [kordamp-gradle-plugins](https://github.com/kordamp/kordamp-gradle-plugins) for this purpose.

### Neutral
* The development team must become familiar with the strategic structure, which requires some time.

## Considered Options
* Traditional flat project structure.

## References
* [Organizing modules and packages](../../article/modules-and-packages/modulesAndPackages.md)
* [kordamp-gradle-plugins](https://github.com/kordamp/kordamp-gradle-plugins)
