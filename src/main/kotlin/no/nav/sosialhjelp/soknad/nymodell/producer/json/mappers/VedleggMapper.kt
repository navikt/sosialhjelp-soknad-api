//package no.nav.sosialhjelp.soknad.nymodell.fullfort.mappers
//
//import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
//import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
//import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
//import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
//import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.FilMeta
//import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.Vedlegg
//import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.repository.FilMetaRepository
//import no.nav.sosialhjelp.soknad.nymodell.domene.vedlegg.repository.VedleggRepository
//import org.springframework.stereotype.Component
//import java.util.*
//
//@Component
//class VedleggMapper (
//    private val vedleggRepository: VedleggRepository,
//    private val filMetaRepository: FilMetaRepository
//): DomainToJsonMapper {
//    override fun mapDomainToJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
//        val alleVedleggForSoknad = vedleggRepository.findAllBySoknadId(soknadId)
//
//        val jsonVedleggList = alleVedleggForSoknad
////            .map { it.toJsonVedlegg().withFiler(mapFilerToJsonObject(it.id)) }
//
//        jsonInternalSoknad.withVedlegg(
//            JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggList)
//        )
//    }
//
//    private fun mapFilerToJsonObject(vedleggId: Long): List<JsonFiler> {
//        val alleFilerForVedlegg = filMetaRepository.findAllByVedleggId(vedleggId)
//        return alleFilerForVedlegg.map { it.toJsonFiler() }
//    }
//}
//
//fun Vedlegg.toJsonVedlegg(): JsonVedlegg =
//    JsonVedlegg()
//        .withType(vedleggType.name)
//        .withTilleggsinfo(vedleggType.name)
//        .withStatus(status)
//        .withHendelseType(JsonVedlegg.HendelseType.valueOf(hendelseType.name))
//        .withHendelseReferanse(hendelseReferanse)
//
//fun FilMeta.toJsonFiler(): JsonFiler =
//    JsonFiler()
//        .withFilnavn(filnavn)
//        .withSha512(sha512)
