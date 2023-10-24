package no.nav.sosialhjelp.soknad.fullfort.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.fullfort.SoknadToJsonMapper
import no.nav.sosialhjelp.soknad.domene.soknad.Fil
import no.nav.sosialhjelp.soknad.domene.soknad.Vedlegg
import no.nav.sosialhjelp.soknad.domene.soknad.FilRepository
import no.nav.sosialhjelp.soknad.domene.soknad.VedleggRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class VedleggMapper (
    private val vedleggRepository: VedleggRepository,
    private val filRepository: FilRepository
): SoknadToJsonMapper {
    override fun mapToSoknadJson(soknadId: UUID, jsonInternalSoknad: JsonInternalSoknad) {
        val alleVedleggForSoknad = vedleggRepository.findAllBySoknadId(soknadId)

        val jsonVedleggList = alleVedleggForSoknad
            .map {
                it.toJsonObject()
                    .withFiler(mapFilerToJsonObject(it.id))
            }

        jsonInternalSoknad.withVedlegg(
            JsonVedleggSpesifikasjon().withVedlegg(jsonVedleggList)
        )
    }

    private fun mapFilerToJsonObject(vedleggId: Long): List<JsonFiler> {
        val alleFilerForVedlegg = filRepository.findAllByVedleggId(vedleggId)
        return alleFilerForVedlegg.map { it.toJsonObject() }
    }
}

fun Vedlegg.toJsonObject(): JsonVedlegg =
    JsonVedlegg()
        .withType(vedleggType.type)
        .withTilleggsinfo(vedleggType.tilleggsinfo)
        .withStatus(status)
        .withHendelseType(JsonVedlegg.HendelseType.valueOf(hendelseType.name))
        .withHendelseReferanse(hendelseReferanse)

fun Fil.toJsonObject(): JsonFiler =
    JsonFiler()
        .withFilnavn(filnavn)
        .withSha512(sha512)
