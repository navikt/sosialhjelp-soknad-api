package no.nav.sosialhjelp.soknad.v2.okonomi

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Midlertidig håndtering av okonomiske elementer.
 * Håndterer elementene mer eksplisitt enn Spring Data JDBC gjør når de er en del av et aggregat.
 */

@Component
class OkonomiElementDbHandler(
    private val okonomiRepository: OkonomiRepository,
) {
    fun updateBekreftelseInDb(
        soknadId: UUID,
        bekreftelse: Bekreftelse,
    ) {
        okonomiRepository.deleteBekreftelse(soknadId, bekreftelse.type)
        okonomiRepository.insertBekreftelse(soknadId, bekreftelse.type, bekreftelse.tidspunkt, bekreftelse.verdi)

        okonomiRepository.findByIdOrNull(soknadId)
            ?.also { okonomi ->
                okonomi.bekreftelser.find { it.type == bekreftelse.type }
                    ?.also { db ->
                        if (db.verdi != bekreftelse.verdi) {
                            error("Verdi er ikke lik i bekreftelse og db (${bekreftelse.verdi})/(${db.verdi})")
                        }
                        if (db.tidspunkt.notEqualsSeconds(bekreftelse.tidspunkt)) {
                            error("Tidspunkt er ikke lik i bekreftelse og db (${bekreftelse.tidspunkt})/(${db.tidspunkt})")
                        }
                    }
                    ?: error("Bekreftelse(${bekreftelse.type} finnes ikke.")
            }
    }

    fun deleteBekreftelseInDb(
        soknadId: UUID,
        type: BekreftelseType,
    ) {
        okonomiRepository.deleteBekreftelse(soknadId, type)

        okonomiRepository.findByIdOrNull(soknadId)?.also { okonomi ->
            okonomi.bekreftelser.find { it.type == type }?.also { error("Bekreftelse($type) finnes fortsatt.") }
        }
    }
}

private fun LocalDateTime.notEqualsSeconds(other: LocalDateTime) =
    this.truncatedTo(ChronoUnit.SECONDS) != other.truncatedTo(ChronoUnit.SECONDS)
