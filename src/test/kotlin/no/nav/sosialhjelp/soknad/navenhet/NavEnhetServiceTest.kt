package no.nav.sosialhjelp.soknad.navenhet

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import no.finn.unleash.Unleash
import no.nav.sbl.soknadsosialhjelp.soknad.JsonSoknadsmottaker
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslagType
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.exceptions.PdlApiException
import no.nav.sosialhjelp.soknad.app.mapper.KommuneTilNavEnhetMapper
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.repository.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.OldSoknadService
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.kodeverk.KodeverkService
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetService.Companion.FEATURE_SEND_TIL_NAV_TESTKOMMUNE
import no.nav.sosialhjelp.soknad.navenhet.bydel.BydelFordelingService
import no.nav.sosialhjelp.soknad.navenhet.domain.NavEnhet
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.navenhet.finnadresse.FinnAdresseService
import no.nav.sosialhjelp.soknad.navenhet.gt.GeografiskTilknytningService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class NavEnhetServiceTest {

    private val norgService: NorgService = mockk()
    private val kommuneInfoService: KommuneInfoService = mockk()
    private val bydelFordelingService: BydelFordelingService = mockk()
    private val finnAdresseService: FinnAdresseService = mockk()
    private val geografiskTilknytningService: GeografiskTilknytningService = mockk()
    private val kodeverkService: KodeverkService = mockk()
    private val unleash: Unleash = mockk()

    private val navEnhetService = NavEnhetService(
        norgService,
        kommuneInfoService,
        bydelFordelingService,
        finnAdresseService,
        geografiskTilknytningService,
        kodeverkService,
        unleash
    )

    @BeforeEach
    fun setUp() {
        mockkObject(MiljoUtils)
        every { MiljoUtils.isNonProduction() } returns true

        every { kommuneInfoService.kanMottaSoknader(any()) } returns true
        every { kommuneInfoService.harMidlertidigDeaktivertMottak(any()) } returns true
        every { unleash.isEnabled(FEATURE_SEND_TIL_NAV_TESTKOMMUNE, false) } returns false
    }

    @AfterEach
    internal fun tearDown() {
        unmockkObject(MiljoUtils)
    }

    @Test
    fun `getNavEnhet - adressevalg er soknad - skal returnere NavEnhetFrontend riktig konvertert`() {
        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD))

        every { finnAdresseService.finnAdresseFraSoknad(any(), JsonAdresseValg.SOKNAD) } returns SOKNADSMOTTAKER_FORSLAG
        every { norgService.getEnhetForGt(KOMMUNENR) } returns NAV_ENHET
        every { kommuneInfoService.getBehandlingskommune(KOMMUNENR, KOMMUNENAVN) } returns KOMMUNENAVN
        every { kodeverkService.getKommunenavn(KOMMUNENR) } returns KOMMUNENAVN

        val navEnhetFrontend = navEnhetService.getNavEnhet(EIER, soknadUnderArbeid.jsonInternalSoknad!!.soknad, JsonAdresseValg.SOKNAD)

        assertThat(navEnhetFrontend).isNotNull
        assertThatEnhetIsCorrectlyConverted(navEnhetFrontend, SOKNADSMOTTAKER)
        assertThat(navEnhetFrontend?.valgt).isTrue
    }

    @Test
    internal fun `getNavEnhet - adressevalg er soknad - skal returnere NavEnhetFrontend riktig konvertert ved bydel Marka`() {
        val annenBydel = "030112"
        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.withMottaker(SOKNADSMOTTAKER_2).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD))

        every { finnAdresseService.finnAdresseFraSoknad(any(), JsonAdresseValg.SOKNAD) } returns SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA
        every { bydelFordelingService.getBydelTilForMarka(SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA) } returns annenBydel
        every { norgService.getEnhetForGt(annenBydel) } returns NAV_ENHET_2
        every { kommuneInfoService.getBehandlingskommune(KOMMUNENR_2, KOMMUNENAVN_2) } returns KOMMUNENAVN_2
        every { kodeverkService.getKommunenavn(KOMMUNENR_2) } returns KOMMUNENAVN_2

        val navEnhetFrontend = navEnhetService.getNavEnhet(EIER, soknadUnderArbeid.jsonInternalSoknad!!.soknad, JsonAdresseValg.SOKNAD)

        assertThat(navEnhetFrontend).isNotNull
        assertThatEnhetIsCorrectlyConverted(navEnhetFrontend, SOKNADSMOTTAKER_2)
        assertThat(navEnhetFrontend?.valgt).isTrue
    }

    @Test
    internal fun `getNavEnhet - adressevalg er soknad - skal returnere null hvis norgService gir null`() {
        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.withMottaker(SOKNADSMOTTAKER_2).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.SOKNAD))

        every { finnAdresseService.finnAdresseFraSoknad(any(), JsonAdresseValg.SOKNAD) } returns SOKNADSMOTTAKER_FORSLAG
        every { norgService.getEnhetForGt(KOMMUNENR) } returns null

        val navEnhetFrontend = navEnhetService.getNavEnhet(EIER, soknadUnderArbeid.jsonInternalSoknad!!.soknad, JsonAdresseValg.SOKNAD)

        assertThat(navEnhetFrontend).isNull()
    }

    @Test
    internal fun `getValgtNavEnhet skal returnere NavEnhet riktig konvertert`() {
        val response = navEnhetService.getValgtNavEnhet(SOKNADSMOTTAKER)
        assertThatEnhetIsCorrectlyConverted(response, SOKNADSMOTTAKER)
        assertThat(response.valgt).isTrue
    }

    @Test
    internal fun `getNavEnhet skal returnere null hvis oppholdsadresse ikke er valgt`() {
        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(null))

        every { finnAdresseService.finnAdresseFraSoknad(any(), null) } returns null

        val navEnhetFrontend = navEnhetService.getNavEnhet(EIER, soknadUnderArbeid.jsonInternalSoknad!!.soknad, null)
        assertThat(navEnhetFrontend).isNull()
    }

    @Test
    internal fun `getNavEnhet - adressevalg er folkeregistrert - skal bruke kommunenummer fra GT og kommunenavn fra kodeverk`() {
        every { MiljoUtils.isNonProduction() } returns false

        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))

        every { geografiskTilknytningService.hentGeografiskTilknytning(any()) } returns OPPHOLDSADRESSE_KOMMUNENR
        every { norgService.getEnhetForGt(OPPHOLDSADRESSE_KOMMUNENR) } returns NAV_ENHET
        every { kodeverkService.getKommunenavn(OPPHOLDSADRESSE_KOMMUNENR) } returns KOMMUNENAVN
        every { kommuneInfoService.getBehandlingskommune(OPPHOLDSADRESSE_KOMMUNENR, KOMMUNENAVN) } returns KOMMUNENAVN

        val response = navEnhetService.getNavEnhet(BEHANDLINGSID, soknadUnderArbeid.jsonInternalSoknad!!.soknad, JsonAdresseValg.FOLKEREGISTRERT)

        assertThat(response).isNotNull
        assertThat(response?.kommuneNr).isEqualTo(OPPHOLDSADRESSE_KOMMUNENR)
        assertThat(response?.kommunenavn).isEqualTo(KOMMUNENAVN)
    }

    @Test
    internal fun `getNavEnhet - adressevalg er folkeregistrert - skal bruke bydelsnummer fra GT og kommunenavn fra kodeverk`() {
        every { MiljoUtils.isNonProduction() } returns false

        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))

        every { geografiskTilknytningService.hentGeografiskTilknytning(any()) } returns OPPHOLDSADRESSE_BYDELSNR
        every { norgService.getEnhetForGt(OPPHOLDSADRESSE_BYDELSNR) } returns NAV_ENHET
        every { kodeverkService.getKommunenavn(OPPHOLDSADRESSE_KOMMUNENR) } returns KOMMUNENAVN
        every { kommuneInfoService.getBehandlingskommune(OPPHOLDSADRESSE_KOMMUNENR, KOMMUNENAVN) } returns KOMMUNENAVN

        val response = navEnhetService.getNavEnhet(BEHANDLINGSID, soknadUnderArbeid.jsonInternalSoknad!!.soknad, JsonAdresseValg.FOLKEREGISTRERT)

        assertThat(response).isNotNull
        assertThat(response?.kommuneNr).isEqualTo(OPPHOLDSADRESSE_KOMMUNENR)
        assertThat(response?.kommunenavn).isEqualTo(KOMMUNENAVN)
    }

    @Test
    internal fun `getNavEnhet - adressevalg er folkeregistrert - skal bruke adressesok som fallback hvis hentGeografiskTilknytning feiler`() {
        val soknadUnderArbeid = createSoknadUnderArbeid(EIER)
        soknadUnderArbeid.jsonInternalSoknad!!.soknad.withMottaker(SOKNADSMOTTAKER).data.personalia
            .withOppholdsadresse(OPPHOLDSADRESSE.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))

        every { geografiskTilknytningService.hentGeografiskTilknytning(any()) } throws PdlApiException("pdl feil")
        every { finnAdresseService.finnAdresseFraSoknad(any(), JsonAdresseValg.FOLKEREGISTRERT) } returns SOKNADSMOTTAKER_FORSLAG
        every { norgService.getEnhetForGt(SOKNADSMOTTAKER_FORSLAG.geografiskTilknytning) } returns NAV_ENHET
        every { kommuneInfoService.getBehandlingskommune(KOMMUNENR, KOMMUNENAVN) } returns KOMMUNENAVN
        every { kodeverkService.getKommunenavn(KOMMUNENR) } returns KOMMUNENAVN

        val response = navEnhetService.getNavEnhet(BEHANDLINGSID, soknadUnderArbeid.jsonInternalSoknad!!.soknad, JsonAdresseValg.FOLKEREGISTRERT)

        assertThat(response).isNotNull
        assertThat(response?.kommuneNr).isEqualTo(KOMMUNENR)
        assertThat(response?.kommunenavn).isEqualTo(KOMMUNENAVN)
    }

    private fun assertThatEnhetIsCorrectlyConverted(
        navEnhetFrontend: NavEnhetFrontend?,
        soknadsmottaker: JsonSoknadsmottaker
    ) {
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

    private fun createSoknadUnderArbeid(eier: String): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = BEHANDLINGSID,
            tilknyttetBehandlingsId = null,
            eier = eier,
            jsonInternalSoknad = OldSoknadService.createEmptyJsonInternalSoknad(eier),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
    }

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
        private val KOMMUNENR = KommuneTilNavEnhetMapper.digisoskommuner[0]
        private const val ENHETSNR = "1234"
        private const val ORGNR = "123456789"
        private const val ENHETSNAVN_2 = "NAV Van"
        private const val KOMMUNENAVN_2 = "Enummok kommune"
        private val KOMMUNENR_2 = KommuneTilNavEnhetMapper.digisoskommuner[1]
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

        private val SOKNADSMOTTAKER_FORSLAG = AdresseForslag(null, null, null, KOMMUNENR, KOMMUNENAVN, null, null, KOMMUNENR, null, null, AdresseForslagType.GATEADRESSE)
        private val SOKNADSMOTTAKER_FORSLAG_BYDEL_MARKA = AdresseForslag(null, null, null, KOMMUNENR_2, KOMMUNENAVN_2, null, null, BydelFordelingService.BYDEL_MARKA_OSLO, null, null, AdresseForslagType.GATEADRESSE)

        private val NAV_ENHET = NavEnhet(ENHETSNR, ENHETSNAVN, null, ORGNR)
        private val NAV_ENHET_2 = NavEnhet(ENHETSNR_2, ENHETSNAVN_2, null, ORGNR_2)
        private const val EIER = "123456789101"
    }
}
