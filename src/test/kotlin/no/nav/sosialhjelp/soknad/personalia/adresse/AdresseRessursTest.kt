package no.nav.sosialhjelp.soknad.personalia.adresse

import io.mockk.called
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonUstrukturertAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.common.JsonKilde
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadunderarbeid.SoknadUnderArbeidRepository
import no.nav.sosialhjelp.soknad.business.service.soknadservice.SoknadService
import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.domain.model.exception.AuthorizationException
import no.nav.sosialhjelp.soknad.domain.model.oidc.StaticSubjectHandlerService
import no.nav.sosialhjelp.soknad.domain.model.oidc.SubjectHandler
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetRessurs
import no.nav.sosialhjelp.soknad.navenhet.dto.NavEnhetFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresseFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.AdresserFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.GateadresseFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.MatrikkeladresseFrontend
import no.nav.sosialhjelp.soknad.personalia.adresse.dto.UstrukturertAdresseFrontend
import no.nav.sosialhjelp.soknad.web.sikkerhet.Tilgangskontroll
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Arrays

internal class AdresseRessursTest {

    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val adresseSystemdata: AdresseSystemdata = mockk()
    private val soknadUnderArbeidRepository: SoknadUnderArbeidRepository = mockk()
    private val navEnhetRessurs: NavEnhetRessurs = mockk()

    private val adresseRessurs =
        AdresseRessurs(tilgangskontroll, adresseSystemdata, soknadUnderArbeidRepository, navEnhetRessurs)

    @BeforeEach
    fun setUp() {
        System.setProperty("environment.name", "test")
        SubjectHandler.setSubjectHandlerService(StaticSubjectHandlerService())
    }

    @AfterEach
    fun tearDown() {
        SubjectHandler.resetOidcSubjectHandlerService()
        System.clearProperty("environment.name")
    }

