package no.nav.sosialhjelp.soknad.integrationtest

import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import no.nav.sosialhjelp.soknad.v2.soknad.Navn
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractIntegrationTest {

    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    fun opprettSoknad(
        personId: String = "1234567890",
        fornavn: String = "Test",
        etternavn: String = "Testesen",
    ): Soknad {
        return Soknad(
            eier = Eier(
                personId = personId,
                navn = Navn(
                    fornavn = fornavn,
                    etternavn = etternavn
                )
            ),
            innsendingstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
        ).run {
            soknadRepository.save(this)
        }
    }
}
