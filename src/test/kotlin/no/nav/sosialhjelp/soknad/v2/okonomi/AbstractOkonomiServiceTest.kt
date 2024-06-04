package no.nav.sosialhjelp.soknad.v2.okonomi

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import no.nav.sosialhjelp.soknad.v2.soknad.SoknadRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test", "test-container")
abstract class AbstractOkonomiServiceTest {
    protected lateinit var soknad: Soknad

    @Autowired
    protected lateinit var soknadRepository: SoknadRepository

    @Autowired
    protected lateinit var okonomiRepository: OkonomiRepository

    @Autowired
    protected lateinit var dokumentasjonRepository: DokumentasjonRepository

    @BeforeEach
    fun setup() {
        soknad = soknadRepository.save(Soknad(eierPersonId = "1234561212345"))
    }
}
