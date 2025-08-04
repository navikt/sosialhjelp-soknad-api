package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.app.exceptions.SosialhjelpSoknadApiException
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
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

    @Transactional
    fun updateBekreftelse(
        soknadId: UUID,
        type: BekreftelseType,
        verdi: Boolean,
        tidspunkt: LocalDateTime = LocalDateTime.now(),
    ) {
        findOrCreateOkonomi(soknadId)
        okonomiRepository.deleteBekreftelse(soknadId, type)
        okonomiRepository.addBekreftelse(soknadId, type, tidspunkt, verdi)
    }

    @Transactional
    fun deleteBekreftelse(
        soknadId: UUID,
        type: BekreftelseType,
    ) {
        findOkonomi(soknadId)?.also { okonomiRepository.deleteBekreftelse(soknadId, type) }
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

    @Transactional(isolation = Isolation.READ_COMMITTED)
    fun addElementToOkonomi(
        soknadId: UUID,
        opplysning: OkonomiOpplysning,
    ) {
        findOrCreateOkonomi(soknadId)
        when (opplysning) {
            is Formue -> {
                okonomiRepository.deleteFormue(soknadId, opplysning.type)
                okonomiRepository.addFormue(soknadId, opplysning.type, opplysning.beskrivelse, opplysning.formueDetaljer.toJson())
            }
            is Inntekt -> {
                okonomiRepository.deleteInntekt(soknadId, opplysning.type)
                okonomiRepository.addInntekt(soknadId, opplysning.type, opplysning.beskrivelse, opplysning.inntektDetaljer.toJson())
            }
            is Utgift -> {
                okonomiRepository.deleteUtgift(soknadId, opplysning.type)
                okonomiRepository.addUtgift(soknadId, opplysning.type, opplysning.beskrivelse, opplysning.utgiftDetaljer.toJson())
            }
        }
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
                    is Inntekt -> {
                        inntekter.find { it.type == opplysning.type } ?: error("Opplysning finnes ikke i inntekter")
                        addElementToOkonomi(soknadId, opplysning)
                    }
                    is Utgift -> {
                        utgifter.find { it.type == opplysning.type } ?: error("Opplysning finnes ikke i utgifter")
                        addElementToOkonomi(soknadId, opplysning)
                    }
                    is Formue -> {
                        formuer.find { it.type == opplysning.type } ?: error("Opplysning finnes ikke i formuer")
                        addElementToOkonomi(soknadId, opplysning)
                    }
                }
            }
    }

    @Transactional
    fun removeElementFromOkonomi(
        soknadId: UUID,
        type: OkonomiOpplysningType,
    ) {
        findOkonomi(soknadId)
            ?.also {
                when (type) {
                    is FormueType -> okonomiRepository.deleteFormue(soknadId, type)
                    is UtgiftType -> okonomiRepository.deleteUtgift(soknadId, type)
                    is InntektType -> okonomiRepository.deleteInntekt(soknadId, type)
                }
            }
        if (type.dokumentasjonForventet) dokumentasjonService.fjernForventetDokumentasjon(soknadId, type)
    }

    private fun findOrCreateOkonomi(soknadId: UUID) = findOkonomi(soknadId) ?: okonomiRepository.save(Okonomi(soknadId))

    private fun findOkonomi(soknadId: UUID): Okonomi? = okonomiRepository.findByIdOrNull(soknadId)
}

private fun <T : OkonomiDetalj> OkonomiDetaljer<T>.toJson() = OkonomiskeDetaljerToStringConverter<T>().convert(this)

data class OkonomiElementFinnesIkkeException(
    override val message: String,
    override val cause: Throwable? = null,
    val soknadId: UUID? = null,
) : SosialhjelpSoknadApiException(
        message = message,
        cause = null,
        id = soknadId?.toString(),
    )
