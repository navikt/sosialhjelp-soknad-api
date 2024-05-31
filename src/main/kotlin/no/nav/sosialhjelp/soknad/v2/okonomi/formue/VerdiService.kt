package no.nav.sosialhjelp.soknad.v2.okonomi.formue

import no.nav.sosialhjelp.soknad.v2.okonomi.Bekreftelse
import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.BeskrivelserAnnet
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface VerdiService {
    fun getVerdier(soknadId: UUID): List<Formue>

    fun getBekreftelse(soknadId: UUID): Bekreftelse?

    fun getBeskrivelseVerdi(soknadId: UUID): String?

    fun removeVerdier(soknadId: UUID)

    fun updateVerdier(
        soknadId: UUID,
        input: HarVerdierInput,
    ): List<Formue>
}

/**
 * Ansvar for å håndtere business-logikk ved oppretting, oppdatering eller sletting av formue(verdi)-innslag
 */
@Service
@Transactional
class VerdiServiceImpl(
    private val okonomiService: OkonomiService,
) : VerdiService {
    override fun getVerdier(soknadId: UUID) =
        okonomiService.getFormuer(soknadId).filter { verdiTyper.contains(it.type) }

    override fun getBekreftelse(soknadId: UUID) =
        okonomiService.getBekreftelser(soknadId).firstOrNull { it.type == BekreftelseType.BEKREFTELSE_VERDI }

    override fun getBeskrivelseVerdi(soknadId: UUID) = okonomiService.getBeskrivelseAvAnnet(soknadId)?.verdi

    override fun removeVerdier(soknadId: UUID) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_VERDI, false)

        okonomiService.getFormuer(soknadId)
            .filter { verdiTyper.contains(it.type) }
            .forEach { okonomiService.updateFormue(soknadId, it.type, false) }

        val updatedBeskrivelser =
            okonomiService.getBeskrivelseAvAnnet(soknadId)?.copy(verdi = null)
                ?: BeskrivelserAnnet()

        okonomiService.updateBeskrivelse(soknadId, updatedBeskrivelser)
    }

    override fun updateVerdier(
        soknadId: UUID,
        input: HarVerdierInput,
    ): List<Formue> {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_VERDI, true)

        updateAllVerdier(soknadId, input)

        val beskrivelserAnnet = okonomiService.getBeskrivelseAvAnnet(soknadId) ?: BeskrivelserAnnet()
        beskrivelserAnnet
            .copy(verdi = input.beskrivelseVerdi)
            .also { okonomiService.updateBeskrivelse(soknadId, it) }

        return okonomiService.getFormuer(soknadId)
    }

    private fun updateAllVerdier(
        soknadId: UUID,
        input: HarVerdierInput,
    ) {
        okonomiService.updateFormue(soknadId, FormueType.VERDI_BOLIG, input.hasBolig)
        okonomiService.updateFormue(soknadId, FormueType.VERDI_KJORETOY, input.hasKjoretoy)
        okonomiService.updateFormue(soknadId, FormueType.VERDI_CAMPINGVOGN, input.hasCampingvogn)
        okonomiService.updateFormue(soknadId, FormueType.VERDI_FRITIDSEIENDOM, input.hasFritidseiendom)
        okonomiService.updateFormue(soknadId, FormueType.VERDI_ANNET, input.beskrivelseVerdi != null)
    }

    companion object {
        private val verdiTyper: List<FormueType> =
            listOf(
                FormueType.VERDI_KJORETOY,
                FormueType.VERDI_BOLIG,
                FormueType.VERDI_CAMPINGVOGN,
                FormueType.VERDI_FRITIDSEIENDOM,
                FormueType.VERDI_ANNET,
            )
    }
}
