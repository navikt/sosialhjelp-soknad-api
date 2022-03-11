package no.nav.sosialhjelp.soknad.innsending.svarut

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.JsonData
import no.nav.sbl.soknadsosialhjelp.soknad.JsonDriftsinformasjon
import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknad
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.db.repositories.sendtsoknad.SendtSoknad
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.domain.OpplastetVedlegg
import no.nav.sosialhjelp.soknad.domain.OpplastetVedleggType
import no.nav.sosialhjelp.soknad.domain.Vedleggstatus
import no.nav.sosialhjelp.soknad.innsending.InnsendingService
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.svarut.client.SvarUtService
import no.nav.sosialhjelp.soknad.pdf.SosialhjelpPdfGenerator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.time.LocalDateTime
import java.util.UUID

internal class FiksSenderTest {
    private val dokumentKrypterer: DokumentKrypterer = mockk()
    private val innsendingService: InnsendingService = mockk()
    private val sosialhjelpPdfGenerator: SosialhjelpPdfGenerator = mockk()
    private val svarUtService: SvarUtService = mockk()

    private var fiksSender: FiksSender? = null

    @BeforeEach
    fun setUp() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns false
        every { dokumentKrypterer.krypterData(any()) } returns byteArrayOf(3, 2, 1)
        val sendtSoknad = lagSendtSoknad()
        sendtSoknad.fiksforsendelseId = FIKSFORSENDELSE_ID
        every { innsendingService.finnSendtSoknadForEttersendelse(any()) } returns sendtSoknad
        every { innsendingService.hentSoknadUnderArbeid(any(), any()) } returns mockk()
        every { sosialhjelpPdfGenerator.generate(any(), any()) } returns byteArrayOf(1, 2, 3)
        every { sosialhjelpPdfGenerator.generateEttersendelsePdf(any(), any()) } returns byteArrayOf(1, 2, 3)
        every { sosialhjelpPdfGenerator.generateBrukerkvitteringPdf() } returns byteArrayOf(1, 2, 3)
        every { innsendingService.hentAlleOpplastedeVedleggForSoknad(any()) } returns emptyList()

