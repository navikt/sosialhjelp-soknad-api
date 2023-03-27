package no.nav.sosialhjelp.soknad.innsending

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.VedleggMetadata
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import java.util.UUID

object JsonVedleggUtils {
    const val ANNET = "annet"

    fun getVedleggFromInternalSoknad(soknadUnderArbeid: SoknadUnderArbeid): MutableList<JsonVedlegg> {
        return soknadUnderArbeid.jsonInternalSoknad?.vedlegg?.vedlegg ?: mutableListOf()
    }

    fun isVedleggskravAnnet(vedlegg: VedleggMetadata): Boolean {
        return ANNET == vedlegg.skjema && ANNET == vedlegg.tillegg
    }

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

    private fun isVedleggskravAnnet(vedlegg: JsonVedlegg): Boolean {
        return ANNET == vedlegg.type && ANNET == vedlegg.tilleggsinfo
    }
}
