package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokument
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
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
        dokumentasjonRepository.findAllBySoknadId(soknadId).let { list -> doMapping(list, jsonInternalSoknad) }
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
        .withType(type.name)
        .withStatus(status.name)
        .withTilleggsinfo(mapToTilleggsinfo())
        .withFiler(dokumenter.map { it.toJsonFiler() })
        .withHendelseType(JsonVedlegg.HendelseType.BRUKER)
        // TODO Hvordan ønsker vi å benytte denne referansen... Altså hva skal den peke på?
        .withHendelseReferanse(UUID.randomUUID().toString())

private fun Dokumentasjon.mapToTilleggsinfo(): String {
    // TODO må sammenstille svarene fra FSL'er og se hva de forventer / trenger på tilleggsinfo hvis vi benytter type..
    // TODO ...til å knytte sammen OkonomiElementer og Dokumentasjon.
    return ""
}

private fun Dokument.toJsonFiler() =
    JsonFiler()
        .withFilnavn(filnavn)
        .withSha512(sha512)
