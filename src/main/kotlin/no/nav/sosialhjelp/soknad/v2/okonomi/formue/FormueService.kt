package no.nav.sosialhjelp.soknad.v2.okonomi.formue

import no.nav.sosialhjelp.soknad.v2.okonomi.BekreftelseType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

interface FormueService {
    fun getFormuer(soknadId: UUID): Set<Formue>?

    fun updateFormuer(
        soknadId: UUID,
        input: FormueInput,
    ): Set<Formue>
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
    override fun getFormuer(soknadId: UUID): Set<Formue>? =
        okonomiService.getFormuer(soknadId)?.filter { formueTyper.contains(it.type) }?.toSet()

    override fun updateFormuer(
        soknadId: UUID,
        input: FormueInput,
    ): Set<Formue> {
        val hasAny = input.hasAny()
        updateBekreftelse(soknadId = soknadId, verdi = hasAny)

        if (hasAny) {
            updateAllFormuer(soknadId, input)
        } else {
            updateAllFormuer(soknadId = soknadId, input = FormueInput())
        }

        return okonomiService.getFormuer(soknadId) ?: error("FEIL i lagring av formue")
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

    private fun updateAllFormuer(
        soknadId: UUID,
        input: FormueInput,
    ) {
        updateFormue(soknadId, FormueType.FORMUE_BRUKSKONTO, input.hasBrukskonto)
        updateFormue(soknadId, FormueType.FORMUE_BSU, input.hasBsu)
        updateFormue(soknadId, FormueType.FORMUE_SPAREKONTO, input.hasSparekonto)
        updateFormue(soknadId, FormueType.FORMUE_LIVSFORSIKRING, input.hasLivsforsikring)
        updateFormue(soknadId, FormueType.FORMUE_VERDIPAPIRER, input.hasVerdipapirer)
        updateFormue(
            soknadId,
            FormueType.FORMUE_ANNET,
            input.hasBeskrivelseSparing,
            if (input.hasBeskrivelseSparing) input.beskrivelseSparing else null,
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
                okonomiService.addElementToOkonomi(soknadId, it, beskrivelse)
            } else {
                okonomiService.removeElementFromOkonomi(soknadId, it)
            }
        }
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
    return hasBrukskonto || hasBsu || hasSparekonto || hasLivsforsikring || hasVerdipapirer || hasBeskrivelseSparing
}
