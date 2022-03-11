package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.OpplastetVedleggType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Arrays

internal class VedleggMapperTest {

    @Test
    fun skalReturnereAlleVedleggSomSortertListeAvEttersendteVedleggHvisSoknadBleSendtForMindreEnn30DagerSiden() {
        val innsendingstidspunkt = LocalDateTime.now()
        val opplastedeVedlegg = createOpplastetVedleggList()
        val originaleVedlegg = createOriginaleVedlegg()
        val result = VedleggMapper.mapVedleggToSortedListOfEttersendteVedlegg(
            innsendingstidspunkt,
            opplastedeVedlegg,
            originaleVedlegg
        )
        assertThat(result).hasSize(3)
        assertThat(result[0].type).isEqualTo(BOSTOTTE.sammensattType)
        assertThat(result[0].vedleggStatus).isEqualTo(Vedleggstatus.LastetOpp.toString())
        assertThat(result[1].type).isEqualTo(SKATTEMELDING.sammensattType)
        assertThat(result[1].vedleggStatus).isEqualTo(Vedleggstatus.VedleggKreves.toString())
        assertThat(result[2].type).isEqualTo(ANNET.sammensattType)
        assertThat(result[2].vedleggStatus).isEqualTo(Vedleggstatus.VedleggKreves.toString())
    }

    @Test
    fun skalKunReturnereAnnetOgLastetOppHvisSoknadBleSendtForMerEnn30DagerSiden() {
        val innsendingstidspunkt = LocalDateTime.now().minusDays(31)
        val opplastedeVedlegg = createOpplastetVedleggList()
        val originaleVedlegg = createOriginaleVedlegg()
        val result = VedleggMapper.mapVedleggToSortedListOfEttersendteVedlegg(
            innsendingstidspunkt,
            opplastedeVedlegg,
            originaleVedlegg
        )
        assertThat(result).hasSize(2)
        assertThat(result[0].type).isEqualTo(BOSTOTTE.sammensattType)
        assertThat(result[0].vedleggStatus).isEqualTo(Vedleggstatus.LastetOpp.toString())
        assertThat(result[1].type).isEqualTo(ANNET.sammensattType)
        assertThat(result[1].vedleggStatus).isEqualTo(Vedleggstatus.VedleggKreves.toString())
    }

    private fun createOriginaleVedlegg(): List<JsonVedlegg> {
        return Arrays.asList(
            JsonVedlegg()
                .withType(BOSTOTTE.type)
                .withTilleggsinfo(BOSTOTTE.tilleggsinfo)
                .withStatus(Vedleggstatus.LastetOpp.toString()),
            JsonVedlegg()
                .withType(SKATTEMELDING.type)
                .withTilleggsinfo(SKATTEMELDING.tilleggsinfo)
                .withStatus(Vedleggstatus.VedleggKreves.toString()),
            JsonVedlegg()
                .withType(ANNET.type)
                .withTilleggsinfo(ANNET.tilleggsinfo)
                .withStatus(Vedleggstatus.VedleggKreves.toString())
        )
    }

    private fun createOpplastetVedleggList(): List<OpplastetVedlegg> {
        val opplastedeVedlegg: MutableList<OpplastetVedlegg> = ArrayList()
        opplastedeVedlegg.add(createOpplastetVedlegg(BOSTOTTE))
        opplastedeVedlegg.add(createOpplastetVedlegg(ANNET))
        return opplastedeVedlegg
    }

    private fun createOpplastetVedlegg(type: OpplastetVedleggType): OpplastetVedlegg {
        return OpplastetVedlegg()
            .withVedleggType(type)
            .withEier(EIER)
    }

    companion object {
        private val BOSTOTTE =
            OpplastetVedleggType("bostotte|annetboutgift")
        private val SKATTEMELDING =
            OpplastetVedleggType("skatt|melding")
        private val ANNET = OpplastetVedleggType("annet|annet")
        private const val EIER = "12345678910"
    }
}
