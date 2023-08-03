# cargotracking-lib-axon-cqrs

Module `cargotracking-lib-axon-cqrs` contains helper classes for easier usage of Axon API.

Those helper classes include:
- adapters for Axon command and query gateways, where adapters simplify sending of metadata through Axon gateways and handling of remote Axon exceptions on the client-side
- traits for command and query handler implementations, where traits simplify Axon's exception handling by provisioning a payload for Axon's remote exceptions
