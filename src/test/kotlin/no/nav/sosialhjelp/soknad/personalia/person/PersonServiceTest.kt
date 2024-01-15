package no.nav.sosialhjelp.soknad.personalia.person

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.soknad.personalia.person.domain.Barn
import no.nav.sosialhjelp.soknad.personalia.person.domain.Ektefelle
import no.nav.sosialhjelp.soknad.personalia.person.domain.MapperHelper
import no.nav.sosialhjelp.soknad.personalia.person.domain.PdlDtoMapper
import no.nav.sosialhjelp.soknad.personalia.person.domain.Person
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EndringDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.ForelderBarnRelasjonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import no.nav.sosialhjelp.soknad.personalia.person.dto.MetadataDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.SivilstandType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class PersonServiceTest {

    companion object {
        private const val BARN_IDENT = "11111111111"
        private const val EKTEFELLE_IDENT = "22222222222"
        private const val FDAT_IDENT = "11122200000"
    }

    private val person = Person(
        fornavn = "fornavn",
        mellomnavn = "mellomnavn",
        etternavn = "etternavn",
        fnr = "fnr",
        sivilstatus = "ugift",
        statsborgerskap = emptyList(),
        ektefelle = null,
        bostedsadresse = null,
        oppholdsadresse = null,
    )
    private val ektefelle = Ektefelle(
        fornavn = "fornavn",
        mellomnavn = null,
        etternavn = "etternavn",
        fodselsdato = LocalDate.now(),
        fnr = "fnr2",
        folkeregistrertSammen = true,
        ikkeTilgangTilEktefelle = false,
    )
    private val barn = Barn(
        fornavn = "fornavn",
        mellomnavn = null,
        etternavn = "etternavn",
        fnr = "barnident",
        fodselsdato = null,
        folkeregistrertSammen = true,
    )

    private val hentPersonClient: HentPersonClient = mockk()
    private val mapper: PdlDtoMapper = mockk()
    private val helper: MapperHelper = MapperHelper()
    private val personService = PersonService(hentPersonClient, helper, mapper)

    private val mockPersonDto = mockk<PersonDto>()
    private val mockEktefelleDto = mockk<EktefelleDto>()
    private val mockBarnDto = mockk<BarnDto>()

    private val defaultMetadataDto = MetadataDto(
        master = "PDL",
        endringer = listOf(
            EndringDto(kilde = "PDL", registrert = LocalDateTime.now(), type = "type"),
        ),
    )

    @BeforeEach
    internal fun setUp() {
        clearAllMocks()
    }

    @Test
    fun skalHentePersonMedEktefelle() {
        every { hentPersonClient.hentPerson(any()) } returns mockPersonDto
        every { mapper.personDtoToDomain(any(), any()) } returns person
        every { mockPersonDto.sivilstand } returns listOf(
            SivilstandDto(
                type = SivilstandType.GIFT,
                relatertVedSivilstand = EKTEFELLE_IDENT,
                metadata = defaultMetadataDto,
                folkeregistermetadata = null,
            ),
        )
        every { hentPersonClient.hentEktefelle(any()) } returns mockEktefelleDto
        every { mapper.ektefelleDtoToDomain(any(), any(), any()) } returns ektefelle

        val result = personService.hentPerson("ident")
        assertThat(result!!.ektefelle).isEqualTo(ektefelle)
    }

    @Test
    internal fun skalHentePersonMenIkkeEktefelleHvisEktefelleidentErNull() {
        every { hentPersonClient.hentPerson(any()) } returns mockPersonDto
        every { mapper.personDtoToDomain(any(), any()) } returns person
        every { mockPersonDto.sivilstand } returns listOf(
            SivilstandDto(
                type = SivilstandType.GIFT,
                relatertVedSivilstand = null,
                metadata = defaultMetadataDto,
                folkeregistermetadata = null,
            ),
        )

        val result = personService.hentPerson("ident")
        assertThat(result!!.ektefelle).isNull()

        verify(exactly = 0) { hentPersonClient.hentEktefelle(any()) }
        verify(exactly = 0) { mapper.ektefelleDtoToDomain(any(), any(), any()) }
    }

    @Test
    internal fun skalHentePersonMenIkkeEktefelleHvisEktefelleidentErFDAT() {
        every { hentPersonClient.hentPerson(any()) } returns mockPersonDto
        every { mapper.personDtoToDomain(any(), any()) } returns person
        every { mockPersonDto.sivilstand } returns listOf(
            SivilstandDto(
                type = SivilstandType.GIFT,
                relatertVedSivilstand = FDAT_IDENT,
                metadata = defaultMetadataDto,
                folkeregistermetadata = null,
            ),
        )

        val result = personService.hentPerson("ident")
        assertThat(result!!.ektefelle).isNull()

        verify(exactly = 0) { hentPersonClient.hentEktefelle(any()) }
        verify(exactly = 0) { mapper.ektefelleDtoToDomain(any(), any(), any()) }
    }

    @Test
    internal fun skalHentePersonUtenEktefelle() {
        every { hentPersonClient.hentPerson(any()) } returns mockPersonDto
        every { mapper.personDtoToDomain(any(), any()) } returns person
        every { mockPersonDto.sivilstand } returns emptyList()

        val result = personService.hentPerson("ident")
        assertThat(result!!.ektefelle).isNull()
    }

    @Test
    internal fun skalHenteBarn() {
        every { hentPersonClient.hentPerson(any()) } returns mockPersonDto
        every { mockPersonDto.forelderBarnRelasjon } returns listOf(ForelderBarnRelasjonDto(BARN_IDENT, "BARN", "MOR"))
        every { hentPersonClient.hentBarn(any()) } returns mockBarnDto
        every { mapper.barnDtoToDomain(any(), any(), any()) } returns barn

        val result = personService.hentBarnForPerson("ident")
        assertThat(result).hasSize(1)
        assertThat(result!![0]).isEqualTo(barn)
    }

    @Test
    internal fun skalFiltrereVekkNullBarn() {
        every { hentPersonClient.hentPerson(any()) } returns mockPersonDto
        every { mockPersonDto.forelderBarnRelasjon } returns listOf(ForelderBarnRelasjonDto(BARN_IDENT, "BARN", "MOR"))
        every { hentPersonClient.hentBarn(any()) } returns mockBarnDto
        every { mapper.barnDtoToDomain(any(), any(), any()) } returns null

        val result = personService.hentBarnForPerson("ident")
        assertThat(result).isEmpty()
    }

    @Test
    internal fun skalIkkeHenteBarnHvisIdentErNull() {
        every { hentPersonClient.hentPerson(any()) } returns mockPersonDto
        every { mockPersonDto.forelderBarnRelasjon } returns listOf(ForelderBarnRelasjonDto(null, "BARN", "MOR"))

        val result = personService.hentBarnForPerson("ident")
        assertThat(result).isNull()

        verify(exactly = 0) { hentPersonClient.hentBarn(any()) }
        verify(exactly = 0) { mapper.barnDtoToDomain(any(), any(), any()) }
    }

    @Test
    internal fun skalIkkeHenteBarnHvisIdentErFDAT() {
        every { hentPersonClient.hentPerson(any()) } returns mockPersonDto
        every { mockPersonDto.forelderBarnRelasjon } returns listOf(ForelderBarnRelasjonDto(FDAT_IDENT, "BARN", "MOR"))

        val result = personService.hentBarnForPerson("ident")
        assertThat(result).isNull()

        verify(exactly = 0) { hentPersonClient.hentBarn(any()) }
        verify(exactly = 0) { mapper.barnDtoToDomain(any(), any(), any()) }
    }

    @Test
    internal fun skalHenteAdressebeskyttelse() {
        val adressebeskyttelse = mockk<PersonAdressebeskyttelseDto>()
        every { hentPersonClient.hentAdressebeskyttelse(any()) } returns adressebeskyttelse
        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns Gradering.UGRADERT

        val result = personService.hentAdressebeskyttelse("ident")
        assertThat(result).isNotNull
        assertThat(result).isEqualTo(Gradering.UGRADERT)
    }

    @Test
    internal fun gjenkjennerAdressebeskyttelse() {
        every { hentPersonClient.hentAdressebeskyttelse(any()) } returns mockk<PersonAdressebeskyttelseDto>()

        // Ihht. https://pdl-docs.intern.nav.no/ekstern/index.html#_adressebeskyttelse er det som oftest null,
        // hvilket betyr ingen addressebeskyttelse.
        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns null
        assertThat(personService.harAdressebeskyttelse("ident")).isFalse()

        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns Gradering.UGRADERT
        assertThat(personService.harAdressebeskyttelse("ident")).isFalse()

        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns Gradering.FORTROLIG
        assertThat(personService.harAdressebeskyttelse("ident")).isTrue()

        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns Gradering.STRENGT_FORTROLIG
        assertThat(personService.harAdressebeskyttelse("ident")).isTrue()

        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns Gradering.STRENGT_FORTROLIG_UTLAND
        assertThat(personService.harAdressebeskyttelse("ident")).isTrue()
    }
}
