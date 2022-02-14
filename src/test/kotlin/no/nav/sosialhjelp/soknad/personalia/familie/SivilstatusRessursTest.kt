package no.nav.sosialhjelp.soknad.personalia.familie

import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonNavn
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonEktefelle
import no.nav.sbl.soknadsosialhjelp.soknad.familie.JsonSivilstatus
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.common.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.personalia.familie.PersonMapper.getPersonnummerFromFnr
import no.nav.sosialhjelp.soknad.personalia.familie.dto.EktefelleFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.NavnFrontend
import no.nav.sosialhjelp.soknad.personalia.familie.dto.SivilstatusFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SivilstatusRessursTest {

    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()

    private val sivilstatusRessurs = SivilstatusRessurs(tilgangskontroll, soknadUnderArbeidRepository)

    @BeforeEach
    fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true

        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
    }

    @Test
    fun sivilstatusSkalReturnereNull() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(null, null, null, null, null, null)

        val sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID)
        assertThat(sivilstatusFrontend).isNull()
    }

    @Test
    fun sivilstatusSkalReturnereKunBrukerdefinertStatus() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(
                true, JsonSivilstatus.Status.GIFT, null, null, null, null
            )

        val sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID)
        assertThat(sivilstatusFrontend?.sivilstatus).isEqualTo(JsonSivilstatus.Status.GIFT)
        assertThat(sivilstatusFrontend?.kildeErSystem).isFalse
        assertThat(sivilstatusFrontend?.ektefelle).isNull()
        assertThat(sivilstatusFrontend?.harDiskresjonskode).isNull()
        assertThat(sivilstatusFrontend?.erFolkeregistrertSammen).isNull()
    }

    @Test
    fun sivilstatusSkalReturnereBrukerdefinertEktefelleRiktigKonvertert() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(
                true, JsonSivilstatus.Status.GIFT, JSON_EKTEFELLE, null, null, true
            )

        val sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID)
        assertThat(sivilstatusFrontend?.kildeErSystem).isFalse
        assertThat(sivilstatusFrontend?.sivilstatus).isEqualTo(JsonSivilstatus.Status.GIFT)
        assertThatEktefelleIsCorrectlyConverted(sivilstatusFrontend?.ektefelle, JSON_EKTEFELLE)
        assertThat(sivilstatusFrontend?.harDiskresjonskode).isNull()
        assertThat(sivilstatusFrontend?.erFolkeregistrertSammen).isNull()
        assertThat(sivilstatusFrontend?.borSammenMed).isTrue
    }

    @Test
    fun sivilstatusSkalReturnereSystemdefinertEktefelleRiktigKonvertert() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(
                false, JsonSivilstatus.Status.GIFT, JSON_EKTEFELLE, false, true, null
            )

        val sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID)
        assertThat(sivilstatusFrontend?.kildeErSystem).isTrue
        assertThat(sivilstatusFrontend?.sivilstatus).isEqualTo(JsonSivilstatus.Status.GIFT)
        assertThatEktefelleIsCorrectlyConverted(sivilstatusFrontend?.ektefelle, JSON_EKTEFELLE)
        assertThat(sivilstatusFrontend?.harDiskresjonskode).isFalse
        assertThat(sivilstatusFrontend?.erFolkeregistrertSammen).isTrue
        assertThat(sivilstatusFrontend?.borSammenMed).isNull()
    }

    @Test
    fun sivilstatusSkalReturnereSystemdefinertEktefelleMedDiskresjonskode() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(
                false, JsonSivilstatus.Status.GIFT, JSON_EKTEFELLE, true, null, null
            )

        val sivilstatusFrontend = sivilstatusRessurs.hentSivilstatus(BEHANDLINGSID)
        assertThat(sivilstatusFrontend?.kildeErSystem).isTrue
        assertThat(sivilstatusFrontend?.sivilstatus).isEqualTo(JsonSivilstatus.Status.GIFT)
        assertThat(sivilstatusFrontend?.harDiskresjonskode).isTrue
        assertThat(sivilstatusFrontend?.erFolkeregistrertSammen).isNull()
        assertThat(sivilstatusFrontend?.borSammenMed).isNull()
    }

    @Test
    fun putSivilstatusSkalKunneSetteAlleTyperSivilstatus() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(null, null, null, null, null, null)

        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.GIFT)
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.ENKE)
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.SAMBOER)
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.SEPARERT)
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.SKILT)
        assertThatPutSivilstatusSetterRiktigStatus(JsonSivilstatus.Status.UGIFT)
    }

    @Test
    fun putSivilstatusSkalSetteStatusGiftOgEktefelle() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithSivilstatus(null, null, null, null, null, null)

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val sivilstatusFrontend = SivilstatusFrontend(
            false, JsonSivilstatus.Status.GIFT, EKTEFELLE_FRONTEND, null, null, null
        )
        sivilstatusRessurs.updateSivilstatus(BEHANDLINGSID, sivilstatusFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val sivilstatus = soknadUnderArbeid.jsonInternalSoknad.soknad.data.familie.sivilstatus
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
            false, JsonSivilstatus.Status.GIFT, EKTEFELLE_FRONTEND, null, null, null
        )

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { sivilstatusRessurs.updateSivilstatus(BEHANDLINGSID, sivilstatusFrontend) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    private fun assertThatEktefelleIsCorrectlyConverted(
        ektefelle: EktefelleFrontend?,
        jsonEktefelle: JsonEktefelle
    ) {
        assertThat(ektefelle?.fodselsdato).isEqualTo(jsonEktefelle.fodselsdato)
        assertThat(ektefelle?.personnummer).isEqualTo(getPersonnummerFromFnr(jsonEktefelle.personIdentifikator))
        assertThat(ektefelle?.navn?.fornavn).isEqualTo(jsonEktefelle.navn.fornavn)
        assertThat(ektefelle?.navn?.mellomnavn).isEqualTo(jsonEktefelle.navn.mellomnavn)
        assertThat(ektefelle?.navn?.etternavn).isEqualTo(jsonEktefelle.navn.etternavn)
    }

    private fun assertThatPutSivilstatusSetterRiktigStatus(status: JsonSivilstatus.Status) {
        val sivilstatusFrontend = SivilstatusFrontend(false, status, null, null, null, null)

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        sivilstatusRessurs.updateSivilstatus(BEHANDLINGSID, sivilstatusFrontend)
        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val sivilstatus = soknadUnderArbeid.jsonInternalSoknad.soknad.data.familie.sivilstatus
        assertThat(sivilstatus.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat(sivilstatus.status).isEqualTo(status)
    }

    private fun createJsonInternalSoknadWithSivilstatus(
        brukerutfylt: Boolean?,
        status: JsonSivilstatus.Status?,
        ektefelle: JsonEktefelle?,
        harDiskresjonskode: Boolean?,
        folkeregistrertMed: Boolean?,
        borSammen: Boolean?
    ): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(createEmptyJsonInternalSoknad(EIER))
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.familie
            .withSivilstatus(
                if (brukerutfylt == null) null else JsonSivilstatus()
                    .withKilde(if (brukerutfylt) JsonKilde.BRUKER else JsonKilde.SYSTEM)
                    .withStatus(status)
                    .withEktefelle(ektefelle)
                    .withEktefelleHarDiskresjonskode(harDiskresjonskode)
                    .withFolkeregistrertMedEktefelle(folkeregistrertMed)
                    .withBorSammenMed(borSammen)
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
    }
}
