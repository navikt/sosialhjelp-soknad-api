package no.nav.sosialhjelp.soknad.app.mdc

// Copy from modig-log-common

import org.slf4j.MDC

/**
 * Utility-klasse for kommunikasjon med MDC.
 */
object MdcOperations {
    const val MDC_PATH = "path"
    const val MDC_HTTP_METHOD = "httpMethod"
    const val MDC_SOKNAD_ID = "soknadId"

    fun putToMDC(
        key: String?,
        value: String?,
    ) {
        MDC.put(key, value)
    }

    fun clearMDC() {
        MDC.remove(MDC_SOKNAD_ID)
    }
}
