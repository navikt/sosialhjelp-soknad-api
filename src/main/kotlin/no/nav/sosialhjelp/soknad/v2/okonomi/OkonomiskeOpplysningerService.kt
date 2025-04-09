package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import org.springframework.stereotype.Service
import java.util.UUID

interface OkonomiskeOpplysningerService {
    fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        type: OpplysningType,
        dokumentasjonLevert: Boolean,
        detaljer: List<OkonomiDetalj>?,
    )

    fun getForventetDokumentasjon(soknadId: UUID): Map<Dokumentasjon, List<OkonomiDetalj>?>
}

@Service
class OkonomiskeOpplysningerServiceImpl(
    private val okonomiService: OkonomiService,
    private val dokumentasjonService: DokumentasjonService,
) : OkonomiskeOpplysningerService {
    override fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        type: OpplysningType,
        dokumentasjonLevert: Boolean,
        detaljer: List<OkonomiDetalj>?,
    ) {
        if (type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER) {
            if (detaljer.isNullOrEmpty()) {
                updateDokumentasjonStatus(soknadId, type, dokumentasjonLevert)
                return
            }
            okonomiService.addElementToOkonomi(soknadId = soknadId, type = UtgiftType.UTGIFTER_ANDRE_UTGIFTER)
        }

        if (type is OkonomiOpplysningType) {
            detaljer?.also { detaljerNotNull ->
                addDetaljerToElement(soknadId, type, detaljerNotNull)
                    .also { okonomiService.updateElement(soknadId = soknadId, opplysning = it) }
            }
        }

        updateDokumentasjonStatus(soknadId, type, dokumentasjonLevert)
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

    override fun getForventetDokumentasjon(soknadId: UUID): Map<Dokumentasjon, List<OkonomiDetalj>?> =
        dokumentasjonService.findDokumentasjonForSoknad(soknadId).associateWith {
            if (it.type is OkonomiOpplysningType) it.type.getOkonomiskeDetaljerForType(soknadId) else null
        }

    private fun OkonomiOpplysningType.getOkonomiskeDetaljerForType(soknadId: UUID): List<OkonomiDetalj> =
        okonomiService.findDetaljerOrNull(soknadId, this) ?: emptyList()

    private fun updateDokumentasjonStatus(
        soknadId: UUID,
        type: OpplysningType,
        levertTidligere: Boolean,
    ) {
        // TODO hvis 'levertTidligere' er false - er det ikke sikkert man skal røre noe mer
        // TODO er den true, bør man kanskje slette eventuelle lagret Dokumentasjon (og Dokumenter)?
        // TODO Hvis status er LEVERT_TIDLIGERE ved innsending - slett alle referanser og filer i mellomlager
        when {
            levertTidligere -> DokumentasjonStatus.LEVERT_TIDLIGERE
            dokumentasjonService.hasDokumenterForType(soknadId, type) -> DokumentasjonStatus.LASTET_OPP
            else -> DokumentasjonStatus.FORVENTET
        }
            .let { dokumentasjonService.updateDokumentasjonStatus(soknadId, type, it) }
    }
}

private fun List<OkonomiDetalj>.mapToBelopList(): List<Belop> {
    return this.map { it as Belop }
}
