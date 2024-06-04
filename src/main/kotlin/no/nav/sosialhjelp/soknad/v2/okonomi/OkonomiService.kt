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
    private val dokumentasjonForventningService: DokumentasjonForventningService,
) {
    fun getFormuer(soknadId: UUID) = okonomiRepository.findByIdOrNull(soknadId)?.formuer ?: emptyList()

    fun getBeskrivelseAvAnnet(soknadId: UUID) = okonomiRepository.findByIdOrNull(soknadId)?.beskrivelserAnnet

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

    fun updateFormue(
        soknadId: UUID,
        type: FormueType,
        isPresent: Boolean,
    ): List<Formue> {
        val okonomi = findOrCreate(soknadId)

        // TODO Slik logikken er nå (og tidligere), så bevarer den ikke eventuell rad/beløp-info hvis bruker sjekker av - riktig?
        if (isPresent) {
            okonomi.formuer.firstOrNull { it.type == type }
                ?: okonomi
                    .copy(formuer = okonomi.formuer.plus(Formue(type)))
                    .also { okonomiRepository.save(it) }
        } else {
            okonomi.formuer
                .filter { it.type != type }
                .let { formuer -> okonomi.copy(formuer = formuer) }
                .also { okonomiRepository.save(it) }
        }

        if (type.dokumentasjonForventet) {
            dokumentasjonForventningService.updateForventedeVedlegg(soknadId, type, isPresent)
        }
        return okonomiRepository.findByIdOrNull(soknadId)?.formuer ?: error("Okonomi ble ikke lagret")
    }

    fun updateInntekt(
        soknadId: UUID,
        type: InntektType,
        isPresent: Boolean,
    ): List<Inntekt> {
        val okonomi = findOrCreate(soknadId)

        if (isPresent) {
            okonomi.inntekter.firstOrNull { it.type == type }
                ?: okonomi
                    .copy(inntekter = okonomi.inntekter.plus(Inntekt(type = type)))
                    .also { okonomiRepository.save(it) }
        } else {
            okonomi.inntekter
                .filter { it.type != type }
                .let { inntekter -> okonomi.copy(inntekter = inntekter) }
                .also { okonomiRepository.save(it) }
        }

        if (type.dokumentasjonForventet) {
            dokumentasjonForventningService.updateForventedeVedlegg(soknadId, type, isPresent)
        }

        return okonomiRepository.findByIdOrNull(soknadId)?.inntekter ?: error("Okonomi ble ikke lagret")
    }

    fun updateUtgift(
        soknadId: UUID,
        type: UtgiftType,
        isPresent: Boolean,
    ): List<Utgift> {
        val okonomi = findOrCreate(soknadId)

        if (isPresent) {
            okonomi.utgifter.firstOrNull { it.type == type }
                ?: okonomi
                    .copy(utgifter = okonomi.utgifter.plus(Utgift(type = type)))
                    .also { okonomiRepository.save(it) }
        } else {
            okonomi.utgifter
                .filter { it.type != type }
                .let { utgifter -> okonomi.copy(utgifter = utgifter) }
                .also { okonomiRepository.save(it) }
        }

        if (type.dokumentasjonForventet) {
            dokumentasjonForventningService.updateForventedeVedlegg(soknadId, type, isPresent)
        }

        return okonomiRepository.findByIdOrNull(soknadId)?.utgifter ?: error("Okonomi ble ikke lagret")
    }

    fun updateBeskrivelse(
        soknadId: UUID,
        beskrivelserAnnet: BeskrivelserAnnet,
    ) {
        findOrCreate(soknadId)
            .run { copy(beskrivelserAnnet = beskrivelserAnnet) }
            .also { okonomiRepository.save(it) }
    }

    private fun findOrCreate(soknadId: UUID) =
        okonomiRepository.findByIdOrNull(soknadId)
            ?: okonomiRepository.save(Okonomi(soknadId))
}
