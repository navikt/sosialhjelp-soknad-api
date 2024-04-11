package no.nav.sosialhjelp.soknad.v2

import junit.framework.TestCase.assertTrue
import no.nav.sosialhjelp.soknad.app.exceptions.AuthorizationException
import no.nav.sosialhjelp.soknad.tilgangskontroll.XsrfGenerator
import no.nav.sosialhjelp.soknad.v2.familie.EktefelleInput
import no.nav.sosialhjelp.soknad.v2.familie.FamilieRepository
import no.nav.sosialhjelp.soknad.v2.familie.Sivilstatus
import no.nav.sosialhjelp.soknad.v2.familie.sivilstatus.SivilstandInput
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.navn.Navn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InterceptorTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var familieRepository: FamilieRepository

    @Test
    fun `PUT til familie skal kaste exception hvis bruker ikke har tilgang`() {
        val soknad = soknadRepository.save(opprettSoknad(eierPersonId = "69691337420"))
        val token = mockOAuth2Server.issueToken("selvbetjening", "abc", "someaudience", claims = mapOf("acr" to "idporten-loa-high"))

        val result =
            webTestClient.put()
                .uri("/soknad/${soknad.id}/familie/sivilstatus")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer ${token.serialize()}")
                .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknad.id.toString(), id = token.jwtClaimsSet.subject))
                .body(
                    BodyInserters.fromValue(
                        SivilstandInput(Sivilstatus.GIFT, EktefelleInput("123", Navn(fornavn = "Ola", etternavn = "Nordmann"))),
                    ),
                )
                .exchange()
                .expectStatus().isForbidden
                .expectBody(AuthorizationException::class.java)
                .returnResult()
                .responseBody!!

        assertThat(result.message).isEqualTo("Ikke tilgang til ressurs")
    }

    @Test
    fun `PUT til familie skal ikke kaste exception hvis bruker har tilgang`() {
        val soknad = soknadRepository.save(opprettSoknad(eierPersonId = "69691337420"))
        val token =
            mockOAuth2Server.issueToken(
                "selvbetjening",
                "69691337420",
                "someaudience",
                claims = mapOf("acr" to "idporten-loa-high"),
            )

        webTestClient.put()
            .uri("/soknad/${soknad.id}/familie/sivilstatus")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer ${token.serialize()}")
            .header("X-XSRF-TOKEN", XsrfGenerator.generateXsrfToken(soknad.id.toString(), id = token.jwtClaimsSet.subject))
            .body(
                BodyInserters.fromValue(
                    SivilstandInput(Sivilstatus.GIFT, EktefelleInput("121337", Navn(fornavn = "Ola", etternavn = "Nordmann"))),
                ),
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(Any::class.java)
            .returnResult()
            .responseBody!!

        familieRepository.findById(soknad.id).let {
            assertTrue(it.isPresent)
            assertThat(it.get().ektefelle?.personId).isEqualTo("121337")
        }
    }
}
