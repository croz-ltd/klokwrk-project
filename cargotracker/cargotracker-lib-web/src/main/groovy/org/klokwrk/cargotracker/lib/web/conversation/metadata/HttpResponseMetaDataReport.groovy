package org.klokwrk.cargotracker.lib.web.conversation.metadata

import groovy.transform.CompileStatic
import net.croz.cargotracker.infrastructure.project.boundary.api.conversation.metadata.ResponseMetaDataReport

@CompileStatic
class HttpResponseMetaDataReport extends ResponseMetaDataReport {
  HttpResponseMetaDataReportPart http
}
