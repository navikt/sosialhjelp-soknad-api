package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import java.util.UUID

object JsonVedleggUtils {
    const val ANNET = "annet"

    fun getVedleggFromInternalSoknad(soknadUnderArbeid: SoknadUnderArbeid): MutableList<JsonVedlegg> =
        soknadUnderArbeid.jsonInternalSoknad?.vedlegg?.vedlegg ?: mutableListOf()

    /**
     * Sjekker om JsonVedlegg og VedleggFrontend er samme vedleggstype.
     * Frontend vedleggtype er satt sammen av JsonVedlegg type og tilleggsinfo.
     */
    fun isSameVedleggType(vedlegg: JsonVedlegg, frontendType: VedleggType) =
        frontendType.toString() == "${vedlegg.type}|${vedlegg.tilleggsinfo}"

    /** Returnerer vedleggene fra SoknadUnderArbeid som er av samme type som vedleggFrontend */
    fun vedleggByFrontendType(
        soknad: SoknadUnderArbeid,
        type: VedleggType,
    ): List<JsonVedlegg> =
        getVedleggFromInternalSoknad(soknad).filter { isSameVedleggType(it, type) }

    fun isVedleggskravAnnet(vedlegg: VedleggMetadata) = ANNET == vedlegg.skjema && ANNET == vedlegg.tillegg
    private fun isVedleggskravAnnet(vedlegg: JsonVedlegg) = ANNET == vedlegg.type && ANNET == vedlegg.tilleggsinfo

    fun addHendelseTypeAndHendelseReferanse(
        jsonVedleggSpesifikasjon: JsonVedleggSpesifikasjon,
        isSoknad: Boolean,
    ) {
        jsonVedleggSpesifikasjon.vedlegg.forEach {
            if (isVedleggskravAnnet(it)) {
                it.hendelseType = JsonVedlegg.HendelseType.BRUKER
            } else if (isSoknad) {
                it.hendelseType = JsonVedlegg.HendelseType.SOKNAD
                it.hendelseReferanse = UUID.randomUUID().toString()
            }
        }
    }
}
