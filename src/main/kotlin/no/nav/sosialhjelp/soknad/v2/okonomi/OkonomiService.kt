package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * Ansvar for oppdatering av Okonomi-aggregatet, samt knytningen mellom Okonomi og Vedlegg(-forventninger)
 */
@Service
class OkonomiService(
    private val okonomiRepository: OkonomiRepository,
    private val dokumentasjonService: DokumentasjonService,
) {
    @Transactional(readOnly = true)
    fun getFormuer(soknadId: UUID): Set<Formue> = findOkonomi(soknadId)?.formuer ?: emptySet()

    @Transactional(readOnly = true)
    fun getInntekter(soknadId: UUID): Set<Inntekt> = findOkonomi(soknadId)?.inntekter ?: emptySet()

    @Transactional(readOnly = true)
    fun getUtgifter(soknadId: UUID): Set<Utgift> = findOkonomi(soknadId)?.utgifter ?: emptySet()

    @Transactional(readOnly = true)
    fun getBekreftelser(soknadId: UUID): Set<Bekreftelse> = findOkonomi(soknadId)?.bekreftelser ?: emptySet()

    @Transactional(readOnly = true)
    fun getBostotteSaker(soknadId: UUID): List<BostotteSak> = findOkonomi(soknadId)?.bostotteSaker ?: emptyList()

    @Transactional(readOnly = true)
    fun findDetaljerOrNull(
        soknadId: UUID,
        type: OkonomiOpplysningType,
    ): List<OkonomiDetalj>? {
        return when (type) {
            is InntektType -> getInntekter(soknadId).find { it.type == type }?.inntektDetaljer?.detaljer
            is UtgiftType -> getUtgifter(soknadId).find { it.type == type }?.utgiftDetaljer?.detaljer
            is FormueType -> getFormuer(soknadId).find { it.type == type }?.formueDetaljer?.detaljer
        }
    }

    @Transactional
    fun updateBekreftelse(
        soknadId: UUID,
        type: BekreftelseType,
        verdi: Boolean,
        tidspunkt: LocalDateTime = LocalDateTime.now(),
    ) {
        findOrCreateOkonomi(soknadId)
            .run {
                copy(
                    bekreftelser = bekreftelser.filter { it.type != type }.plus(Bekreftelse(type, verdi = verdi)).toSet(),
                )
            }
            .also { okonomiRepository.save(it) }
    }

    @Transactional
    fun deleteBekreftelse(
        soknadId: UUID,
        type: BekreftelseType,
    ) {
        findOkonomi(soknadId)
            ?.run { copy(bekreftelser = bekreftelser.filter { it.type != type }.toSet()) }
            ?.also { okonomiRepository.save(it) }
    }

    @Transactional
    fun addBostotteSaker(
        soknadId: UUID,
        saker: List<BostotteSak>,
    ) {
        findOrCreateOkonomi(soknadId)
            .run { copy(bostotteSaker = saker) }
            .also { okonomiRepository.save(it) }
    }

    @Transactional
    fun addBostotteSak(
        soknadId: UUID,
        sak: BostotteSak,
    ) {
        findOrCreateOkonomi(soknadId)
            .run { copy(bostotteSaker = bostotteSaker.plus(sak)) }
            .also { okonomiRepository.save(it) }
    }

    @Transactional
    fun removeBostotteSaker(soknadId: UUID) {
        okonomiRepository.findByIdOrNull(soknadId)
            ?.run { copy(bostotteSaker = emptyList()) }
            ?.also { okonomiRepository.save(it) }
    }

    @Transactional
    fun addElementToOkonomi(
        soknadId: UUID,
        type: OkonomiOpplysningType,
        beskrivelse: String? = null,
    ) {
        when (type) {
            is FormueType -> addElementToOkonomi(soknadId, Formue(type, beskrivelse))
            is UtgiftType -> addElementToOkonomi(soknadId, Utgift(type, beskrivelse))
            is InntektType -> addElementToOkonomi(soknadId, Inntekt(type, beskrivelse))
        }
    }

    @Transactional
    fun addElementToOkonomi(
        soknadId: UUID,
        opplysning: OkonomiOpplysning,
    ) {
        findOrCreateOkonomi(soknadId)
            .run {
                when (opplysning) {
                    is Formue -> copy(formuer = formuer.addOpplysning(opplysning))
                    is Inntekt -> copy(inntekter = inntekter.addOpplysning(opplysning))
                    is Utgift -> copy(utgifter = utgifter.addOpplysning(opplysning))
                }
            }
            .also { updatedOkonomi -> okonomiRepository.save(updatedOkonomi) }

        if (opplysning.type.dokumentasjonForventet) dokumentasjonService.opprettDokumentasjon(soknadId, opplysning.type)
    }

    @Transactional
    fun updateElement(
        soknadId: UUID,
        opplysning: OkonomiOpplysning,
    ) {
        findOkonomi(soknadId)
            ?.run {
                when (opplysning) {
                    is Inntekt -> copy(inntekter = inntekter.updateSet(opplysning))
                    is Utgift -> copy(utgifter = utgifter.updateSet(opplysning))
                    is Formue -> copy(formuer = formuer.updateSet(opplysning))
                }
            }
            ?.also { updatedOkonomi -> okonomiRepository.save(updatedOkonomi) }
            ?: error("Okonomi finnes ikke")
    }

    @Transactional
    fun removeElementFromOkonomi(
        soknadId: UUID,
        type: OkonomiOpplysningType,
    ) {
        findOkonomi(soknadId)
            ?.run {
                when (type) {
                    is FormueType -> copy(formuer = formuer.filter { it.type != type }.toSet())
                    is UtgiftType -> copy(utgifter = utgifter.filter { it.type != type }.toSet())
                    is InntektType -> copy(inntekter = inntekter.filter { it.type != type }.toSet())
                }
            }
            ?.also { updatedOkonomi -> okonomiRepository.save(updatedOkonomi) }

        if (type.dokumentasjonForventet) dokumentasjonService.fjernForventetDokumentasjon(soknadId, type)
    }

    private fun findOrCreateOkonomi(soknadId: UUID) = findOkonomi(soknadId) ?: okonomiRepository.save(Okonomi(soknadId))

    private fun findOkonomi(soknadId: UUID): Okonomi? = okonomiRepository.findByIdOrNull(soknadId)
}

private fun <E : OkonomiOpplysning> Set<E>.addOpplysning(element: E): Set<E> =
    filter { it.type != element.type }.plus(element).toSet()

private fun <E : OkonomiOpplysning> Set<E>.updateSet(opplysning: E): Set<E> =
    when (any { it.type == opplysning.type }) {
        true -> this.filter { it.type != opplysning.type }.plus(opplysning).toSet()
        false -> error("Inntekt (${opplysning.type} finnes ikke i Set")
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
