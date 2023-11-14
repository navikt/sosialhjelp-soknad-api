package no.nav.sosialhjelp.soknad.personalia.person

import io.mockk.every
import io.mockk.mockk
import no.nav.sosialhjelp.soknad.personalia.person.domain.PdlDtoMapper
import no.nav.sosialhjelp.soknad.personalia.person.dto.Gradering
import no.nav.sosialhjelp.soknad.personalia.person.dto.PersonAdressebeskyttelseDto
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class AdressebeskyttelseServiceTest {
    private val hentPersonClient: HentPersonClient = mockk()
    private val mapper: PdlDtoMapper = mockk()
    private val adressebeskyttelseService = AdressebeskyttelseService(hentPersonClient, mapper)

    @Test
    internal fun skalHenteAdressebeskyttelse() {
        val adressebeskyttelse = mockk<PersonAdressebeskyttelseDto>()
        every { hentPersonClient.hentAdressebeskyttelse(any()) } returns adressebeskyttelse
        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns Gradering.UGRADERT

        val result = adressebeskyttelseService.harAdressebeskyttelse("ident")
        Assertions.assertThat(result).isFalse()
    }

    @Test
    internal fun gjenkjennerAdressebeskyttelse() {
        every { hentPersonClient.hentAdressebeskyttelse(any()) } returns mockk<PersonAdressebeskyttelseDto>()

        // Ihht. https://pdl-docs.intern.nav.no/ekstern/index.html#_adressebeskyttelse er det som oftest null,
        // hvilket betyr ingen addressebeskyttelse.
        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns null
        Assertions.assertThat(adressebeskyttelseService.harAdressebeskyttelse("ident")).isFalse()

        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns Gradering.UGRADERT
        Assertions.assertThat(adressebeskyttelseService.harAdressebeskyttelse("ident")).isFalse()

        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns Gradering.FORTROLIG
        Assertions.assertThat(adressebeskyttelseService.harAdressebeskyttelse("ident")).isTrue()

        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns Gradering.STRENGT_FORTROLIG
        Assertions.assertThat(adressebeskyttelseService.harAdressebeskyttelse("ident")).isTrue()

        every { mapper.personAdressebeskyttelseDtoToGradering(any()) } returns Gradering.STRENGT_FORTROLIG_UTLAND
        Assertions.assertThat(adressebeskyttelseService.harAdressebeskyttelse("ident")).isTrue()
    }
}
