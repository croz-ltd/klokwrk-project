# PostgreSLQ container notes
- Default config is available at `/usr/share/postgresql/postgresql.conf.sample` in image. To retrieve it one may use the following command on already running `klokwrk-project-postgres` container:

      docker exec -it klokwrk-project-postgres cat /usr/share/postgresql/postgresql.conf.sample > my-postgres.conf

## Configuration
### Default timezone
PostgreSQL internally stores all date/time values in UTC, but it displays and interprets them according to the `timezone` configuration parameter. If not configured, `initdb` will install a setting
corresponding to its system environment (for example, from the `TZ` environment variable).

In general, we want to set a database timezone to `UTC` as we do not wish to interpret the time according to the system's local timezone. However, the application can do this according to the needs
if needed. For example, the application can decide whether it will use the end-user time zone detected from the request, database connection or the local system's time zone.

To set a default timezone to UTC, we set `timezone` configuration parameter to `UTC` in `postgresql.conf` file. In addition, we'll also set `log_timezone` to `UTC` to get logging time in UTC.

    log_timezone = 'UTC'
    timezone = 'UTC'

When connected to the database, the current time zone can be displayed with

    SHOW TIMEZONE;

To list all available time zones we can use

    SELECT * FROM pg_timezone_names;

For storing an instant (the moment in time), it is best to use `timestamptz` data type. Here are some useful references:
- Don't Do This - Date/Time storage - https://wiki.postgresql.org/wiki/Don't_Do_This#Date.2FTime_storage
- Zone of Misunderstanding - https://www.toolbox.com/tech/data-management/blogs/zone-of-misunderstanding-092811/
- How to store dates and times in PostgreSQL - https://medium.com/building-the-system/how-to-store-dates-and-times-in-postgresql-269bda8d6403
