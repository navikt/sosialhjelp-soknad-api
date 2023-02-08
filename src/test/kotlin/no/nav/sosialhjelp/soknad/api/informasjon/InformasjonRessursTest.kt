package no.nav.sosialhjelp.soknad.api.informasjon

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import no.nav.sosialhjelp.api.fiks.KommuneInfo
import no.nav.sosialhjelp.soknad.api.nedetid.NedetidService
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.app.subjecthandler.StaticSubjectHandlerImpl
import no.nav.sosialhjelp.soknad.app.subjecthandler.SubjectHandlerUtils
import no.nav.sosialhjelp.soknad.db.repositories.soknadmetadata.SoknadMetadataRepository
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoService
import no.nav.sosialhjelp.soknad.personalia.person.PersonService
import no.nav.sosialhjelp.soknad.tekster.NavMessageSource
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
    private val soknadMetadataRepository: SoknadMetadataRepository = mockk()
    private val nedetidService: NedetidService = mockk()

    private val ressurs = InformasjonRessurs(
        messageSource,
        mockk(),
        kommuneInfoService,
        personService,
        soknadMetadataRepository,
        mockk(),
        nedetidService
    )

    private var norskBokmaal = Locale("nb", "NO")

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
        val mappedeKommuneStatuser = ressurs.mapManueltPakobledeKommunerTilKommunestatusFrontend(manuelleKommuner)

        val kommune1234 = mappedeKommuneStatuser["1234"]
        assertThat(kommune1234).isNotNull
        assertThat(kommune1234?.kanMottaSoknader).isTrue
        assertThat(kommune1234?.kanOppdatereStatus).isFalse
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

        val mappedeKommuneStatuser = ressurs.mapDigisosKommunerTilKommunestatus(digisosKommuner)

        val kommune1234 = mappedeKommuneStatuser["1234"]
        assertThat(kommune1234).isNotNull
        assertThat(kommune1234?.kanMottaSoknader).isTrue
        assertThat(kommune1234?.kanOppdatereStatus).isTrue
        assertThat(kommune1234?.harMidlertidigDeaktivertMottak).isFalse
        assertThat(kommune1234?.harMidlertidigDeaktivertOppdateringer).isFalse
        assertThat(kommune1234?.harNksTilgang).isFalse

        val kommune5678 = mappedeKommuneStatuser["5678"]
        assertThat(kommune5678).isNotNull
        assertThat(kommune5678?.kanMottaSoknader).isTrue
        assertThat(kommune5678?.kanOppdatereStatus).isTrue
        assertThat(kommune5678?.harMidlertidigDeaktivertMottak).isTrue
        assertThat(kommune5678?.harMidlertidigDeaktivertOppdateringer).isFalse
        assertThat(kommune5678?.harNksTilgang).isFalse
    }

    @Test
    fun duplikatIDigisosKommuneSkalOverskriveManuellKommune() {
        val manuelleKommuner = listOf("1234")
        val manueltMappedeKommuner = ressurs.mapManueltPakobledeKommunerTilKommunestatusFrontend(manuelleKommuner)

        assertThat(manueltMappedeKommuner["1234"]!!.kanOppdatereStatus).isFalse // Manuelle kommuner får ikke innsyn

        val digisosKommuner: MutableMap<String, KommuneInfo> = HashMap()
        digisosKommuner["1234"] = KommuneInfo(
            kommunenummer = "1234",
            kanMottaSoknader = true,
            kanOppdatereStatus = true,
            harMidlertidigDeaktivertMottak = false,
            harMidlertidigDeaktivertOppdateringer = false,
            kontaktpersoner = null,
            harNksTilgang = false,
            behandlingsansvarlig = null
        )

        val mappedeKommuneStatuser = ressurs.mapDigisosKommunerTilKommunestatus(digisosKommuner)
        val mergedKommuneStatuser = ressurs.mergeManuelleKommunerMedDigisosKommunerKommunestatus(manueltMappedeKommuner, mappedeKommuneStatuser)
        assertThat(mergedKommuneStatuser).hasSize(1)
        assertThat(mergedKommuneStatuser["1234"]?.kanOppdatereStatus).isTrue
    }

    @Test
    fun harNyligInnsendteSoknader_tomResponse() {
        every {
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any())
        } returns emptyList()

        val response = ressurs.harNyligInnsendteSoknader()

        assertThat(response.antallNyligInnsendte).isZero
    }

    @Test
    fun harNyligInnsendteSoknader_flereSoknaderResponse() {
        every {
            soknadMetadataRepository.hentInnsendteSoknaderForBrukerEtterTidspunkt(any(), any())
        } returns listOf(mockk(), mockk())

        val response = ressurs.harNyligInnsendteSoknader()

        assertThat(response.antallNyligInnsendte).isEqualTo(2)
    }

    companion object {
        const val SOKNADSTYPE = "type"
    }
}
