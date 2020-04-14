/**
 * Contains application services (a.k.a. facades) and all lower-level services that application services need.
 * <p/>
 * Main sub-package is <code>service</code>, and it hosts all application services (they might be organized into subpackages if appropriate).
 * <p/>
 * Sub-package <code>factory</code> is intended to host classes that encapsulate conversion logic between outside-world's DTOs and internal domain classes.
 * <p/>
 * Sub-package <code>repository</code> hosts repository interfaces required by application or factory services. For example factory service can use repositories to resolve IDs (sent through DTOs)
 * into domain value objects. While doing so, factories can use one or more repositories for resolving IDs via some kind of registry (i.e. registry data from classic RDBMS database).
 * </p>
 * Of course, above recommendations are not carved into stone, and should be used only as guidelines. For example, in simple cases, one can put all classes into <code>application</code> package
 * itself. Further, for the simplest cases, functionality of factories and repositories can be moved in application service.
 */
package net.croz.cargotracker.booking.commandside.application;
