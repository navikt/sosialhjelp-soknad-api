package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.ForventetDokumentasjonService
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
    private val forventetDokumentasjonService: ForventetDokumentasjonService,
    private val updateOkonomiService: UpdateOkonomiService,
) {
    // TODO Skal vi fortsette med denne "en kvern for alt"-løsningen, eller skal vi tenkte litt annerledes ?
    @GetMapping
    fun getForventetDokumentasjon(
        @PathVariable("soknadId") soknadId: UUID,
    ): ForventetDokumentasjonDto {
        return forventetDokumentasjonService.getForventetDokumentasjon(soknadId)
    }

    @PutMapping
    fun updateOkonomiskeDetaljer(
        @PathVariable("soknadId") soknadId: UUID,
        @RequestBody input: OkonomiskeOpplysningerInput,
    ): ForventetDokumentasjonDto {
        // TODO OkonomiElement for Andre Utgifter "må" opprettes her, da det kun finnes dialog på side 8

        return forventetDokumentasjonService.getForventetDokumentasjon(soknadId)
    }
}

data class ForventetDokumentasjonDto(
    val forventetDokumentasjon: List<DokumentasjonDto>,
)

data class DokumentasjonDto(
    val type: OkonomiType,
    val detaljer: List<OkonomiskeDetaljerDto>?,
    val dokumentasjonStatus: DokumentasjonStatus,
    val dokumenter: List<DokumentDto>,
)

// TODO Skal vi ha en "one takes all"-løsning som det er nå, eller type det basert på belop/brutto-netto/avdrag-renter?
// TODO Renter og avdrag er kun en utgift - og frontend kan sjekke type for å tilpasse teksten, og vi kan fortsatt forholde oss til belop internt
data class OkonomiskeDetaljerDto(
    val beskrivelse: String? = null,
    val belop: Double? = null,
    val brutto: Double? = null,
    val netto: Double? = null,
)

data class DokumentDto(
    val uuid: String,
    val filnavn: String,
)

data class OkonomiskeOpplysningerInput(
    val type: OkonomiType,
    val dokumentasjonLevert: Boolean = false,
    val inputList: List<OkonomiOpplysningInput> = emptyList(),
)

data class OkonomiOpplysningInput(
    val beskrivelse: String? = null,
    val belop: Double? = null,
    val brutto: Double? = null,
    val netto: Double? = null,
)

//
// @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
// @JsonSubTypes(
//    JsonSubTypes.Type(BelopInput::class),
//    JsonSubTypes.Type(BruttoNettoInput::class),
// )
// interface OkonomiskeOpplysningerInput {
//    val type: OkonomiType
//    val beskrivelse: String?
// }
//
// data class BelopInput(
//    override val type: OkonomiType,
//    override val beskrivelse: String? = null,
//    val belopList: List<Double>
// ): OkonomiskeOpplysningerInput
//
// data class BruttoNettoInput(
//    override val type: OkonomiType,
//    override val beskrivelse: String? = null,
//    val bruttoNettoList: List<BruttoNettoPair>
// ): OkonomiskeOpplysningerInput
//
// data class BruttoNettoPair(
//    val brutto: Double?,
//    val netto: Double?
// )
