package no.nav.sosialhjelp.soknad.v2.okonomi

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping
import io.swagger.v3.oas.annotations.media.Schema
import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonType
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.toDokumentasjonType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

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
        return okonomiskeOpplysningerService.getOkonomiskeOpplysninger(soknadId)
            .map { it.toOkonomiskOpplysningDto() }
            .let { OkonomiskeOpplysningerDto(it) }
    }

    @PutMapping
    fun updateOkonomiskOpplysning(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: AbstractOkonomiInput,
    ): OkonomiskeOpplysningerDto {
        input.type.opplysningType.also { if (it !is OkonomiOpplysningType) error("$it er ikke en okonomiType") }

        okonomiskeOpplysningerService.updateOkonomiskeOpplysninger(
            soknadId = soknadId,
            type = input.type.opplysningType as OkonomiOpplysningType,
            detaljer = input.mapToOkonomiDetalj(),
        )
        return getOkonomiskeOpplysninger(soknadId)
    }
}

data class OkonomiskeOpplysningerDto(
    val opplysninger: List<OkonomiskOpplysningDto>,
)

data class OkonomiskOpplysningDto(
    val type: DokumentasjonType,
    val detaljer: List<OkonomiDetaljDto>?,
)

private fun OkonomiOpplysning.toOkonomiskOpplysningDto(): OkonomiskOpplysningDto {
    return OkonomiskOpplysningDto(
        type = type.toDokumentasjonType(),
        detaljer = this.mapFromOkonomiElement(),
    )
}

private fun OkonomiOpplysning.mapFromOkonomiElement(): List<OkonomiDetaljDto> {
    return when (this) {
        is Inntekt -> this.inntektDetaljer.detaljer.map { it.toOkonomiskDetaljDto() }
        is Utgift -> this.utgiftDetaljer.detaljer.map { it.toOkonomiskDetaljDto() }
        is Formue -> this.formueDetaljer.detaljer.map { it.toOkonomiskDetaljDto() }
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

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "_type",
)
@JsonSubTypes(
    JsonSubTypes.Type(GenericOkonomiInput::class),
    JsonSubTypes.Type(LonnsInput::class),
    JsonSubTypes.Type(BoliglanInput::class),
)
@Schema(
    discriminatorProperty = "_type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "GenericOkonomiInput", schema = GenericOkonomiInput::class),
        DiscriminatorMapping(value = "LonnsInput", schema = LonnsInput::class),
        DiscriminatorMapping(value = "BoliglanInput", schema = BoliglanInput::class),
    ],
    subTypes = [
        GenericOkonomiInput::class,
        LonnsInput::class,
        BoliglanInput::class,
    ],
)
sealed interface AbstractOkonomiInput {
    val type: DokumentasjonType
}

// For de fleste felter hvor bruker legger til okonomiske opplysninger
data class GenericOkonomiInput(
    override val type: DokumentasjonType,
    val detaljer: List<BelopDto>,
) : AbstractOkonomiInput

// Hvis bruker ikke har samtykket til å hente lønnsinntekt, kan vedkommende fylle ut selv.
data class LonnsInput(
    override val type: DokumentasjonType = DokumentasjonType.JOBB,
    val detalj: LonnsInntektDto,
) : AbstractOkonomiInput

// For boliglån hentes det inn ett eller flere renter og avdrag-par.
data class BoliglanInput(
    override val type: DokumentasjonType = DokumentasjonType.UTGIFTER_BOLIGLAN,
    val detaljer: List<AvdragRenterDto>,
) : AbstractOkonomiInput

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(BelopDto::class),
    JsonSubTypes.Type(LonnsInntektDto::class),
    JsonSubTypes.Type(AvdragRenterDto::class),
)
@Schema(
    discriminatorProperty = "type",
    discriminatorMapping = [
        DiscriminatorMapping(value = "BelopDto", schema = BelopDto::class),
        DiscriminatorMapping(value = "LonnsInntektDto", schema = LonnsInntektDto::class),
        DiscriminatorMapping(value = "AvdragRenterDto", schema = AvdragRenterDto::class),
    ],
    subTypes = [
        BelopDto::class,
        LonnsInntektDto::class,
        AvdragRenterDto::class,
    ],
)
sealed interface OkonomiDetaljDto

// Brukes for mesteparten av de opplysningene bruker oppdaterer
data class BelopDto(
    val beskrivelse: String? = null,
    val belop: Double? = null,
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

private fun AbstractOkonomiInput.mapToOkonomiDetalj(): List<OkonomiDetalj> =
    when (this) {
        is GenericOkonomiInput -> detaljer.map { Belop(belop = it.belop, beskrivelse = it.beskrivelse) }
        is LonnsInput -> listOf(BruttoNetto(brutto = detalj.brutto, netto = detalj.netto))
        is BoliglanInput -> detaljer.map { AvdragRenter(it.avdrag, it.renter) }
    }
