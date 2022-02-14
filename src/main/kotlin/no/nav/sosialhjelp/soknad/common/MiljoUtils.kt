package no.nav.sosialhjelp.soknad.common

import no.nav.sosialhjelp.kotlin.utils.logger

object MiljoUtils {

    private const val NAIS_APP_IMAGE = "NAIS_APP_IMAGE"
    private const val NAIS_APP_NAME = "NAIS_APP_NAME"
    private const val NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME"

    private val log by logger()

    val naisAppImage: String
        get() = getenv(NAIS_APP_IMAGE, "version")

    val naisAppName: String
        get() = getenv(NAIS_APP_NAME, "sosialhjelp-soknad-api")

    private fun getenv(env: String, defaultValue: String): String {
        return try {
            System.getenv(env)
        } catch (e: Exception) {
            log.warn("Fant ikke env variabel ($env), bruker default verdi: ($defaultValue)")
            defaultValue
        }
    }

    fun isNonProduction(): Boolean {
        // Bruk isNonProduction() -sjekk fremfor å sjekke om miljø configurert som prod. På denne måten er default-configurasjon vår alltid prodlik.
        // Slik at ved evt. endringer eller feilkonfigurasjoner, vil ikke prod bli ødelagt. Feks. ved sende ekte søknader til testkommuner som ikke finnes.
        // Prod-konfigurasjon i test vil oppdages raskt og man vil ikke klare å skape problemer for prod da man trenger secrets som ikke er tilgjengelig i testmiljøer.
        val clusterName = System.getenv(NAIS_CLUSTER_NAME)
        return clusterName == null || !clusterName.contains("prod")
    }
}
