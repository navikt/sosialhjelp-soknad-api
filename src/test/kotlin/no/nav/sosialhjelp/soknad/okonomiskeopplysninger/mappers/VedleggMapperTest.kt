package no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.mappers.VedleggMapper.mapMellomlagredeVedleggToVedleggFrontend
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagretVedleggMetadata
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

    @Test
    internal fun `hotfix - mellomlagret vedlegg feilet men vedleggjson ble oppdatert`() {
        // behandlingsId i listen behandlingsIdsToPass i VedleggMapper
        val behandlingsId = "11001ZKC1"
        val bostotteVedlegg = JsonVedlegg()
            .withType("faktura")
            .withTilleggsinfo("husleie")
            .withStatus(Vedleggstatus.LastetOpp.toString())
            .withFiler(
                mutableListOf(
                    JsonFiler()
                        .withFilnavn("hubbabubba.jpg")
                        .withSha512("sha512"),
                    JsonFiler()
                        .withFilnavn("juicyfruit.pdf")
                        .withSha512("shasha512512")
                )
            )
        val mellomlagredeVedlegg = listOf(
            MellomlagretVedleggMetadata(filnavn = "hubbabubba.jpg", filId = "id123")
        )

        val jsonOkonomi = JsonOkonomi()
            .withOversikt(
                JsonOkonomioversikt()
                    .withUtgift(
                        mutableListOf()
                    )
            )
            .withOpplysninger(
                JsonOkonomiopplysninger()
                    .withUtgift(
                        mutableListOf()
                    )
            )

        val vedleggFrontend = mapMellomlagredeVedleggToVedleggFrontend(bostotteVedlegg, jsonOkonomi, mellomlagredeVedlegg, behandlingsId)
        assertThat(vedleggFrontend).isNotNull
        assertThat(vedleggFrontend.filer).hasSize(1)
        assertThat(vedleggFrontend.filer!![0].filNavn).isEqualTo("hubbabubba.jpg")
        assertThat(vedleggFrontend.filer!![0].uuid).isEqualTo("id123")
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
                .withStatus(Vedleggstatus.VedleggKreves.toString())
        )
    }

    private fun createOpplastetVedleggList(): List<OpplastetVedlegg> {
        return mutableListOf(
            createOpplastetVedlegg(BOSTOTTE),
            createOpplastetVedlegg(ANNET)
        )
    }

    private fun createOpplastetVedlegg(type: OpplastetVedleggType): OpplastetVedlegg {
        return OpplastetVedlegg(
            eier = EIER,
            vedleggType = type,
            data = byteArrayOf(1, 2, 3),
            soknadId = 123L,
            filnavn = "FILNAVN",
            sha512 = "sha512"
        )
    }

    companion object {
        private val BOSTOTTE = OpplastetVedleggType("bostotte|annetboutgift")
        private val SKATTEMELDING = OpplastetVedleggType("skatt|melding")
        private val ANNET = OpplastetVedleggType("annet|annet")
        private const val EIER = "12345678910"
    }
}
