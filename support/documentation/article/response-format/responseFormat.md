# The format of rendered responses
* **Author:** Damir Murat
* **Created:** 14.04.2021.
* **Updated:** 14.04.2021.

This article explains how `klokwrk-project` applications deal with structuring server-side responses. The primary purpose is presenting base ideas and documenting current implementation. However,
explained ideas might help all readers involved in developing any kind of client-server system like web applications, for example.

## Introduction
Some kind of response rendering is present in every application. However, rendered responses are rarely given appropriate attention and are typically used in a form provided by leveraged framework or
technology stack. This might work well for more straightforward applications that deal with a single inbound/outbound channel, like, for example, smaller to medium web applications.

Even for complex systems, the lack of a prescribed response format is not immediately apparent as a problem. At first, applications focus on their primary outbound channels, like HTTP-based channels,
for example, and most development efforts are focused on successful responses for main use cases. Even if there are some issues, the application can usually adapt quickly with ad-hoc solutions that
combine features of the framework and mechanisms of the current protocol. Although such changes require some refactoring on the client-side, the lack of response format typically is not yet perceived
as a severe problem. After all, clients are primarily interested in data from the payload of successful responses, and that data is always in a free-form shape since it is different for each endpoint.

With time, the focus shifts to erroneous scenarios like input data validation or business exceptions. Those scenarios are typically crammed into the existing free-form shape, often implying implicit
schema that the client needs to know for each erroneous scenario type. Again, the combination of protocol mechanics together with modern web framework facilities helps to remedy problems. However,
almost inevitably, additional small ad-hoc solutions creep in, primarily when the out-of-the-box solution does not provide a placeholder for every piece of metadata that the application wants to
transfer to clients. But it looks like the overall situation is still under control.

Eventually, when the application starts adding support for different types of inbound/outbound channels (i.e., different types of messaging), the lack of a prescribed response format becomes more
apparent. It is awkward to introduce a new kind of metadata for responses, and previous ad-hoc additions prevent efficient refactoring. If the application was already deployed into production, any
change in response format is problematic as it might break existing clients.

If you experienced any described scenarios and problems, you might appreciate the consistency that the prescribed response format can bring into the system. We certainly had similar issues in the
past. We took the opportunity to rethink the formatting of responses in `klokwrk-project`, and try to develop an appropriate response format that can be used for all our responses.

## Implementing response format
In the `klokwrk-project`, response rendering relies on interceptors specific for each channel type. At the moment, only web channel response interceptors are implemented, but principles should be
applicable for other channel types.

In general, the response format is divided into two main sections - `payload` and `metaData`:
```
{
  "metaData": {
    ...
  },
  "payload": {
    ...
  }
}
```

For successful responses, the `payload` contains domain data. Domain data is not constrained by the format and can be in the free-form shape appropriate for domain and actual endpoint. There is nothing
new. Each web application works that way.

On the other hand, application-focused metadata in the response payload is not so standard in the wild. On the client-side of web environments, some common metadata pieces are typically extracted
from HTTP statuses and headers. Any custom metadata usually requires introducing a new custom header, which makes metadata quite fragmented. For non-HTTP channel types, a similar mechanism exists,
but application support must be reimplemented and agreed upon with clients.

It might be easier, simpler, and more consistent conveying application-specific metadata through the payload itself. This does not mean we want to discard everything that each established protocol
can offer. In the web environment, we still can and should use HTTP statuses and headers for determining response types to support high-level processing without parsing the payload. But eventually,
we will probably parse the payload, and having application-specific metadata in the payload can simplify response processing.

Back to our format. For successful responses, `metaData` is minimal. It is used for transmitting some general information about the response like timestamp and locale of the response.

Metadata becomes more useful for erroneous responses. The `payload` is still present but is empty. On the other hand, `metaData` is responsible for communicating every sensible detail about the
failure. The contained metadata information may vary depending on failure type and severity, but for each failure category, the metadata response format is defined. It is no longer in a free-form
shape.

