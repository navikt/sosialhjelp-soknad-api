package no.nav.sosialhjelp.soknad.app.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import no.nav.sosialhjelp.soknad.innsending.digisosapi.kommuneinfo.KommuneInfoClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class DigisosApiCheck(
    @Value("\${digisos_api_baseurl}") private val digisosApiEndpoint: String,
    private val kommuneInfoClient: KommuneInfoClient
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "Fiks Digisos API"
    override val address = digisosApiEndpoint
    override val importance = Importance.CRITICAL

    override fun doCheck() {
        kommuneInfoClient.ping()
    }
}
