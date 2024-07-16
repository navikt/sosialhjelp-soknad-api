package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.stereotype.Service
import java.util.UUID

interface UpdateOkonomiService {
    fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        type: OkonomiType,
        detaljer: List<OkonomiDetalj>,
    )
}

@Service
class UpdateOkonomiServiceImpl(
    private val okonomiService: OkonomiService,
) : UpdateOkonomiService {
    override fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        type: OkonomiType,
        detaljer: List<OkonomiDetalj>,
    ) {
        addSpecialCaseElement(soknadId, type)

        createElement(type, detaljer).let {
            okonomiService.updateElement(soknadId = soknadId, element = it)
        }
    }

    // Typer som ikke er opprettet før i søknaden
    private fun addSpecialCaseElement(
        soknadId: UUID,
        type: OkonomiType,
    ) {
        if (type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER) {
            okonomiService.addElementToOkonomi(soknadId = soknadId, type = type)
        }
    }

    private fun createElement(
        type: OkonomiType,
        detaljer: List<OkonomiDetalj>,
    ): OkonomiElement {
        return when (type) {
            is InntektType -> Inntekt(type, inntektDetaljer = OkonomiskeDetaljer(detaljer))
            is UtgiftType -> Utgift(type, utgiftDetaljer = OkonomiskeDetaljer(detaljer.mapToBelopList()))
            is FormueType -> Formue(type, formueDetaljer = OkonomiskeDetaljer(detaljer.mapToBelopList()))
            else -> error("Ukjent Okonomi-type")
        }
    }
}

private fun List<OkonomiDetalj>.mapToBelopList(): List<Belop> {
    return this.map { it as Belop }
}
