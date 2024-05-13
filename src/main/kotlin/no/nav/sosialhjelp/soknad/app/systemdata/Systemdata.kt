package no.nav.sosialhjelp.soknad.app.systemdata

import no.nav.sosialhjelp.soknad.app.LoggingUtils.logger
import no.nav.sosialhjelp.soknad.db.repositories.soknadunderarbeid.SoknadUnderArbeid
import no.nav.sosialhjelp.soknad.v2.register.RegisterDataService
import org.springframework.stereotype.Component
import java.util.UUID

interface Systemdata {
    fun updateSystemdataIn(soknadUnderArbeid: SoknadUnderArbeid)
}

@Component
class SystemdataUpdater(
    private val systemdatas: List<Systemdata>,
    private val registerDataService: RegisterDataService,
) {
    private val logger by logger()

    fun update(soknadUnderArbeid: SoknadUnderArbeid) {
        systemdatas.forEach {
            it.updateSystemdataIn(soknadUnderArbeid)
        }

        logger.info("NyModell: Starter innhenting av Register-data")
        soknadUnderArbeid.behandlingsId.let {
            registerDataService.runAllRegisterDataFetchers(soknadId = UUID.fromString(it))
        }
    }
}
