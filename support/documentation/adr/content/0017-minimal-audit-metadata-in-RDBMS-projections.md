# ADR-0017 - Minimal audit metadata in RDBMS projections
* **Status: accepted**
* Dates: proposed - 2022-04-07
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
In traditional applications that rely on the RDBMS databases, it is quite common to have a set of audit columns in tables of main entities. We can typically find columns like `created_at`,
`created_by`, `last_updated_at` and `last_updated_by`. We can also find the `version` column in systems relying on optimistic locking. Those columns can be helpful as a quick way for collecting
basic information about table activity which might be handy for database administrators and application developers.

All values are populated or updated at the database transaction time, usually as a direct consequence of user interaction or communication with external systems. We can think about the time of a
database transaction as the moment at which our system becomes aware of some external change or event. In that sense, columns `created_at` and `last_updated_at` represent the moment when we have
recorded some external fact. From that perspective, we can see those audit columns contain potentially valuable domain information - the moment of recording a reality.

In event-sourced systems, the moment of recording an event or a fact is also valuable information. However, it does not correspond to the moment of inserting or updating a record in the RDBMS
projection table but rather to the moment we created the related event in our system. For this reason, `created_at` and `last_updated_at` columns should not contain projection database transaction
time. They have to be updated based on event metadata information. The column names should also reflect this, so using names like `first_event_recorded_at` and `last_event_recorded_at` is more
appropriate.

If we still need, for some reason, the time of database transaction in our projection tables, you can still add corresponding columns if necessary. However, always be aware of how and when those
columns are updated. For example, if you start the replay of events for creating a fresh projection, the database transaction moments will be completely different than previously.

You may wonder if there are other interesting moments that we should persist. This is highly dependent on the concrete domain. Still, typically we can find interesting the moment of some occurrence
in the real world, the moment when people or other systems become aware of that occurrence, and similar. However, we cannot reduce those under common metadata information, and we have to deal with
them in a domain-specific way. You can find more details about such multi-temporal events in references.

In systems built on top of Axon framework, domain events (events originating from aggregates) also contain another valuable information - the sequence number of the event in the context of its
aggregate. That sequence number can be handy in various contexts like poling for the latest projection data, updating subscribing queries when projection and query applications are physically
separated, or implementing optimistic locking. Therefore, we should also record the sequence number in the column with a name like `last_event_sequence_number`.

You have probably noticed we skipped the `created_by` and `last_updated_by` columns. There are several reasons for that. First, the command-side application and projection application can be
physically separated, meaning they do not share the same thread context, which is usually the basis for extracting security-principal information. We can supply the principal as part of the event
payload, but the Axon framework does not automatically provide this, which is not necessarily a bad thing.

Second, the availability of security-principal information may be domain or use-case specific and might not be available on all occasions. For example, when dealing with sagas or issuing commands
from event handlers. For those reasons, we are leaving out those columns in the context of this ADR.

## Architectural Context
* RDBMS projections (`cargotracking-booking-app-queryside-projection-rdbms`, `cargotracking-booking-lib-queryside-model-rdbms-jpa`)

## Decision
**We will store the minimal set of event audit metadata in the following  columns of projection tables:**
- `first_event_recorded_at` - the moment of recording the first projected event related to the aggregate corresponding to the record of projection table
- `last_event_recorded_at` - the moment of recording the last projected event related to the aggregate corresponding to the record of projection table
- `last_event_sequence_number` - the sequence number of the last projected event relative to the aggregate corresponding to the record of projection table

## Consequences
### Positive
- The minimal set of event metadata is available in projection table
- Stored event metadata can be a basis for implementing various domain and technical concerns

### Neutral
- Slight performance degradation of event projection handling
- Increase of maintenance cost

### Negative
- Increase of stored data for every projected record

## Considered Options
- Not having a minimal set of audit metadata in projections

## References
- https://verraes.net/2022/03/multi-temporal-events/
