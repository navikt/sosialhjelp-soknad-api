package no.nav.sosialhjelp.soknad.v2.okonomi

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.okonomi.formue.Formue
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.Inntekt
import no.nav.sosialhjelp.soknad.v2.okonomi.inntekt.InntektType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.Utgift
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
    @GetMapping
    fun getOkonomiskeOpplysninger(
        @PathVariable("soknadId") soknadId: UUID,
    ): OkonomiskeOpplysningerDto {
        return okonomiskeOpplysningerService.getOkonomiskeOpplysningerForTyper(soknadId)
            .map { it.toOkonomiskOpplysningDto() }
            .let { OkonomiskeOpplysningerDto(it) }
    }

    @PutMapping
    fun updateOkonomiskOpplysning(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: AbstractOkonomiInput,
    ): OkonomiskeOpplysningerDto {
        okonomiskeOpplysningerService.updateOkonomiskeOpplysninger(
            soknadId = soknadId,
            type = input.getOpplysningType(),
            detaljer = input.mapToOkonomiDetalj(),
        )
        return getOkonomiskeOpplysninger(soknadId)
    }
}

data class OkonomiskeOpplysningerDto(
    val opplysninger: List<OkonomiskOpplysningDto>,
)

data class OkonomiskOpplysningDto(
    val type: OpplysningType,
    val detaljer: List<OkonomiDetaljDto>?,
)

private fun OkonomiElement.toOkonomiskOpplysningDto(): OkonomiskOpplysningDto {
    return OkonomiskOpplysningDto(
        type = type,
        detaljer = this.mapFromOkonomiElement(),
    )
}

private fun OkonomiElement.mapFromOkonomiElement(): List<OkonomiDetaljDto> {
    return when (this) {
        is Inntekt -> this.inntektDetaljer.detaljer.map { it.toOkonomiskDetaljDto() }
        is Utgift -> this.utgiftDetaljer.detaljer.map { it.toOkonomiskDetaljDto() }
        is Formue -> this.formueDetaljer.detaljer.map { it.toOkonomiskDetaljDto() }
        else -> error("Ugyldig okonomi-element")
    }
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
sealed interface AbstractOkonomiInput

// For de fleste felter hvor bruker legger til okonomiske opplysninger
data class GenericOkonomiInput(
    val opplysningType: OpplysningType,
    val detaljer: List<BelopDto>,
) : AbstractOkonomiInput

// Hvis bruker ikke har samtykket til å hente lønnsinntekt, kan vedkommende fylle ut selv.
data class LonnsInput(
    val inntektType: InntektType = InntektType.JOBB,
    val detalj: LonnsInntektDto,
) : AbstractOkonomiInput

// For boliglån hentes det inn ett eller flere renter og avdrag-par.
data class BoliglanInput(
    val utgiftType: UtgiftType = UtgiftType.UTGIFTER_BOLIGLAN,
    val detaljer: List<AvdragRenterDto>,
) : AbstractOkonomiInput

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
