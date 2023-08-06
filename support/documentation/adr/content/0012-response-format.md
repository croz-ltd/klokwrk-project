# ADR-0012 - Response Format
* **Status: accepted**
* Dates:
  - proposed - 2021-04-14
  - updated - 2021-08-10
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
Every server-side application sends some kind of responses to its clients. Unfortunately, the exact structure of these responses is rarely defined.

The most probable cause is that each **successful** response has a different content and structure as it communicates various domain data depending on the requested endpoint. Consequently, it is
impossible to prescribe a predefined structure for such responses carrying only "concrete" data in its payload.

However, there is often a need to communicate some kind of metadata with every response in more elaborate systems. While metadata can be useful for successful responses, they are almost unavoidable
for responses communicating failures. No matter if the failure cause originates from the client or the server.

The standard way for sharing metadata is through means of a concrete protocol. For example, HTTP uses statuses and headers. Messaging systems also employ headers but of different format, etc.
However, if there is a need for more detailed metadata, headers are often combined with payloads.

Such a situation brings fragmentation and inconsistency in metadata transfers, especially when multiple protocols and channels have to be supported. While standard protocol features must be obeyed,
from the application perspective, it might be helpful having all necessary data and metadata in one place.

To enable generalized creation on the server-side and generalized processing on the client-side, metadata should follow some kind of prescribed shape and format.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We will render responses following the prescribed format.**

Format structure is independent of concrete protocol or channel and should be used for all generated responses.

Concrete details of the prescribed format are given in the "[The format of rendered responses](../../article/response-format/responseFormat.md)" article. Although the article presents prescribed
structures in JSON, concrete format implementation is not essential. It can be anything else that is more suitable for some chosen protocol, as long as defined structures are followed.

Implementations of response renderers are specific for each supported channel. At the moment, we have an implementation for the Spring MVC channel that can be found in `cargotracking-lib-web` module
in `org.klokwrk.cargotracking.lib.web.spring.mvc` package.

## Consequences
### Positive
- Improved consistency for response rendering on the server-side.
- Improved consistency for response handling on the client-side.
- Enablement for creating generalized implementations for both rendering and handling of response metadata.

### Negative
- The prescribed format is not defined by any standard.
- We are not using any standard metadata formats.
- We might have missed some vital metadata.
- It is quite possible that the format may be changed and evolved as we apply it in practice.

### Neutral
- The custom format might require some time to accustom and accept.

## Considered Options
- No custom response format.
- Standard formats for presenting failures like "RFC 7807" or "JSON:API Error Objects".

## References
- [The format of rendered responses](../../article/response-format/responseFormat.md)
- [RFC 7807](https://tools.ietf.org/html/rfc7807)
- [JSON:API Error Objects](https://jsonapi.org/format/#errors)
