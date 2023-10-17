package no.nav.sosialhjelp.soknad.fullfort

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import java.util.*

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test")
class JsonInternalSoknadCreatorTest {

    @Autowired
    private lateinit var jsonInternalSoknadCreator: JsonInternalSoknadCreator

    @Test
    fun `Opprett jsonInternalSoknad`() {

        jsonInternalSoknadCreator.createJsonInternalSoknad(UUID.randomUUID())

    }

}