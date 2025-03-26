package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.stereotype.Service
import java.util.UUID

interface OkonomiskeOpplysningerService {
    fun getOkonomiskeOpplysningerForTyper(
        soknadId: UUID,
        typer: List<OpplysningType>,
    ): List<OkonomiElement>

    fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        type: OpplysningType,
        detaljer: List<OkonomiDetalj>,
    )
}

@Service
class OkonomiskeOpplysningerServiceImpl(
    private val okonomiService: OkonomiService,
) : OkonomiskeOpplysningerService {
    override fun getOkonomiskeOpplysningerForTyper(
        soknadId: UUID,
        typer: List<OpplysningType>,
    ): List<OkonomiElement> =
        typer.map { findElement(soknadId, it) }

    override fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        type: OpplysningType,
        detaljer: List<OkonomiDetalj>,
    ) {
        if (type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER) {
            if (detaljer.isEmpty()) {
                okonomiService.removeElementFromOkonomi(soknadId, type)
                return
            }
            okonomiService.addElementToOkonomi(soknadId, type)
        }

        if (type is OkonomiOpplysningType) {
            detaljer?.also { detaljerNotNull ->
                addDetaljerToElement(soknadId, type, detaljerNotNull)
                    .also { okonomiService.updateElement(soknadId = soknadId, opplysning = it) }
            }
        }
    }

    private fun addDetaljerToElement(
        soknadId: UUID,
        type: OpplysningType,
        detaljer: List<OkonomiDetalj>,
    ): OkonomiOpplysning {
        return findElement(soknadId, type)
            .run {
                when (this) {
                    is Inntekt -> copy(inntektDetaljer = OkonomiDetaljer(detaljer))
                    is Utgift -> copy(utgiftDetaljer = OkonomiDetaljer(detaljer))
                    is Formue -> copy(formueDetaljer = OkonomiDetaljer(detaljer.mapToBelopList()))
                }
            }
    }

    private fun findElement(
        soknadId: UUID,
        type: OpplysningType,
    ): OkonomiOpplysning {
        return when (type) {
            is InntektType -> okonomiService.getInntekter(soknadId).find { it.type == type }
            is UtgiftType -> okonomiService.getUtgifter(soknadId).find { it.type == type }
            is FormueType -> okonomiService.getFormuer(soknadId).find { it.type == type }
            else -> error("Ukjent Okonomi-type")
        }
            ?: throw OkonomiElementFinnesIkkeException(
                message = "Okonomi-element finnes ikke: $type",
                soknadId = soknadId,
            )
    }

    companion object {
        val typesWithOkonomiElement = listOf(InntektType::class.java, UtgiftType::class.java, FormueType::class.java)
    }
}

private fun List<OkonomiDetalj>.mapToBelopList(): List<Belop> {
    return this.map { it as Belop }
}
