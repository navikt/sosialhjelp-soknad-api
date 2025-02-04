package no.nav.sosialhjelp.soknad.v2.okonomi

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
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
            } ?: error("Okonomi finnes ikke.")
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

    fun updateFormueInDb(
        soknadId: UUID,
        formue: Formue,
    ) {
        okonomiRepository.deleteFormue(soknadId, formue.type)

        val detaljerJson = mapper.writeValueAsString(formue.formueDetaljer)
        okonomiRepository.updateFormue(soknadId, formue.type, formue.beskrivelse, detaljerJson)

        okonomiRepository.findByIdOrNull(soknadId)
            ?.also { okonomi ->
                okonomi.formuer.find { it.type == formue.type }
                    ?.also { db ->
                        if (db.beskrivelse != formue.beskrivelse) {
                            error("Beskrivelse er ikke lik i formue og db")
                        }
                        if (db.formueDetaljer != formue.formueDetaljer) {
                            error("FormueDetaljer er ikke lik i formue og db")
                        }
                    }
                    ?: error("Formue(${formue.type} finnes ikke.")
            } ?: error("Okonomi finnes ikke.")
    }

    fun deleteFormueInDb(
        soknadId: UUID,
        type: FormueType,
    ) {
        okonomiRepository.deleteFormue(soknadId, type)

        okonomiRepository.findByIdOrNull(soknadId)?.also { okonomi ->
            okonomi.formuer.find { it.type == type }?.also { error("Formue($type) finnes fortsatt.") }
        }
    }

    fun updateUtgiftInDb(
        soknadId: UUID,
        utgift: Utgift,
    ) {
        deleteUtgiftInDb(soknadId, utgift.type)

        val detaljerJson = mapper.writeValueAsString(utgift.utgiftDetaljer)
        okonomiRepository.updateUtgift(soknadId, utgift.type, utgift.beskrivelse, detaljerJson)

        okonomiRepository.findByIdOrNull(soknadId)
            ?.also { okonomi ->
                okonomi.utgifter.find { it.type == utgift.type }
                    ?.also { db ->
                        if (db.beskrivelse != utgift.beskrivelse) {
                            error("Beskrivelse er ikke lik i utgift og db")
                        }
                        if (db.utgiftDetaljer != utgift.utgiftDetaljer) {
                            error("UtgiftDetaljer er ikke lik i utgift og db")
                        }
                    }
                    ?: error("Utgift(${utgift.type} finnes ikke.")
            } ?: error("Okonomi finnes ikke.")
    }

    fun deleteUtgiftInDb(
        soknadId: UUID,
        type: UtgiftType,
    ) {
        okonomiRepository.deleteUtgift(soknadId, type)

        okonomiRepository.findByIdOrNull(soknadId)?.also { okonomi ->
            okonomi.utgifter.find { it.type == type }?.also { error("Utgift($type) finnes fortsatt.") }
        }
    }

    fun updateInntektInDb(
        soknadId: UUID,
        inntekt: Inntekt,
    ) {
        deleteInntektInDb(soknadId, inntekt.type)

        val inntektJson = mapper.writeValueAsString(inntekt.inntektDetaljer)
        okonomiRepository.updateInntekt(soknadId, inntekt.type, inntekt.beskrivelse, inntektJson)

        okonomiRepository.findByIdOrNull(soknadId)
            ?.also { okonomi ->
                okonomi.inntekter.find { it.type == inntekt.type }
                    ?.also { db ->
                        if (db.beskrivelse != inntekt.beskrivelse) {
                            error("Beskrivelse er ikke lik i inntekt og db")
                        }
                        if (db.inntektDetaljer != inntekt.inntektDetaljer) {
                            error("InntektDetaljer er ikke lik i inntekt og db")
                        }
                    }
                    ?: error("Inntekt(${inntekt.type} finnes ikke.")
            } ?: error("Okonomi finnes ikke.")
    }

    fun deleteInntektInDb(
        soknadId: UUID,
        type: InntektType,
    ) {
        okonomiRepository.deleteInntekt(soknadId, type)

        okonomiRepository.findByIdOrNull(soknadId)?.also { okonomi ->
            okonomi.inntekter.find { it.type == type }?.also { error("Inntekt($type) finnes fortsatt.") }
        }
    }
}

private val mapper =
    jacksonObjectMapper().apply {
        registerModules(JavaTimeModule())
    }

private fun LocalDateTime.notEqualsSeconds(other: LocalDateTime) =
    this.truncatedTo(ChronoUnit.SECONDS) != other.truncatedTo(ChronoUnit.SECONDS)
