package no.nav.sosialhjelp.soknad.common

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
open class ServiceUtils(
    @Value("\${digisosapi.sending.enable}") private val digisosSendingEnabled: Boolean,
    @Value("\${digisosapi.sending.alltidTilTestkommune.enable}") private val alltidSendTilTestkommune: Boolean,
    @Value("\${digisosapi.henting.alltidTestkommune.enable}") private val alltidHentTestkommune: Boolean,
    private val env: Environment
) {

    companion object {
        private const val NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME"
        private const val ENVIRONMENT_NAME = "ENVIRONMENT_NAME"
    }

    open fun isNonProduction(): Boolean {
        // Bruk isNonProduction() -sjekk fremfor å sjekke om miljø configurert som prod. På denne måten er default-configurasjon vår alltid prodlik.
        // Slik at ved evt. endringer eller feilkonfigurasjoner, vil ikke prod bli ødelagt. Feks. ved sende ekte søknader til testkommuner som ikke finnes.
        // Prod-konfigurasjon i test vil oppdages raskt og man vil ikke klare å skape problemer for prod da man trenger secrets som ikke er tilgjengelig i testmiljøer.
        val clusterName = System.getenv(NAIS_CLUSTER_NAME)
        return clusterName == null || !clusterName.contains("prod")
    }

    open fun isSendingTilFiksEnabled(): Boolean {
        return digisosSendingEnabled
    }

    open fun isMockAltProfil(): Boolean {
        return env.activeProfiles.any { it.equals("mock-alt") || it.equals("test") }
    }

    open fun isAlltidSendTilNavTestkommune(): Boolean {
        return alltidSendTilTestkommune
    }

    open fun isAlltidHentKommuneInfoFraNavTestkommune(): Boolean {
        return alltidHentTestkommune
    }

    open val environmentName: String
        get() = System.getenv(ENVIRONMENT_NAME) ?: ""
}
