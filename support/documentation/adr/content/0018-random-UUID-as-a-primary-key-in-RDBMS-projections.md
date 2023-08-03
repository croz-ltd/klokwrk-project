# ADR-0018 - Random UUID as a primary key in RDBMS projections
* **Status: accepted**
* Dates: proposed - 2022-04-08
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
Random UUIDs have many attractive characteristics that make them suitable to be used as publicly exposed identifiers of aggregates. For example, they are universally unique, unpredictable, and easy
to generate.

In systems based on the Axon framework, random UUIDs are used as default aggregate identifiers if the user does not specify an identifier by themselves. As an aggregate identifier, random UUIDs are
commonly used as a primary key in RDBMS projection tables.

However, random UUIDs as primary keys can cause performance issues for big data sets. Unfortunately, this can become obvious too late, typically when the system is in production for months or even
years. There are ways to add a bit of sequentiality to UUIDs to resolve those issues, which leads us towards COMB UUIDs and, more specifically, the short prefix COMB UUIDs.

To get a full story about using random UUIDs and COMB variants as primary keys in databases, refer to the article
"[Random UUID as a Primary Key in the Relational Database](../../article/random-uuid-as-database-primary-key/random-uuid-as-database-primary-key.md)". The first part of the
article explains problems and solutions in detail, while the second part also explores UUID usage in some other areas. Therefore, the first part of the article can be considered a full context of
this ADR, and we will not repeat the discussion here. Please read the article to get a full explanation.

## Architectural Context
* RDBMS projections (`cargotracking-booking-app-queryside-projection-rdbms`, `cargotracking-booking-lib-queryside-model-rdbms-jpa`)

## Decision
**We will use short prefix COMB UUIDs as aggregate identifiers and as primary keys in related projection tables.**

## Consequences
### Positive
- Acceptable performance when UUID is used as a database table primary key

### Neutral
- UUID is generated in a non-standard way

### Negative
- Can require a development of client-side support

## Considered Options
- Using plain random UUIDs for aggregate identifiers and for primary keys

## References
- [Random UUID as a Primary Key in the Relational Database](../../article/random-uuid-as-database-primary-key/random-uuid-as-database-primary-key.md)
