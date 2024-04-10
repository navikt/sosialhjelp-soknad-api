package no.nav.sosialhjelp.soknad.adressesok

import no.nav.sosialhjelp.soknad.util.mockWebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AdressesokClientTest {
    @Test
    fun getAdressesokResult() {
        val mockClientBuilder = PdlGraphQlMockClientBuilder(mockWebClient("pdl/pdlSokAdresseResponse.json"))

        val sokAdresse = AdressesokClient(mockClientBuilder).getAdressesokResult(mapOf("this code" to "must go")).block()

        assertThat(sokAdresse!!).isNotNull
        assertThat(sokAdresse.hits).hasSize(1)
        assertThat(sokAdresse.hits?.get(0)?.score).isZero
        assertThat(sokAdresse.hits?.get(0)?.vegadresse?.adressenavn).isEqualTo("Heggsnipvegen")
    }
}
