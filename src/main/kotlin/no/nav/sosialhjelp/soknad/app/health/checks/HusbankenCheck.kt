package no.nav.sosialhjelp.soknad.app.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import no.nav.sosialhjelp.soknad.app.MiljoUtils
import no.nav.sosialhjelp.soknad.inntekt.husbanken.HusbankenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class HusbankenCheck(
    @Value("\${soknad.bostotte.url}") private val bostotteBaseUrl: String,
    private val husbankenClient: HusbankenClient,
) : DependencyCheck {
    override val type = DependencyType.REST
    override val name = "Husbanken"
    override val address = "$bostotteBaseUrl/ping"
    override val importance = Importance.WARNING

    override fun doCheck() {
        // TODO Skal vi disable alle selftest-sjekker i test-systemer ?
        if (MiljoUtils.isProduction()) {
            husbankenClient.ping()
        }
    }
}
