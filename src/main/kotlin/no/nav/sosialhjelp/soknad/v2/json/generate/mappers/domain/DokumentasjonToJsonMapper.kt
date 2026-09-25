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
    ): JsonInternalSoknad {
        val dokumentasjonList = dokumentasjonRepository.findAllBySoknadId(soknadId)
        if (unleash.isEnabled("sosialhjelp.soknad.tusUpload", false)) {
            val uploadVedlegg = uploadClient.getVedleggSpesifikasjon(soknadId)
            return doMapping(dokumentasjonList, uploadVedlegg, jsonInternalSoknad)
        } else {
            return doMappingLegacy(dokumentasjonList, jsonInternalSoknad)
        }
    }

    internal companion object Mapper {
        private val log by logger()

        fun doMapping(
            dokumentasjonList: List<Dokumentasjon>,
            uploadVedlegg: JsonVedleggSpesifikasjon,
            json: JsonInternalSoknad,
        ): JsonInternalSoknad {
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
                    val tusEntry = uploadByKey[key]
                    // TODO: Kan slettes når alle vedlegg er lastet opp via tus/upload. 14 dager etter release
                    val mergedFiler =
                        (tusEntry?.filer.orEmpty() + dokumentasjon.dokumenter.map { it.toJsonFiler() })
                            .distinctBy { it.filnavn }
                    (tusEntry ?: dokumentasjon.toJsonVedleggWithoutFiler()).copy(filer = mergedFiler)
                }

            val extraFromUpload = uploadByKey.keys - localKeys
            if (extraFromUpload.isNotEmpty()) {
                // TODO: Avklar hva som skal skje med vedlegg fra upload som ikke finnes lokalt. Må slettes i mellomlager?
                // Hva skjer hvis det er filer i mellomlager som ikke er nevnt i vedlegg.json?
                log.warn(
                    "Vedlegg fra sosialhjelp-upload finnes ikke i lokal DokumentasjonList: {}",
                    extraFromUpload.joinToString { "(type=${it.type}, tilleggsinfo=${it.tilleggsinfo})" },
                )
            }

            return json.copy(vedlegg = JsonVedleggSpesifikasjon(mergedVedlegg + extraFromUpload.map { uploadByKey.getValue(it) }))
        }

        fun doMappingLegacy(
            dokumentasjonList: List<Dokumentasjon>,
            json: JsonInternalSoknad,
        ): JsonInternalSoknad = json.copy(vedlegg = JsonVedleggSpesifikasjon(dokumentasjonList.map { it.toJsonVedlegg() }))
    }
}

private data class VedleggKey(val type: String?, val tilleggsinfo: String?)

private fun Dokumentasjon.toJsonVedlegg() =
    JsonVedlegg(type.getVedleggTypeString(), mapToTilleggsinfo(), null, status.toVedleggStatusString(), dokumenter.map { it.toJsonFiler() }, if (type.isUtgiftTypeAnnet()) JsonVedlegg.HendelseType.BRUKER else JsonVedlegg.HendelseType.SOKNAD, if (type.isUtgiftTypeAnnet()) null else UUID.randomUUID().toString())

private fun Dokumentasjon.toJsonVedleggWithoutFiler() =
    JsonVedlegg(type.getVedleggTypeString(), mapToTilleggsinfo(), null, status.toVedleggStatusString(), emptyList(), if (type.isUtgiftTypeAnnet()) JsonVedlegg.HendelseType.BRUKER else JsonVedlegg.HendelseType.SOKNAD, if (type.isUtgiftTypeAnnet()) null else UUID.randomUUID().toString())

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
    JsonFiler(filnavn)

private fun OpplysningType.isUtgiftTypeAnnet() = this == UtgiftType.UTGIFTER_ANDRE_UTGIFTER
