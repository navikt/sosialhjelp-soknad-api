package no.nav.sosialhjelp.soknad.v2.register.fetchers

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
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

abstract class AbstractPersonDataFetcherTest : AbstractRegisterDataTest() {
    @Autowired
    protected lateinit var fetchPerson: PersonDataFetcher

    @MockkBean
    protected lateinit var hentPersonClient: HentPersonClient

    @MockkBean
    protected lateinit var kontonummerClient: KontonummerClient

    @MockkBean
    protected lateinit var hentAdresseClient: HentAdresseClient

    @BeforeEach
    fun mockKontonummer() {
        coEvery { kontonummerClient.getKontonummer() } returns kontoDto
    }

    fun createAnswerForHentPersonUgiftMedMatrikkelAdresse(): MatrikkeladresseDto {
        defaultResponseFromHentPerson(
            sivilstandDto = null,
            vegAdresseDto = null,
            matrikkeladresseDto = matrikkeladresseDto,
        ).also { coEvery { hentPersonClient.hentPerson(any(), any()) } returns it }

        return defaultResponseFromHentMatrikkelAdresse().also {
            every { hentAdresseClient.hentMatrikkelAdresse(any()) } returns it
        }
    }

    fun createAnswerForHentPersonUgift(): PersonDto {
        return defaultResponseFromHentPerson(sivilstandDto = null).also {
            coEvery { hentPersonClient.hentPerson(soknad.eierPersonId, any()) } returns it
        }
    }

    fun createAnswerForHentEktefelle(
        fnr: String = ektefelleFnr,
        vegAdresse: VegadresseDto? = vegadresseDto,
    ): EktefelleDto {
        return defaultResponseFromHentEktefelle(vegAdresse).also {
            coEvery { hentPersonClient.hentEktefelle(fnr) } returns it
        }
    }

    fun createAnswerForHentBarn(): List<BarnDto> {
        var offsetYear = 4
        return defaultResponseHentPersonWithEktefelleOgBarn().forelderBarnRelasjon!!
            .map {
                val dto = defaultResponseFromHentBarn(fnr = it.relatertPersonsIdent!!, offsetYear = offsetYear)
                coEvery { hentPersonClient.hentBarn(it.relatertPersonsIdent) } returns dto
                offsetYear += 2
                dto
            }
    }

    protected fun createAnswerForPersonMedEktefelleOgBarn(): FamilieDtos {
        val personDto =
            defaultResponseHentPersonWithEktefelleOgBarn().also {
                coEvery { hentPersonClient.hentPerson(soknad.eierPersonId, "token") } returns it
            }
        val ektefelleDto = createAnswerForHentEktefelle(ektefelleFnr)
        val barnDtoList = createAnswerForHentBarn()

        return FamilieDtos(
            forelder = personDto,
            ektefelle = ektefelleDto,
            barn = barnDtoList,
        )
    }

    protected data class FamilieDtos(
        val forelder: PersonDto,
        val ektefelle: EktefelleDto,
        val barn: List<BarnDto>,
    )
}
