package no.nav.sosialhjelp.soknad.v2.okonomi

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokument
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
// TODO... eller holder det kanskje å gjøre det når skrivefelt mister fokus?
// TODO 2. Nå håndteres andre utgifter forskjellig - FORMUE_ANNET og VERDI_ANNET innhenter beskrivelse i første dialog,
// TODO... men UTGIFTER_ANNET_BO, UTGIFTER_ANNET_BARN og UTGIFTER_ANDRE_UTGIFTER har beskrivelse pr. okonomiske detalj.
// TODO... Litt vanskelig å håndtere det likt fordi beskrivelse for sistnevnte er knyttet til beløpet, mens første er
// TODO... knyttet til elementet før beløp er hentet inn... La det være sånn - eller prøve finne en felles måte?
// TODO 3. Skal vi ha en "one takes all"-løsning som det er nå, eller type det basert på belop/brutto-netto/avdrag-renter?
// TODO ...Renter og avdrag er kun en utgift - og frontend kan sjekke type for å tilpasse teksten, og vi kan fortsatt forholde oss til belop internt

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
            type = input.getType(),
            dokumentasjonLevert = input.dokumentasjonLevert,
            detaljer = input.mapToOkonomiDetalj(),
        )
        return getForventetDokumentasjon(soknadId)
    }
}

private fun AbstractOkonomiInput.getType(): OkonomiType =
    when (this) {
        is GenericOkonomiInput -> type
        is LonnsInput -> InntektType.JOBB
        is BoliglanInput -> UtgiftType.UTGIFTER_BOLIGLAN
    }

private fun AbstractOkonomiInput.mapToOkonomiDetalj(): List<OkonomiDetalj> =
    when (this) {
        is GenericOkonomiInput -> detaljer.map { it.toOkonomiDetalj() }
        is LonnsInput -> listOf(detalj.toOkonomiDetalj())
        is BoliglanInput -> detaljer.map { it.toOkonomiDetalj() }
    }

private fun OkonomiDetaljDto.toOkonomiDetalj(): OkonomiDetalj {
    return when (this) {
        is BelopDto -> Belop(belop = belop, beskrivelse = beskrivelse)
        is LonnsInntektDto -> BruttoNetto(brutto = brutto, netto = netto)
        is AvdragRenterDto -> AvdragRenter(avdrag = avdrag, renter = renter)
    }
}

data class ForventetDokumentasjonDto(
    val forventetDokumentasjon: List<DokumentasjonDto>,
)

data class DokumentasjonDto(
    val type: OkonomiType,
    val gruppe: String,
    val detaljer: List<OkonomiDetaljDto>?,
    val dokumentasjonStatus: DokumentasjonStatus,
    val dokumenter: List<DokumentDto>,
)

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

data class DokumentDto(
    val uuid: UUID,
    val filnavn: String,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(
    JsonSubTypes.Type(GenericOkonomiInput::class),
    JsonSubTypes.Type(LonnsInput::class),
    JsonSubTypes.Type(BoliglanInput::class),
)
sealed interface AbstractOkonomiInput {
    val dokumentasjonLevert: Boolean
}

data class GenericOkonomiInput(
    val type: OkonomiType,
    override val dokumentasjonLevert: Boolean,
    val detaljer: List<BelopDto>,
) : AbstractOkonomiInput

data class LonnsInput(
    override val dokumentasjonLevert: Boolean,
    val detalj: LonnsInntektDto,
) : AbstractOkonomiInput

data class BoliglanInput(
    override val dokumentasjonLevert: Boolean,
    val detaljer: List<AvdragRenterDto>,
) : AbstractOkonomiInput

private fun Map.Entry<Dokumentasjon, List<OkonomiDetalj>>.toDokumentasjonDto(): DokumentasjonDto {
    return DokumentasjonDto(
        type = key.type,
        gruppe = key.type.group,
        dokumentasjonStatus = key.status,
        detaljer = value.map { it.toOkonomiskDetaljDto() },
        dokumenter = key.dokumenter.map { it.toDokumentDto() },
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

private fun Dokument.toDokumentDto() =
    DokumentDto(
        uuid = dokumentId,
        filnavn = filnavn,
    )
