package no.nav.sosialhjelp.soknad.app.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import no.nav.sosialhjelp.soknad.personalia.telefonnummer.KrrClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class KrrCheck(
    @Value("\${krr_url}") private val krrUrl: String,
    private val krrClient: KrrClient
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "digdir-krr-proxy"
    override val address = "$krrUrl/rest/ping"
    override val importance = Importance.WARNING

    override fun doCheck() {
        krrClient.ping()
    }
}
