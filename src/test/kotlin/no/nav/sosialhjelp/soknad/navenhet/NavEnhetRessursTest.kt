package no.nav.sosialhjelp.soknad.navenhet

import io.mockk.Called
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
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.app.mapper.KommuneTilNavEnhetMapper
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class NavEnhetRessursTest {

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val OPPHOLDSADRESSE_KOMMUNENR = "0123"
        private val OPPHOLDSADRESSE: JsonAdresse = JsonGateAdresse()
            .withKilde(JsonKilde.BRUKER)
            .withType(JsonAdresse.Type.GATEADRESSE)
            .withLandkode("NOR")
            .withKommunenummer(OPPHOLDSADRESSE_KOMMUNENR)
            .withAdresselinjer(null)
            .withBolignummer("1")
            .withPostnummer("2")
            .withPoststed("Oslo")
            .withGatenavn("Sanntidsgata")
            .withHusnummer("1337")
            .withHusbokstav("A")

        private const val ENHETSNAVN = "NAV Testenhet"
        private const val KOMMUNENAVN = "Test kommune"
        private val KOMMUNENR = KommuneTilNavEnhetMapper.digisoskommuner[0]
        private const val ENHETSNR = "1234"
        private const val ORGNR = "123456789"

        private val SOKNADSMOTTAKER = JsonSoknadsmottaker()
            .withNavEnhetsnavn("$ENHETSNAVN, $KOMMUNENAVN")
            .withEnhetsnummer(ENHETSNR)
            .withKommunenummer(KOMMUNENR)
        private const val EIER = "123456789101"
    }

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val navEnhetService: NavEnhetService = mockk()

    private val navEnhetRessurs = NavEnhetRessurs(
        tilgangskontroll = tilgangskontroll,
        soknadUnderArbeidRepository = soknadUnderArbeidRepository,
        navEnhetService = navEnhetService
    )

    private val navEnhetFrontend = NavEnhetFrontend(
        orgnr = ORGNR,
        enhetsnr = ENHETSNR,
        enhetsnavn = ENHETSNAVN,
        kommunenavn = KOMMUNENAVN,
        kommuneNr = KOMMUNENR,
        behandlingsansvarlig = null,
        valgt = true,
        isMottakMidlertidigDeaktivert = false,
        isMottakDeaktivert = false
    )

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()

        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
        every { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(any()) } just runs
    }

    @AfterEach
    internal fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
        unmockkObject(MiljoUtils)
    }

    @Test
    internal fun `getNavEnheter - skal returnere NavEnhetFrontend-liste`() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { navEnhetService.getNavEnhet(any(), any(), any()) } returns navEnhetFrontend

        val response = navEnhetRessurs.getNavEnheter(BEHANDLINGSID)

        assertThat(response).hasSize(1)
        assertThat(response[0].valgt).isTrue
    }

    @Test
    fun `getNavEnheter - skal returnere tom liste`() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { navEnhetService.getNavEnhet(any(), any(), any()) } returns null

        val response = navEnhetRessurs.getNavEnheter(BEHANDLINGSID)

        assertThat(response).isEmpty()
    }

    @Test
    fun `getNavEnheter - skal kaste feil`() {
        val soknadUnderArbeid: SoknadUnderArbeid = mockk()
        every { soknadUnderArbeid.jsonInternalSoknad?.soknad } returns null
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid

        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { navEnhetRessurs.getNavEnheter(BEHANDLINGSID) }

        verify { navEnhetService wasNot called }
    }

    @Test
    fun `getValgtNavEnhet - skal hente NavEnhetFrontend`() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { navEnhetService.getValgtNavEnhet(SOKNADSMOTTAKER) } returns navEnhetFrontend

        val response = navEnhetRessurs.getValgtNavEnhet(BEHANDLINGSID)

        assertThat(response).isNotNull
        assertThat(response?.valgt).isTrue
    }

    @Test
    fun `getValgtNavEnhet - null hvis kommunenummer er null`() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad
            .withMottaker(
                JsonSoknadsmottaker()
                    .withNavEnhetsnavn(ENHETSNAVN)
                    .withEnhetsnummer(ENHETSNR)
                    .withKommunenummer(null)
            )
            .data.personalia.withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid

        assertThat(navEnhetRessurs.getValgtNavEnhet(BEHANDLINGSID)).isNull()
    }

    @Test
    fun `getValgtNavEnhet - null hvis navEnhetsnavn er null`() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        soknadUnderArbeid.jsonInternalSoknad!!.soknad
            .withMottaker(
                JsonSoknadsmottaker()
                    .withNavEnhetsnavn(null)
                    .withEnhetsnummer(ENHETSNR)
                    .withKommunenummer(KOMMUNENR)
            )
            .data.personalia.withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid

        assertThat(navEnhetRessurs.getValgtNavEnhet(BEHANDLINGSID)).isNull()
    }

    @Test
    fun `getValgtNavEnhet - skal kaste feil`() {
        val soknadUnderArbeid: SoknadUnderArbeid = mockk()
        every { soknadUnderArbeid.jsonInternalSoknad?.soknad?.mottaker } returns null
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid

        assertThatExceptionOfType(IllegalStateException::class.java)
            .isThrownBy { navEnhetRessurs.getValgtNavEnhet(BEHANDLINGSID) }

        verify { navEnhetService wasNot called }
    }

    @Test
    fun `putNavEnhet - skal oppdatere JsonSoknadsmottaker`() {
        val soknadUnderArbeid = createSoknadUnderArbeid()

        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        navEnhetRessurs.putNavEnhet(BEHANDLINGSID, navEnhetFrontend)

        val oppdatertSoknadUnderArbeid = slot.captured

        val mottaker = oppdatertSoknadUnderArbeid.jsonInternalSoknad?.mottaker
        assertThat(mottaker).isNotNull
        assertThat(mottaker?.navEnhetsnavn).contains(navEnhetFrontend.enhetsnavn, navEnhetFrontend.kommunenavn)
        assertThat(mottaker?.organisasjonsnummer).isEqualTo(navEnhetFrontend.orgnr)

        val soknadsmottaker = oppdatertSoknadUnderArbeid.jsonInternalSoknad!!.soknad.mottaker
        val kombinertnavn = soknadsmottaker.navEnhetsnavn
        val enhetsnavn = kombinertnavn.substring(0, kombinertnavn.indexOf(','))
        val kommunenavn = kombinertnavn.substring(kombinertnavn.indexOf(',') + 2)
        assertThat(navEnhetFrontend.enhetsnavn).isEqualTo(enhetsnavn)
        assertThat(navEnhetFrontend.kommunenavn).isEqualTo(kommunenavn)
        assertThat(navEnhetFrontend.enhetsnr).isEqualTo(soknadsmottaker.enhetsnummer)
    }

    @Test
    internal fun `hentNavEnheter skal kaste AuthorizationException ved manglende tilgang`() {
        every { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(any()) } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { navEnhetRessurs.getNavEnheter(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot Called }
    }

    @Test
    internal fun `hentValgtNavEnhet skal kaste AuthorizationException ved manglende tilgang`() {
        every { tilgangskontroll.verifiserBrukerHarTilgangTilSoknad(any()) } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { navEnhetRessurs.getValgtNavEnhet(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot Called }
    }

    @Test
    internal fun `updateNavEnhet skal kaste AuthorizationException ved manglende tilgang`() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val navEnhetFrontend = mockk<NavEnhetFrontend>()

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { navEnhetRessurs.putNavEnhet(BEHANDLINGSID, navEnhetFrontend) }

        verify { soknadUnderArbeidRepository wasNot Called }
    }

    private fun createSoknadUnderArbeid(): SoknadUnderArbeid {
        return SoknadUnderArbeid(
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