Having an exact format for response metadata brings consistency in the client-side implementation. The client now knows where to look for information regardless of the channel and protocol type.
Consequently, significant parts of implementation can be reused as each new channel is added.

In `kolkwrk-project`, the general response metadata structures are implemented in classes of `org.klokwrk.cargotracker.lib.boundary.api.metadata.response` package from `cargotracking-lib-boundary-api`
module. The general response metadata format can be extended for specific outbound channels, if needed. Those extensions communicate additional metadata required for seamless response handling tied
to some concrete protocol. For example, we can find HTTP-specific response metadata format extensions in the `org.klokwrk.cargotracking.lib.web.metadata.response` package of `cargotracking-lib-web`
module.

Now, we'll take a look how the response format looks like for various response types. All following examples are presented with a typical web outbound channel in mind. It means that some parts of the
metadata will be dedicated to the **HTTP** protocol. Still, all other elements are general and should be present in the response regardless of the channel type.

Note that the presented response format might change in the future if we find the need to include more data.

## Successful response format
The next example represents the format of a successful response:

```
{
  "metaData": {
    "general": {
      "timestamp": "2021-04-11T08:08:18.621924Z",
      "severity": "info",
      "locale": "en"
    },
    "http": { // present only for reponses transmitted over HTTP-based channel
      "status": "200",
      "message": "OK"
    }
  },
  "payload": {
    // domain data
    ...
  }
}
```

The `payload` section contains domain data that is not constrained by the format and can be in any free-form shape as appropriate.

The `metaData` section is divided into `general` and `http` sections. The `general` section is always rendered, and it communicates basic information about the response:
- `severity` - Response severity. It might be `info`, `warning`, or `error`. For successful responses, it is always `info`. It is set to `warning` for erroneous responses that represent client error.
  It is set to `error` when server-side processing fails unexpectedly.
- `locale` - Locale presented as language tag.
- `timestamp` - Timestamp with UTC zone presented in 27 characters long ISO-8601 format.

The `http` section contains minimal information specific to the HTTP protocol:
- `status` - HTTP status code. Must be the same as a status code of transmitted HTTP response.
- `message` - HTTP status message. Must be the same as a status message of transmitted HTTP response.

The `http` section is only present if the response was transmitted over HTTP. For web channel it is mandatory, but that section will not exist for other, non-HTTP based, channels.

The usefulness of the protocol-specific `http` section might feel questionable. It only duplicates data that are readily available from the protocol itself. However, data is minimal, and the presence
of protocol or channel-specific data in the payload can bring consistency in the client-side response processing. In particular when multiple protocols and channel types have to be supported.

## Erroneous response formats
Erroneous responses represent and report the occurrence of some violation in request processing. The majority of such responses will have `warning` severity signaling the problem in the request.
Something like invalid input data, inability to find some domain data, inappropriate request considering the server-side state, the invalid HTTP method usage, etc.

In case of `error` severity, we usually have a problem with the server-side logic, meaning some bug needs to be resolved, or some piece of infrastructure should be more resilient.

Severity alone does not provide enough information, and for different violation types, we might want to give quite different details in the response. Violation types represent the base categorization
of violations. At the moment, we have three main categories (`domain`, `validation`, and `unknown`) and one auxiliary (`infrastructure_web`):
- `domain` - domain violation signals unsatisfied invariant in the domain. For example, when requested operation is not allowed for the current aggregate state, or when requested domain data cannot
  be found, etc. `domain` violations have the `warning` severity.
- `validation` - this violation signals an unsatisfied invariant in submitted input data. Severity is also the `warning`.
- `unknown` - this violation represents any kind of unhandled exception that is typically caused by a bug. For example, something like `NullPointerException` or something similar. All `unknown`
  violations will have the `error` severity.
- `infrastructure_web`- any `infrastructure` violation category represents some broken invariants handled by framework code outside our control. For example, the current implementation uses the
  `infrastructure_web` category for signaling exceptions handled by `ResponseEntityExceptionHandler` from the Spring MVC framework. Severity might be either the `warning` or the `error`, depending on
  concrete exception.

