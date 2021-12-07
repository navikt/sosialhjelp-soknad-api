package no.nav.sosialhjelp.soknad.common.systemdata

import no.nav.sosialhjelp.soknad.domain.SoknadUnderArbeid
import org.springframework.stereotype.Component

interface Systemdata {
    fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid)
}

@Component
open class SystemdataUpdater(private val systemdatas: List<Systemdata>) {
    open fun update(soknadUnderArbeid: SoknadUnderArbeid) {
        systemdatas.forEach {
            it.updateSystemdataIn(soknadUnderArbeid)
        }
    }
}
