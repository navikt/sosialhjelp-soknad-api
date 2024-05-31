package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.tekster.TextService
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.vedlegg.VedleggForventningService
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
    private val vedleggForventningService: VedleggForventningService,
    private val textService: TextService,
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
            .plus(Bekreftelse(type, getTittel(type.tittelKey), verdi))
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
                    .copy(formuer = okonomi.formuer.plus(Formue(type, getTittel(type.tittelKey))))
                    .also { okonomiRepository.save(it) }
        } else {
            okonomi.formuer
                .filter { it.type != type }
                .let { formuer -> okonomi.copy(formuer = formuer) }
                .also { okonomiRepository.save(it) }
        }

        if (type.vedleggKreves) {
            vedleggForventningService.updateForventedeVedlegg(soknadId, type, isPresent)
        }
        return okonomiRepository.findByIdOrNull(soknadId)?.formuer ?: error("Okonomi ble ikke lagret")
    }

    fun updateBeskrivelse(
        soknadId: UUID,
        beskrivelserAnnet: BeskrivelserAnnet,
    ) {
        findOrCreate(soknadId)
            .run { copy(beskrivelserAnnet = beskrivelserAnnet) }
            .also { okonomiRepository.save(it) }
    }

    private fun getTittel(typeString: String): String = textService.getJsonOkonomiTittel(typeString)

    private fun findOrCreate(soknadId: UUID) =
        okonomiRepository.findByIdOrNull(soknadId)
            ?: okonomiRepository.save(Okonomi(soknadId))
}
