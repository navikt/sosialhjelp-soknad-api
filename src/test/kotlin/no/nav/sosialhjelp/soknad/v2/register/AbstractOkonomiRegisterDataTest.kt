package no.nav.sosialhjelp.soknad.v2.register

import no.nav.sosialhjelp.soknad.v2.dokumentasjon.DokumentasjonRepository
import no.nav.sosialhjelp.soknad.v2.okonomi.OkonomiRepository
import org.springframework.beans.factory.annotation.Autowired

abstract class AbstractOkonomiRegisterDataTest : AbstractRegisterDataTest() {
    @Autowired
    protected lateinit var okonomiRepository: OkonomiRepository

    @Autowired
    protected lateinit var dokumentasjonRepository: DokumentasjonRepository
}
