package no.nav.sosialhjelp.soknad.app.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import no.nav.sosialhjelp.soknad.inntekt.skattbarinntekt.SkatteetatenClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SkatteetatenCheck(
    @Value("\${skatteetaten_api_baseurl}") private val skatteetatenUrl: String,
    private val skatteetatenClient: SkatteetatenClient
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "SkatteetatenApi"
    override val address = skatteetatenUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        skatteetatenClient.ping()
    }
}
