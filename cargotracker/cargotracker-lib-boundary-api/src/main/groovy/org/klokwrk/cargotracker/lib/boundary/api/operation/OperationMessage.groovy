package org.klokwrk.cargotracker.lib.boundary.api.operation

/**
 * Defines the basic format of messages exchanged over domain facade boundary.
 * <p/>
 * Any inbound channel (i.e., web, CLI, messaging, etc.) must convert its accepted messages to this format to be able to send a message into a domain facade.
 * <p/>
 * Metadata is a map containing any non-functional information that must be conveyed from inbound channels into the domain. These might be security-related information, quality-of-service information,
 * metrics and tracing related information, etc. Names of commonly used metadata map keys are enumerated in <code>MetaDataConstant</code> class.
 * <p/>
 * The payload conveys information that might be considered as domain facade's API data structure. Usually, the payload corresponds (at least to some degree) to the data sent by the end-user.
 */
interface OperationMessage<P, M extends Map<String, ?>> {
  M getMetaData()
  P getPayload()
}
