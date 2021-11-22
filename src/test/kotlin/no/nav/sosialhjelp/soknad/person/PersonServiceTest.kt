package no.nav.sosialhjelp.soknad.person

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.sosialhjelp.soknad.consumer.pdl.person.PdlHentPersonConsumer
import no.nav.sosialhjelp.soknad.person.domain.Barn
import no.nav.sosialhjelp.soknad.person.domain.Ektefelle
import no.nav.sosialhjelp.soknad.person.domain.MapperHelper
import no.nav.sosialhjelp.soknad.person.domain.PdlDtoMapper
import no.nav.sosialhjelp.soknad.person.domain.Person
import no.nav.sosialhjelp.soknad.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.person.dto.EndringDto
import no.nav.sosialhjelp.soknad.person.dto.ForelderBarnRelasjonDto
import no.nav.sosialhjelp.soknad.person.dto.Gradering
import no.nav.sosialhjelp.soknad.person.dto.MetadataDto
import no.nav.sosialhjelp.soknad.person.dto.PersonAdressebeskyttelseDto
import no.nav.sosialhjelp.soknad.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.person.dto.SivilstandDto
import no.nav.sosialhjelp.soknad.person.dto.SivilstandType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class PersonServiceTest {

    private val BARN_IDENT = "11111111111"
    private val EKTEFELLE_IDENT = "22222222222"
    private val FDAT_IDENT = "11122200000"

    private val person = Person("fornavn", "mellomnavn", "etternavn", "fnr", "ugift", emptyList(), null, null, null, null)
    private val ektefelle = Ektefelle("fornavn", null, "etternavn", LocalDate.now(), "fnr2", true, false)
    private val barn = Barn("fornavn", null, "etternavn", "barnident", null, true)

    private val pdlHentPersonConsumer: PdlHentPersonConsumer = mockk()
    private val mapper: PdlDtoMapper = mockk()
    private val helper: MapperHelper = MapperHelper()
    private val personService = PersonService(pdlHentPersonConsumer, helper, mapper)

    private val mockPersonDto = mockk<PersonDto>()
    private val mockEktefelleDto = mockk<EktefelleDto>()
    private val mockBarnDto = mockk<BarnDto>()

    @Test
    fun skalHentePersonMedEktefelle() {
        every { pdlHentPersonConsumer.hentPerson(any()) } returns mockPersonDto
        every { mapper.personDtoToDomain(any(), any()) } returns person
        every { mockPersonDto.sivilstand } returns listOf(
            SivilstandDto(
                SivilstandType.GIFT,
                EKTEFELLE_IDENT,
                MetadataDto("PDL", listOf(EndringDto("PDL", LocalDateTime.now(), "type"))),
                null
            )
        )
        every { pdlHentPersonConsumer.hentEktefelle(any()) } returns mockEktefelleDto
        every { mapper.ektefelleDtoToDomain(any(), any(), any()) } returns ektefelle

        val result = personService.hentPerson("ident")
        assertThat(result!!.ektefelle).isEqualTo(ektefelle)
    }

    @Test
    internal fun skalHentePersonMenIkkeEktefelleHvisEktefelleidentErNull() {
        every { pdlHentPersonConsumer.hentPerson(any()) } returns mockPersonDto
        every { mapper.personDtoToDomain(any(), any()) } returns person
        every { mockPersonDto.sivilstand } returns listOf(
            SivilstandDto(
                SivilstandType.GIFT,
                null,
                MetadataDto("PDL", listOf(EndringDto("PDL", LocalDateTime.now(), "type"))),
                null
            )
        )

        val result = personService.hentPerson("ident")
        assertThat(result!!.ektefelle).isNull()

        verify(exactly = 0) { pdlHentPersonConsumer.hentEktefelle(any()) }
        verify(exactly = 0) { mapper.ektefelleDtoToDomain(any(), any(), any()) }
    }

    @Test
    internal fun skalHentePersonMenIkkeEktefelleHvisEktefelleidentErFDAT() {
        every { pdlHentPersonConsumer.hentPerson(any()) } returns mockPersonDto
        every { mapper.personDtoToDomain(any(), any()) } returns person
        every { mockPersonDto.sivilstand } returns listOf(
            SivilstandDto(
                SivilstandType.GIFT,
                FDAT_IDENT,
                MetadataDto("PDL", listOf(EndringDto("PDL", LocalDateTime.now(), "type"))),
                null
            )
        )

        val result = personService.hentPerson("ident")
        assertThat(result!!.ektefelle).isNotNull
        assertThat(result.ektefelle?.fnr).isEqualTo(FDAT_IDENT)
        assertThat(result.ektefelle!!.fodselsdato).hasToString(LocalDate.of(1922, 12, 11).toString())

        verify(exactly = 0) { pdlHentPersonConsumer.hentEktefelle(any()) }
        verify(exactly = 0) { mapper.ektefelleDtoToDomain(any(), any(), any()) }
    }

    @Test
    internal fun skalHentePersonUtenEktefelle() {
        every { pdlHentPersonConsumer.hentPerson(any()) } returns mockPersonDto
        every { mapper.personDtoToDomain(any(), any()) } returns person
        every { mockPersonDto.sivilstand } returns emptyList()

        val result = personService.hentPerson("ident")
        assertThat(result!!.ektefelle).isNull()
    }

    @Test
    internal fun skalHenteBarn() {
        every { pdlHentPersonConsumer.hentPerson(any()) } returns mockPersonDto
        every { mockPersonDto.forelderBarnRelasjon } returns listOf(ForelderBarnRelasjonDto(BARN_IDENT, "BARN", "MOR"))
        every { pdlHentPersonConsumer.hentBarn(any()) } returns mockBarnDto
        every { mapper.barnDtoToDomain(any(), any(), any()) } returns barn

        val result = personService.hentBarnForPerson("ident")
        assertThat(result).hasSize(1)
        assertThat(result!![0]).isEqualTo(barn)
    }

    @Test
    internal fun skalFiltrereVekkNullBarn() {
        every { pdlHentPersonConsumer.hentPerson(any()) } returns mockPersonDto
        every { mockPersonDto.forelderBarnRelasjon } returns listOf(ForelderBarnRelasjonDto(BARN_IDENT, "BARN", "MOR"))
        every { pdlHentPersonConsumer.hentBarn(any()) } returns mockBarnDto
        every { mapper.barnDtoToDomain(any(), any(), any()) } returns null

        val result = personService.hentBarnForPerson("ident")
        assertThat(result).isEmpty()
    }

    @Test
    internal fun skalIkkeHenteBarnHvisIdentErFDAT() {
        every { pdlHentPersonConsumer.hentPerson(any()) } returns mockPersonDto
        every { mockPersonDto.forelderBarnRelasjon } returns listOf(ForelderBarnRelasjonDto(FDAT_IDENT, "BARN", "MOR"))

        val result = personService.hentBarnForPerson("ident")
        assertThat(result).isEmpty()

        verify(exactly = 0) { pdlHentPersonConsumer.hentBarn(any()) }
        verify(exactly = 0) { mapper.barnDtoToDomain(any(), any(), any()) }
    }

    @Test
    internal fun skalHenteAdressebeskyttelse() {
        val adressebeskyttelse = mockk<PersonAdressebeskyttelseDto>()
        every { pdlHentPersonConsumer.hentAdressebeskyttelse(any()) } returns adressebeskyttelse
        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns Gradering.UGRADERT

        val result = personService.hentAdressebeskyttelse("ident")
        assertThat(result).isNotNull
        assertThat(result).isEqualTo(Gradering.UGRADERT)
    }
}
