# Project Klokwrk - Connecting the Business with Technology
* **Author:** Damir Murat
* **Created:** 13.08.2022.

When a new software project starts, developers give much attention to the possibility of applying various exciting and trending technologies. And there is nothing wrong with that. After all, new
projects are ideal opportunities to learn and advance in our careers. The problem is the typical lack of equal excitement about correctly understanding and designing the business concepts. The harsh
truth is, unfortunately, our users don't care about shiny new technologies or frameworks. They just want to improve their everyday workflows. They care only about their business domain.

## DDD - bringing business closer to technology
The philosophy of domain-driven design (DDD) focuses on the business - the heart and reason for the existence of any enterprise application. Luckily, DDD does this by describing technology-neutral
principles and patterns that are not in conflict with the latest technological advancements. DDD practices are divided into two main groups - strategic and tactical patterns. Strategic patterns try
to separate the business domain into manageable and sensible pieces with defined, explicit boundaries (bounded context) and terminology (ubiquitous language). Inside those boundaries of bounded
context, DDD uses tactical patterns to describe principles that can help and guide the development of concrete implementation artifacts.

If you start exploring and learning about DDD, you will quickly discover that this is not an easy task. Grasping DDD literature can be complex and demanding, but the lack of concrete examples of
applying it is even more problematic. There are articles and quick examples explaining some DDD patterns in isolation, but finding a full-fledged, non-trivial, documented, and complete project
example is almost impossible. And this is where Project Klokwrk tries to jump in and help.

## DDD applied
Project Klokwrk is an open-source effort that we at [CROZ](https://croz.net/) created to demonstrate applying DDD principles to a complex multi-module project. Klokwrk tries to cover and offer
solutions in a multitude of areas. From high-level project layout organization around the DDD concepts of domains and subdomains, then over implementation details like efficient usage of exceptions
in a distributed microservices system, all the way down to the low-level things like proposing and enforcing a sensible commit message format.

To get an overview of Klokwrk features, take a look at the [current list of architectural decision records](../../adr/index.md) (ADRs), or glance over the following incomplete and short selection:
- Structuring the project layout around DDD concepts.
- Defining module abstraction layers and architectural skeleton to help with dependency management and avoiding uncontrolled coupling between modules.
- Implementing hexagonal architecture for isolating domain artifacts from technology.
- Implementing CQRS and Event Sourcing backed by Axon Framework.
- Architectural tests based on custom DSL backed by ArchUnit.
- Physically distributed logical microservice on commandside, projection, and queryside.
- Many more ...

Klokwrk is not a framework. You can't download and reuse it in your project. Klokwrk is a blueprint and a working example from which you can harvest ideas and apply them yourself, taking concrete
implementations as a helpful guide and illustration. Therefore, you can't become coupled to the Klokwrk, but you can use any part you need in your project. Implemented concepts are universal, and
concrete implementation illustrates just one of potentially many ways to apply them. There is no need to wait for the next major release. Just take the latest snapshot and start exploring.

## How to start
On the Klokwrk home page (https://github.com/croz-ltd/klokwrk-project), you'll find several resources covering different topics. Of course, they can be read in any order depending on your interests,
but for newcomers, I would suggest the following:
- "[Starting up and trying the whole thing](../starting-up/startingUp.md)" - a step-by-step tutorial describing how one can run and play with Klokwrk
- "[Organizing modules and packages](../modules-and-packages/modulesAndPackages.md)" - a cornerstone article about organizing high-level project artifacts, internal module structures, and using
  hexagonal architecture in CQRS/Event Sourcing context.
- [Architecture decision records log](../../adr/index.md) (ADR log) - a series of articles describing significant implementation decisions. Those can serve as a valuable overview of various Klokwrk
  features, including the reasons to choose one implementation path over the other. Just keep in mind that not all Klokwrk features are covered there yet.
- Explore other available articles and resources on the home page.
- Explore the source code and its documentation to get an idea of how some concept or a feature is implemented.

If you have any feedback, we would like to hear it. You can use GitHub tools like [Discussions](https://github.com/croz-ltd/klokwrk-project/discussions) or
[Issues](https://github.com/croz-ltd/klokwrk-project/issues) or just send an email to the authors.

## Why and how to benefit from Project Klokwrk
Many software projects start quickly, driven by enthusiasm mainly focused on technology. But a few more aspects must be addressed for long and prosperous project life. Things like sensible project
structure, management and control of dependencies and couplings, usage of appropriate application architecture, handling distributed system issues, adhering to overarching principles like DDD, and
many more.

Trying to incorporate all those things into a single project is not easy. Teams usually build on previous experiences and scattered bits of advice from literature, trying to create a homogenous
development environment for new projects. And this is also what we had been doing in a [CROZ](https://croz.net/) for years. During that process, we often missed a single place where we could go and
take solutions from. And this is how the idea for creating Project Klokwrk was born.

Project Klokwrk is an open-source effort where we are trying to encode some best practices from our previous experiences and knowledge when implementing complex multi-module enterprise projects. It
is also an ongoing endeavor that we use for exploring new ideas or improving existing ones around the main themes of DDD, hexagonal architecture, and CQRS/Event Sourcing.

Project Klokwrk is never meant to be an all-or-nothing solution. It covers many things that can be used individually as one needs them in their projects. At [CROZ](https://croz.net/), we have already
applied Klokwrk on several projects, and none of them uses all Klokwrk features. Each of those projects took the Klokwrk parts they needed and applied them in their context.

We believe Klokwrk will continue bringing great value to us, and we hope you can find it helpful too. If you decide to invest some of your valuable time in exploring Klokwrk, we would like to know
about your experiences. Whether good or bad, your feedback will be of great value as it is an opportunity for us to improve and evolve.
