package org.klokwrk.cargotracker.lib.web.conversation.metadata

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.conversation.metadata.ResponseMetaDataReport

@CompileStatic
class HttpResponseMetaDataReport extends ResponseMetaDataReport {
  HttpResponseMetaDataReportPart http
}
