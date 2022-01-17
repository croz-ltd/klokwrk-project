# ADR-0015 - Handling Exceptions in Distributed CQRS System
* **Status: accepted**
* Dates: proposed - 2021-08-10
* Dates:
  - proposed - 2021-08-10
  - updated - 2022-01-17
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
Developers consider dealing with local (runtime) exceptions in JVM quite convenient and easy. As a consequence, exceptions are often used as a means for an alternative execution flow. One typical
example is reporting structural and business validation violations.

In the local JVM environment, expected execution flow exceptions are usually created with a full stack trace included. However, as these exceptions represent "normal" conditions, creating a full
stack trace is wasteful in terms of performance and resources.

The full stack trace problem is additionally emphasized in a distributed JVM environment. Not only the stack trace creation is wasteful, but it will take much more bandwidth to transfer it on the
other side.

And finally, in a distributed JVM environment, we cannot safely assume that our custom exception classes are present in the classpath of both JVMs. Therefore, we can not assume the exception created
on the server-side can be deserialized on the client.

To remedy the situation, we can use a small number of stack-less exception classes shared between JVMs included in the communication. They belong to the boundary layer and are part of the API used
for distributed communication. To read more about boundary layer, take a look near the end of "Domain libraries" section in
"[Organizing modules and packages](../../article/modules-and-packages/modulesAndPackages.md#domain-libraries)" article.

For communicating various custom exceptions, we can use violation (or error) codes. The violation code defines its severity and contains several additional properties for describing violation details.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**For distributed error reporting, we will use stack-less exceptions for communicating custom error codes between JVMs in a distributed Axon environment.**

In a distributed Axon environment, the dispatching side (let's call it a client) sends commands and queries over Axon Server to the handling side (let's call it a server). When the handling side
detects broken business invariants, it can raise an exception to report the details to the dispatching side.

Axon framework already contains base infrastructure for reporting such distributed errors via so-called exception details. Exception details can be any DTO-like class shared between communicating
parties, including all necessary data describing error conditions.

### Decision details
#### Business exception handling
In our case, as exception details DTO, we will use the `DomainException` class and its descendants. It is a stack-less exception that carries `ViolationInfo` property. `ViolationInfo` contains
`Severity` and `ViolationCode` properties. `ViolationCode` contains `code` and `codeMessage` in English. There are also a `resolvableMessageKey` and `resolvableMessageParameters` that support error
code resolving through a resource bundle for internationalization purposes.

It is worth noting that `DomainException` can be used on the dispatching side (client) too. Exception (un)wrapping on the handling side and centralized exception handling on the dispatching side are
hidden in infrastructural code. From the developer's perspective, he works with `CommandException` or `QueryException` only (both are extended from `DomainException`). This is a nice addition as
business invariants are handled the same way on handling and dispatching sides, without depending on classes from the Axon framework.

As a usage example on the handling side, take a look at `BookingOfferAggregate` or `BookingOfferSummaryQueryHandlerService` classes. An example for the dispatching side can be found in
`BookingOfferFactoryService` class.

#### Unexpected exception handling
When handling business exceptions, we are not interested in stack traces. Since business exceptions are just a form of the alternate execution flow, there isn't much benefit in logging their
stacktrace.

Contrary, we want to log the stack trace when an unexpected exception (i.e., NullPointerException) occurs at the remote handler. However, communication constraints still hold, and we still have to
use error codes for communicating exceptions. Further, to correlate exceptions on handling and dispatching sides, we must use an exception identifier and put it in the log messages on both sides.

The logic of handling unexpected exceptions on the handling side can be seen in the `CommandHandlerExceptionInterceptor` and `QueryHandlerExceptionInterceptor` classes.

## Consequences
### Positive
- consistent and optimal handling of remote business exceptions
- consistent and optimal handling of remote unexpected exceptions
- identical handling of local and remote business exceptions for developers
- correlation of remote unexpected exceptions

### Neutral
- a bit unusual way of constructing exceptions

### Negative
- n/a

## Considered Options
- n/a

## References
- Organizing modules and packages (https://github.com/croz-ltd/klokwrk-project/blob/master/support/documentation/article/modules-and-packages/modulesAndPackages.md)
- Steven van Beelen - Live coding session: Axon 4.4 in practice (start time: 29:25) (https://www.youtube.com/watch?v=UcmxyEjbzf4&t=1765s)
- Steven van Beelen - Live coding session: Axon 4.4 in practice - sample code (https://github.com/smcvb/gamerental)
- Axon Reference Guide - Exception Handling (https://docs.axoniq.io/reference-guide/axon-framework/messaging-concepts/exception-handling)
- The Exceptional Performance of Lil' Exception (https://shipilev.net/blog/2014/exceptional-performance/)
