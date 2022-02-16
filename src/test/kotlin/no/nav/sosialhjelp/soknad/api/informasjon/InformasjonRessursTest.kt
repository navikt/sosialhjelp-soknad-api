package no.nav.sosialhjelp.soknad.api.informasjon

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.business.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.common.MiljoUtils
import no.nav.sosialhjelp.soknad.common.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.common.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
import no.nav.sosialhjelp.soknad.tilgangskontroll.Tilgangskontroll
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

internal class InformasjonRessursTest {

    private val messageSource: NavMessageSource = mockk()
    private val kommuneInfoService: KommuneInfoService = mockk()
    private val personService: PersonService = mockk()
    private val tilgangskontroll: Tilgangskontroll = mockk()
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val nedetidService: NedetidService = mockk()

    private val ressurs = InformasjonRessurs(
        messageSource,
        mockk(),
        kommuneInfoService,
        personService,
        tilgangskontroll,
        soknadMetadataRepository,
        mockk(),
        nedetidService
    )

    var norskBokmaal = Locale("nb", "NO")

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
        unmockkObject(MiljoUtils)
    }

    @Test
    fun spraakDefaulterTilNorskBokmaalHvisIkkeSatt() {
        every { messageSource.getBundleFor(any(), any()) } returns mockk()

        ressurs.hentTekster(SOKNADSTYPE, null)
        ressurs.hentTekster(SOKNADSTYPE, " ")

        verify(exactly = 2) { messageSource.getBundleFor(SOKNADSTYPE, norskBokmaal) }
    }

    @Test
    fun skalHenteTeksterForSoknadsosialhjelpViaBundle() {
        every { messageSource.getBundleFor(any(), any()) } returns mockk()

        ressurs.hentTekster("soknadsosialhjelp", null)

        verify { messageSource.getBundleFor("soknadsosialhjelp", norskBokmaal) }
    }

    @Test
    fun skalHenteTeksterForAlleBundlesUtenType() {
        every { messageSource.getBundleFor(any(), any()) } returns mockk()

        ressurs.hentTekster("", null)

        verify { messageSource.getBundleFor("", norskBokmaal) }
    }

    @Test
    fun kastExceptionHvisIkkeSpraakErPaaRiktigFormat() {
        assertThatExceptionOfType(IllegalArgumentException::class.java)
            .isThrownBy { ressurs.hentTekster(SOKNADSTYPE, "NORSK") }
    }

    @Test
    fun skalReturnereMappetListeOverManueltPakobledeKommuner() {
        val manuelleKommuner = listOf("1234")
        val mappedeKommuner = ressurs.mapManueltPakobledeKommuner(manuelleKommuner)

        assertThat(mappedeKommuner["1234"]).isNotNull
        assertThat(mappedeKommuner["1234"]!!.kanMottaSoknader).isTrue
        assertThat(mappedeKommuner["1234"]!!.kanOppdatereStatus).isFalse
    }

    @Test
    fun skalReturnereMappetListeOverDigisosKommuner() {
        val digisosKommuner: MutableMap<String, KommuneInfo> = HashMap()
        digisosKommuner["1234"] = KommuneInfo(
            "1234",
            kanMottaSoknader = true,
            kanOppdatereStatus = true,
            harMidlertidigDeaktivertMottak = false,
            harMidlertidigDeaktivertOppdateringer = false,
            kontaktpersoner = null,
            harNksTilgang = false,
            behandlingsansvarlig = null
        )
        digisosKommuner["5678"] = KommuneInfo(
            "5678",
            kanMottaSoknader = true,
            kanOppdatereStatus = true,
            harMidlertidigDeaktivertMottak = true,
            harMidlertidigDeaktivertOppdateringer = false,
            kontaktpersoner = null,
            harNksTilgang = false,
            behandlingsansvarlig = null
        )

        val mappedeKommuner = ressurs.mapDigisosKommuner(digisosKommuner)

        assertThat(mappedeKommuner["1234"]).isNotNull
        assertThat(mappedeKommuner["1234"]!!.kanMottaSoknader).isTrue
        assertThat(mappedeKommuner["1234"]!!.kanOppdatereStatus).isTrue
        assertThat(mappedeKommuner["5678"]).isNotNull
        assertThat(mappedeKommuner["5678"]!!.kanMottaSoknader).isFalse
        assertThat(mappedeKommuner["5678"]!!.kanOppdatereStatus).isTrue
    }

    @Test
    fun duplikatIDigisosKommuneSkalOverskriveManuellKommune() {
        val manuelleKommuner = listOf("1234")
        val manueltMappedeKommuner = ressurs.mapManueltPakobledeKommuner(manuelleKommuner)

        assertThat(manueltMappedeKommuner["1234"]!!.kanOppdatereStatus).isFalse // Manuelle kommuner får ikke innsyn

        val digisosKommuner: MutableMap<String, KommuneInfo> = HashMap()
        digisosKommuner["1234"] = KommuneInfo("1234", true, true, false, false, null, false, null)

        val mappedeDigisosKommuner = ressurs.mapDigisosKommuner(digisosKommuner)
        val margedKommuner = ressurs.mergeManuelleKommunerMedDigisosKommuner(manueltMappedeKommuner, mappedeDigisosKommuner)
        assertThat(margedKommuner).hasSize(1)
        assertThat(margedKommuner["1234"]!!.kanOppdatereStatus).isTrue
    }

//    @Test
//    fun harNyligInnsendteSoknader_AuthorizationExceptionVedManglendeTilgang() {
//        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } throws AuthorizationException("Not for you my friend")
//
//        assertThatExceptionOfType(AuthorizationException::class.java)
//            .isThrownBy { ressurs.harNyligInnsendteSoknader() }
//
//        verify { soknadMetadataRepository wasNot called }
//    }

    @Test
    fun harNyligInnsendteSoknader_tomResponse() {
//        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any()) } returns emptyList()

        val response = ressurs.harNyligInnsendteSoknader()

        assertThat(response.antallNyligInnsendte).isZero
    }

    @Test
    fun harNyligInnsendteSoknader_tomResponse_null() {
//        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any()) } returns null

        val response = ressurs.harNyligInnsendteSoknader()

        assertThat(response.antallNyligInnsendte).isZero
    }

    @Test
    fun harNyligInnsendteSoknader_flereSoknaderResponse() {
//        every { tilgangskontroll.verifiserAtBrukerHarTilgang() } just runs
        every { soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any()) } returns listOf(
            mockk(), mockk()
        )

        val response = ressurs.harNyligInnsendteSoknader()

        assertThat(response.antallNyligInnsendte).isEqualTo(2)
    }

    companion object {
        const val SOKNADSTYPE = "type"
    }
}
