package no.nav.sosialhjelp.soknad.innsending.digisosapi

import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import org.springframework.stereotype.Component

@Component
class DigisosApiV2Service {

    fun sendSoknad(soknadUnderArbeid: SoknadUnderArbeid, token: String?, kommunenummer: String): String {
        // todo implement
        return ""
    }
}
