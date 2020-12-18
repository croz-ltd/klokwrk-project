[![License](https://img.shields.io/badge/License-Apache%202.0-success.svg)](https://opensource.org/licenses/Apache-2.0)
[![Continuous Integration](https://github.com/croz-ltd/klokwrk-project/workflows/Continuous%20Integration/badge.svg)](https://github.com/croz-ltd/klokwrk-project/actions?query=workflow%3A%22Continuous+Integration%22+branch%3Amaster)
[![codecov](https://codecov.io/gh/croz-ltd/klokwrk-project/branch/master/graph/badge.svg)](https://codecov.io/gh/croz-ltd/klokwrk-project)
[![Groovydoc](https://img.shields.io/badge/API%20doc-Groovydoc-brightgreen)](https://croz-ltd.github.io/klokwrk-project/groovydoc/index.html)

# Project Klokwrk
Project Klokwrk is envisioned at [CROZ](https://croz.net/) as a showcase and blueprint for implementing elaborated distributed systems on JVM. It should also serve as a playground for incubating
reusable libraries useful on its own.

The vision behind Klokwrk could be expressed in different ways, but essentially we could put it like this:
- FOR software architects and teams
- WHO are building microservice systems
- THE Klokwrk is a showcase and a blueprint
- THAT demonstrates how to set up the clean technical architecture of a complex system properly
- UNLIKE many Hello World examples that focus narrowly on just proving the concept
- OUR PRODUCT represents a holistic foundation/showcase that better addresses real-world challenges by leveraging DDD, Clean Architecture, CQRS & Event Sourcing principles, and relying on the power
  and expressiveness of Spring Boot, Axon, and Groovy.

Being a showcase, Klokwrk tries to be complete and ready to be used as a starting point in your next project. It brings the initial structures and mechanisms to help you maintain a sustainable design
from a clean foundation. Klokwrk will strive to demonstrate good practices on non-trivial use cases, accompanying them with appropriate documentation.

At the moment, Klokwrk is in its incubation, and we are still thinking about what would be interesting to include and how. Despite this, we decided to show it to the open-source community. Although
in its very early stages, we are hoping it might attract some attention and feedback.

A much broader story about Project Klokwrk's vision and roadmap can be found in the following blog posts:
- [Introducing Project Klokwrk](https://croz.net/news/introducing-project-klokwrk/)
- [Why Hello World examples are bad for clean architecture?](https://croz.net/news/why-hello-world-examples-are-bad-for-clean-architecture/)

Klokwrk is an open-source project, started by [Damir Murat](https://github.com/dmurat), one of our senior architects, with the active contribution of the broader [team](https://croz.net/) at
[CROZ](https://github.com/croz-ltd).

## Find out more
### Articles
There are several articles describing some `klokwrk-project` features in more details:
* [Starting up and trying the whole thing](support/documentation/article/startingUp.md)<br/>
  Step-by-step tutorial describing how one can run and try `klokwrk-project`.

* [Organizing modules and packages](support/documentation/article/modulesAndPackages.md)<br/>
  Discussion about principles and ideas behind organizing high-level artifacts of a project and using hexagonal architecture in CQRS/ES context.

### Videos
* [Turning lessons learned of using Axon in a legacy environment to OSS Project Klokwrk](https://www.youtube.com/watch?v=shl847FRVMI) <br/>
  Turning lessons learned from using Axon Framework in a complex legacy environment to an open-source Project Klokwrk.

* [Project Klokwrk: how it helps define software architecture and solves integration and component testing](https://www.youtube.com/watch?v=35oUxjXWNYU) <br/>
  In this workshop, we will dive deep into technical details of two interesting Klokwrk features: how it helps in defining software architecture, and how it solves integration and component
  testing (with event replay).

### Architecture decision log
`klokwkr-project` maintains a log of architecturally significant decisions by leveraging architecture decision records (ADRs). You can [explore it here](support/documentation/adr/index.md).

### Misc
* [klokwrk-tool-gradle-source-repack](tool/klokwrk-tool-gradle-source-repack/README.md) utility

  The tool for repackaging Gradle source files into an archive suitable to be used as a repository of sources when debugging Gradle internals from IDEA. It is also interesting that the tool is
  created in Groovy and compiled into GraalVM native image without any reflection related GraalVM configuration.


* [Lists of 3rd party software issues related to `klokwkr-project`](support/documentation/misc/klokwrkRelatedIssuesInTheWild.md)
