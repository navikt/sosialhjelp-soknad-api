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
        existingTypes: Set<FormueType>,
        beskrivelseAnnet: String?,
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
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_VERDI, verdi = false)

        okonomiService.getFormuer(soknadId)
            ?.filter { verdiTyper.contains(it.type) }
            ?.forEach { okonomiService.removeElementFromOkonomi(soknadId, it.type) }
    }

    override fun updateVerdier(
        soknadId: UUID,
        existingTypes: Set<FormueType>,
        beskrivelseAnnet: String?,
    ): Set<Formue> {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_VERDI, verdi = true)

        verdiTyper.forEach { type ->
            if (existingTypes.contains(type)) {
                okonomiService.addElementToOkonomi(
                    soknadId = soknadId,
                    type = type,
                    beskrivelse = if (type == FormueType.VERDI_ANNET) beskrivelseAnnet else null,
                )
            } else {
                okonomiService.removeElementFromOkonomi(soknadId, type)
            }
        }

        return okonomiService.getFormuer(soknadId) ?: error("Kunne ikke oppdatere verdier")
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
