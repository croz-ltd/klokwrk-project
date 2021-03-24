# cargotracker-lib-boundary-api

Module `cargotracker-lib-boundary-api` formalizes general interfaces, exceptions, and data structures of domain boundary API that all inbound adapters (web, messaging, etc.) must understand and
follow to be able to speak with domain application services (a.k.a. domain facades).

Boundary API classes are part of the contract between the outside world and domain hidden behind domain application services/facades. They are allowed to be shared between them. Domain facades handle
all boundary requests by converting them into appropriate internal commands or queries. On the other side, deep domain artifacts like aggregates are allowed to throw boundary exceptions understood by
the outside world without any facade-level translation necessary.
