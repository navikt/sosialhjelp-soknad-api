package no.nav.sosialhjelp.soknad.v2.register.handlers.person

import no.nav.sosialhjelp.soknad.v2.kontakt.KontaktRepository
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.MatrikkelAdresse
import no.nav.sosialhjelp.soknad.v2.kontakt.adresse.VegAdresse
import no.nav.sosialhjelp.soknad.v2.register.handlers.AbstractHandlePersonTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class HandleAdresseTest: AbstractHandlePersonTest() {

    @Autowired
    private lateinit var kontaktRepository: KontaktRepository

    @Test
    fun `Hente fra PDL skal lagre data i db`() {
        val dto = createAnswerForHentPersonUgift().bostedsadresse?.let { it[0].vegadresse } ?: fail("Fant ikke adresse")

        handlePerson.handle(soknad.id)

        kontaktRepository
            .findByIdOrNull(soknad.id)?.adresser?.folkeregistrert
            ?.let {
                val vegAdresse = it as VegAdresse
                assertThat(vegAdresse.gatenavn).isEqualTo(dto.adressenavn)
                assertThat(vegAdresse.postnummer).isEqualTo(dto.postnummer)
            }
            ?: fail("Fant ikke Kontakt for soknad")
    }

    @Test
    fun `Hente person med matrikkeladresse skal lagres i db`() {
        val dto = createAnswerForHentPersonUgiftMedMatrikkelAdresse()

        handlePerson.handle(soknad.id)

        kontaktRepository.findByIdOrNull(soknad.id)?.adresser?.folkeregistrert?.let {
            assertThat(it).isInstanceOf(MatrikkelAdresse::class.java)
            assertThat((it as MatrikkelAdresse).gaardsnummer).isEqualTo(dto.matrikkelnummer?.gaardsnummer)
        }
            ?: fail("Fant ikke Kontakt i db")
    }
}
