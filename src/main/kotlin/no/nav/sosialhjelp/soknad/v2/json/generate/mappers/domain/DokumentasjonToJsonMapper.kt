package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.domain

import io.getunleash.Unleash
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.metrics.Vedleggstatus
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentRef
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.Dokumentasjon
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonStatus
import no.nav.sosialhjelp.soknad.v2.dokumentasjon.UploadClient
import no.nav.sosialhjelp.soknad.v2.json.generate.DomainToJsonMapper
import no.nav.sosialhjelp.soknad.v2.json.getVedleggTillegginfoString
import no.nav.sosialhjelp.soknad.v2.json.getVedleggTypeString
import no.nav.sosialhjelp.soknad.v2.okonomi.OpplysningType
import no.nav.sosialhjelp.soknad.v2.okonomi.UtgiftType
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class DokumentasjonToJsonMapper(
    private val dokumentasjonRepository: DokumentasjonRepository,
    private val uploadClient: UploadClient,
    private val unleash: Unleash,
) : DomainToJsonMapper {
    override fun mapToJson(
        soknadId: UUID,
        jsonInternalSoknad: JsonInternalSoknad,
    ) {
        val dokumentasjonList = dokumentasjonRepository.findAllBySoknadId(soknadId)
        if (unleash.isEnabled("sosialhjelp.soknad.tusUpload", false)) {
            val uploadVedlegg = uploadClient.getVedleggSpesifikasjon(soknadId)
            doMapping(dokumentasjonList, uploadVedlegg, jsonInternalSoknad)
        } else {
            doMappingLegacy(dokumentasjonList, jsonInternalSoknad)
        }
    }

    internal companion object Mapper {
        private val log by logger()

        fun doMapping(
            dokumentasjonList: List<Dokumentasjon>,
            uploadVedlegg: JsonVedleggSpesifikasjon,
            json: JsonInternalSoknad,
        ) {
            json.vedlegg ?: json.withVedlegg(JsonVedleggSpesifikasjon())

            val uploadByKey =
                uploadVedlegg.vedlegg
                    .orEmpty()
                    .associateBy { VedleggKey(it.type, it.tilleggsinfo) }

            val localKeys = mutableSetOf<VedleggKey>()

            val mergedVedlegg =
                dokumentasjonList.map { dokumentasjon ->
                    val key =
                        VedleggKey(
                            type = dokumentasjon.type.getVedleggTypeString(),
                            tilleggsinfo = dokumentasjon.mapToTilleggsinfo(),
                        )
                    localKeys += key
                    uploadByKey[key] ?: dokumentasjon.toJsonVedleggWithoutFiler()
                }

            val extraFromUpload = uploadByKey.keys - localKeys
            if (extraFromUpload.isNotEmpty()) {
                // TODO: Avklar hva som skal skje med vedlegg fra upload som ikke finnes lokalt
                log.warn(
                    "Vedlegg fra sosialhjelp-upload finnes ikke i lokal DokumentasjonList: {}",
                    extraFromUpload.joinToString { "(type=${it.type}, tilleggsinfo=${it.tilleggsinfo})" },
                )
            }

            json.vedlegg.withVedlegg(mergedVedlegg + extraFromUpload.map { uploadByKey.getValue(it) })
        }

        fun doMappingLegacy(
            dokumentasjonList: List<Dokumentasjon>,
            json: JsonInternalSoknad,
        ) {
            json.vedlegg ?: json.withVedlegg(JsonVedleggSpesifikasjon())
            json.vedlegg.withVedlegg(dokumentasjonList.map { it.toJsonVedlegg() })
        }
    }
}

private data class VedleggKey(val type: String?, val tilleggsinfo: String?)

private fun Dokumentasjon.toJsonVedlegg() =
    JsonVedlegg()
        .withType(type.getVedleggTypeString())
        .withStatus(status.toVedleggStatusString())
        .withTilleggsinfo(mapToTilleggsinfo())
        .withFiler(dokumenter.map { it.toJsonFiler() })
        .withHendelseType(if (type.isUtgiftTypeAnnet()) JsonVedlegg.HendelseType.BRUKER else JsonVedlegg.HendelseType.SOKNAD)
        // TODO Hvordan ønsker vi å benytte denne referansen... Altså hva skal den peke på?
        .withHendelseReferanse(if (type.isUtgiftTypeAnnet()) null else UUID.randomUUID().toString())

private fun Dokumentasjon.toJsonVedleggWithoutFiler() =
    JsonVedlegg()
        .withType(type.getVedleggTypeString())
        .withStatus(status.toVedleggStatusString())
        .withTilleggsinfo(mapToTilleggsinfo())
        .withFiler(emptyList())
        .withHendelseType(if (type.isUtgiftTypeAnnet()) JsonVedlegg.HendelseType.BRUKER else JsonVedlegg.HendelseType.SOKNAD)
        .withHendelseReferanse(if (type.isUtgiftTypeAnnet()) null else UUID.randomUUID().toString())

internal fun DokumentasjonStatus.toVedleggStatusString(): String =
    when (this) {
        DokumentasjonStatus.LASTET_OPP -> Vedleggstatus.LastetOpp.name
        DokumentasjonStatus.FORVENTET -> Vedleggstatus.VedleggKreves.name
        DokumentasjonStatus.LEVERT_TIDLIGERE -> Vedleggstatus.VedleggAlleredeSendt.name
    }

private fun Dokumentasjon.mapToTilleggsinfo(): String {
    return type.getVedleggTillegginfoString()
        ?: error("Mangler mapping for vedleggType.tilleggsinfo: $type")
}

private fun DokumentRef.toJsonFiler() =
    JsonFiler()
        .withFilnavn(filnavn)

private fun OpplysningType.isUtgiftTypeAnnet() = this == UtgiftType.UTGIFTER_ANDRE_UTGIFTER
