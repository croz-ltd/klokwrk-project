# cargotracker-booking-domain-model-value

Module `cargotracker-booking-domain-model-value` contains subdomain's value objects.

In CQRS applications, aggregates and entities belong only to the command side. They cannot be used either from projections or query side. But domain value objects can be shared among all of these.
In one part, value objects are used for expressing the internal state of aggregates. They are also used as building blocks for modeling events. Query side can also use domain value objects while
describing query requirements.

That broad reusability potential across subdomain is the main reason for extracting domain value objects into a standalone module.
