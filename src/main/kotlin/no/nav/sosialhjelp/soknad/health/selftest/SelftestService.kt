package no.nav.sosialhjelp.soknad.health.selftest

import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Function

@Component
class SelftestService(
    @Value("\${application.name}") private val applicationName: String,
    @Value("\${application.version}") private val applicationVersion: String,
    private val pingables: List<Pingable>
) {
    private var result: List<Pingable.Ping> = mutableListOf()

    @Volatile
    private var lastResultTime: Long = 0

    fun lagSelftest(): Selftest {
        doPing()
        return Selftest(
            application = applicationName,
            version = applicationVersion,
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
            aggregateResult = aggregertStatus,
            checks = result.map { lagSelftestEndpoint(it) }
        )
    }

    private fun doPing() {
        val requestTime = System.currentTimeMillis()
        // Beskytter pingables mot mange samtidige/tette requester.
        // Særlig viktig hvis det tar lang tid å utføre alle pingables
        synchronized(this) {
            if (requestTime > lastResultTime) {
                result = pingables.map { PING.apply(it) }
                lastResultTime = System.currentTimeMillis()
            }
        }
    }

    private val aggregertStatus: Int
        get() {
            val harKritiskFeil = result.any { it.harFeil() && it.metadata?.isKritisk ?: false }
            val harFeil = result.any { it.harFeil() }
            if (harKritiskFeil) {
                return STATUS_ERROR
            } else if (harFeil) {
                return STATUS_WARNING
            }
            return STATUS_OK
        }

    private fun lagSelftestEndpoint(ping: Pingable.Ping): SelftestEndpoint {
        return SelftestEndpoint(
            endpoint = ping.metadata?.endepunkt,
            description = ping.metadata?.beskrivelse,
            errorMessage = ping.feilmelding,
            critical = ping.metadata?.isKritisk ?: false,
            result = if (ping.harFeil()) STATUS_ERROR else STATUS_OK,
            responseTime = String.format("%dms", ping.responstid),
            stacktrace = ping.feil?.let { ExceptionUtils.getStackTrace(it) }
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(SelftestService::class.java)

        const val STATUS_OK = 0
        const val STATUS_ERROR = 1
        const val STATUS_WARNING = 2

        private val PING = Function { pingable: Pingable ->
            val startTime = System.currentTimeMillis()
            val ping = pingable.ping()
            ping.responstid = (System.currentTimeMillis() - startTime)
            if (!ping.erVellykket()) {
                log.warn("Feil ved SelfTest av ${ping.metadata?.endepunkt}", ping.feil)
            }
            ping
        }
    }
}
