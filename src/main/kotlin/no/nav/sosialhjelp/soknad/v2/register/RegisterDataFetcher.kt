package no.nav.sosialhjelp.soknad.v2.register

import no.nav.sosialhjelp.soknad.v2.soknad.Eier
import java.util.*

interface RegisterDataFetcher {
    fun hentEierData(): Eier
    fun updateRegisterData()
    fun updateRegisterData(soknadId: UUID?)
}
