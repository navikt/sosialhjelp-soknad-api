package no.nav.sosialhjelp.soknad.okonomiskeopplysninger

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonFiler
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedlegg
import no.nav.sbl.soknadsosialhjelp.vedlegg.JsonVedleggSpesifikasjon
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.opplastetvedlegg.OpplastetVedleggRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.Vedleggstatus
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.innsending.soknadunderarbeid.SoknadUnderArbeidService
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggStatus
import no.nav.sosialhjelp.soknad.okonomiskeopplysninger.dto.VedleggType
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagretVedleggMetadata
import no.nav.sosialhjelp.soknad.vedlegg.fiks.MellomlagringService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class OkonomiskeOpplysningerRessursTest {

    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val opplastetVedleggRepository: OpplastetVedleggRepository = mockk()
    private val mellomlagringService: MellomlagringService = mockk()
    private val soknadUnderArbeidService: SoknadUnderArbeidService = mockk()

    private val okonomiskeOpplysningerRessurs = OkonomiskeOpplysningerRessurs(
        tilgangskontroll = tilgangskontroll,
        soknadUnderArbeidRepository = soknadUnderArbeidRepository,
        opplastetVedleggRepository = opplastetVedleggRepository,
        mellomlagringService = mellomlagringService,
        soknadUnderArbeidService = soknadUnderArbeidService
    )

    private val behandlingsId = "123"

    private val soknadUnderArbeid = SoknadUnderArbeid(
        versjon = 1L,
        behandlingsId = behandlingsId,
        eier = "eier",
        jsonInternalSoknad = createEmptyJsonInternalSoknad("eier"),
        status = SoknadUnderArbeidStatus.UNDER_ARBEID,
        opprettetDato = LocalDateTime.now(),
        sistEndretDato = LocalDateTime.now()
    )

    @BeforeEach
    fun setUp() {
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
    }

    @Test
    fun `hentOkonomiskeOpplysninger happy path med 1 mellomlagret vedlegg`() {
        val soknadMedVedlegg = soknadUnderArbeid.copy(
            jsonInternalSoknad = createEmptyJsonInternalSoknad("eier")
                .withVedlegg(
                    JsonVedleggSpesifikasjon()
                        .withVedlegg(
                            mutableListOf(
                                JsonVedlegg()
                                    .withType("skattemelding")
                                    .withTilleggsinfo("skattemelding")
                                    .withStatus(Vedleggstatus.LastetOpp.toString())
                                    .withFiler(
                                        mutableListOf(
                                            JsonFiler()
                                                .withFilnavn("hubbabubba.jpg")
                                                .withSha512("sha512")
                                        )
                                    )
                            )
                        )
                )
        )

        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, any()) } returns soknadMedVedlegg
        every { soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(any()) } returns true

        every { mellomlagringService.getAllVedlegg(behandlingsId) } returns listOf(
            MellomlagretVedleggMetadata(filnavn = "hubbabubba.jpg", filId = "id123")
        )

        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs

        val response = okonomiskeOpplysningerRessurs.hentOkonomiskeOpplysninger(behandlingsId)

        assertThat(response.okonomiskeOpplysninger).hasSize(3)
        assertThat(response.okonomiskeOpplysninger!![0].type).isEqualTo(VedleggType.SkattemeldingSkattemelding)
        assertThat(response.okonomiskeOpplysninger!![0].vedleggStatus).isEqualTo(VedleggStatus.LastetOpp)
        assertThat(response.okonomiskeOpplysninger!![0].filer).hasSize(1)

        assertThat(response.okonomiskeOpplysninger!![1].type).isEqualTo(VedleggType.OppholdstillatelOppholdstillatel)
        assertThat(response.okonomiskeOpplysninger!![1].vedleggStatus).isEqualTo(VedleggStatus.VedleggKreves)
        assertThat(response.okonomiskeOpplysninger!![1].filer).hasSize(0)

        assertThat(response.okonomiskeOpplysninger!![2].type).isEqualTo(VedleggType.AnnetAnnet)
        assertThat(response.okonomiskeOpplysninger!![2].vedleggStatus).isEqualTo(VedleggStatus.VedleggKreves)
        assertThat(response.okonomiskeOpplysninger!![2].filer).hasSize(0)

        assertThat(response.slettedeVedlegg).isEmpty()
        assertThat(response.isOkonomiskeOpplysningerBekreftet).isFalse
    }

    @Test
    fun `hentOkonomiskeOpplysninger kaster IllegalStateException`() {
        val soknadMedVedlegg = soknadUnderArbeid.copy(
            jsonInternalSoknad = createEmptyJsonInternalSoknad("eier")
                .withVedlegg(
                    JsonVedleggSpesifikasjon()
                        .withVedlegg(
                            mutableListOf(
                                JsonVedlegg()
                                    .withType("skattemelding")
                                    .withTilleggsinfo("skattemelding")
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
                            )
                        )
                )
        )

        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, any()) } returns soknadMedVedlegg
        every { soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(any()) } returns true

        // kun 1 mellomlagret fil - 1 færre enn soknad.json over viser
        every { mellomlagringService.getAllVedlegg(behandlingsId) } returns listOf(
            MellomlagretVedleggMetadata(filnavn = "hubbabubba.jpg", filId = "id123")
        )

        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs

        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { okonomiskeOpplysningerRessurs.hentOkonomiskeOpplysninger(behandlingsId) }
    }

    @Test
    fun `hentOkonomiskeOpplysninger JsonVedlegg med VedleggKreves og filer - kaster IllegalStateException`() {
        val soknadMedVedlegg = soknadUnderArbeid.copy(
            jsonInternalSoknad = createEmptyJsonInternalSoknad("eier")
                .withVedlegg(
                    JsonVedleggSpesifikasjon()
                        .withVedlegg(
                            mutableListOf(
                                JsonVedlegg()
                                    .withType("skattemelding")
                                    .withTilleggsinfo("skattemelding")
                                    .withStatus(Vedleggstatus.VedleggKreves.toString())
                                    .withFiler(
                                        mutableListOf(
                                            JsonFiler()
                                                .withFilnavn("hubbabubba.jpg")
                                                .withSha512("sha512")
                                        )
                                    )
                            )
                        )
                )
        )

        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(behandlingsId) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(behandlingsId, any()) } returns soknadMedVedlegg
        every { soknadUnderArbeidService.skalSoknadSendesMedDigisosApi(any()) } returns true

        every { mellomlagringService.getAllVedlegg(behandlingsId) } returns listOf(
            MellomlagretVedleggMetadata(filnavn = "asdasd.jpg", filId = "id123")
        )

        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs

        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { okonomiskeOpplysningerRessurs.hentOkonomiskeOpplysninger(behandlingsId) }
    }

    @Test
    fun `should return VedleggAlleredeSendt when alleredeLevert is true`() {
        val result = okonomiskeOpplysningerRessurs.determineVedleggStatus(true, VedleggStatus.LastetOpp, true)
        Assertions.assertEquals(VedleggStatus.VedleggAlleredeSendt, result)
    }

    @Test
    fun `should return VedleggAlleredeSendt when vedleggStatus is VedleggAlleredeSendt and alleredeLevert is not true`() {
        val result =
            okonomiskeOpplysningerRessurs.determineVedleggStatus(false, VedleggStatus.VedleggAlleredeSendt, true)
        Assertions.assertEquals(VedleggStatus.VedleggAlleredeSendt, result)
    }

    @Test
    fun `should return LastetOpp when alleredeLevert is false, vedleggStatus is not VedleggAlleredeSendt and hasFiles is true`() {
        val result = okonomiskeOpplysningerRessurs.determineVedleggStatus(false, VedleggStatus.LastetOpp, true)
        Assertions.assertEquals(VedleggStatus.LastetOpp, result)
    }

    @Test
    fun `should return VedleggKreves when alleredeLevert is false, vedleggStatus is not VedleggAlleredeSendt and hasFiles is false`() {
        val result = okonomiskeOpplysningerRessurs.determineVedleggStatus(false, VedleggStatus.LastetOpp, false)
        Assertions.assertEquals(VedleggStatus.VedleggKreves, result)
    }
}
