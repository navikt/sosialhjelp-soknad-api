package no.nav.sosialhjelp.soknad.app.mdc

// Copy from modig-log-common

import org.slf4j.MDC
import java.security.SecureRandom

/**
 * Utility-klasse for kommunikasjon med MDC.
 */
object MdcOperations {
    const val MDC_CALL_ID = "callId"
    const val MDC_BEHANDLINGS_ID = "behandlingsId"
    const val MDC_CONSUMER_ID = "consumerId"
    const val MDC_PATH = "path"
    const val MDC_SOKNAD_ID = "soknadId"

    private val random = SecureRandom()

    fun generateCallId(): String {
        return "CallId_${systemTime}_$randomNumber"
    }

    fun getFromMDC(key: String?): String? {
        return MDC.get(key)
    }

    fun putToMDC(
        key: String?,
        value: String?,
    ) {
        MDC.put(key, value)
    }

    fun remove(key: String?) {
        MDC.remove(key)
    }

    fun clearMDC() {
        MDC.remove(MDC_CALL_ID)
        MDC.remove(MDC_BEHANDLINGS_ID)
        MDC.remove(MDC_CONSUMER_ID)
    }

    private val randomNumber: Int
        get() = random.nextInt(Int.MAX_VALUE)
    private val systemTime: Long
        get() = System.currentTimeMillis()
}
