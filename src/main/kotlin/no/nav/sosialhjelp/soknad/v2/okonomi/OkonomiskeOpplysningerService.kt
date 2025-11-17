package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.app.exceptions.IkkeFunnetException
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import org.springframework.stereotype.Service
import java.util.UUID

interface OkonomiskeOpplysningerService {
    fun getOkonomiskeOpplysninger(
        soknadId: UUID,
    ): List<OkonomiOpplysning>

    fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        type: OkonomiOpplysningType,
        detaljer: List<OkonomiDetalj>,
    )
}

@Service
class OkonomiskeOpplysningerServiceImpl(
    private val okonomiService: OkonomiService,
    private val dokumentasjonService: DokumentasjonService,
) : OkonomiskeOpplysningerService {
    override fun getOkonomiskeOpplysninger(soknadId: UUID): List<OkonomiOpplysning> {
        return dokumentasjonService.findDokumentasjonForSoknad(soknadId)
            .filter { it.type is OkonomiOpplysningType }
            .mapNotNull { findElementOrNull(soknadId, it.type as OkonomiOpplysningType) }
    }

    override fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        type: OkonomiOpplysningType,
        detaljer: List<OkonomiDetalj>,
    ) {
        type.validate(soknadId)

        if (type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER) {
            okonomiService.addElementToOkonomi(soknadId, type)
        }

        addDetaljerToElement(soknadId, type, detaljer).also { okonomiService.updateElement(soknadId, it) }
    }

    private fun OkonomiOpplysningType.validate(soknadId: UUID) {
        if (dokumentasjonService.findDokumentasjonByType(soknadId, this) == null) {
            throw IkkeFunnetException("Finnes ikke dokumentasjon for ${this.name}")
        }
    }

    private fun addDetaljerToElement(
        soknadId: UUID,
        type: OkonomiOpplysningType,
        detaljer: List<OkonomiDetalj>,
    ): OkonomiOpplysning {
        return findElement(soknadId, type)
            .run {
                when (this) {
                    is Inntekt -> copy(inntektDetaljer = OkonomiDetaljer(detaljer))
                    is Utgift -> copy(utgiftDetaljer = OkonomiDetaljer(detaljer))
                    is Formue -> copy(formueDetaljer = OkonomiDetaljer(detaljer.map { it as Belop }))
                }
            }
    }

    private fun findElementOrNull(
        soknadId: UUID,
        type: OkonomiOpplysningType,
    ): OkonomiOpplysning? {
        return when (type) {
            is InntektType -> okonomiService.getInntekter(soknadId).find { it.type == type }
            is UtgiftType -> okonomiService.getUtgifter(soknadId).find { it.type == type }
            is FormueType -> okonomiService.getFormuer(soknadId).find { it.type == type }
        }
    }

    private fun findElement(
        soknadId: UUID,
        type: OkonomiOpplysningType,
    ): OkonomiOpplysning =
        findElementOrNull(soknadId, type)
            ?: throw OkonomiElementFinnesIkkeException(
                message = "Okonomi-element finnes ikke: $type",
                soknadId = soknadId,
            )
}
