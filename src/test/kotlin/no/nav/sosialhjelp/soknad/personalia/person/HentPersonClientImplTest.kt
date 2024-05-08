package no.nav.sosialhjelp.soknad.personalia.person

import no.nav.sosialhjelp.soknad.adressesok.PdlGraphQlMockClientBuilder
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import no.nav.sosialhjelp.soknad.util.mockWebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HentPersonClientImplTest {
    private fun buildClient(responsePath: String): HentPersonClientImpl =
        HentPersonClientImpl(PdlGraphQlMockClientBuilder(mockWebClient(responsePath)))

    @Test
    fun hentPerson() {
        val personDto = buildClient("pdl/pdlPersonResponse.json").hentPerson("someIdent").block()

        assertThat(personDto!!).isNotNull()
        assertThat(personDto.navn!!.first().fornavn).isEqualTo("TEST")
        assertThat(personDto.navn!!.first().etternavn).isEqualTo("PERSON")
        assertThat(personDto.statsborgerskap!!.size).isEqualTo(2)
    }

    @Test
    fun hentEktefelle() {
        val ektefelleDto = buildClient("pdl/pdlEktefelleResponse.json").hentEktefelle("someIdent").block()

        assertThat(ektefelleDto!!).isNotNull()
        assertThat(ektefelleDto.navn!!.first().fornavn).isEqualTo("EKTEFELLE")
        assertThat(ektefelleDto.navn!!.first().etternavn).isEqualTo("PERSON")
    }

    @Test
    fun hentBarn() {
        val barnDto = buildClient("pdl/pdlBarnResponse.json").hentBarn("someIdent").block()

        assertThat(barnDto!!).isNotNull()
        assertThat(barnDto.navn!!.first().fornavn).isEqualTo("BARN")
        assertThat(barnDto.navn!!.first().etternavn).isEqualTo("PERSON")
    }

    @Test
    fun hentAdressebeskyttelseUgradert() {
        val adressebeskyttelseDto = buildClient("pdl/pdlAdressebeskyttelseTomResponse.json").hentAdressebeskyttelse("someIdent").block()

        assertThat(adressebeskyttelseDto!!).isNotNull()
        assertThat(adressebeskyttelseDto.adressebeskyttelse).isEmpty()
    }

    @Test
    fun hentAdressebeskyttelseGradert() {
        val adressebeskyttelseDto = buildClient("pdl/pdlAdressebeskyttelseResponse.json").hentAdressebeskyttelse("someIdent").block()

        assertThat(adressebeskyttelseDto!!).isNotNull()
        assertThat(adressebeskyttelseDto.adressebeskyttelse).isNotEmpty()
        assertThat(adressebeskyttelseDto.adressebeskyttelse!!.first().gradering).isEqualTo(Gradering.STRENGT_FORTROLIG)
    }
}
