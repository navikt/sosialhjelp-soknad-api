package no.nav.sosialhjelp.soknad.vedlegg.konvertering

import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(profiles = ["no-interceptor", "no-redis", "test"])
class FileConverterTest {

    @MockK
    private lateinit var fileConverter: FileConverter

    @Test
    fun `Konvertere fil som ikke stottes skal gi`() {



    }
}