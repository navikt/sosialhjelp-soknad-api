package no.nav.sosialhjelp.soknad.v2.register

import no.nav.sosialhjelp.soknad.v2.eier.Eier
import java.util.UUID

interface RegisterDataFetcher {
    fun hentEierData(): Eier

    fun updateRegisterData()

    fun updateRegisterData(soknadId: UUID?)
}
