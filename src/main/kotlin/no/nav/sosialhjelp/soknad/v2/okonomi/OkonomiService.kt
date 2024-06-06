package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonForventningService
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Ansvar for oppdatering av Okonomi-aggregatet, samt knytningen mellom Okonomi og Vedlegg(-forventninger)
 */
@Service
@Transactional
class OkonomiService(
    private val okonomiRepository: OkonomiRepository,
    private val dokumentasjonService: DokumentasjonForventningService,
) {
    fun getFormuer(soknadId: UUID): Set<Formue>? = okonomiRepository.findByIdOrNull(soknadId)?.formuer

    fun getBekreftelser(soknadId: UUID) = okonomiRepository.findByIdOrNull(soknadId)?.bekreftelser ?: emptyList()

    fun updateBekreftelse(
        soknadId: UUID,
        type: BekreftelseType,
        verdi: Boolean,
    ) {
        val okonomi = findOrCreate(soknadId)

        okonomi.bekreftelser
            .filter { it.type != type }
            .plus(Bekreftelse(type, verdi))
            .let { bekreftelser -> okonomi.copy(bekreftelser = bekreftelser.toSet()) }
            .also { okonomiRepository.save(it) }
    }

    fun addType(
        soknadId: UUID,
        type: OkonomiType,
        beskrivelse: String? = null,
    ): Set<*> {
        val updatedSet =
            findOrCreate(soknadId).run {
                when (type) {
                    is FormueType -> addAndSave(formuer, Formue(type, beskrivelse)) { copy(formuer = it) }
                    is UtgiftType -> addAndSave(utgifter, Utgift(type, beskrivelse)) { copy(utgifter = it) }
                    is InntektType -> addAndSave(inntekter, Inntekt(type, beskrivelse)) { copy(inntekter = it) }
                    else -> error("Ukjent OkonomiType for oppretting")
                }
            }
        if (type.dokumentasjonForventet) dokumentasjonService.opprettForventetVedlegg(soknadId, type)

        return updatedSet
    }

    fun removeType(
        soknadId: UUID,
        type: OkonomiType,
    ): Set<*> {
        val updatedSet =
            findOrCreate(soknadId).run {
                when (type) {
                    is FormueType -> removeAndSave(formuer, type) { copy(formuer = it) }
                    is UtgiftType -> removeAndSave(utgifter, type) { copy(utgifter = it) }
                    is InntektType -> removeAndSave(inntekter, type) { copy(inntekter = it) }
                    else -> error("Ukjent OkonomiType for removal")
                }
            }
        if (type.dokumentasjonForventet) dokumentasjonService.fjernForventetVedlegg(soknadId, type)

        return updatedSet
    }

    /**
     * Felles-funksjon for å fjerne et element fra et set av Inntekt, Utgift eller Formue (OkonomiPoster)
     * Sjekker om elementet finnes og beskrivelse er lik, legger det til hvis det ikke gjør det
     */
    private fun <E : OkonomiElement> addAndSave(
        sourceSet: Set<E>,
        entity: E,
        updateOkonomiFunction: (Set<E>) -> Okonomi,
    ): Set<E> {
        return if (existsAndEqualBeskrivelse(sourceSet, entity.type, entity.beskrivelse)) {
            sourceSet
        } else {
            sourceSet
                .filter { it.type != entity.type }.toSet()
                .plus(entity)
                .also { updatedSet -> okonomiRepository.save(updateOkonomiFunction.invoke(updatedSet)) }
        }
    }

    /**
     * Felles-funksjon for å fjerne et element fra et set (Inntekt, Utgift, Formue)
     * Sjekker om elementet finnes, lager en kopi av settet uten elementet, og oppdaterer riktig variabel i Okonomi
     */
    private fun <E : OkonomiElement> removeAndSave(
        sourceSet: Set<E>,
        type: OkonomiType,
        updateOkonomiFunction: (Set<E>) -> Okonomi,
    ): Set<E> {
        return sourceSet.find { it.type == type }
            ?.let { existingEntity -> sourceSet.minus(existingEntity) }
            ?.also { updatedSet -> okonomiRepository.save(updateOkonomiFunction.invoke(updatedSet)) }
            ?: sourceSet
    }

    private fun <E : OkonomiElement> existsAndEqualBeskrivelse(
        set: Set<E>,
        type: OkonomiType,
        beskrivelse: String?,
    ): Boolean {
        set.find { it.type == type }?.let {
            if (it.beskrivelse == beskrivelse) return true
        }
        return false
    }

    private fun findOrCreate(soknadId: UUID) =
        okonomiRepository.findByIdOrNull(soknadId)
            ?: okonomiRepository.save(Okonomi(soknadId))
}