The following sections will explore the details of the response format for each violation type.

### Format of `unknown` violation
As they represent unforeseen situations, `unknown` violations will have the `error` severity. Here is an example of the format for `unknown` violation:

```
{
  "metaData": {
    "general": {
      "timestamp": "2021-04-12T09:23:21.225528Z",
      "severity": "error",
      "locale": "en"
    },
    "http": {
      "status": "500",
      "message": "Internal Server Error"
    },
    "violation": {
      "code": "500",                       // custom code of violation. Does not have to match HTTP status code.
      "message": "Internal server error.", // custom localizable message of violation.
      "type": "unknown",                   // violation type. Can be one of "unknown", "domain", "validation" or "infrastructure_web".
      "logUuid": "a3cb8feb-f867-4c62-b5c8-7a3f9d2522f9"  // Violation identifier in server-side log. Optional element which appears only for violations with "error" severity.
    }
  },
  "payload": {}                            // always empty for erroneous responses
}
```

Sections `general` and `http` are very similar to the successful response, except for the `severity` value. Section `violation` carries all details about specific `unknown` violation as follows:
- `code`- this is a custom code for violation. In our example, it matches the HTTP status code, but it does not have to. It can be anything appropriate for the application. At this point, our
  implementation uses codes that are the same as HTTP statuses as they provide a pretty good general categorization of failures.
- `message`- this is a custom localized message that we can present to the end-user. At this point, our implementation uses messages that are the same as HTTP status messages, except they are
  localized based on the locale of the current request.
- `type`- contains the type of the violation. Type is used for a high-level categorization of violations. Besides `unknown`, at the moment, the value can be `domain`, `validation`, or
  `infrastructure_web`.
- `logUuid`- optional element present only for violations with the severity of `error`. When we get an unplanned exception, we want to log it on the server-side, but we do not wish to send a
  stacktrace to the client for many reasons. However, to empower the client for sensible issue reporting, we are sending the exception identifier instead.

### Format of `domain` violation
When domain logic concludes that we cannot execute a particular request, it will signal broken invariant via the violation of `domain` type. An example of a domain invariant might be forbidding
adding items to the order once it is confirmed and shipped. Domain failures have `warning` severity.

Here is an example of the format for `domain` violation:
```
{
  "metaData": {
    "general": {
      "timestamp": "2021-04-12T09:25:49.220167Z",
      "severity": "warning",
      "locale": "en"
    },
    "http": {
      "status": "400",
      "message": "Bad Request"
    },
    "violation": {
      "code": "400",
      "message": "Destination location cannot accept cargo from specified origin location.",
      "type": "domain"
    }
  },
  "payload": {}
}
```

Data from the `violation` section looks very similar to `unknown` violations. We have the `type`, and custom `code` and `message` elements. They all have the same meaning as before. However, the
`logUuid` element is missing because we report a violation of the expected domain invariant. There is no actual error here. The client should send another request that will be appropriate for the
current state of some domain aggregate:
- `code`- custom code for violation
- `message`- custom localized message that we can present to the end-user
- `type`- the type of the violation (`domain` in this case)

### Format of `validation` violation
Some kind of input data validation comes with every non-trivial application, so we want to have the appropriate format for reporting invalid data. Validation failures always have a `warning`
severity.

We are reporting validation failures with the `validation` violation. It contains common `code`, `message` and `type` elements, but it also adds new `validationReport` element - a container for all
relevant details of failing validation. Element `validationReport` is divided in two sections. Section `root` provides some context about failed validation, while `constraintViolations` lists all
individual broken constraints:

```
{
  "metaData": {
    "general": {
      "timestamp": "2021-04-12T09:27:41.435321Z",
      "severity": "warning",
      "locale": "en"
    },
    "http": {
      "status": "400",
      "message": "Bad Request"
    },
    "violation": {
      "code": "400",
      "message": "Request is not valid.",
      "type": "validation",

      "validationReport": {
        "root": {
          "type": "bookCargoCommandRequest"
        },
        "constraintViolations": [
          { "type": "notBlank", "scope": "property", "path": "originLocation", "message": "must not be blank" },
          { "type": "notBlank", "scope": "property", "path": "destinationLocation", "message": "must not be blank" }
        ]
      }
    }
  },
  "payload": {}
}
```