    @Test
    fun adresserSkalReturnereAdresserRiktigKonvertert() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val soknadUnderArbeid = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD)
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia.folkeregistrertAdresse = JSON_SYS_MATRIKKELADRESSE
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { adresseSystemdata.innhentMidlertidigAdresse(any()) } returns JSON_SYS_USTRUKTURERT_ADRESSE

        val adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID)
        assertThatAdresserAreCorrectlyConverted(
            adresserFrontend,
            JSON_SYS_MATRIKKELADRESSE,
            JSON_SYS_USTRUKTURERT_ADRESSE,
            JSON_BRUKER_GATE_ADRESSE
        )
    }

    @Test
    fun adresserSkalReturnereOppholdsAdresseLikFolkeregistrertAdresse() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val soknadUnderArbeid = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.FOLKEREGISTRERT)
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia.folkeregistrertAdresse = JSON_SYS_MATRIKKELADRESSE
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { adresseSystemdata.innhentMidlertidigAdresse(any()) } returns JSON_SYS_USTRUKTURERT_ADRESSE

        val adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID)
        assertThatAdresserAreCorrectlyConverted(
            adresserFrontend,
            JSON_SYS_MATRIKKELADRESSE,
            JSON_SYS_USTRUKTURERT_ADRESSE,
            JSON_SYS_MATRIKKELADRESSE
        )
    }

    @Test
    fun adresserSkalReturnereOppholdsAdresseLikMidlertidigAdresse() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        val soknadUnderArbeid = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.MIDLERTIDIG)
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia.folkeregistrertAdresse = JSON_SYS_MATRIKKELADRESSE
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeid
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { adresseSystemdata.innhentMidlertidigAdresse(any()) } returns JSON_SYS_USTRUKTURERT_ADRESSE

        val adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID)
        assertThatAdresserAreCorrectlyConverted(
            adresserFrontend,
            JSON_SYS_MATRIKKELADRESSE,
            JSON_SYS_USTRUKTURERT_ADRESSE,
            JSON_SYS_USTRUKTURERT_ADRESSE
        )
    }

    @Test
    fun adresserSkalReturnereAdresserLikNull() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithOppholdsadresse(null)
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(any(), any()) } just runs
        every { adresseSystemdata.innhentMidlertidigAdresse(any()) } returns null
        val adresserFrontend = adresseRessurs.hentAdresser(BEHANDLINGSID)
        assertThatAdresserAreCorrectlyConverted(adresserFrontend, null, null, null)
    }

    @Test
    fun putAdresseSkalSetteOppholdsAdresseLikFolkeregistrertAdresseOgReturnereTilhorendeNavenhet() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        val soknadUnderArbeidIRepo = createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD)
        soknadUnderArbeidIRepo.jsonInternalSoknad.soknad.data.personalia.folkeregistrertAdresse = JSON_SYS_MATRIKKELADRESSE
        every { adresseSystemdata.createDeepCopyOfJsonAdresse(any()) } answers { callOriginal() }
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns soknadUnderArbeidIRepo
        every { navEnhetRessurs.findSoknadsmottaker(any(), any(), any(), any()) } returns listOf(
            NavEnhetFrontend("1", "1111", "Folkeregistrert NavEnhet", "4321", null, null, null, null, null)
        )

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val adresserFrontend = AdresserFrontend(valg = JsonAdresseValg.FOLKEREGISTRERT)
        val navEnheter = adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val oppholdsadresse = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia.oppholdsadresse
        assertThat(oppholdsadresse.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(oppholdsadresse).isEqualTo(adresseSystemdata.createDeepCopyOfJsonAdresse(JSON_SYS_MATRIKKELADRESSE)!!.withAdresseValg(JsonAdresseValg.FOLKEREGISTRERT))
        assertThat(navEnheter).hasSize(1)
        assertThat(navEnheter!![0].enhetsnavn).isEqualTo("Folkeregistrert NavEnhet")
    }

    @Test
    fun putAdresseSkalSetteOppholdsAdresseLikMidlertidigAdresseOgReturnereTilhorendeNavenhet() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { adresseSystemdata.innhentMidlertidigAdresse(any()) } returns JSON_SYS_USTRUKTURERT_ADRESSE
        every { adresseSystemdata.createDeepCopyOfJsonAdresse(any()) } answers { callOriginal() }
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.SOKNAD)
        every { navEnhetRessurs.findSoknadsmottaker(any(), any(), any(), any()) } returns listOf(
            NavEnhetFrontend("2", "2222", "Midlertidig NavEnhet", "kommune", "4321", null, null, null, null)
        )

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val adresserFrontend = AdresserFrontend(
            valg = JsonAdresseValg.MIDLERTIDIG,
            soknad = AdresseFrontend()
        )
        val navEnheter = adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val oppholdsadresse = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia.oppholdsadresse
        assertThat(oppholdsadresse.kilde).isEqualTo(JsonKilde.SYSTEM)
        assertThat(oppholdsadresse).isEqualTo(JSON_SYS_USTRUKTURERT_ADRESSE)
        assertThat(oppholdsadresse.adresseValg).isEqualTo(JsonAdresseValg.MIDLERTIDIG)
        assertThat(navEnheter).hasSize(1)
        assertThat(navEnheter!![0].enhetsnavn).isEqualTo("Midlertidig NavEnhet")
    }

    @Test
    fun putAdresseSkalSetteOppholdsAdresseLikSoknadsadresseOgReturnereTilhorendeNavenhet() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } just runs
        every { adresseSystemdata.createDeepCopyOfJsonAdresse(any()) } answers { callOriginal() }
        every { soknadUnderArbeidRepository.hentSoknad(any<String>(), any()) } returns
            createJsonInternalSoknadWithOppholdsadresse(JsonAdresseValg.FOLKEREGISTRERT)
        every { navEnhetRessurs.findSoknadsmottaker(any(), any(), any(), any()) } returns listOf(
            NavEnhetFrontend("3", "333", "Soknad NavEnhet", "4321", null, null, null, null, null)
        )

        val soknadUnderArbeidSlot = slot<SoknadUnderArbeid>()
        every { soknadUnderArbeidRepository.oppdaterSoknadsdata(capture(soknadUnderArbeidSlot), any()) } just runs

        val adresserFrontend = AdresserFrontend(
            valg = JsonAdresseValg.SOKNAD,
            soknad = AdresseFrontend(
                type = JsonAdresse.Type.GATEADRESSE,
                gateadresse = GateadresseFrontend(gatenavn = "Søknadsgata")
            )
        )
        val navEnheter = adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend)

        val soknadUnderArbeid = soknadUnderArbeidSlot.captured
        val oppholdsadresse = soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia.oppholdsadresse
        assertThat(oppholdsadresse.kilde).isEqualTo(JsonKilde.BRUKER)
        assertThat((oppholdsadresse as JsonGateAdresse).gatenavn).isEqualTo("Søknadsgata")
        assertThat(oppholdsadresse.getAdresseValg()).isEqualTo(JsonAdresseValg.SOKNAD)
        assertThat(navEnheter).hasSize(1)
        assertThat(navEnheter!![0].enhetsnavn).isEqualTo("Soknad NavEnhet")
    }

    @Test
    fun adresserSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")

        Assertions.assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { adresseRessurs.hentAdresser(BEHANDLINGSID) }

        verify { soknadUnderArbeidRepository wasNot called }
    }

    @Test
    fun putAdresserSkalKasteAuthorizationExceptionVedManglendeTilgang() {
        every { tilgangskontroll.verifiserAtBrukerKanEndreSoknad(any()) } throws AuthorizationException("Not for you my friend")
        val adresserFrontend = AdresserFrontend(valg = JsonAdresseValg.FOLKEREGISTRERT, null, null, null)
        Assertions.assertThatExceptionOfType(AuthorizationException::class.java)
            .isThrownBy { adresseRessurs.updateAdresse(BEHANDLINGSID, adresserFrontend) }
        verify { soknadUnderArbeidRepository wasNot called }
    }

    private fun assertThatAdresserAreCorrectlyConverted(
        adresserFrontend: AdresserFrontend,
        folkeregAdresse: JsonAdresse?,
        midlertidigAdresse: JsonAdresse?,
        valgtAdresse: JsonAdresse?
    ) {
        assertThatAdresseIsCorrectlyConverted(adresserFrontend.folkeregistrert, folkeregAdresse)
        assertThatAdresseIsCorrectlyConverted(adresserFrontend.midlertidig, midlertidigAdresse)
        assertThatAdresseIsCorrectlyConverted(adresserFrontend.soknad, valgtAdresse)
    }

    private fun assertThatAdresseIsCorrectlyConverted(adresseFrontend: AdresseFrontend?, jsonAdresse: JsonAdresse?) {
        if (adresseFrontend == null) {
            assertThat(jsonAdresse).isNull()
            return
        }
        assertThat(adresseFrontend.type).isEqualTo(jsonAdresse!!.type)
        when (jsonAdresse.type) {
            JsonAdresse.Type.GATEADRESSE -> assertThatGateadresseIsCorrectlyConverted(
                adresseFrontend.gateadresse!!,
                jsonAdresse
            )
            JsonAdresse.Type.MATRIKKELADRESSE -> assertThatMatrikkeladresseIsCorrectlyConverted(
                adresseFrontend.matrikkeladresse!!,
                jsonAdresse
            )
            JsonAdresse.Type.USTRUKTURERT -> assertThatUstrukturertAdresseIsCorrectlyConverted(
                adresseFrontend.ustrukturert!!,
                jsonAdresse
            )
            else -> {
                assertThat(jsonAdresse).isNull()
                assertThat(adresseFrontend.gateadresse).isNull()
                assertThat(adresseFrontend.matrikkeladresse).isNull()
                assertThat(adresseFrontend.ustrukturert).isNull()
            }
        }
    }

    private fun assertThatGateadresseIsCorrectlyConverted(gateadresse: GateadresseFrontend, jsonAdresse: JsonAdresse?) {
        val jsonGateAdresse = jsonAdresse as JsonGateAdresse?
        assertThat(gateadresse.landkode).isEqualTo(jsonGateAdresse?.landkode)
        assertThat(gateadresse.kommunenummer).isEqualTo(jsonGateAdresse?.kommunenummer)
        assertThat(gateadresse.adresselinjer).isEqualTo(jsonGateAdresse?.adresselinjer)
        assertThat(gateadresse.bolignummer).isEqualTo(jsonGateAdresse?.bolignummer)
        assertThat(gateadresse.postnummer).isEqualTo(jsonGateAdresse?.postnummer)
        assertThat(gateadresse.poststed).isEqualTo(jsonGateAdresse?.poststed)
        assertThat(gateadresse.gatenavn).isEqualTo(jsonGateAdresse?.gatenavn)
        assertThat(gateadresse.husnummer).isEqualTo(jsonGateAdresse?.husnummer)
        assertThat(gateadresse.husbokstav).isEqualTo(jsonGateAdresse?.husbokstav)
    }

    private fun assertThatMatrikkeladresseIsCorrectlyConverted(
        matrikkeladresse: MatrikkeladresseFrontend,
        jsonAdresse: JsonAdresse?
    ) {
        val jsonMatrikkelAdresse = jsonAdresse as JsonMatrikkelAdresse?
        assertThat(matrikkeladresse.kommunenummer).isEqualTo(jsonMatrikkelAdresse?.kommunenummer)
        assertThat(matrikkeladresse.gaardsnummer).isEqualTo(jsonMatrikkelAdresse?.gaardsnummer)
        assertThat(matrikkeladresse.bruksnummer).isEqualTo(jsonMatrikkelAdresse?.bruksnummer)
        assertThat(matrikkeladresse.festenummer).isEqualTo(jsonMatrikkelAdresse?.festenummer)
        assertThat(matrikkeladresse.seksjonsnummer).isEqualTo(jsonMatrikkelAdresse?.seksjonsnummer)
        assertThat(matrikkeladresse.undernummer).isEqualTo(jsonMatrikkelAdresse?.undernummer)
    }

    private fun assertThatUstrukturertAdresseIsCorrectlyConverted(
        ustrukturertAdresse: UstrukturertAdresseFrontend,
        jsonAdresse: JsonAdresse?
    ) {
        val jsonUstrukturertAdresse = jsonAdresse as JsonUstrukturertAdresse?
        assertThat(ustrukturertAdresse.adresse).isEqualTo(jsonUstrukturertAdresse?.adresse)
    }

    private fun createJsonInternalSoknadWithOppholdsadresse(valg: JsonAdresseValg?): SoknadUnderArbeid {
        val soknadUnderArbeid = SoknadUnderArbeid().withJsonInternalSoknad(
            SoknadService.createEmptyJsonInternalSoknad(EIER)
        )
        soknadUnderArbeid.jsonInternalSoknad.soknad.data.personalia
            .withOppholdsadresse(getSelectedAdresse(valg))
        return soknadUnderArbeid
    }

    private fun getSelectedAdresse(valg: JsonAdresseValg?): JsonAdresse? {
        return if (valg == null) {
            null
        } else when (valg) {
            JsonAdresseValg.FOLKEREGISTRERT -> JSON_SYS_MATRIKKELADRESSE
            JsonAdresseValg.MIDLERTIDIG -> JSON_SYS_USTRUKTURERT_ADRESSE
            JsonAdresseValg.SOKNAD -> JSON_BRUKER_GATE_ADRESSE
            else -> null
        }
    }

    companion object {
        private const val BEHANDLINGSID = "123"
        private val JSON_SYS_MATRIKKELADRESSE: JsonAdresse = JsonMatrikkelAdresse()
            .withKilde(JsonKilde.SYSTEM)
            .withType(JsonAdresse.Type.MATRIKKELADRESSE)
            .withKommunenummer("321")
            .withGaardsnummer("314")
            .withBruksnummer("15")
            .withFestenummer("92")
            .withSeksjonsnummer("65")
            .withUndernummer("36")
        private val JSON_SYS_USTRUKTURERT_ADRESSE: JsonAdresse = JsonUstrukturertAdresse()
            .withKilde(JsonKilde.SYSTEM)
            .withType(JsonAdresse.Type.USTRUKTURERT).withAdresse(Arrays.asList("Trenger", "Strukturgata", "3"))
        private val JSON_BRUKER_GATE_ADRESSE: JsonAdresse = JsonGateAdresse()
            .withKilde(JsonKilde.BRUKER)
            .withType(JsonAdresse.Type.GATEADRESSE)
            .withLandkode("NOR")
            .withKommunenummer("123")
            .withAdresselinjer(null)
            .withBolignummer("1")
            .withPostnummer("2")
            .withPoststed("Oslo")
            .withGatenavn("Sanntidsgata")
            .withHusnummer("1337")
            .withHusbokstav("A")
        private const val EIER = "123456789101"
    }
}
