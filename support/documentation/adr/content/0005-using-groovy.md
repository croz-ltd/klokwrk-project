# ADR-0005 - Using Groovy
* **Status: accepted**
* Dates: proposed - 2020-11-02
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
We would like to have a language or language extension that is very close, very similar, and 100% compatible with Java, but that offers significant productivity boosters at the same time.

Java syntax is often verbose, and it is common to use various IDE tricks (folding/unfolding) or 3rd party libraries (e.g., Lombok) to deal with some of that clutter. It would be much better to have
a language with a clear, concise syntax that is almost naturally understandable for Java developers. At the same time, the language should provide user-friendly extensions of Java SDK and best
practice implementations of various programming patterns.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We will use Groovy as a primary language.**

## Consequences
### Positive
* Groovy is a very concise and productive alternative to Java language. It has a straightforward syntax for Java developers as it feels like Java, but without clutter.
* Groovy is entirely compatible with Java. In fact, almost every Java code can be compiled and executed as a Groovy code.
* Any Java library can be used from Groovy and vice-versa.
* Besides concise and straightforward syntax, Groovy offers various extensions to the JDK and implements many best-practice patterns through the use of annotations and AST (Abstract Syntax Tree)
  transformations.
* Groovy is, at the same time, statically compiled and dynamic language, allowing the programmer to tune dynamism as appropriate to the problem at hand.
* Great support for general-purpose testing via [Spock library](http://spockframework.org/spock/docs/2.0-M4/).
* Groovy can be used in various contexts, from scripting to developing full-featured enterprise applications.
* Groovy is an open-source language, maintained by the community, and hosted by Apache foundation.
* Groovy offers ways for quick prototyping with minimal ceremony.
* `klokwrk-project` development team has a significant experience with Groovy.

### Negative
* Java tooling often lags with support for Groovy.
  * Although the situation is currently quite acceptable (e.g., IDE support, code coverage support, static analysis), there are some areas where much more work is needed in the future (e.g., GraalVM
    native image support).
  * Related to GraalVM native image support, it looks like things are improving considerably with Java 11 and latest GraalVM releases
    ([GraalVM native-image - from 2.1s to 0.013s startup time | Groovy Tutorial](https://www.youtube.com/watch?v=RPdugI8eZgo)). Also, GraalVM native image still has limited usage. In the domain of
    full-featured enterprise applications, it is not critical how fast an application can start, but rather how quickly it can run
    ([Pros and Cons for Using GraalVM Native-Images](https://dzone.com/articles/profiling-native-images-in-java)).

### Neutral
* Many non-informed developers perceive Groovy as a strictly dynamic language capable and suitable only for scripting.
* There is some learning curve, especially if one wants to use Groovy in an idiomatic way. However, that learning curve is significantly lower than in any other JVM language.

## Considered Options
* Java
* Kotlin
  * Not considered extensively since the development team has an only basic knowledge of the language and its ecosystem. Nevertheless, Kotlin looks like an interesting option.
  * Much more different from Java than is the case with Groovy.
  * It looks like many Kotlin features have their counterparts in Groovy. The only significant exception is coroutines, and that might be addressed with
    [Project Loom](https://openjdk.java.net/projects/loom/) in the future.

## References
* [Design patterns in Groovy](https://groovy-lang.org/design-patterns.html)
* [Groovy Language Documentation](https://groovy-lang.org/single-page-documentation.html)
* [Spock library](http://spockframework.org/spock/docs/2.0-M4/)
* [Groovy in Action, Second Edition](https://www.manning.com/books/groovy-in-action-second-edition)
* [GraalVM native-image - from 2.1s to 0.013s startup time | Groovy Tutorial](https://www.youtube.com/watch?v=RPdugI8eZgo)
* [Pros and Cons for Using GraalVM Native-Images](https://dzone.com/articles/profiling-native-images-in-java)
