package no.nav.sosialhjelp.soknad.v2.integrationtest.okonomi

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.integrationtest.AbstractIntegrationTest
import no.nav.sosialhjelp.soknad.v2.okonomi.AbstractOkonomiInput
import no.nav.sosialhjelp.soknad.v2.okonomi.ForventetDokumentasjonDto
import no.nav.sosialhjelp.soknad.v2.okonomi.Okonomi
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRepository
import no.nav.sosialhjelp.soknad.v2.opprettSoknad
import no.nav.sosialhjelp.soknad.v2.soknad.IntegrasjonStatusService
import no.nav.sosialhjelp.soknad.v2.soknad.Soknad
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractOkonomiIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    protected lateinit var okonomiRepository: OkonomiRepository

    @Autowired
    protected lateinit var dokRepository: DokumentasjonRepository

    @Autowired
    protected lateinit var integrasjonStatusService: IntegrasjonStatusService

    protected lateinit var soknad: Soknad
    protected lateinit var okonomi: Okonomi

    @BeforeEach
    protected fun setup() {
        soknad = soknadRepository.save(opprettSoknad())
        okonomi =
            okonomiRepository.save(
                Okonomi(soknadId = soknad.id),
            )
    }

    protected fun doPutInputAndReturnDto(input: AbstractOkonomiInput): ForventetDokumentasjonDto {
        return doPut(
            uri = OkonomiskeOpplysningerIntegrationTest.getUrl(soknad.id),
            requestBody = input,
            responseBodyClass = ForventetDokumentasjonDto::class.java,
            soknadId = soknad.id,
        )
    }
}
