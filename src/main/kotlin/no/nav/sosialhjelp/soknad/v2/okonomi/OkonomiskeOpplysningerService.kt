package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.stereotype.Service
import java.util.UUID

interface OkonomiskeOpplysningerService {
    fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        type: OpplysningType,
        dokumentasjonLevert: Boolean,
        detaljer: List<OkonomiDetalj>,
    )

    fun getForventetDokumentasjon(soknadId: UUID): Map<Dokumentasjon, List<OkonomiDetalj>>
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
        detaljer: List<OkonomiDetalj>,
    ) {
        if (type == UtgiftType.UTGIFTER_ANDRE_UTGIFTER) {
            if (detaljer.isEmpty()) {
                updateDokumentasjonStatus(soknadId, type, dokumentasjonLevert)
                return
            }
            okonomiService.addElementToOkonomi(soknadId = soknadId, type = UtgiftType.UTGIFTER_ANDRE_UTGIFTER)
        }

        if (typesWithOkonomiElement.contains(type.javaClass)) {
            addDetaljerToElement(soknadId, type, detaljer)
                .let { okonomiService.updateElement(soknadId = soknadId, element = it) }
        }
        updateDokumentasjonStatus(soknadId, type, dokumentasjonLevert)
    }

//    private fun findElement(
//        type: OpplysningType,
//        detaljer: List<OkonomiDetalj>,
//    ): OkonomiElement {
//        return when (type) {
//            is InntektType -> Inntekt(type, inntektDetaljer = OkonomiDetaljer(detaljer))
//            is UtgiftType -> Utgift(type, utgiftDetaljer = OkonomiDetaljer(detaljer))
//            is FormueType -> Formue(type, formueDetaljer = OkonomiDetaljer(detaljer.mapToBelopList()))
//            else -> error("Ukjent Okonomi-type")
//        }
//    }

    private fun addDetaljerToElement(
        soknadId: UUID,
        type: OpplysningType,
        detaljer: List<OkonomiDetalj>,
    ): OkonomiElement {
        return when (type) {
            is InntektType ->
                okonomiService.getInntekter(soknadId).find { it.type == type }
                    ?.copy(inntektDetaljer = OkonomiDetaljer(detaljer))
            is UtgiftType ->
                okonomiService.getUtgifter(soknadId).find { it.type == type }
                    ?.copy(utgiftDetaljer = OkonomiDetaljer(detaljer))
            is FormueType ->
                okonomiService.getFormuer(soknadId).find { it.type == type }
                    ?.copy(formueDetaljer = OkonomiDetaljer(detaljer.mapToBelopList()))
            else -> error("Ukjent Okonomi-type")
        }
            ?: throw OkonomiElementFinnesIkkeException(
                message = "Okonomi-element finnes ikke: $type",
                soknadId = soknadId,
            )
    }

    private fun getOkonomiskeDetaljerForType(
        soknadId: UUID,
        type: OpplysningType,
    ): List<OkonomiDetalj> {
        // kan finnes dokumentasjon som ikke er knyttet til okonomiske
        if (!typesWithOkonomiElement.contains(type.javaClass)) return emptyList()

        return okonomiService.findDetaljerOrNull(soknadId, type) ?: emptyList()
    }

    override fun getForventetDokumentasjon(soknadId: UUID): Map<Dokumentasjon, List<OkonomiDetalj>> {
        return dokumentasjonService.findDokumentasjonForSoknad(soknadId)
            .associateWith { getOkonomiskeDetaljerForType(soknadId, it.type) }
    }

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
            .let {
                dokumentasjonService.updateDokumentasjonStatus(soknadId, type, it)
            }
    }

    companion object {
        // kan finnes forventet dokumentasjon som ikke har okonomi-element (skattemelding, annet, etc.)
        val typesWithOkonomiElement = listOf(InntektType::class.java, UtgiftType::class.java, FormueType::class.java)
    }
}

private fun List<OkonomiDetalj>.mapToBelopList(): List<Belop> {
    return this.map { it as Belop }
}
