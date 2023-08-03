# cargotracking-domain-model-service

Module `cargotracking-domain-model-service` contains **simple domain services** for the `cargotracking` bounded context.

Simple domain services, as we call them here, are intended to be called by aggregates and entities or by other simple domain services. Simple domain services are typically injected as singletons into
aggregates or entities.

Simple domain services should depend only on other simple domain services or on value objects provided as method parameters or supplied as properties at initialization time.
Simple domain service should not depend on aggregate or entity instances.

Suppose you still need domain services dependent on aggregates or entities. In that case, you can implement them close to aggregates or entities (i.e., in the `aggregate` (sub)package of the
`cargotracking-domain-model-aggregate module`). The suggested name for such services would be **"aggregate domain services"**. They are at a higher level of abstraction than simple domain services.
Regarding dependencies, aggregate domain services can call or use whatever aggregate or entities can call or use, including simple domain services.

Still, when you think you have to use aggregate domain service, take a more detailed look, as there is a chance that aggregate domain service can be rewritten as a simple domain service.

On the other hand, the concept of domain service can also occur at the level of value objects when you need to call a service from the value object. Therefore, such domain services should go in the
`cargotracking-domain-model-value` module's `value` (sub)package. The suggested name for such services would be **"value domain services"**. They are at a lower level of abstraction than simple
domain services. Regarding dependencies, value domain services can call or use whatever other value objects can call or use.
