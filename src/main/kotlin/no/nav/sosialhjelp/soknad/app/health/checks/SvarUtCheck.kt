package no.nav.sosialhjelp.soknad.app.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import no.nav.sosialhjelp.soknad.innsending.svarut.client.SvarUtClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SvarUtCheck(
    @Value("\${svarut_url}") private var svarUtUrl: String,
    private val svarUtClient: SvarUtClient
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "SvarUt"
    override val address = svarUtUrl
    override val importance = Importance.WARNING

    override fun doCheck() {
        svarUtClient.ping()
    }
}
