# ADR-0016 - Handling UTC timestamps in the application and relational database
* **Status: accepted**
* Dates: proposed - 2022-04-06
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
Handling time with time zones is not the easiest task in Java. There are many reasons for that, but we can probably relate all of them to the history of date/time API evolution on the JVM. The new
Java date/time API introduced in Java 8 was a big step forward in explicitness and correctness. But unfortunately, the API itself is quite complex and requires careful study to use it correctly.
Moreover, adding the database to the picture makes things even more difficult since we have additional players like JDBC driver, ORM frameworks like Hibernate, and the database itself. And those new
players can have their own ideas and nuances with date/time handling.

When dealing with timestamps in the application and the database, using the UTC zone is standard best practice and a sensible approach. Ideally, neither the application nor the database will depend
anymore on the local time zones in which they execute. Then we can freely move executables between time zones (quite common in today's cloud era), and they will always function correctly.
Furthermore, UTC has a few additional advantages. It is the primary time standard and a base reference that regulates the world clocks and time. It is stable since it never changes because of
daylight saving time (DST), and it has a standard textual representation parsable at any platform without ambiguity.

### Modeling UTC timestamps at the application level
In Java 8 date/time API, there are a few classes that we can use to represent UTC timestamp:
* `java.time.Instant`<br/>
  `Instant` represents a single moment, a specific point in time at the UTC timeline. Therefore, it can not be misinterpreted or confused with any other moment in the history. In short, it is the
  simplest and probably the best choice to model UTC timestamps at the application level.
* `java.time.OffsetDateTime`<br/>
  `OffsetDateTime` can be perceived as an `Instant` generalization. `OffsetDateTime` also models a single moment in time but also includes a configurable offset relative to the UTC. Therefore,
  `OffsetDateTime` with the offset of `+00` is the logical equivalent of the `Instant`.
* `java.time.LocalDateTime`<br/>
  `LocalDateTime` is often misused for modeling UTC timestamps. The problem is that `LocalDateTime` does not represent a single moment in time. Rather it represents a wallclock time which is always
  highly coupled to the time zone in which that conceptual wallclock is running. Depending on the time zone, it stands for 24+ different moments, all simultaneously. Of course, it can be attached to
  the UTC time zone, but its strong relation to the local time zone will always pop up in one way or the other. There are some usages where this might be considered an advantage, but `LocalDateTime`
  should generally be avoided for modeling UTC timestamps.

There is also a `ZonedDateTime` class which brings in all the nuances of time zone handling from the real world. For example, `ZonedDateTime` handles daylight saving time (DST), including tracking
the latest political decisions about it. Conceptually, however, `ZonedDateTime` is just an `Instant` with the assigned `ZoneId`. As such, it is not the best choice for storing UTC timestamps but
might be handy for other use cases like presenting the timestamp in the user's time zone.

For the reasons described above, we are choosing `java.time.Instant` for modeling the UTC timestamps.

Let's discuss now how Hibernate 5 handles the `Instant`.

### UTC timestamps with Hibernate 5 and `timestamp` columns
In the following discussion, we assume the PostgreSQL database usage and its corresponding JDBC driver. Internal workings might be different for other databases, but the general mechanism should be
quite similar.

For mapping an `Instant` to the database, Hibernate 5 uses `java.sql.Timestamp`. When writing to the database, Hibernate 5 converts an `Instant` into `java.sql.Timestamp` and passes that `Timestamp`
to the JDBC driver. By default, Hibernate 5 will do this via `PreparedStatement.setTimestamp(int, Timestamp)` method (the same mechanism is used for `OffsetDateTime` and `LocalDateTime`). Note that
there is also a more appropriate `PreparedStatement.setTimestamp(int, Timestamp, Calendar)` method, but Hibernate will not use it unless we explicitly configure the time zone for Hibernate to use
when communicating with the database.

Next, the JDBC driver converts `java.sql.Timestamp` into the string representation. The last part of that string is time zone offset. Since we are using the `setTimestamp()` method with two
parameters, an explicit time zone is not provided, and the JDBC driver uses the local application's time zone to calculate the offset.

Although it may look like the local time zone will cause issues, we are still on the good path. The problem really happens when the targeted database column can not accept time zone offset
information (i.e., the `timestamp` column). If this is the case, the offset part of the string is simply discarded. That loss of the time zone offset is the leading cause of all problems because we
have just written the local application timestamp into the database.

When reading from the database, Hibernate 5 uses `ResultSet.getTimeStamp(String)` method. Again, there is also a `ResultSet.getTimeStamp(String, Calendar)` method, but it is not used unless we
explicitly configure Hibernate to do so. Therefore, the JDBC driver will interpret fetched string from the database as a `Timestamp` with the local application's time zone. Finally, Hibernate
converts the passed `Timestamp` into the `Instant` via epoch seconds and hands over the `Instant` to the application.

Ironically, everything seems to work correctly from the application perspective. Ok, there is a wrong timestamp written in the database, but the application works. Right? Well, it will work until you
move it to another time zone. If this happens, previously recorded timestamps will be invalid from the application perspective, and the new ones will be stored as shifted relative to the old ones.
In other words, your application and your data are invalid now.

### `timestamp` vs `timestamptz`
The standard SQL data type of `timestamp` is commonly misused for columns intended to store UTC timestamps. Unfortunately, the `timestamp` SQL data type cannot keep time zone offset information which
is evident if you consider its full name - `timestamp **without** time zone`. If the database supports it, we should always use the SQL type of `timestamp **with** time zone` to preserve the time
zone offset.

In PostgreSQL `timestamp` stands for `timestamp without time zone` data type while `timestamptz` describes `timestamp with time zone`. Going further, I will use PostgreSQL data type names for brevity.

We can avoid the problem described in the previous section simply by using `timestamptz` columns instead of `timestamp` columns. Even if we move the application and/or the database into other time
zones, they will continue to work correctly. But few drawbacks still remain.

First, we don't have a setup that works for both `timestamptz` and `timestamp` columns. We should avoid this if possible.

Second, when we are using some kind of database viewer tool, the `timestamptz` columns will display data in the local time zone of the database. The data is correct as it includes the offset, but we
always have to convert to UTC in our heads.

Finally, if we rely on direct query execution (without Hibernate or JDBC driver mediation), the database can display results in its own local time zone. For example, PostgreSQL internally stores
`timestamptz` in UTC format. However, for all display and direct query purposes, it will always convert the UTC timestamp into the local time zone of the database before displaying the results.

### Fixing inconsistencies
We can fix the issues with the `timestamp` columns by instructing Hibernate to use the UTC time zone for handling timestamps. To do this, we can leverage the `hibernate.jdbc.time_zone` configuration
property. For example, in the Spring Boot application, we can put the following line in the `application.properties` file:

    spring.jpa.properties.hibernate.jdbc.time_zone=UTC

With this configuration set, Hibernate will use `setTimestamp()` and `getTimestamp()` methods with the `Calendar` parameter where that `Calendar` instance is created for the configured UTC time zone.

To avoid issues with direct queries and data display in database management tools, we should configure UTC as the database's default time zone. The exact procedure is highly dependent on the concrete
database, but for PostgreSQL, you can examine `support/docker/postgres/postgresql.conf` and `support/docker/docker-compose-infrastructure.yml` files in the `klokwrk-project`.

There is one more thing to be aware of. When using database management tools for exploring the data, not all tools will correctly display the content of `timestamptz` columns. In my experience,
pgAdmin and IDEA database tools work correctly. For DBeaver, you should configure UTC as a local time zone. And in the case of DBVisualizer, there is currently no support for distinguishing between
`timestamp` and `timestamptz` columns, and unfortunately, the same display format is used for both data types.

## Architectural Context
* System (`klokwrk-project`)

## Decision
**We will use the following recommendations for handling UTC timestamps:**
- Use `java.time.Instant` as a type for modeling UTC timestamps
- Configure Hibernate with UTC time zone to be used when handling `java.sql.Timestamp` (via `hibernate.jdbc.time_zone` configuration property)
- Configure default time zone of the database to the UTC time zone
- In PostgreSQL, use `timestamptz` SQL data type for UTC timestamp columns

## Consequences
### Positive
- Consistent, correct and unambiguous handling of UTC timestamps.
- At all system levels it is obvious what the timestamp represents.
- It is much harder to misinterpret timestamp as anything else then UTC timestamp.

### Neutral
- Requires (minimal) changes in the infrastructure like configuring a default database time zone

### Negative
- With already existing data present, it might require data migration.

## Considered Options
- Ignoring the problem.

## References
- https://stackoverflow.com/questions/32437550/whats-the-difference-between-instant-and-localdatetime/32443004#32443004
- https://wiki.postgresql.org/wiki/Don't_Do_This#Date.2FTime_storage
- https://www.toolbox.com/tech/data-management/blogs/zone-of-misunderstanding-092811/
- https://medium.com/building-the-system/how-to-store-dates-and-times-in-postgresql-269bda8d6403
- https://kaiwern.com/posts/2021/07/20/what-you-need-to-know-about-postgresql-timezone/
- https://kb.objectrocket.com/postgresql/postgresql-set-time-zone-1064
- https://vladmihalcea.com/how-to-store-date-time-and-timestamps-in-utc-time-zone-with-jdbc-and-hibernate/
- https://vladmihalcea.com/date-timestamp-jpa-hibernate/
- https://vladmihalcea.com/whats-new-in-jpa-2-2-java-8-date-and-time-types/
- https://github.com/hibernate/hibernate-orm/blob/main/hibernate-core/src/main/java/org/hibernate/type/descriptor/jdbc/InstantAsTimestampWithTimeZoneJdbcType.java
- https://github.com/hibernate/hibernate-orm/blob/main/hibernate-core/src/main/java/org/hibernate/type/descriptor/jdbc/TimestampWithTimeZoneJdbcType.java
- https://www.timeanddate.com/worldclock/converter.html
