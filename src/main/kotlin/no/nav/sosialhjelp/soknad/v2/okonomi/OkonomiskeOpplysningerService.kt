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
        detaljer: List<OkonomiDetalj>,
    )

    fun getForventetDokumentasjon(soknadId: UUID): Map<Dokumentasjon, List<OkonomiDetalj>>

    fun updateDokumentasjonStatus(
        soknadId: UUID,
        type: OkonomiType,
        levertTidligere: Boolean,
    )
}

@Service
class OkonomiskeOpplysningerServiceImpl(
    private val okonomiService: OkonomiService,
    private val dokumentasjonService: DokumentasjonService,
) : OkonomiskeOpplysningerService {
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

    override fun updateDokumentasjonStatus(
        soknadId: UUID,
        type: OkonomiType,
        levertTidligere: Boolean,
    ) {
        // TODO hvis 'levertTidligere' er false - er det ikke sikkert man skal røre noe mer
        // TODO er den true, bør man kanskje slette eventuelle lagret Dokumentasjon (og Dokumenter)?
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

private fun OkonomiType.isNotRenterOrAvdrag(): Boolean {
    return this != UtgiftType.UTGIFTER_BOLIGLAN_RENTER && this != UtgiftType.UTGIFTER_BOLIGLAN_AVDRAG
}
