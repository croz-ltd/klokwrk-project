# ADR-0002 - Strategic Project Structure
* **Status: accepted**
* Dates: proposed - 2020-10-28
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
Excluding the simplest hello-world-like cases, any useful project typically contains several modules. The traditional way of organizing project modules is just putting them under the project root.
We can call that structure - **traditional flat project structure**, or just **flat structure**.

While the flat structure is appropriate and sufficient for simpler projects, when the project grows and the number of modules increase, the flat structure starts suffering from many drawbacks:
* Flat structure does not scale when the project and number of modules grows.
* Flat structure is hard and confusing to navigate with numerous modules in the project.
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
In strategic structure, we think about project root as an equivalent to the whole system. One step below the system, we have organizational directories for each domain in our system, then the
directory for generic reusable modules and the directory for language extensions.

Domain directories can contain modules representing domain libraries that we can share across the whole domain. We can distinguish domain library modules by the presence of the `lib` prefix in their
name.

We can further organize the content of domain directories into appropriate subdomains using a flat approach. In that case, an organization and grouping mean is the module name itself, and it must
contain the subdomain name. Optionally, we can organize subdomain modules into subdomain directories. Using subdomain directories is useful when we have, for example, more than two subdomains in the
same domain.

Generic reusable modules contain infrastructural code that often deals with nuances of integration with 3rd party libraries. Generic reusable modules have their organizational directory
called - `lib`. It is placed directly under the project root. We can use generic reusable modules across the whole system.

Language extension modules contain code that extends the feature set of programming language or its SDK library. The organizational directory of language extensions is - `lang`. We can use generic
reusable modules across the whole system.

Much more details about is available in "[Organizing modules and packages](../../article/modulesAndPackages.md)".

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
  * With flexible enough build tool, can be solved with appropriate tool configuration, by 3rd party plugins, or by developing custom build tool plugins. `klokwrk-project` uses
    [kordamp-gradle-plugins](https://github.com/kordamp/kordamp-gradle-plugins) for this purpose.

### Neutral
* The development team must become familiar with the strategic structure, which requires some time.

## Considered Options
* Traditional flat project structure.

## References
* [Organizing modules and packages](../../article/modulesAndPackages.md)
* [kordamp-gradle-plugins](https://github.com/kordamp/kordamp-gradle-plugins)
