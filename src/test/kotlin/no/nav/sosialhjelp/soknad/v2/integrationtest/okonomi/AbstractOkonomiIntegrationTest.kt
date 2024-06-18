package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRepository
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractOkonomiIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    protected lateinit var okonomiRepository: OkonomiRepository

    @Autowired
    protected lateinit var dokRepository: DokumentasjonRepository

    protected lateinit var soknad: Soknad

    @BeforeEach
    protected fun setup() {
        soknad = soknadRepository.save(opprettSoknad())
    }
}
