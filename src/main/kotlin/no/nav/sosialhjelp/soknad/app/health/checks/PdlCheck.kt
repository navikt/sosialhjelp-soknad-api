package no.nav.sosialhjelp.soknad.app.health.checks

import no.nav.sosialhjelp.selftest.DependencyCheck
import no.nav.sosialhjelp.selftest.DependencyType
import no.nav.sosialhjelp.selftest.Importance
import no.nav.sosialhjelp.soknad.personalia.adresse.adresseregister.HentAdresseClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PdlCheck(
    @Value("\${pdl_api_url}") private val pdlUrl: String,
    private val hentAdresseClient: HentAdresseClient
) : DependencyCheck {

    override val type = DependencyType.REST
    override val name = "PDL"
    override val address = pdlUrl
    override val importance = Importance.CRITICAL

    override fun doCheck() {
        hentAdresseClient.ping()
    }
}
