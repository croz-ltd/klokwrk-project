# ADR-0013 - Validation taxonomy
* **Status: accepted**
* Dates: proposed - 2021-04-15
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
With today's technologies and frameworks, validation of input data looks like a solved problem. You just add few annotations on DTOs (Data Transfer Objects), hook up the validation service with the
app, and move on.

However, there are much more under the cover. When digging into the details, various questions pop up. Where exactly is the best place for input data validation? Are there multiple levels or phases
of validation? What if we need a system state for validation? What happens when numerous inbound channels are in play?

After trying to answer these, it turns out that validation is more involved than what is commonly shown in basic examples. Solutions are not too complex but require establishing some principles and
rules.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We will execute syntactic validation at the application facade layer. If necessary for UI requirements, it may also be executed on the adapter layer.**

Core validation at the application facade layer ensures that input data from any inbound channel will be validated in the same way. An example can be seen in `BookingOfferCommandApplicationService`
class from `cargotracking-booking-app-commandside` module.

Validation at the inbound channel adapter layer is used when there is a need for UI or client-specific validations. A typical example is an email verification scenario where, with repetition, we want
to assure that the user submitted the correct email.

**We will organize validation into syntactic and semantic phases. Each of them will be divided further into subphases if necessary.**

**We will execute semantic validation inside the domain layer. Depending on nature, this will happen either in the aggregate or in the application facade.**

### Syntactic validations
We consider validation being syntactic if it does not require any system state. It validates input data fields in isolation and possibly some interdependencies between them (cross-field validation).
Syntactic "stateless" validations are executed first. If they fail, semantic validations won't be triggered at all.

### Semantic validations
On the other hand, semantic validation requires access to the system state. It may deal with checking if, for the current aggregate state, the request is valid or not. It may also check some other
non-aggregate conditions, such as the existence of necessary data in registries. We are reporting semantic validation failures as domain exceptions.

Regarding the order, it is advisable to group and execute all non-aggregate-related validation first (checking against registry data, for example). Such validations do not require transactional
aggregate locking. They may use the transaction but must not have the aggregate in that transaction. Only if they pass, we should proceed with validations that require opening a transaction for the
aggregate (for example, if input data are valid for the current aggregate state).

### Syntactic validation subphases
We can divide syntactic validation into several internal phases. Those phases are ordered from the simplest to the more complex - existence, size, lexical content, and syntax format.

Existence validation ensures that provided data exist and are not empty. Further processing makes no sense when data is empty. In this subphase, we check for null objects, empty strings (zero-length
or whitespace only), empty collections, etc.

Size validation verifies if data are reasonably big. Before further phases, we are checking the length/size of input data no matter od the data type. Size checks will prevent additional processing of
too big data, which might cause performance issues. Also, reporting about size failures might be helpful from the user perspective as it is a widespread mistake.

Lexical content validation checks if data contain the correct characters and encoding. This phase might be helpful if we are receiving the data in complex formats like JSON, XML, or HTML. For simpler
data inputs like size-limited strings, this phase is commonly executed as a part of the following stage.

Syntax format validation verifies if the format is correct. For strings, this is often achieved with regular expressions. When regex is too complicated, we might get better results with specialized
validator implementations.

We are reporting syntactic validation failures through means of an employed validation framework. As we are using JSR 380 (Jakarta Bean Validation 2.0) implementation, syntactic validation failures
are reported through `jakarta.validation.ConstraintViolationException`. With JSR 380, we are implementing syntactic validation ordering with `GroupSequence` annotation, as demonstrated by
`BookCargoCommandRequest` class from `cargotracking-booking-app-commandside` module.

## Consequences
### Positive
- Consistency and predictability in validation handling and processing.

### Negative
- None.

## Considered Options
- None.

## References
- Manning - Secure by Design (https://www.manning.com/books/secure-by-design), Chapter 4.3: Validation
- Domain-Driven Hexagon - https://github.com/Sairyss/domain-driven-hexagon#types-of-validation

