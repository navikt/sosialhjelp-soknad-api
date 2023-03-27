package no.nav.sosialhjelp.soknad.app.systemdata

import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import org.springframework.stereotype.Component

interface Systemdata {
    fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid)
}

@Component
class SystemdataUpdater(private val systemdatas: List<Systemdata>) {
    fun update(soknadUnderArbeid: SoknadUnderArbeid) {
        systemdatas.forEach {
            it.updateSystemdataIn(soknadUnderArbeid)
        }
    }
}
