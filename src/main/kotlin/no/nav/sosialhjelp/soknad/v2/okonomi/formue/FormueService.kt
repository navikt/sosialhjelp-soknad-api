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
        existingTypes: Set<FormueType>,
        beskrivelse: String?,
    )

    fun removeFormuer(soknadId: UUID)
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
        existingTypes: Set<FormueType>,
        beskrivelse: String?,
    ) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_SPARING, verdi = true)

        formueTyper.forEach { type ->
            if (existingTypes.contains(type)) {
                okonomiService.addElementToOkonomi(
                    soknadId = soknadId,
                    type = type,
                    beskrivelse = if (type == FormueType.FORMUE_ANNET) beskrivelse else null,
                )
            } else {
                okonomiService.removeElementFromOkonomi(soknadId, type)
            }
        }
    }

    override fun removeFormuer(soknadId: UUID) {
        okonomiService.updateBekreftelse(soknadId, BekreftelseType.BEKREFTELSE_SPARING, verdi = false)

        formueTyper.forEach { type -> okonomiService.removeElementFromOkonomi(soknadId, type) }
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
