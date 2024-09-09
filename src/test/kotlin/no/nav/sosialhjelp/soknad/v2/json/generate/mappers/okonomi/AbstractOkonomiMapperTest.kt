package no.nav.sosialhjelp.soknad.v2.json.generate.mappers.okonomi

import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomi
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomiopplysninger
import no.nav.sbl.soknadsosialhjelp.soknad.okonomi.JsonOkonomioversikt
import org.junit.jupiter.api.BeforeEach

abstract class AbstractOkonomiMapperTest {
    protected lateinit var jsonOkonomi: JsonOkonomi

    @BeforeEach
    fun setup() {
        jsonOkonomi =
            JsonOkonomi()
                .withOversikt(JsonOkonomioversikt())
                .withOpplysninger(JsonOkonomiopplysninger())
    }
}
