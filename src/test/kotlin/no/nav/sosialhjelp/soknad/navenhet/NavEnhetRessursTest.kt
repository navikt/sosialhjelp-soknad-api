package no.nav.sosialhjelp.soknad.navenhet

import io.mockk.Called
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslagType
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService
import no.nav.sosialhjelp.soknad.client.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.client.fiks.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.client.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.domain.model.util.KommuneTilNavEnhetMapper
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService.Companion.BYDEL_MARKA_OSLO
import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnhet
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.navenhet.finnadresse.FinnAdresseService
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningService
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NavEnhetRessursTest {

    companion object {
        private const val BEHANDLINGSID = "123"
        private const val OPPHOLDSADRESSE_KOMMUNENR = "0123"
        private const val OPPHOLDSADRESSE_BYDELSNR = "012301"
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
        private val KOMMUNENR = KommuneTilNavEnhetMapper.getDigisoskommuner()[0]
        private const val ENHETSNR = "1234"
        private const val ORGNR = "123456789"
        private const val ENHETSNAVN_2 = "NAV Van"
        private const val KOMMUNENAVN_2 = "Enummok kommune"
        private val KOMMUNENR_2 = KommuneTilNavEnhetMapper.getDigisoskommuner()[1]
        private const val ENHETSNR_2 = "5678"
        private const val ORGNR_2 = "987654321"
        private val SOKNADSMOTTAKER = JsonSoknadsmottaker()
            .withNavEnhetsnavn("$ENHETSNAVN, $KOMMUNENAVN")
            .withEnhetsnummer(ENHETSNR)
            .withKommunenummer(KOMMUNENR)

        private val SOKNADSMOTTAKER_2 = JsonSoknadsmottaker()
            .withNavEnhetsnavn("$ENHETSNAVN_2, $KOMMUNENAVN_2")
            .withEnhetsnummer(ENHETSNR_2)
            .withKommunenummer(KOMMUNENR_2)

        private val SOKNADSMOTTAKER_FORSLAG = AdresseForslag(null, null, null, KOMMUNENR, KOMMUNENAVN, null, null, ENHETSNAVN, null, null, AdresseForslagType.GATEADRESSE)
        private val SOKNADSMOTTAKER_FORSLAG_2 = AdresseForslag(null, null, null, KOMMUNENR_2, KOMMUNENAVN_2, null, null, ENHETSNAVN_2, null, null, AdresseForslagType.GATEADRESSE)
        private val SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA = AdresseForslag(null, null, null, KOMMUNENR_2, KOMMUNENAVN_2, null, null, BYDEL_MARKA_OSLO, null, null, AdresseForslagType.GATEADRESSE)

        private val NAV_ENHET = NavEnhet(ENHETSNR, ENHETSNAVN, null, ORGNR)
        private val NAV_ENHET_2 = NavEnhet(ENHETSNR_2, ENHETSNAVN_2, null, ORGNR_2)
        private const val EIER = "123456789101"
    }

    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val finnAdresseService: FinnAdresseService = mockk()
    private val navEnhetService: NavEnhetService = mockk()
    private val kommuneInfoService: KommuneInfoService = mockk()
    private val bydelFordelingService: BydelFordelingService = mockk()
    private val geografiskTilknytningService: GeografiskTilknytningService = mockk()
    private val kodeverkService: KodeverkService = mockk()

    private val navEnhetRessurs = NavEnhetRessurs(
        tilgangskontroll, soknadUnderArbeidRepository, navEnhetService, kommuneInfoService, bydelFordelingService, finnAdresseService, geografiskTilknytningService, kodeverkService
    )

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        System.setProperty("environment.name", "test")
        SubjectHandler.setSubjectHandlerService(StaticSubjectHandlerService())
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { kommuneInfoService.kanMottaSoknader(any()) } returns true
        every { kommuneInfoService.harMidlertidigDeaktivertMottak(any()) } returns true
    }

    @AfterEach
    internal fun tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService()
        System.clearProperty("environment.name")
    }

    @Test
    internal fun hentNavEnheter_oppholdsadresseFraAdressesok_skalReturnereEnheterRiktigKonvertert() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { finnAdresseService.finnAdresseFraSoknad(any(), "soknad") } returns listOf(SOKNADSMOTTAKER_FORSLAG, SOKNADSMOTTAKER_FORSLAG_2)
        every { navEnhetService.getEnhetForGt(ENHETSNAVN) } returns NAV_ENHET
        every { navEnhetService.getEnhetForGt(ENHETSNAVN_2) } returns NAV_ENHET_2
        every { kommuneInfoService.getBehandlingskommune(KOMMUNENR, KOMMUNENAVN) } returns KOMMUNENAVN
        every { kommuneInfoService.getBehandlingskommune(KOMMUNENR_2, KOMMUNENAVN_2) } returns KOMMUNENAVN_2

        val response = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID)

        assertThatEnheterAreCorrectlyConverted(response!!, listOf(SOKNADSMOTTAKER, SOKNADSMOTTAKER_2))
        assertThat(response[0].valgt).isTrue
        assertThat(response[1].valgt).isFalse
    }

    @Test
    internal fun hentNavEnheter_oppholdsadresseFraAdressesok_skalReturnereEnheterRiktigKonvertertVedBydelMarka() {
        val annenBydel = "030112"
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.withMottaker(SOKNADSMOTTAKER_2).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { finnAdresseService.finnAdresseFraSoknad(any(), "soknad") } returns listOf(SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA)
        every { bydelFordelingService.getBydelTilForMarka(SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA) } returns annenBydel
        every { navEnhetService.getEnhetForGt(annenBydel) } returns NAV_ENHET_2
        every { kommuneInfoService.getBehandlingskommune(KOMMUNENR_2, KOMMUNENAVN_2) } returns KOMMUNENAVN_2

        val response = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID)
        assertThatEnheterAreCorrectlyConverted(response!!, listOf(SOKNADSMOTTAKER_2))
        assertThat(response[0].valgt).isTrue
    }

    @Test
    internal fun hentValgtNavEnhet_skalReturnereEnhetRiktigKonvertert() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid

        val response = navEnhetRessurs.hentValgtNavEnhet(BEHANDLINGSID)
        assertThatEnhetIsCorrectlyConverted(response, SOKNADSMOTTAKER)
        assertThat(response?.valgt).isTrue
    }

    @Test
    internal fun hentNavEnheter_oppholdsadresseIkkeValgt_skalReturnereTomListe() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(null))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { finnAdresseService.finnAdresseFraSoknad(any(), null) } returns emptyList()

        val response = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID)
        assertThat(response).isEmpty()
    }

    @Test
    internal fun hentValgtNavEnhet_oppholdsadresseIkkeValgt_skalReturnereNull() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(null))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid

        val response = navEnhetRessurs.hentValgtNavEnhet(BEHANDLINGSID)
        assertThat(response).isNull()
    }

    @Test
    internal fun updateNavEnhet_skalSetteNavEnhet() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs

        val slot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(slot), any()) } just runs

        val navEnhetFrontend = NavEnhetFrontend(
            enhetsnavn = ENHETSNAVN_2,
            enhetsnr = ENHETSNR_2,
            kommunenavn = KOMMUNENAVN_2,
            orgnr = ORGNR_2
        )

        navEnhetRessurs.updateNavEnhet(BEHANDLINGSID, navEnhetFrontend)

        val jsonSoknadsmottaker = slot.captured.jsonInternalSoknad.soknad.mottaker
        assertThatEnhetIsCorrectlyConverted(navEnhetFrontend, jsonSoknadsmottaker)
    }

    @Test
    internal fun hentNavEnheter_oppholdsadresseFolkeregistrert_skalBrukeKommunenummerFraGtOgKommunenavnFraKodeverk() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { geografiskTilknytningService.hentGeografiskTilknytning(any()) } returns OPPHOLDSADRESSE_KOMMUNENR
        every { navEnhetService.getEnhetForGt(OPPHOLDSADRESSE_KOMMUNENR) } returns NAV_ENHET
        every { kodeverkService.getKommunenavn(OPPHOLDSADRESSE_KOMMUNENR) } returns KOMMUNENAVN
        every { kommuneInfoService.getBehandlingskommune(OPPHOLDSADRESSE_KOMMUNENR, KOMMUNENAVN) } returns KOMMUNENAVN

        val response = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID)

        assertThat(response!!).hasSize(1)
        assertThat(response[0].kommuneNr).isEqualTo(OPPHOLDSADRESSE_KOMMUNENR)
        assertThat(response[0].kommunenavn).isEqualTo(KOMMUNENAVN)
    }

    @Test
    internal fun hentNavEnheter_oppholdsadresseFolkeregistrert_skalBrukeBydelsnummerFraGtOgKommunenavnFraKodeverk() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { geografiskTilknytningService.hentGeografiskTilknytning(any()) } returns OPPHOLDSADRESSE_BYDELSNR
        every { navEnhetService.getEnhetForGt(OPPHOLDSADRESSE_BYDELSNR) } returns NAV_ENHET
        every { kodeverkService.getKommunenavn(OPPHOLDSADRESSE_KOMMUNENR) } returns KOMMUNENAVN
        every { kommuneInfoService.getBehandlingskommune(OPPHOLDSADRESSE_KOMMUNENR, KOMMUNENAVN) } returns KOMMUNENAVN

        val response = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID)

        assertThat(response!!).hasSize(1)
        assertThat(response[0].kommuneNr).isEqualTo(OPPHOLDSADRESSE_KOMMUNENR)
        assertThat(response[0].kommunenavn).isEqualTo(KOMMUNENAVN)
    }

    @Test
    internal fun hentNavEnheter_oppholdsadresseFolkeregistrert_skalBrukeAdressesokSomFallback() {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))

        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { geografiskTilknytningService.hentGeografiskTilknytning(any()) } throws PdlApiException("pdl feil")
        every { finnAdresseService.finnAdresseFraSoknad(any(), "folkeregistrert") } returns listOf(SOKNADSMOTTAKER_FORSLAG)
        every { navEnhetService.getEnhetForGt(SOKNADSMOTTAKER_FORSLAG.geografiskTilknytning) } returns NAV_ENHET
        every { kommuneInfoService.getBehandlingskommune(KOMMUNENR, KOMMUNENAVN) } returns KOMMUNENAVN

        val response = navEnhetRessurs.hentNavEnheter(BEHANDLINGSID)

        assertThat(response!!).hasSize(1)
        assertThat(response[0].kommuneNr).isEqualTo(KOMMUNENR)
        assertThat(response[0].kommunenavn).isEqualTo(KOMMUNENAVN)

        verify { kodeverkService wasNot Called }
    }

    @Test
    internal fun hentNavEnheter_skalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { navEnhetRessurs.hentNavEnheter(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot Called }
    }

    @Test
    internal fun hentValgtNavEnhet_skalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { navEnhetRessurs.hentValgtNavEnhet(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot Called }
    }

    @Test
    internal fun updateNavEnhet_skalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")

        val navEnhetFrontend = mockk<NavEnhetFrontend>()

        assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { navEnhetRessurs.updateNavEnhet(BEHANDLINGSID, navEnhetFrontend) }

        verify { soknadUnderArbeidRepository wasNot Called }
    }

    private fun assertThatEnheterAreCorrectlyConverted(navEnhetFrontends: List<NavEnhetFrontend>, jsonSoknadsmottakers: List<JsonSoknadsmottaker>) {
        navEnhetFrontends.indices.forEach { i ->
            assertThatEnhetIsCorrectlyConverted(navEnhetFrontends[i], jsonSoknadsmottakers[i])
        }
    }

    private fun assertThatEnhetIsCorrectlyConverted(navEnhetFrontend: NavEnhetFrontend?, soknadsmottaker: JsonSoknadsmottaker) {
        if (navEnhetFrontend == null) {
            assertThat(soknadsmottaker).isNull()
            return
        }
        val kombinertnavn = soknadsmottaker.navEnhetsnavn
        val enhetsnavn = kombinertnavn.substring(0, kombinertnavn.indexOf(','))
        val kommunenavn = kombinertnavn.substring(kombinertnavn.indexOf(',') + 2)
        assertThat(navEnhetFrontend.enhetsnavn).isEqualTo(enhetsnavn)
        assertThat(navEnhetFrontend.kommunenavn).isEqualTo(kommunenavn)
        assertThat(navEnhetFrontend.enhetsnr).isEqualTo(soknadsmottaker.enhetsnummer)
    }
}
