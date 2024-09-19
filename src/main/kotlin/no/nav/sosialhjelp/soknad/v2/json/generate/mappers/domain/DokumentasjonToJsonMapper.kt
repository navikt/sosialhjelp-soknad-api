package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokument
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.getJsonVerdier
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DokumentasjonToJsonMapper(
    private val dokumentasjonRepository: DokumentasjonRepository,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        doMapping(dokumentasjonRepository.findAllBySoknadId(soknadId), jsonInternalSoknad)
    }

    internal companion object Mapper {
        fun doMapping(
            dokumentasjonList: List<Dokumentasjon>,
            json: JsonInternalSoknad,
        ) {
            json.vedlegg ?: json.withVedlegg(JsonVedleggSpesifikasjon())

            json.vedlegg.withVedlegg(dokumentasjonList.map { it.toJsonVedlegg() })
        }
    }
}

private fun Dokumentasjon.toJsonVedlegg() =
    JsonVedlegg()
        .withType(type.getVedleggTypeString())
        .withStatus(status.name)
        .withTilleggsinfo(mapToTilleggsinfo())
        .withFiler(dokumenter.map { it.toJsonFiler() })
        .withHendelseType(JsonVedlegg.HendelseType.BRUKER)
        // TODO Hvordan ønsker vi å benytte denne referansen... Altså hva skal den peke på?
        .withHendelseReferanse(UUID.randomUUID().toString())

private fun Dokumentasjon.mapToTilleggsinfo(): String {
    // TODO Se hva denne skal/bør inneholde etter vi forhåpentligvis har gått over til felles typer for elementer
    return type.getJsonVerdier().vedleggType?.getTilleggsinfoString()
        ?: error("Mangler mapping for vedleggType.tilleggsinfo: $type")
}

private fun Dokument.toJsonFiler() =
    JsonFiler()
        .withFilnavn(filnavn)
        .withSha512(sha512)

private fun OpplysningType.getVedleggTypeString(): String =
    this.getJsonVerdier().vedleggType?.getTypeString()
        ?: error("Manglende mapping for VedleggType: $this")
