package no.nav.sosialhjelp.soknad.v2.okonomi

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

// TODO 1. For f.eks. annen boutgift og annen barneutgift - skal frontend oppdatere for hver eneste bokstav/skrivepause...
// TODO... eller holder det kanskje å gjøre det når skrivefelt mister fokus? - Tore

@RestController
@ProtectionSelvbetjeningHigh
@RequestMapping("/soknad/{soknadId}/okonomiskeOpplysninger")
class OkonomiskeOpplysningerController(
    private val okonomiskeOpplysningerService: OkonomiskeOpplysningerService,
) {
    // TODO Skal vi fortsette med denne "en kvern for alt"-løsningen, eller skal vi tenkte litt annerledes ?
    @GetMapping
    fun getForventetDokumentasjon(
        @PathVariable("soknadId") soknadId: UUID,
    ): ForventetDokumentasjonDto {
        return okonomiskeOpplysningerService.getForventetDokumentasjon(soknadId)
            .map { it.toDokumentasjonDto() }
            .let { dtos -> ForventetDokumentasjonDto(dtos) }
    }

    @PutMapping
    fun updateOkonomiskeDetaljer(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: AbstractOkonomiInput,
    ): ForventetDokumentasjonDto {
        okonomiskeOpplysningerService.updateOkonomiskeOpplysninger(
            soknadId = soknadId,
            type = input.getOpplysningType(),
            dokumentasjonLevert = input.dokumentasjonLevert,
            detaljer = input.mapToOkonomiDetalj(),
        )
        return getForventetDokumentasjon(soknadId)
    }
}

data class ForventetDokumentasjonDto(
    val forventetDokumentasjon: List<DokumentasjonDto>,
)

data class DokumentasjonDto(
    val type: OpplysningType,
    val gruppe: String,
    val detaljer: List<OkonomiDetaljDto>?,
    val dokumentasjonStatus: DokumentasjonStatus,
    val dokumenter: List<DokumentDto>,
)

data class DokumentDto(
    val dokumentId: UUID,
    val filnavn: String,
)

private fun Map.Entry<Dokumentasjon, List<OkonomiDetalj>>.toDokumentasjonDto(): DokumentasjonDto {
    return DokumentasjonDto(
        type = key.type,
        gruppe = key.type.group,
        dokumentasjonStatus = key.status,
        detaljer = value.map { dokumentasjon -> dokumentasjon.toOkonomiskDetaljDto() },
        dokumenter =
            key.dokumenter.map { dokument ->
                DokumentDto(dokumentId = dokument.dokumentId, filnavn = dokument.filnavn)
            },
    )
}

private fun OkonomiDetalj.toOkonomiskDetaljDto(): OkonomiDetaljDto {
    return when (this) {
        is Belop -> BelopDto(belop = belop, beskrivelse = beskrivelse)
        is BruttoNetto -> LonnsInntektDto(brutto = brutto, netto = netto)
        is AvdragRenter -> AvdragRenterDto(avdrag = avdrag, renter = renter)
        is Utbetaling -> BelopDto(belop = belop ?: 0.0)
        is UtbetalingMedKomponent -> BelopDto(belop = utbetaling.belop ?: 0.0)
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(GenericOkonomiInput::class),
    JsonSubTypes.Type(LonnsInput::class),
    JsonSubTypes.Type(BoliglanInput::class),
)
sealed interface AbstractOkonomiInput {
    val dokumentasjonLevert: Boolean
}

// For de fleste felter hvor bruker legger til okonomiske opplysninger
data class GenericOkonomiInput(
    val opplysningType: OpplysningType,
    override val dokumentasjonLevert: Boolean,
    val detaljer: List<BelopDto>,
) : AbstractOkonomiInput

// Hvis bruker ikke har samtykket til å hente lønnsinntekt, kan vedkommende fylle ut selv.
data class LonnsInput(
    override val dokumentasjonLevert: Boolean,
    val detalj: LonnsInntektDto,
) : AbstractOkonomiInput

// For boliglån hentes det inn ett eller flere renter og avdrag-par.
data class BoliglanInput(
    override val dokumentasjonLevert: Boolean,
    val detaljer: List<AvdragRenterDto>,
) : AbstractOkonomiInput

// TODO Navngivning på disse dtos og inputs?
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(BelopDto::class),
    JsonSubTypes.Type(LonnsInntektDto::class),
    JsonSubTypes.Type(AvdragRenterDto::class),
)
sealed interface OkonomiDetaljDto

// Brukes for mesteparten av de opplysningene bruker oppdaterer
data class BelopDto(
    val beskrivelse: String? = null,
    val belop: Double,
) : OkonomiDetaljDto

// hvis bruker ikke velger å hente lønn via skatteetaten, kan vedkommende fylle inn selv
data class LonnsInntektDto(
    val brutto: Double?,
    val netto: Double?,
) : OkonomiDetaljDto

data class AvdragRenterDto(
    val avdrag: Double?,
    val renter: Double?,
) : OkonomiDetaljDto

private fun AbstractOkonomiInput.getOpplysningType(): OpplysningType =
    when (this) {
        is GenericOkonomiInput -> opplysningType
        is LonnsInput -> InntektType.JOBB
        is BoliglanInput -> UtgiftType.UTGIFTER_BOLIGLAN
    }

private fun AbstractOkonomiInput.mapToOkonomiDetalj(): List<OkonomiDetalj> =
    when (this) {
        is GenericOkonomiInput -> detaljer.map { Belop(belop = it.belop, beskrivelse = it.beskrivelse) }
        is LonnsInput -> listOf(BruttoNetto(brutto = detalj.brutto, netto = detalj.netto))
        is BoliglanInput -> detaljer.map { AvdragRenter(it.avdrag, it.renter) }
    }
