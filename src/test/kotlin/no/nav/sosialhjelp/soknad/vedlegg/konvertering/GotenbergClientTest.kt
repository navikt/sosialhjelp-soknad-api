package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import no.nav.sosialhjelp.soknad.util.ExampleFileRepository
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
// @ActiveProfiles(profiles = ["no-interceptor", "no-redis", "test", "test-container"])
class GotenbergClientTest {

//    @Autowired
    private lateinit var fileConverter: FileConverter

    @Disabled("Brukes for manuel feilsøking")
    @Test
    fun testGotenbergClient() {

        val file = ExampleFileRepository.EXCEL_FILE

        val pdf = fileConverter.toPdf(file.name, file.readBytes())
        val a = 4
    }
}
