# klokwrk-lib-hibernate

Module `klokwrk-lib-hibernate` deals with some peculiarities of the internal workings of Hibernate ORM.

Currently, there is a custom dialect for PostgreSQL, which customizes the original dialect to align it with Axon's EventStore and Axon's TokenStore usage needs.
