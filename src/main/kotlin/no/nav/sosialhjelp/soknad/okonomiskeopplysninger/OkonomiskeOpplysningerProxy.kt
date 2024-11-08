package no.nav.sosialhjelp.soknad.okonomiskeopplysninger

import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggRadFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggStatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.json.getJsonVerdier
import no.nav.sosialhjelp.soknad.v2.okonomi.AbstractOkonomiInput
import no.nav.sosialhjelp.soknad.v2.okonomi.AvdragRenterDto
import no.nav.sosialhjelp.soknad.v2.okonomi.BelopDto
import no.nav.sosialhjelp.soknad.v2.okonomi.BoliglanInput
import no.nav.sosialhjelp.soknad.v2.okonomi.DokumentasjonDto
import no.nav.sosialhjelp.soknad.v2.okonomi.ForventetDokumentasjonDto
import no.nav.sosialhjelp.soknad.v2.okonomi.GenericOkonomiInput
import no.nav.sosialhjelp.soknad.v2.okonomi.LonnsInntektDto
import no.nav.sosialhjelp.soknad.v2.okonomi.LonnsInput
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljDto
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeOpplysningerController
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import no.nav.sosialhjelp.soknad.vedlegg.dto.DokumentUpload
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OkonomiskeOpplysningerProxy(
    private val okonomiskeOpplysningerController: OkonomiskeOpplysningerController,
    private val okonomiService: OkonomiService,
) {
    fun getOkonomiskeOpplysninger(behandlingsId: String): VedleggFrontends {
        return okonomiskeOpplysningerController
            .getForventetDokumentasjon(UUID.fromString(behandlingsId))
            .toVedleggFrontends(isOpplysningerBekreftet(behandlingsId))
    }

    fun updateOkonomiskeOpplysninger(
        behandlingsId: String,
        vedleggFrontend: VedleggFrontend,
    ) {
        okonomiskeOpplysningerController.updateOkonomiskeDetaljer(
            soknadId = UUID.fromString(behandlingsId),
            input = vedleggFrontend.resolveOkonomiInput(),
        )
    }

    private fun isOpplysningerBekreftet(behandlingsId: String): Boolean {
        return okonomiService.getBekreftelser(UUID.fromString(behandlingsId)).isNotEmpty()
    }
}

private fun ForventetDokumentasjonDto.toVedleggFrontends(opplysningerBekreftet: Boolean) =
    VedleggFrontends(
        isOkonomiskeOpplysningerBekreftet = opplysningerBekreftet,
        slettedeVedlegg = null,
        okonomiskeOpplysninger = this.forventetDokumentasjon.map { it.toVedleggFrontend() },
    )

private fun DokumentasjonDto.toVedleggFrontend(): VedleggFrontend {
    val vedleggType = type.getJsonVerdier().vedleggType ?: error("Mangler type for mapping til VedleggType")

    return VedleggFrontend(
        type = vedleggType,
        alleredeLevert = dokumentasjonStatus == DokumentasjonStatus.LEVERT_TIDLIGERE,
        rader = detaljer.resolveRader(vedleggType),
        gruppe = gruppe,
        vedleggStatus = dokumentasjonStatus.toVedleggStatus(),
        filer = dokumenter.map { DokumentUpload(it.filnavn, it.dokumentId.toString()) },
    )
}

// TODO Frontend rendrer input-felter basert på hva som returneres for den spesifikke typen
// TODO Derfor må det sendes med et tomt element for typer som skal ha input...
// TODO Dette bør gjøres annerledes på frontend
private fun VedleggType.getRadForEmptyList(): List<VedleggRadFrontend> {
    return VedleggTypeToSoknadTypeMapper.vedleggTypeToSoknadType[this]
        ?.let { listOf(VedleggRadFrontend()) }
        ?: emptyList()
}

private fun List<OkonomiDetaljDto>?.resolveRader(vedleggType: VedleggType): List<VedleggRadFrontend> {
    return if (this.isNullOrEmpty()) vedleggType.getRadForEmptyList() else this.map { it.toVedleggRadFrontend() }
}

private fun OkonomiDetaljDto.toVedleggRadFrontend(): VedleggRadFrontend {
    return when (this) {
        is AvdragRenterDto -> this.toVedleggRadFrontend()
        is BelopDto -> this.toVedleggRadFrontend()
        is LonnsInntektDto -> this.toVedleggRadFrontend()
    }
}

private fun AvdragRenterDto.toVedleggRadFrontend(): VedleggRadFrontend {
    return VedleggRadFrontend(
        avdrag = avdrag?.toInt(),
        renter = renter?.toInt(),
    )
}

private fun BelopDto.toVedleggRadFrontend(): VedleggRadFrontend {
    return VedleggRadFrontend(
        beskrivelse = beskrivelse,
        belop = belop.toInt(),
    )
}

private fun LonnsInntektDto.toVedleggRadFrontend(): VedleggRadFrontend {
    return VedleggRadFrontend(
        brutto = brutto?.toInt(),
        netto = netto?.toInt(),
    )
}

private fun DokumentasjonStatus.toVedleggStatus(): VedleggStatus {
    return when (this) {
        DokumentasjonStatus.LEVERT_TIDLIGERE -> VedleggStatus.VedleggAlleredeSendt
        DokumentasjonStatus.LASTET_OPP -> VedleggStatus.LastetOpp
        DokumentasjonStatus.FORVENTET -> VedleggStatus.VedleggKreves
    }
}

private fun VedleggFrontend.resolveOkonomiInput(): AbstractOkonomiInput {
    val opplysningType: OpplysningType =
        type.opplysningType
            ?: throw IllegalArgumentException("VedleggType ${type.name} har ingen mapping til OpplysningType")

    return when (opplysningType) {
        InntektType.JOBB -> toLonnsInput()
        UtgiftType.UTGIFTER_BOLIGLAN -> toBoliglanInput()
        else -> toGenericOkonomiInput(opplysningType)
    }
}

private fun VedleggFrontend.toLonnsInput() =
    LonnsInput(
        dokumentasjonLevert = alleredeLevert ?: false,
        detalj = rader?.first().let { LonnsInntektDto(brutto = it?.brutto?.toDouble(), netto = it?.netto?.toDouble()) },
    )

private fun VedleggFrontend.toBoliglanInput() =
    BoliglanInput(
        dokumentasjonLevert = alleredeLevert ?: false,
        detaljer = rader?.map { AvdragRenterDto(it.avdrag?.toDouble(), it.renter?.toDouble()) } ?: emptyList(),
    )

private fun VedleggFrontend.toGenericOkonomiInput(opplysningType: OpplysningType) =
    GenericOkonomiInput(
        opplysningType = opplysningType,
        dokumentasjonLevert = alleredeLevert ?: false,
        detaljer =
            if (rader.isNullOrEmpty() || (rader.size == 1 && rader[0].allFieldsNull())) {
                emptyList()
            } else {
                rader.map {
                    BelopDto(beskrivelse = it.beskrivelse, belop = it.belop?.toDouble() ?: 0.0)
                }
            },
    )

private fun VedleggRadFrontend.allFieldsNull(): Boolean =
    listOf(beskrivelse, belop, brutto, netto, avdrag, renter).all { it == null }
