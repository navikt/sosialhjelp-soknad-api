package no.nav.sosialhjelp.soknad.okonomiskeopplysninger

import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggGruppe
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggRadFrontend
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggStatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggTypeToSoknadTypeMapper
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.AnnenDokumentasjonTypeDto
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonController
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonDto
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonInput
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.FormueTypeDto
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.InntektTypeDto
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.OpplysningTypeDto
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.UtgiftTypeDto
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.toDto
import no.nav.sosialhjelp.soknad.v2.json.getJsonVerdier
import no.nav.sosialhjelp.soknad.v2.okonomi.AbstractOkonomiInput
import no.nav.sosialhjelp.soknad.v2.okonomi.AvdragRenterDto
import no.nav.sosialhjelp.soknad.v2.okonomi.BelopDto
import no.nav.sosialhjelp.soknad.v2.okonomi.BoliglanInput
import no.nav.sosialhjelp.soknad.v2.okonomi.GenericOkonomiInput
import no.nav.sosialhjelp.soknad.v2.okonomi.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.LonnsInntektDto
import no.nav.sosialhjelp.soknad.v2.okonomi.LonnsInput
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiDetaljDto
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiOpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiService
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeOpplysningerController
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiskeOpplysningerDto
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import no.nav.sosialhjelp.soknad.vedlegg.dto.DokumentUpload
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OkonomiskeOpplysningerProxy(
    private val okonomiskeOpplysningerController: OkonomiskeOpplysningerController,
    private val dokumentasjonController: DokumentasjonController,
    private val okonomiService: OkonomiService,
) {
    fun getOkonomiskeOpplysninger(behandlingsId: String): VedleggFrontends {
        val forventetDokumentasjon = dokumentasjonController.getForventetDokumentasjon(UUID.fromString(behandlingsId))
        val okonomiskeOpplysningerForTyper =
            okonomiskeOpplysningerController.getOkonomiskeOpplysninger(soknadId = UUID.fromString(behandlingsId))

        return forventetDokumentasjon.dokumentasjon.toVedleggFrontends(
            okonomiskeOpplysningerForTyper = okonomiskeOpplysningerForTyper,
            isOpplysningerBekreftet = isOpplysningerBekreftet(behandlingsId),
        )
    }

    private fun List<DokumentasjonDto>.toVedleggFrontends(
        okonomiskeOpplysningerForTyper: OkonomiskeOpplysningerDto,
        isOpplysningerBekreftet: Boolean,
    ): VedleggFrontends {
        return VedleggFrontends(
            isOkonomiskeOpplysningerBekreftet = isOpplysningerBekreftet,
            slettedeVedlegg = null,
            okonomiskeOpplysninger = this.map { it.toVedleggFrontend(okonomiskeOpplysningerForTyper) },
        )
    }

    fun updateOkonomiskeOpplysninger(
        behandlingsId: String,
        vedleggFrontend: VedleggFrontend,
    ) {
        dokumentasjonController.updateDokumentasjonStatus(
            soknadId = UUID.fromString(behandlingsId),
            input =
                DokumentasjonInput(
                    type = vedleggFrontend.type.opplysningType?.toDto() ?: error("Manglende mapping for VedleggType -> OpplysningType"),
                    hasLevert = vedleggFrontend.alleredeLevert ?: false,
                ),
        )

        okonomiskeOpplysningerController.updateOkonomiskOpplysning(
            soknadId = UUID.fromString(behandlingsId),
            input = vedleggFrontend.resolveOkonomiInput(),
        )
    }

    private fun isOpplysningerBekreftet(behandlingsId: String): Boolean {
        return okonomiService.getBekreftelser(UUID.fromString(behandlingsId)).isNotEmpty()
    }
}

private fun DokumentasjonDto.toVedleggFrontend(okonomiskeOpplysningerForTyper: OkonomiskeOpplysningerDto): VedleggFrontend {
    val vedleggType: VedleggType = type.getVedleggType() ?: error("Mangler type for mapping til VedleggType")

    val detaljer = okonomiskeOpplysningerForTyper.opplysninger.find { it.type == type }?.detaljer

    return VedleggFrontend(
        type = vedleggType,
        alleredeLevert = dokumentasjonStatus == DokumentasjonStatus.LEVERT_TIDLIGERE,
        rader = detaljer.resolveRader(vedleggType),
        gruppe = type.getVedleggGruppe(),
        vedleggStatus = dokumentasjonStatus.toVedleggStatus(),
        filer = dokumenter.map { DokumentUpload(it.filnavn, it.dokumentId.toString()) },
    )
}

private fun OpplysningTypeDto.getVedleggType(): VedleggType? =
    when (this) {
        is InntektTypeDto -> this.value.getJsonVerdier().vedleggType
        is UtgiftTypeDto -> this.value.getJsonVerdier().vedleggType
        is FormueTypeDto -> this.value.getJsonVerdier().vedleggType
        is AnnenDokumentasjonTypeDto -> this.value.getJsonVerdier().vedleggType
    }

private fun OpplysningTypeDto.getVedleggGruppe(): VedleggGruppe =
    when (this) {
        is InntektTypeDto -> this.value.group
        is UtgiftTypeDto -> this.value.group
        is FormueTypeDto -> this.value.group
        is AnnenDokumentasjonTypeDto -> this.value.group
    }

// TODO Forklaring: Hvis det ikke sendes med tomt element - rendres ikke input-feltet
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
        detalj = rader?.first().let { LonnsInntektDto(brutto = it?.brutto?.toDouble(), netto = it?.netto?.toDouble()) },
    )

private fun VedleggFrontend.toBoliglanInput() =
    BoliglanInput(
        detaljer = rader?.map { AvdragRenterDto(it.avdrag?.toDouble(), it.renter?.toDouble()) } ?: emptyList(),
    )

private fun VedleggFrontend.toGenericOkonomiInput(type: OkonomiOpplysningType) =
    GenericOkonomiInput(
        okonomiOpplysningType = type,
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
