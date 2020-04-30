package net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.violation

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import groovy.transform.MapConstructor
import groovy.transform.PropertyOptions
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import net.croz.cargotracker.infrastructure.project.boundary.api.severity.Severity
import net.croz.cargotracker.lang.groovy.constructor.support.PostMapConstructorCheckable
import net.croz.cargotracker.lang.groovy.transform.options.RelaxedPropertyHandler

/**
 * Defines an immutable data structure that describes the reason for the <code>DomainException</code> exception.
 * <p/>
 * It contains the violation's severity and data structure describing the code of the violation. Both members need to be specified at construction time.
 *
 * @see net.croz.cargotracker.infrastructure.project.boundary.api.exceptional.exception.DomainException
 * @see ViolationCode
 * @see Severity
 */
@Immutable
@PropertyOptions(propertyHandler = RelaxedPropertyHandler)
@TupleConstructor(visibilityId = "privateVisibility", pre = { throw new IllegalArgumentException("Calling a private constructor is not allowed") })
@VisibilityOptions(id = "privateVisibility", value = Visibility.PRIVATE)
@MapConstructor(post = { postMapConstructorCheckProtocol(args as Map) })
@CompileStatic
class ViolationInfo implements PostMapConstructorCheckable {
  static final ViolationInfo UNKNOWN = new ViolationInfo(severity: Severity.ERROR, violationCode: ViolationCode.UNKNOWN)
  static final ViolationInfo BAD_REQUEST = new ViolationInfo(severity: Severity.WARNING, violationCode: ViolationCode.BAD_REQUEST)
  static final ViolationInfo NOT_FOUND = new ViolationInfo(severity: Severity.WARNING, violationCode: ViolationCode.NOT_FOUND)

  Severity severity
  ViolationCode violationCode

  @Override
  void postMapConstructorCheck(Map<String, ?> constructorArguments) {
    assert severity
    assert violationCode
  }
}
