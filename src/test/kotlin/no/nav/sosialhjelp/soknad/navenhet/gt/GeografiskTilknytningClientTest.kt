package no.nav.sosialhjelp.soknad.navenhet.gt

import no.nav.sosialhjelp.soknad.adressesok.PdlGraphQlMockClientBuilder
import no.nav.sosialhjelp.soknad.navenhet.gt.dto.GtType
import no.nav.sosialhjelp.soknad.util.mockWebClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GeografiskTilknytningClientTest {
    @Test
    fun `test hentGeografiskTilknytning returns expected response`() {
        val mockClientBuilder = PdlGraphQlMockClientBuilder(mockWebClient("pdl/pdlHentGeografiskTilknytningResponse.json"))
        val result = GeografiskTilknytningClient(mockClientBuilder).hentGeografiskTilknytning("someIdent").block()

        assertThat(result!!).isNotNull()
        assertThat(result.gtType).isEqualTo(GtType.BYDEL)
        assertThat(result.gtBydel).isEqualTo("030108")
        assertThat(result.gtKommune).isNull()
        assertThat(result.gtLand).isNull()
    }
}
