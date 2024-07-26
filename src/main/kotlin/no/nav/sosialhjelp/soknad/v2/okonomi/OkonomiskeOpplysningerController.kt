package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.app.annotation.ProtectionSelvbetjeningHigh
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokument
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
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
        @RequestBody input: OkonomiOgDokumentasjonInput,
    ): ForventetDokumentasjonDto {
        input.detaljer.let {
            if (it.isEmpty()) {
                okonomiskeOpplysningerService.updateOkonomiskeOpplysninger(soknadId, input.type, emptyList())
            } else {
                okonomiskeOpplysningerService.updateOkonomiskeOpplysninger(
                    soknadId = soknadId,
                    type = input.type,
                    detaljer =
                        it.map { detaljInput ->
                            if (detaljInput.belop != null) {
                                Belop(belop = detaljInput.belop)
                            } else {
                                BruttoNetto(brutto = detaljInput.brutto, netto = detaljInput.netto)
                            }
                        },
                )
            }
        }
        okonomiskeOpplysningerService.updateDokumentasjonStatus(soknadId, input.type, input.dokumentasjonLevert)

        return getForventetDokumentasjon(soknadId)
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

private fun Map.Entry<Dokumentasjon, List<OkonomiDetalj>>.toDokumentasjonDto(): DokumentasjonDto {
    return DokumentasjonDto(
        type = key.type,
        dokumentasjonStatus = key.status,
        detaljer = value.map { it.toOkonomiskeDetaljerDto() },
        dokumenter = key.dokumenter.map { it.toDokumentDto() },
    )
}

private fun OkonomiDetalj.toOkonomiskeDetaljerDto(): OkonomiskeDetaljerDto {
    return when (this) {
        is Belop -> OkonomiskeDetaljerDto(belop = belop)
        is BruttoNetto -> OkonomiskeDetaljerDto(brutto = brutto, netto = netto)
        is Utbetaling -> OkonomiskeDetaljerDto(belop = belop)
        is UtbetalingMedKomponent -> OkonomiskeDetaljerDto(belop = utbetaling.belop)
        else -> error("Ukjent type for OkonomiskeDetaljerDto")
    }
}

private fun Dokument.toDokumentDto() =
    DokumentDto(
        uuid = sha512,
        filnavn = filnavn,
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

data class OkonomiOgDokumentasjonInput(
    val type: OkonomiType,
    val dokumentasjonLevert: Boolean = false,
    val detaljer: List<OkonomiskDetaljInput> = emptyList(),
)

data class OkonomiskDetaljInput(
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
