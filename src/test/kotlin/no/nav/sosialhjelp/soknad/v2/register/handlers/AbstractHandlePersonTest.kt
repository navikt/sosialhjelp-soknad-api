package no.nav.sosialhjelp.soknad.v2.register.handlers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseClient
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.dto.MatrikkeladresseDto
import no.nav.sosialhjelp.soknad.personalia.kontonummer.KontonummerClient
import no.nav.sosialhjelp.soknad.personalia.person.HentPersonClient
import no.nav.sosialhjelp.soknad.personalia.person.dto.BarnDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.EktefelleDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonDto
import no.nav.sosialhjelp.soknad.personalia.person.dto.VegadresseDto
import no.nav.sosialhjelp.soknad.v2.register.AbstractRegisterDataTest
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.ektefelleFnr
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.kontoDto
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.matrikkeladresseDto
import no.nav.sosialhjelp.soknad.v2.register.DefaultValuesForMockedResponses.vegadresseDto
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseFromHentBarn
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseFromHentEktefelle
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseFromHentMatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseFromHentPerson
import no.nav.sosialhjelp.soknad.v2.register.defaultResponseHentPersonWithEktefelleOgBarn
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractHandlePersonTest: AbstractRegisterDataTest() {

    @Autowired
    protected lateinit var handlePerson: HandlePerson

    @MockkBean
    protected lateinit var hentPersonClient: HentPersonClient

    @MockkBean
    protected lateinit var kontonummerClient: KontonummerClient

    @MockkBean
    protected lateinit var hentAdresseClient: HentAdresseClient

    @BeforeEach
    fun mockKontonummer() {
        every { kontonummerClient.getKontonummer(any()) } returns kontoDto
    }

    fun createAnswerForHentPersonUgiftMedMatrikkelAdresse(): MatrikkeladresseDto {

        defaultResponseFromHentPerson(
            sivilstandDto = null,
            vegAdresseDto = null,
            matrikkeladresseDto = matrikkeladresseDto
        ).also { every { hentPersonClient.hentPerson(any()) } returns it }

        return defaultResponseFromHentMatrikkelAdresse().also {
            every { hentAdresseClient.hentMatrikkelAdresse(any()) } returns it
        }
    }

    fun createAnswerForHentPersonUgift(): PersonDto {
        return defaultResponseFromHentPerson(sivilstandDto = null).also {
            every { hentPersonClient.hentPerson(soknad.eierPersonId) } returns it
        }
    }

    fun createAnswerForHentPerson(): PersonDto {
        return defaultResponseFromHentPerson().also {
            every { hentPersonClient.hentPerson(soknad.eierPersonId) } returns it
        }
    }

    fun createAnswerForHentEktefelle(
        fnr: String = ektefelleFnr,
        vegAdresse: VegadresseDto? = vegadresseDto
    ): EktefelleDto {
        return defaultResponseFromHentEktefelle(fnr, vegAdresse).also {
            every { hentPersonClient.hentEktefelle(fnr) } returns it
        }
    }

    fun createAnswerForHentBarn(): List<BarnDto> {
        var year = 2006
        return defaultResponseHentPersonWithEktefelleOgBarn().forelderBarnRelasjon!!
            .map {
                val dto = defaultResponseFromHentBarn(fnr = it.relatertPersonsIdent!!, year = year)
                every { hentPersonClient.hentBarn(it.relatertPersonsIdent!!) } returns dto
                year += 2
                dto
            }
    }

    protected fun createAnswerForPersonMedEktefelleOgBarn(): FamilieDtos {
        val personDto = defaultResponseHentPersonWithEktefelleOgBarn().also {
            every { hentPersonClient.hentPerson(soknad.eierPersonId) } returns it
        }
        val ektefelleDto = createAnswerForHentEktefelle(ektefelleFnr)
        val barnDtoList = createAnswerForHentBarn()

        return FamilieDtos(
            forelder = personDto,
            ektefelle = ektefelleDto,
            barn = barnDtoList,
        )
    }

    protected data class FamilieDtos (
        val forelder: PersonDto,
        val ektefelle: EktefelleDto,
        val barn: List<BarnDto>
    )
}