- `root`- this section describes the basic context for broken validations. At the moment, it contains only a single element - `type`.
  - `type` - Root `type` represents an input object that was validated. It is rendered as an uncapitalized simple class name of an object carrying invalid input data. It also represents a prefix that
    needs to be added to the value of the `path` property of individual constraint violation to get a full path of broken constraint.


- `constraintViolations`:
  - `type` - constraint violation type denotes the kind of broken constraint. It is rendered as the uncapitalized simple class name of the corresponding constraint annotation, for example, `notNull`,
    `notBlank`, etc.
  - `scope` - represents a scope of violated constraint describing whether the constraint is related to the `property` or the `object`. In the case of "cross-field" constraints, it will be `object`.
    While for "field-only" constraints, it will be `property`.
  - `path` - represents the path of a failing property or object in the context of a root type (although root type is not included here).
  - `message` - a localized message that describes a concrete individual constraint violation. It is intended to be presented to the end-user.

It might be worth noting that during localization of `violation.message`, the `root.type` is also taken into account, and it participates in the creation of resolvable message codes.

### Format of `infrastructure_web` violation
Violation category `infrastructure_*` comprises failures that are handled by some infrastructural code. For example, they are dealt with by the currently used framework. Violations in this category
can have the severity of `warning` or `error`.

```
{
  "metaData": {
    "general": {
      "timestamp": "2021-04-12T09:28:55.755546Z",
      "severity": "warning",
      "locale": "en"
    },
    "http": {
      "status": "405",
      "message": "Method Not Allowed"
    },
    "violation": {
      "code": "405",
      "message": "Request is not valid.",
      "type": "infrastructure_web"
    }
  },
  "payload": {}
}
```
There is nothing new regarding the format structure as we have only `type`, `code`, and `message` elements.

## Notes about client-side handling
Besides having a predictable structure for rendering responses, one of the main reasons for having a prescribed response format is consistency at the client-side handling.

As we explained previously, responses with the `error` severity usually represent a bug in the server-side code or a problem with unreliable infrastructure. If feasible, related issues should be
tackled and fixed. When the client receives a response with the `error` severity, it should display a localized message (`metaData.violation.message`) together with server-side log UUID
(`metaData.violation.logUuid`). The end-user can use that UUID for reporting issues.

Responses with the `warning` severity commonly communicate a problem with the client's request. In those cases, the end-user should typically create another request that fixes the previous one's
issues. To help with this, client-side code should adequately display response messages.

In case of `domain` violations, the localized message (`metaData.violation.message`) should give a clue to the end-user what was wrong with the previous request. Hopefully, this should be enough
guidance for a user to make the correct action on the next attempt.

For `validation` failures, the client-side part of the application should create an appropriate UI that maps each constraint issue with the correct input field. That might require involved logic that
deals with the mapping of constraint failure for correct input form fields.

For infrastructural violations, handling depends on severity type. For warnings, client-side logic should display a localized message in the same way as for `domain` violations. On the other hand,
the errors should be handled as already described. The localized message should be displayed, together with the violation's UUID.

## Conclusion
Giving appropriate attention to the definition of your responses' format can bring long-term benefits to your projects. It is okay if you conclude there is no need for it in your current project,
but at least some thought process stays behind that decision.

In more elaborate systems, we believe there is a value in prescribing the response format as it eliminates ad-hoc solutions stacked one over another through time. The available response format brings
consistency in custom server-side response rendering and client-side response handling.

In `klokwrk-project`, we tried to define such a format based on many previous experiences. We do not consider it written in stone, and it might change in the future if the need arises.

## References
- [ADR-0012 - Response Format](../../adr/content/0012-response-format.md)
- [ADR-0013 - Validation taxonomy](../../adr/content/0013-validation-taxonomy.md)
