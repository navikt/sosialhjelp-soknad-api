package no.nav.sosialhjelp.soknad.app.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import no.nav.sosialhjelp.soknad.arbeid.AaregClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AaregCheck(
    @Value("\${aareg_url}") private val aaregUrl: String,
    private val aaregClient: AaregClient,
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "aareg-services"
    override val address = "$aaregUrl/ping"
    override val importance = Importance.WARNING

    override fun doCheck() {
        aaregClient.ping()
    }
}