        fiksSender = FiksSender(dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator, true, svarUtService)
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(MiljoUtils)
    }

    @Test
    fun createForsendelseSetterRiktigInfoPaForsendelsenMedKryptering() {
        every { innsendingService.hentSoknadUnderArbeid(any(), any()) } returns createSoknadUnderArbeid()

        val sendtSoknad = lagSendtSoknad()
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val forsendelse = fiksSender!!.createForsendelse(sendtSoknad, filnavnInputStreamMap)

        val adresse = forsendelse.mottaker.digitalAdresse
        assertThat(adresse.organisasjonsNummer).isEqualTo(ORGNUMMER)
        assertThat(forsendelse.mottaker.postAdresse.navn).isEqualTo(NAVENHETSNAVN)
        assertThat(forsendelse.avgivendeSystem).isEqualTo("digisos_avsender")
        assertThat(forsendelse.forsendelsesType).isEqualTo("nav.digisos")
        assertThat(forsendelse.eksternReferanse).isEqualTo(BEHANDLINGSID)
        assertThat(forsendelse.isKunDigitalLevering).isFalse
        assertThat(forsendelse.utskriftsKonfigurasjon.isTosidig).isTrue
        assertThat(forsendelse.isKryptert).isTrue
        assertThat(forsendelse.isKrevNiva4Innlogging).isTrue
        assertThat(forsendelse.svarPaForsendelse).isNull()
        assertThat(forsendelse.dokumenter).hasSize(5)
        assertThat(forsendelse.metadataFraAvleverendeSystem.dokumentetsDato).isNotNull
        verify(exactly = 5) { dokumentKrypterer.krypterData(any()) }
    }

    @Test
    fun opprettForsendelseSetterRiktigInfoPaForsendelsenUtenKryptering() {
        every { MiljoUtils.isNonProduction() } returns true
        every { MiljoUtils.environmentName } returns "test"
        fiksSender = FiksSender(dokumentKrypterer, innsendingService, sosialhjelpPdfGenerator, false, svarUtService)
        every { innsendingService.hentSoknadUnderArbeid(any(), any()) } returns createSoknadUnderArbeid()

        val sendtSoknad = lagSendtSoknad()
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val forsendelse = fiksSender!!.createForsendelse(sendtSoknad, filnavnInputStreamMap)

        assertThat(forsendelse.isKryptert).isFalse
        assertThat(forsendelse.isKrevNiva4Innlogging).isFalse
        verify(exactly = 0) { dokumentKrypterer.krypterData(any()) }
    }

    @Test
    fun createForsendelseSetterRiktigTittelForNySoknad() {
        every { innsendingService.hentSoknadUnderArbeid(any(), any()) } returns createSoknadUnderArbeid()

        val sendtSoknad = lagSendtSoknad()
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val forsendelse = fiksSender!!.createForsendelse(sendtSoknad, filnavnInputStreamMap)

        assertThat(forsendelse.tittel).isEqualTo(FiksSender.SOKNAD_TIL_NAV)
    }

    @Test
    fun createForsendelseSetterRiktigTittelForEttersendelse() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.tilknyttetBehandlingsId = "12345"
        soknadUnderArbeid.jsonInternalSoknad = lagInternalSoknadForEttersending()
        every { innsendingService.hentSoknadUnderArbeid(any(), any()) } returns soknadUnderArbeid

        val sendtSoknad = lagSendtSoknad()
        sendtSoknad.tilknyttetBehandlingsId = "12345"

        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val forsendelse = fiksSender!!.createForsendelse(sendtSoknad, filnavnInputStreamMap)

        assertThat(forsendelse.tittel).isEqualTo(FiksSender.ETTERSENDELSE_TIL_NAV)
    }

    @Test
    fun opprettForsendelseForEttersendelseUtenSvarPaForsendelseSkalFeile() {
        every { innsendingService.hentSoknadUnderArbeid(any(), any()) } returns createSoknadUnderArbeid()
        every { innsendingService.finnSendtSoknadForEttersendelse(any()) } returns lagSendtSoknad()

        val sendtEttersendelse = lagSendtEttersendelse()
        val filnavnInputStreamMap = HashMap<String, InputStream>()

        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { fiksSender!!.createForsendelse(sendtEttersendelse, filnavnInputStreamMap) }
    }

    @Test
    fun hentDokumenterFraSoknadReturnererFireDokumenterForSoknadUtenVedlegg() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val fiksDokumenter = fiksSender!!.hentDokumenterFraSoknad(createSoknadUnderArbeid(), filnavnInputStreamMap)
        assertThat(fiksDokumenter).hasSize(5)
        assertThat(fiksDokumenter[0].filnavn).isEqualTo("soknad.json")
        assertThat(fiksDokumenter[1].filnavn).isEqualTo("Soknad.pdf")
        assertThat(fiksDokumenter[2].filnavn).isEqualTo("vedlegg.json")
        assertThat(fiksDokumenter[3].filnavn).isEqualTo("Soknad-juridisk.pdf")
        assertThat(fiksDokumenter[4].filnavn).isEqualTo("Brukerkvittering.pdf")
    }

    @Test
    fun hentDokumenterFraSoknadReturnererTreDokumenterForEttersendingMedEtVedlegg() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        every { innsendingService.hentAlleOpplastedeVedleggForSoknad(any()) } returns lagOpplastetVedlegg()

        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.tilknyttetBehandlingsId = "123"
        soknadUnderArbeid.jsonInternalSoknad = lagInternalSoknadForEttersending()
        val fiksDokumenter = fiksSender!!.hentDokumenterFraSoknad(soknadUnderArbeid, filnavnInputStreamMap)
        assertThat(fiksDokumenter).hasSize(4)
        assertThat(fiksDokumenter[0].filnavn).isEqualTo("ettersendelse.pdf")
        assertThat(fiksDokumenter[1].filnavn).isEqualTo("vedlegg.json")
        assertThat(fiksDokumenter[2].filnavn).isEqualTo("Brukerkvittering.pdf")
        assertThat(fiksDokumenter[3].filnavn).isEqualTo(FILNAVN)
    }

    @Test
    fun hentDokumenterFraSoknadKasterFeilHvisSoknadManglerForNySoknad() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad?.soknad = null
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy { fiksSender!!.hentDokumenterFraSoknad(soknadUnderArbeid, filnavnInputStreamMap) }
    }

    @Test
    fun hentDokumenterFraSoknadKasterFeilHvisVedleggManglerForEttersending() {
        val filnavnInputStreamMap = HashMap<String, InputStream>()
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.tilknyttetBehandlingsId = "123"
        soknadUnderArbeid.jsonInternalSoknad?.vedlegg = null
        assertThatExceptionOfType(RuntimeException::class.java)
            .isThrownBy {
                fiksSender!!.hentDokumenterFraSoknad(
                    soknadUnderArbeid,
                    filnavnInputStreamMap
                )
            }
    }

    private fun lagInternalSoknadForEttersending(): JsonInternalSoknad {
        val jsonFiler = mutableListOf<JsonFiler>()
        jsonFiler.add(JsonFiler().withFilnavn(FILNAVN).withSha512("sha512"))
        val jsonVedlegg = mutableListOf<JsonVedlegg>()
        jsonVedlegg.add(
            JsonVedlegg()
                .withStatus(Vedleggstatus.LastetOpp.name)
                .withType("type")
                .withTilleggsinfo("tilleggsinfo")
                .withFiler(jsonFiler)
        )
        return JsonInternalSoknad()
            .withVedlegg(JsonVedleggSpesifikasjon().withVedlegg(jsonVedlegg))
            .withSoknad(
                JsonSoknad()
                    .withDriftsinformasjon(JsonDriftsinformasjon())
                    .withData(
                        JsonData()
                            .withOkonomi(
                                JsonOkonomi()
                                    .withOpplysninger(JsonOkonomiopplysninger())
                            )
                    )
            )
    }

    private fun lagOpplastetVedlegg(): List<OpplastetVedlegg> {
        val opplastedeVedlegg: MutableList<OpplastetVedlegg> = ArrayList()
        opplastedeVedlegg.add(
            OpplastetVedlegg()
                .withFilnavn(FILNAVN)
                .withSha512("sha512")
                .withVedleggType(OpplastetVedleggType("type|tilleggsinfo"))
                .withData(byteArrayOf(1, 2, 3))
        )
        return opplastedeVedlegg
    }

    private fun lagSendtSoknad(): SendtSoknad {
        return SendtSoknad(
            behandlingsId = BEHANDLINGSID,
            tilknyttetBehandlingsId = null,
            eier = EIER,
            fiksforsendelseId = null,
            orgnummer = ORGNUMMER,
            navEnhetsnavn = NAVENHETSNAVN,
            brukerOpprettetDato = LocalDateTime.now(),
            brukerFerdigDato = LocalDateTime.now(),
            sendtDato = null
        )
    }

    private fun lagSendtEttersendelse(): SendtSoknad {
        val sendtSoknad = lagSendtSoknad()
        sendtSoknad.tilknyttetBehandlingsId = "soknadId"
        return sendtSoknad
    }

    companion object {
        private val FIKSFORSENDELSE_ID = UUID.randomUUID().toString()
        private const val FILNAVN = "filnavn.jpg"
        private const val ORGNUMMER = "9999"
        private const val NAVENHETSNAVN = "NAV Sagene"
        private const val BEHANDLINGSID = "12345"
        private const val EIER = "12345678910"

        private fun createSoknadUnderArbeid(): SoknadUnderArbeid {
            return SoknadUnderArbeid(
                versjon = 1L,
                behandlingsId = "behandlingsid",
                tilknyttetBehandlingsId = null,
                eier = EIER,
                jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
                status = SoknadUnderArbeidStatus.UNDER_ARBEID,
                opprettetDato = LocalDateTime.now(),
                sistEndretDato = LocalDateTime.now()
            )
        }
    }
}
