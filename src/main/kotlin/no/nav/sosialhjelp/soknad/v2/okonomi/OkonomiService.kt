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
    fun getFormuer(soknadId: UUID): Set<Formue>? = findOkonomi(soknadId)?.formuer

    fun getInntekter(soknadId: UUID): Set<Inntekt>? = findOkonomi(soknadId)?.inntekter

    fun getUtgifter(soknadId: UUID): Set<Utgift>? = findOkonomi(soknadId)?.utgifter

    fun getBekreftelser(soknadId: UUID): Set<Bekreftelse>? = findOkonomi(soknadId)?.bekreftelser

    fun updateBekreftelse(
        soknadId: UUID,
        type: BekreftelseType,
        verdi: Boolean,
    ) {
        val okonomi = findOrCreateOkonomi(soknadId)

        okonomi.bekreftelser
            .filter { it.type != type }
            .plus(Bekreftelse(type, verdi))
            .let { bekreftelser -> okonomi.copy(bekreftelser = bekreftelser.toSet()) }
            .also { okonomiRepository.save(it) }
    }

    fun addElementToOkonomi(
        soknadId: UUID,
        type: OkonomiType,
        beskrivelse: String? = null,
    ): Set<*> {
        val updatedSet =
            findOrCreateOkonomi(soknadId).run {
                when (type) {
                    is FormueType -> addAndSaveElement(formuer, Formue(type, beskrivelse)) { copy(formuer = it) }
                    is UtgiftType -> addAndSaveElement(utgifter, Utgift(type, beskrivelse)) { copy(utgifter = it) }
                    is InntektType -> addAndSaveElement(inntekter, Inntekt(type, beskrivelse)) { copy(inntekter = it) }
                    else -> error("Ukjent OkonomiType for oppretting")
                }
            }
        if (type.dokumentasjonForventet) dokumentasjonService.opprettForventetVedlegg(soknadId, type)

        return updatedSet
    }

    fun removeElementFromOkonomi(
        soknadId: UUID,
        type: OkonomiType,
    ): Set<*> {
        val updatedSet =
            findOrCreateOkonomi(soknadId).run {
                when (type) {
                    is FormueType -> removeElementByTypeAndSave(formuer, type) { copy(formuer = it) }
                    is UtgiftType -> removeElementByTypeAndSave(utgifter, type) { copy(utgifter = it) }
                    is InntektType -> removeElementByTypeAndSave(inntekter, type) { copy(inntekter = it) }
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
    private fun <E : OkonomiElement> addAndSaveElement(
        sourceSet: Set<E>,
        element: E,
        updateSetInOkonomiFunc: (Set<E>) -> Okonomi,
    ): Set<E> {
        return if (typeExistsAndEqualBeskrivelse(sourceSet, element.type, element.beskrivelse)) {
            sourceSet
        } else {
            sourceSet
                .filter { it.type != element.type }.toSet()
                .plus(element)
                .also { updatedSet -> okonomiRepository.save(updateSetInOkonomiFunc.invoke(updatedSet)) }
        }
    }

    /**
     * Felles-funksjon for å fjerne et element fra et set (Inntekt, Utgift, Formue)
     * Sjekker om elementet finnes, lager en kopi av settet uten elementet, og oppdaterer riktig variabel i Okonomi
     */
    private fun <E : OkonomiElement> removeElementByTypeAndSave(
        sourceSet: Set<E>,
        type: OkonomiType,
        updateOkonomiFunction: (Set<E>) -> Okonomi,
    ): Set<E> {
        return sourceSet.find { it.type == type }
            ?.let { existingEntity -> sourceSet.minus(existingEntity) }
            ?.also { updatedSet -> okonomiRepository.save(updateOkonomiFunction.invoke(updatedSet)) }
            ?: sourceSet
    }

    private fun <E : OkonomiElement> typeExistsAndEqualBeskrivelse(
        set: Set<E>,
        type: OkonomiType,
        beskrivelse: String?,
    ): Boolean {
        set.find { it.type == type }?.let {
            if (it.beskrivelse == beskrivelse) return true
        }
        return false
    }

    private fun findOrCreateOkonomi(soknadId: UUID) = findOkonomi(soknadId) ?: okonomiRepository.save(Okonomi(soknadId))

    private fun findOkonomi(soknadId: UUID): Okonomi? = okonomiRepository.findByIdOrNull(soknadId)
}
