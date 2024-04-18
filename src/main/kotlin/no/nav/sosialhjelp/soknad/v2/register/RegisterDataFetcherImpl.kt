package no.nav.sosialhjelp.soknad.v2.register

import no.nav.sosialhjelp.soknad.v2.eier.Eier
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RegisterDataFetcherImpl : RegisterDataFetcher {
    override fun hentEierData(): Eier {
        TODO("Not yet implemented")
    }

    override fun updateRegisterData() {
        TODO("Not yet implemented")
    }

    override fun updateRegisterData(soknadId: UUID?) {
        TODO("Not yet implemented")
    }
}
