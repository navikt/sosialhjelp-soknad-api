package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.metrics.Vedleggstatus
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentRef
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.getVedleggTillegginfoString
import no.nav.sosialhjelp.soknad.v2.json.getVedleggTypeString
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.utgift.UtgiftType
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
        .withStatus(status.toVedleggStatusString())
        .withTilleggsinfo(mapToTilleggsinfo())
        .withFiler(dokumenter.map { it.toJsonFiler() })
        .withHendelseType(if (type.isUtgiftTypeAnnet()) JsonVedlegg.HendelseType.BRUKER else JsonVedlegg.HendelseType.SOKNAD)
        // TODO Hvordan ønsker vi å benytte denne referansen... Altså hva skal den peke på?
        .withHendelseReferanse(if (type.isUtgiftTypeAnnet()) null else UUID.randomUUID().toString())

// TODO Midlertidig mapping til VedleggStatus
internal fun DokumentasjonStatus.toVedleggStatusString(): String =
    when (this) {
        DokumentasjonStatus.LASTET_OPP -> Vedleggstatus.LastetOpp.name
        DokumentasjonStatus.FORVENTET -> Vedleggstatus.VedleggKreves.name
        DokumentasjonStatus.LEVERT_TIDLIGERE -> Vedleggstatus.VedleggAlleredeSendt.name
    }

private fun Dokumentasjon.mapToTilleggsinfo(): String {
    // TODO Se hva denne skal/bør inneholde etter vi forhåpentligvis har gått over til felles typer for elementer
    return type.getVedleggTillegginfoString()
        ?: error("Mangler mapping for vedleggType.tilleggsinfo: $type")
}

private fun DokumentRef.toJsonFiler() =
    JsonFiler()
        .withFilnavn(filnavn)

private fun OpplysningType.isUtgiftTypeAnnet() = this == UtgiftType.UTGIFTER_ANDRE_UTGIFTER
