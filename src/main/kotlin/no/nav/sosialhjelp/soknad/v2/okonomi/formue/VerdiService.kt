package no.nav.sosialhjelp.soknad.v2.okonomi.formue

import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface VerdiService {
    fun getVerdier(soknadId: UUID): Set<Formue>?

    fun removeVerdier(soknadId: UUID)

    fun updateVerdier(
        soknadId: UUID,
        input: HarVerdierInput,
    ): Set<Formue>
}

/**
 * Ansvar for å håndtere business-logikk ved oppretting, oppdatering eller sletting av formue(verdi)-innslag
 */
@Service
@Transactional
class VerdiServiceImpl(
    private val okonomiService: OkonomiService,
) : VerdiService {
    override fun getVerdier(soknadId: UUID): Set<Formue>? =
        okonomiService.getFormuer(soknadId)?.filter { verdiTyper.contains(it.type) }?.toSet()

    override fun removeVerdier(soknadId: UUID) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_VERDI, false)

        okonomiService.getFormuer(soknadId)
            ?.filter { verdiTyper.contains(it.type) }
            ?.forEach { okonomiService.removeType(soknadId, it.type) }
    }

    override fun updateVerdier(
        soknadId: UUID,
        input: HarVerdierInput,
    ): Set<Formue> {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_VERDI, true)

        updateAllVerdier(soknadId, input)

        return okonomiService.getFormuer(soknadId) ?: error("Kunne ikke oppdatere verdier")
    }

    private fun updateAllVerdier(
        soknadId: UUID,
        input: HarVerdierInput,
    ) {
        updateFormue(soknadId, FormueType.VERDI_BOLIG, input.hasBolig)
        updateFormue(soknadId, FormueType.VERDI_KJORETOY, input.hasKjoretoy)
        updateFormue(soknadId, FormueType.VERDI_CAMPINGVOGN, input.hasCampingvogn)
        updateFormue(soknadId, FormueType.VERDI_FRITIDSEIENDOM, input.hasFritidseiendom)
        updateFormue(
            soknadId,
            FormueType.VERDI_ANNET,
            input.hasBeskrivelseAnnet,
            if (input.hasBeskrivelseAnnet) input.beskrivelseVerdi else null,
        )
    }

    private fun updateFormue(
        soknadId: UUID,
        type: FormueType,
        isPresent: Boolean,
        beskrivelse: String? = null,
    ) {
        type.let {
            if (isPresent) {
                okonomiService.addType(soknadId, type, beskrivelse)
            } else {
                okonomiService.removeType(soknadId, type)
            }
        }
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
