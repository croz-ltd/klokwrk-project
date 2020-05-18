package org.klokwrk.cargotracker.lib.web.conversation.metadata

import groovy.transform.CompileStatic
import org.klokwrk.cargotracker.lib.boundary.api.metadata.ResponseMetaDataReport

@CompileStatic
class HttpResponseMetaDataReport extends ResponseMetaDataReport {
  HttpResponseMetaDataReportPart http
}
