package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * Ansvar for oppdatering av Okonomi-aggregatet, samt knytningen mellom Okonomi og Vedlegg(-forventninger)
 */
@Service
@Transactional
class OkonomiService(
    private val okonomiRepository: OkonomiRepository,
    private val dokumentasjonService: DokumentasjonService,
) {
    fun getFormuer(soknadId: UUID): Set<Formue> = findOkonomi(soknadId)?.formuer ?: emptySet()

    fun getInntekter(soknadId: UUID): Set<Inntekt> = findOkonomi(soknadId)?.inntekter ?: emptySet()

    fun getUtgifter(soknadId: UUID): Set<Utgift> = findOkonomi(soknadId)?.utgifter ?: emptySet()

    fun getBekreftelser(soknadId: UUID): Set<Bekreftelse> = findOkonomi(soknadId)?.bekreftelser ?: emptySet()

    fun getBostotteSaker(soknadId: UUID): List<BostotteSak> = findOkonomi(soknadId)?.bostotteSaker ?: emptyList()

    fun findDetaljerOrNull(
        soknadId: UUID,
        type: OpplysningType,
    ): List<OkonomiDetalj>? {
        return when (type) {
            is InntektType -> getInntekter(soknadId).find { it.type == type }?.inntektDetaljer?.detaljer
            is UtgiftType -> getUtgifter(soknadId).find { it.type == type }?.utgiftDetaljer?.detaljer
            is FormueType -> getFormuer(soknadId).find { it.type == type }?.formueDetaljer?.detaljer
            else -> null
        }
    }

    fun updateBekreftelse(
        soknadId: UUID,
        type: BekreftelseType,
        verdi: Boolean,
        tidspunkt: LocalDateTime = LocalDateTime.now(),
    ) {
        val okonomi = findOrCreateOkonomi(soknadId)

        okonomi.bekreftelser
            .filter { it.type != type }
            .plus(Bekreftelse(type, tidspunkt, verdi))
            .let { bekreftelser -> okonomi.copy(bekreftelser = bekreftelser.toSet()) }
            .also { okonomiRepository.save(it) }
    }

    fun deleteBekreftelse(
        soknadId: UUID,
        type: BekreftelseType,
    ) {
        okonomiRepository.findByIdOrNull(soknadId)
            ?.run { copy(bekreftelser = bekreftelser.filter { it.type != type }.toSet()) }
            ?.let { updatedOkonomi -> okonomiRepository.save(updatedOkonomi) }
    }

    fun addBostotteSak(
        soknadId: UUID,
        sak: BostotteSak,
    ) {
        findOrCreateOkonomi(soknadId)
            .run { copy(bostotteSaker = bostotteSaker.plus(sak)) }
            .also { okonomiRepository.save(it) }
    }

    fun removeBostotteSaker(soknadId: UUID) {
        okonomiRepository.findByIdOrNull(soknadId)
            ?.run { copy(bostotteSaker = emptyList()) }
            ?.also { okonomiRepository.save(it) }
    }

    fun addElementToOkonomi(
        soknadId: UUID,
        type: OpplysningType,
        beskrivelse: String? = null,
    ) {
        when (type) {
            is FormueType -> addElementToOkonomi(soknadId, Formue(type, beskrivelse))
            is UtgiftType -> addElementToOkonomi(soknadId, Utgift(type, beskrivelse))
            is InntektType -> addElementToOkonomi(soknadId, Inntekt(type, beskrivelse))
            else -> error("Ukjent OpplysningType for oppretting")
        }
    }

    fun addElementToOkonomi(
        soknadId: UUID,
        element: OkonomiElement,
    ) {
        findOrCreateOkonomi(soknadId)
            .run {
                when (element) {
                    is Formue -> copy(formuer = formuer.removeOldAddNew(element))
                    is Inntekt -> copy(inntekter = inntekter.removeOldAddNew(element))
                    is Utgift -> copy(utgifter = utgifter.removeOldAddNew(element))
                    else -> error("Ukjent OpplysningType for oppretting")
                }
            }
            .also { updatedOkonomi -> okonomiRepository.save(updatedOkonomi) }

        if (element.type.dokumentasjonForventet == true) dokumentasjonService.opprettDokumentasjon(soknadId, element.type)
    }

    private fun <E : OkonomiElement> Set<E>.removeOldAddNew(element: E): Set<E> =
        filter { it.type != element.type }.plus(element).toSet()

    fun updateElement(
        soknadId: UUID,
        element: OkonomiElement,
    ) {
        findOkonomi(soknadId)
            ?.let {
                when (element) {
                    is Inntekt -> updateInntekter(it, element)
                    is Utgift -> updateUtgifter(it, element)
                    is Formue -> updateFormuer(it, element)
                    else -> error("Ukjent okonomi-element")
                }
            }
            ?.let { updatedOkonomi -> okonomiRepository.save(updatedOkonomi) }
            ?: error("Finnes ikke Okonomi-objekt")
    }

    private fun updateInntekter(
        okonomi: Okonomi,
        inntekt: Inntekt,
    ): Okonomi {
        return okonomi.inntekter
            .run {
                if (none { it.type == inntekt.type }) {
                    throw OkonomiElementFinnesIkkeException("Inntekt finnes ikke: ${inntekt.type}")
                } else {
                    okonomi.copy(inntekter = map { if (it.type == inntekt.type) inntekt else it }.toSet())
                }
            }
    }

    private fun updateUtgifter(
        okonomi: Okonomi,
        utgift: Utgift,
    ): Okonomi {
        return okonomi.utgifter
            .run {
                if (none { it.type == utgift.type }) {
                    throw OkonomiElementFinnesIkkeException("Utgift finnes ikke: + ${utgift.type}")
                } else {
                    okonomi.copy(utgifter = map { if (it.type == utgift.type) utgift else it }.toSet())
                }
            }
    }

    private fun updateFormuer(
        okonomi: Okonomi,
        formue: Formue,
    ): Okonomi {
        return okonomi.formuer
            .run {
                if (none { it.type == formue.type }) {
                    throw OkonomiElementFinnesIkkeException("Formue finnes ikke: + ${formue.type}")
                } else {
                    okonomi.copy(formuer = map { if (it.type == formue.type) formue else it }.toSet())
                }
            }
    }

    //    fun addElementToOkonomi(
//        soknadId: UUID,
//        element: OkonomiElement,
//    ) {
//        findOrCreateOkonomi(soknadId).run {
//            when (element) {
//                is Formue -> addAndSaveElement(formuer, element) { copy(formuer = it) }
//                is Inntekt -> addAndSaveElement(inntekter, element) { copy(inntekter = it) }
//                is Utgift -> addAndSaveElement(utgifter, element) { copy(utgifter = it) }
//                else -> error("Ukjent OpplysningType for oppretting")
//            }
//        }
//        if (element.type.dokumentasjonForventet == true) dokumentasjonService.opprettDokumentasjon(soknadId, element.type)
//    }

//    fun removeElementFromOkonomi(
//        soknadId: UUID,
//        type: OpplysningType,
//    ) {
//        findOrCreateOkonomi(soknadId).run {
//            when (type) {
//                is FormueType -> removeElementByTypeAndSave(formuer, type) { copy(formuer = it) }
//                is UtgiftType -> removeElementByTypeAndSave(utgifter, type) { copy(utgifter = it) }
//                is InntektType -> removeElementByTypeAndSave(inntekter, type) { copy(inntekter = it) }
//                else -> error("Ukjent OpplysningType for removal")
//            }
//        }
//        if (type.dokumentasjonForventet == true) dokumentasjonService.fjernForventetDokumentasjon(soknadId, type)
//    }

    fun removeElementFromOkonomi(
        soknadId: UUID,
        type: OpplysningType,
    ) {
        findOkonomi(soknadId)
            ?.run {
                when (type) {
                    is FormueType -> copy(formuer = formuer.filter { it.type != type }.toSet())
                    is UtgiftType -> copy(utgifter = utgifter.filter { it.type != type }.toSet())
                    is InntektType -> copy(inntekter = inntekter.filter { it.type != type }.toSet())
                    else -> error("Ukjent OpplysningType for removal")
                }
            }
            ?.also { updatedOkonomi -> okonomiRepository.save(updatedOkonomi) }

        if (type.dokumentasjonForventet == true) dokumentasjonService.fjernForventetDokumentasjon(soknadId, type)
    }

//    /**
//     * Felles-funksjon for å fjerne et element fra et set av Inntekt, Utgift eller Formue (OkonomiPoster)
//     * Sjekker om elementet finnes og beskrivelse er lik, legger det til hvis det ikke gjør det
//     */
//    private fun <E : OkonomiElement> addAndSaveElement(
//        sourceSet: Set<E>,
//        element: E,
//        updateSetInOkonomiFunc: (Set<E>) -> Okonomi,
//    ): Set<E> {
//        return if (typeExistsAndEqualBeskrivelse(sourceSet, element.type, element.beskrivelse)) {
//            sourceSet
//        } else {
//            sourceSet
//                .filter { it.type != element.type }.toSet()
//                .plus(element)
//                .also { updatedSet -> okonomiRepository.save(updateSetInOkonomiFunc.invoke(updatedSet)) }
//        }
//    }

//    /**
//     * Felles-funksjon for å fjerne et element fra et set (Inntekt, Utgift, Formue)
//     * Sjekker om elementet finnes, lager en     kopi av settet uten elementet, og oppdaterer riktig variabel i Okonomi
//     */
//    private fun <E : OkonomiElement> removeElementByTypeAndSave(
//        sourceSet: Set<E>,
//        type: OpplysningType,
//        updateOkonomiFunction: (Set<E>) -> Okonomi,
//    ): Set<E> {
//        return sourceSet.find { it.type == type }
//            ?.let { existingEntity -> sourceSet.minus(existingEntity) }
//            ?.also { updatedSet -> okonomiRepository.save(updateOkonomiFunction.invoke(updatedSet)) }
//            ?: sourceSet
//    }

    private fun <E : OkonomiElement> typeExistsAndEqualBeskrivelse(
        set: Set<E>,
        type: OpplysningType,
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

data class OkonomiElementFinnesIkkeException(
    override val message: String,
    override val cause: Throwable? = null,
    val soknadId: UUID? = null,
) : SosialhjelpSoknadApiException(
        message = message,
        cause = null,
        id = soknadId?.toString(),
    )
