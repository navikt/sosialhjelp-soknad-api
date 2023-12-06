package no.nav.sosialhjelp.soknad.personalia.familie

import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.getPersonnummerFromFnr
import no.nav.sosialhjelp.soknad.personalia.familie.dto.EktefelleFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.NavnFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusBrukerResponse
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusGiftResponse
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusGradertResponse
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class SivilstatusRessursTest {

    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()

    private val sivilstatusRessurs = SivilstatusRessurs(tilgangskontroll, soknadUnderArbeidRepository)

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs

        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    fun sivilstatusSkalReturnereBlank() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(null, null, null, null, null, null)

        val sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID) as SivilstatusBrukerResponse
        assertThat(sivilstatusFrontend).isInstanceOf(SivilstatusBrukerResponse::class.java)
        assertThat(sivilstatusFrontend.sivilstatus).isNull()
    }

    @Test
    fun sivilstatusSkalReturnereKunBrukerdefinertStatus() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(
                true,
                JsonSivilstatus.Status.GIFT,
                null,
                null,
                null,
                null
            )

        val sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID) as SivilstatusBrukerResponse
        assertThat(sivilstatusFrontend).isInstanceOf(SivilstatusBrukerResponse::class.java)
        assertThat(sivilstatusFrontend.sivilstatus).isEqualTo(JsonSivilstatus.Status.GIFT)
        assertThat(sivilstatusFrontend.ektefelle).isEqualTo(EktefelleFrontend())
    }

    @Test
    fun sivilstatusSkalReturnereBrukerdefinertEktefelleRiktigKonvertert() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(
                true,
                JsonSivilstatus.Status.GIFT,
                JSON_EKTEFELLE,
                null,
                null,
                true
            )

        val sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID) as SivilstatusBrukerResponse
        assertThat(sivilstatusFrontend).isInstanceOf(SivilstatusBrukerResponse::class.java)
        assertThat(sivilstatusFrontend.sivilstatus).isEqualTo(JsonSivilstatus.Status.GIFT)
        assertThatEktefelleIsCorrectlyConverted(sivilstatusFrontend.ektefelle, JSON_EKTEFELLE)
        assertThat(sivilstatusFrontend.borSammenMed).isTrue
    }

    @Test
    fun sivilstatusSkalReturnereSystemdefinertEktefelleRiktigKonvertert() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(
                brukerutfylt = false,
                status = JsonSivilstatus.Status.GIFT,
                ektefelle = JSON_EKTEFELLE,
                harDiskresjonskode = false,
                folkeregistrertMed = true,
                borSammen = null
            )

        val sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID) as SivilstatusGiftResponse
        assertThat(sivilstatusFrontend).isInstanceOf(SivilstatusGiftResponse::class.java)
        assertThatEktefelleIsCorrectlyConverted(sivilstatusFrontend.ektefelle, JSON_EKTEFELLE)
        assertThat(sivilstatusFrontend.erFolkeregistrertSammen).isTrue
    }

    @Test
    fun sivilstatusSkalReturnereSystemdefinertEktefelleMedDiskresjonskode() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(
                false,
                JsonSivilstatus.Status.GIFT,
                JSON_EKTEFELLE,
                true,
                null,
                null
            )

        val sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID) as SivilstatusGradertResponse
        assertThat(sivilstatusFrontend).isInstanceOf(SivilstatusGradertResponse::class.java)
    }

    @Test
    fun putSivilstatusSkalKunneSetteAlleTyperSivilstatus() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(null, null, null, false, null, null)

        JsonSivilstatus.Status.entries.forEach {
            val sivilstatusFrontend = SivilstatusFrontend(false, it, null, false, null, null)
            val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
            every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs
            sivilstatusRessurs.updateSivilstatus(BEHANDLINGSID, sivilstatusFrontend)
            val soknadUnderArbeid = soknadUnderArbeidSlot.captured
            val sivilstatus = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie.sivilstatus
            assertThat(sivilstatus.kilde).isEqualTo(JsonKilde.BRUKER)
            assertThat(sivilstatus.status).isEqualTo(it)
        }
    }

    @Test
    fun putSivilstatusSkalSetteStatusGiftOgEktefelle() {
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(null, null, null, null, null, null)

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val sivilstatusFrontend = SivilstatusFrontend(
            false,
            JsonSivilstatus.Status.GIFT,
            EKTEFELLE_FRONTEND,
            null,
            null,
            null
        )
        sivilstatusRessurs.updateSivilstatus(BEHANDLINGSID, sivilstatusFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val sivilstatus = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie.sivilstatus
        assertThat(sivilstatus.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(sivilstatus.status).isEqualTo(JsonSivilstatus.Status.GIFT)
        assertThatEktefelleIsCorrectlyConverted(EKTEFELLE_FRONTEND, sivilstatus.ektefelle)
    }

    @Test
    fun sivilstatusSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    @Test
    fun putSivilstatusSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val sivilstatusFrontend = SivilstatusFrontend(
            false,
            JsonSivilstatus.Status.GIFT,
            EKTEFELLE_FRONTEND,
            null,
            null,
            null
        )

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { sivilstatusRessurs.updateSivilstatus(BEHANDLINGSID, sivilstatusFrontend) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    private fun assertThatEktefelleIsCorrectlyConverted(
        ektefelle: EktefelleFrontend,
        jsonEktefelle: JsonEktefelle
    ) {
        assertThat(ektefelle.fodselsdato).isEqualTo(jsonEktefelle.fodselsdato)
        assertThat(ektefelle.personnummer).isEqualTo(getPersonnummerFromFnr(jsonEktefelle.personIdentifikator))
        assertThat(ektefelle.navn.fornavn).isEqualTo(jsonEktefelle.navn.fornavn)
        assertThat(ektefelle.navn.mellomnavn).isEqualTo(jsonEktefelle.navn.mellomnavn)
        assertThat(ektefelle.navn.etternavn).isEqualTo(jsonEktefelle.navn.etternavn)
    }

    private fun createJsonInternalSoknadWithSivilstatus(
        brukerutfylt: Boolean?,
        status: JsonSivilstatus.Status?,
        ektefelle: JsonEktefelle?,
        harDiskresjonskode: Boolean?,
        folkeregistrertMed: Boolean?,
        borSammen: Boolean?
    ): SoknadUnderArbeid {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.familie
            .withSivilstatus(
                brukerutfylt?.let {
                    JsonSivilstatus()
                        .withKilde(if (brukerutfylt) JsonKilde.BRUKER else JsonKilde.SYSTEM)
                        .withStatus(status)
                        .withEktefelle(ektefelle)
                        .withEktefelleHarDiskresjonskode(harDiskresjonskode)
                        .withFolkeregistrertMedEktefelle(folkeregistrertMed)
                        .withBorSammenMed(borSammen)
                }
            )
        return soknadUnderArbeid
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val EIER = "123456789101"
        private val JSON_EKTEFELLE = JsonEktefelle()
            .withNavn(
                JsonNavn()
                    .withFornavn("Alfred")
                    .withMellomnavn("Thaddeus Crane")
                    .withEtternavn("Pennyworth")
            )
            .withFodselsdato("1940-01-01")
            .withPersonIdentifikator("11111111111")
        private val EKTEFELLE_FRONTEND = EktefelleFrontend(
            navn = NavnFrontend("Alfred", "Thaddeus Crane", "Pennyworth"),
            fodselsdato = "1940-01-01",
            personnummer = "12345"
        )

        private fun createSoknadUnderArbeid(): SoknadUnderArbeid = SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = BEHANDLINGSID,
            tilknyttetBehandlingsId = null,
            eier = EIER,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
    }
}
