# cargotracker-booking-out-customer

This module contains a standalone outbound adapter towards a customer management bounded context.

The module provides resource bundle `cargotrackerBookingOutCustomerMessages` containing messages for domain exceptions thrown from the implementation.

The adapter is standalone because its functionality is required from different applications.

The module contains both outbound port interfaces and a default implementation. If multiple implementations are needed, it is possible to split the module further. For example, on a single module
containing port interfaces and multiple implementation modules.

It is also interesting to note the differences between structuring and naming packages in this standalone outbound adapter module and inline outbound adapters present in applications.
