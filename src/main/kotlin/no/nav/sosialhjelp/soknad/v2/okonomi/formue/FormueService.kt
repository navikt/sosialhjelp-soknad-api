package no.nav.sosialhjelp.soknad.v2.okonomi.formue

import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.BeskrivelserAnnet
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface FormueService {
    fun getFormuer(soknadId: UUID): List<Formue>

    fun getBeskrivelseSparing(soknadId: UUID): String?

    fun updateFormue(
        soknadId: UUID,
        input: FormueInput,
    ): List<Formue>
}

/**
 * Ansvar for å oversette input og håndtere business-logikk ved oppretting, oppdatering eller sletting av formue-(innslag).
 * Skal sørge for semantisk consistency.
 */
@Service
@Transactional
class FormueServiceImpl(
    private val okonomiService: OkonomiService,
) : FormueService {
    override fun getFormuer(soknadId: UUID) =
        okonomiService.getFormuer(soknadId).filter { formueTyper.contains(it.type) }

    override fun getBeskrivelseSparing(soknadId: UUID): String? = okonomiService.getBeskrivelseAvAnnet(soknadId)?.sparing

    override fun updateFormue(
        soknadId: UUID,
        input: FormueInput,
    ): List<Formue> {
        val hasAny = input.hasAny()
        updateBekreftelse(soknadId = soknadId, verdi = hasAny)

        if (hasAny) {
            updateFormuer(soknadId, input)

            val beskrivelserAnnet = okonomiService.getBeskrivelseAvAnnet(soknadId) ?: BeskrivelserAnnet()
            beskrivelserAnnet.copy(sparing = input.beskrivelseSparing)
                .also { okonomiService.updateBeskrivelse(soknadId, it) }
        } else {
            updateFormuer(soknadId = soknadId, input = FormueInput())
        }

        return okonomiService.getFormuer(soknadId)
    }

    private fun updateBekreftelse(
        soknadId: UUID,
        verdi: Boolean,
    ) {
        okonomiService.updateBekreftelse(
            soknadId = soknadId,
            type = BekreftelseType.BEKREFTELSE_SPARING,
            verdi,
        )
    }

    private fun updateFormuer(
        soknadId: UUID,
        input: FormueInput,
    ) {
        okonomiService.updateFormue(soknadId, FormueType.FORMUE_BRUKSKONTO, input.hasBrukskonto)
        okonomiService.updateFormue(soknadId, FormueType.FORMUE_BSU, input.hasBsu)
        okonomiService.updateFormue(soknadId, FormueType.FORMUE_SPAREKONTO, input.hasSparekonto)
        okonomiService.updateFormue(soknadId, FormueType.FORMUE_LIVSFORSIKRING, input.hasLivsforsikring)
        okonomiService.updateFormue(soknadId, FormueType.FORMUE_VERDIPAPIRER, input.hasVerdipapirer)
        okonomiService.updateFormue(soknadId, FormueType.FORMUE_ANNET, input.beskrivelseSparing != null)
    }

    companion object {
        private val formueTyper: List<FormueType> =
            listOf(
                FormueType.FORMUE_BRUKSKONTO,
                FormueType.FORMUE_BSU,
                FormueType.FORMUE_SPAREKONTO,
                FormueType.FORMUE_LIVSFORSIKRING,
                FormueType.FORMUE_VERDIPAPIRER,
                FormueType.FORMUE_ANNET,
            )
    }
}

private fun FormueInput.hasAny(): Boolean {
    return hasBrukskonto || hasBsu || hasSparekonto || hasLivsforsikring || hasVerdipapirer || beskrivelseSparing != null
}
