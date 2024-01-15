package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class VedleggMapperTest {

    @Test
    fun skalReturnereAlleVedleggSomSortertListeAvEttersendteVedleggHvisSoknadBleSendtForMindreEnn30DagerSiden() {
        val innsendingstidspunkt = LocalDateTime.now()
        val opplastedeVedlegg = createOpplastetVedleggList()
        val originaleVedlegg = createOriginaleVedlegg()
        val result = VedleggMapper.mapVedleggToSortedListOfEttersendteVedlegg(
            innsendingstidspunkt,
            opplastedeVedlegg,
            originaleVedlegg,
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
            originaleVedlegg,
        )
        assertThat(result).hasSize(2)
        assertThat(result[0].type).isEqualTo(BOSTOTTE.sammensattType)
        assertThat(result[0].vedleggStatus).isEqualTo(Vedleggstatus.LastetOpp.toString())
        assertThat(result[1].type).isEqualTo(ANNET.sammensattType)
        assertThat(result[1].vedleggStatus).isEqualTo(Vedleggstatus.VedleggKreves.toString())
    }

    private fun createOriginaleVedlegg(): List<JsonVedlegg> {
        return mutableListOf(
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
                .withStatus(Vedleggstatus.VedleggKreves.toString()),
        )
    }

    private fun createOpplastetVedleggList(): List<OpplastetVedlegg> {
        return mutableListOf(
            createOpplastetVedlegg(BOSTOTTE),
            createOpplastetVedlegg(ANNET),
        )
    }

    private fun createOpplastetVedlegg(type: OpplastetVedleggType): OpplastetVedlegg {
        return OpplastetVedlegg(
            eier = EIER,
            vedleggType = type,
            data = byteArrayOf(1, 2, 3),
            soknadId = 123L,
            filnavn = "FILNAVN",
            sha512 = "sha512",
        )
    }

    companion object {
        private val BOSTOTTE = OpplastetVedleggType("husbanken|vedtak")
        private val SKATTEMELDING = OpplastetVedleggType("skattemelding|skattemelding")
        private val ANNET = OpplastetVedleggType("annet|annet")
        private const val EIER = "12345678910"
    }
}
