package no.nav.sosialhjelp.soknad.navenhet.finnadresse

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonAdresseValg
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonGateAdresse
import no.nav.sbl.soknadsosialhjelp.soknad.adresse.JsonMatrikkelAdresse
import no.nav.sosialhjelp.soknad.adressesok.AdressesokService
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslag
import no.nav.sosialhjelp.soknad.adressesok.domain.AdresseForslagType
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeidStatus
import no.nav.sosialhjelp.soknad.innsending.SoknadService.Companion.createEmptyJsonInternalSoknad
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseService
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.domain.KartverketMatrikkelAdresse
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class FinnAdresseServiceTest {

    companion object {
        private const val EIER = "123456789101"
        private const val KOMMUNENUMMER = "0300"
        private const val GEOGRAFISK_TILKNYTNING = "0101"
        private const val BYDEL = "0102"
        private const val GATEADRESSE = "gateadresse"
        private const val BOLIGNUMMER = "H0101"
        private const val GATENAVN = "Sandakerveien"
        private const val KOMMUNENAVN1 = "Kommune 1"
        private const val LANDKODE = "NOR"
        private const val POSTNUMMER = "0000"
        private const val POSTSTED = "Oslo"
        private const val HUSNUMMER = "53"
        private const val HUSBOKSTAV = "B"
    }

    private val adressesokService: AdressesokService = mockk()
    private val personService: PersonService = mockk()
    private val hentAdresseService: HentAdresseService = mockk()

    private val finnAdresseService = FinnAdresseService(adressesokService, personService, hentAdresseService)

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
        SubjectHandlerUtils.setNewSubjectHandlerImpl(StaticSubjectHandlerImpl())
    }

    @AfterEach
    internal fun tearDown() {
        SubjectHandlerUtils.resetSubjectHandlerImpl()
    }

    @Test
    fun finnAdresseFraSoknadGirRiktigAdresseForFolkeregistrertGateadresse() {
        every { adressesokService.getAdresseForslag(any()) } returns lagAdresseForslag()
        val soknadUnderArbeid = createSoknadUnderArbeid()
        val personalia = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
        personalia.folkeregistrertAdresse = createGateadresse()
        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(personalia, JsonAdresseValg.FOLKEREGISTRERT.toString())
        assertThat(adresseForslag?.geografiskTilknytning).isEqualTo(GEOGRAFISK_TILKNYTNING)
        assertThat(adresseForslag?.kommunenummer).isEqualTo(KOMMUNENUMMER)
        assertThat(adresseForslag?.kommunenavn).isEqualTo(KOMMUNENAVN1)
        assertThat(adresseForslag?.type?.value).isEqualTo(GATEADRESSE)
    }

    @Test
    fun finnAdresseFraSoknadGirRiktigAdresseForFolkeregistrertMatrikkeladresse() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        val personalia = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
        personalia.folkeregistrertAdresse = createMatrikkeladresse()

        val mockPerson: Person = mockk()
        every { personService.hentPerson(any()) } returns mockPerson
        every { mockPerson.bostedsadresse?.matrikkeladresse?.matrikkelId } returns "matrikkelId"

        val matrikkelAdresse = KartverketMatrikkelAdresse(
            kommunenummer = KOMMUNENUMMER,
            gaardsnummer = "11",
            bruksnummer = "001",
            festenummer = "42",
            seksjonsunmmer = "asd123",
            undernummer = null,
            bydelsnummer = "030107"
        )
        every { hentAdresseService.hentKartverketMatrikkelAdresse(any()) } returns matrikkelAdresse

        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(personalia, JsonAdresseValg.FOLKEREGISTRERT.toString())
        assertThat(adresseForslag?.kommunenummer).isEqualTo(KOMMUNENUMMER)
        assertThat(adresseForslag?.type).isEqualTo(AdresseForslagType.MATRIKKELADRESSE)
    }

    @Test
    fun `finnAdresseFraSoknad returnerer null hvis hentAdresse ikke finner matrikkeladresse`() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        val personalia = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
        personalia.folkeregistrertAdresse = createMatrikkeladresse()

        val mockPerson: Person = mockk()
        every { personService.hentPerson(any()) } returns mockPerson
        every { mockPerson.bostedsadresse?.matrikkeladresse?.matrikkelId } returns "matrikkelId"

        every { hentAdresseService.hentKartverketMatrikkelAdresse(any()) } returns null

        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(personalia, JsonAdresseValg.FOLKEREGISTRERT.toString())
        assertThat(adresseForslag).isNull()
    }

    @Test
    fun finnAdresseFraSoknadReturnererNullHvisAdresseValgMangler() {
        val soknadUnderArbeid = createSoknadUnderArbeid()
        val personalia = soknadUnderArbeid.jsonInternalSoknad!!.soknad.data.personalia
        personalia.oppholdsadresse = createGateadresse()
        val adresseForslag = finnAdresseService.finnAdresseFraSoknad(personalia, null)
        assertThat(adresseForslag).isNull()
    }

    private fun createMatrikkeladresse(): JsonAdresse? {
        return JsonMatrikkelAdresse()
            .withType(JsonAdresse.Type.MATRIKKELADRESSE)
            .withKommunenummer(KOMMUNENUMMER)
    }

    private fun createGateadresse(): JsonAdresse? {
        return JsonGateAdresse()
            .withType(JsonAdresse.Type.GATEADRESSE)
            .withLandkode(LANDKODE)
            .withKommunenummer(KOMMUNENUMMER)
            .withPostnummer(POSTNUMMER)
            .withPoststed(POSTSTED)
            .withGatenavn(GATENAVN)
            .withHusnummer(HUSNUMMER)
            .withHusbokstav(HUSBOKSTAV)
            .withBolignummer(BOLIGNUMMER)
    }

    private fun lagAdresseForslag(kommunenummer: String = KOMMUNENUMMER, kommunenavn: String = KOMMUNENAVN1, adresse: String = "Gateveien"): AdresseForslag {
        return AdresseForslag(adresse, null, null, kommunenummer, kommunenavn, "0030", "Mocka", GEOGRAFISK_TILKNYTNING, null, BYDEL, AdresseForslagType.GATEADRESSE)
    }

    private fun createSoknadUnderArbeid(): SoknadUnderArbeid {
        return SoknadUnderArbeid(
            versjon = 1L,
            behandlingsId = "BEHANDLINGSID",
            tilknyttetBehandlingsId = null,
            eier = EIER,
            jsonInternalSoknad = createEmptyJsonInternalSoknad(EIER),
            status = SoknadUnderArbeidStatus.UNDER_ARBEID,
            opprettetDato = LocalDateTime.now(),
            sistEndretDato = LocalDateTime.now()
        )
    }
}
