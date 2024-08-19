package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonService
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.FormueType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.stereotype.Service
import java.util.UUID

interface OkonomiskeOpplysningerService {
    fun updateOkonomiskeOpplysninger(
        soknadId: UUID,
        type: OkonomiType,
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
        type: OkonomiType,
        dokumentasjonLevert: Boolean,
        detaljer: List<OkonomiDetalj>,
    ) {
        addSpecialCaseElement(soknadId, type)

        createElement(type, detaljer).let {
            try {
                okonomiService.updateElement(soknadId = soknadId, element = it)
            } catch (e: OkonomiElementFinnesIkkeException) {
                throw OkonomiElementFinnesIkkeException(
                    message = e.message,
                    cause = e,
                    soknadId = soknadId,
                )
            }
        }
        updateDokumentasjonStatus(soknadId, type, dokumentasjonLevert)
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
            is InntektType -> Inntekt(type, inntektDetaljer = OkonomiDetaljer(detaljer))
            is UtgiftType -> Utgift(type, utgiftDetaljer = OkonomiDetaljer(detaljer))
            is FormueType -> Formue(type, formueDetaljer = OkonomiDetaljer(detaljer.mapToBelopList()))
            else -> error("Ukjent Okonomi-type")
        }
    }

    private fun getOkonomiskeDetaljerForType(
        soknadId: UUID,
        type: OkonomiType,
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
        type: OkonomiType,
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
