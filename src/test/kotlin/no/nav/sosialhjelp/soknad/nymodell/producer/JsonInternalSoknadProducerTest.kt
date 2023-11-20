package no.nav.sosialhjelp.soknad.nymodell.producer

import no.nav.sbl.soknadsosialhjelp.soknad.JsonInternalSoknad
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("no-redis", "test")
class JsonInternalSoknadProducerTest {

    @Autowired
    private lateinit var producer: SoknadProducer<JsonInternalSoknad>

    @Test
    fun `Opprett jsonInternalSoknad`() {
//        val soknadId = UUID.randomUUID()
//        producer.produceNew(soknadId)
    }
}
