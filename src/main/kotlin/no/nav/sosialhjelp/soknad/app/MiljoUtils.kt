package no.nav.sosialhjelp.soknad.app

object MiljoUtils {

    private const val NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME"
    private const val NAIS_APP_IMAGE = "NAIS_APP_IMAGE"
    private const val ENVIRONMENT_NAME = "ENVIRONMENT_NAME"
    private const val SPRING_PROFILES_ACTIVE = "SPRING_PROFILES_ACTIVE"

    val environmentName: String
        get() = System.getenv(ENVIRONMENT_NAME) ?: ""

    val appImageVersion: String
        get() = System.getenv(NAIS_APP_IMAGE) ?: "version"

    fun isNonProduction(): Boolean {
        // Bruk isNonProduction() -sjekk fremfor å sjekke om miljø configurert som prod. På denne måten er default-configurasjon vår alltid prodlik.
        // Slik at ved evt. endringer eller feilkonfigurasjoner, vil ikke prod bli ødelagt. Feks. ved sende ekte søknader til testkommuner som ikke finnes.
        // Prod-konfigurasjon i test vil oppdages raskt og man vil ikke klare å skape problemer for prod da man trenger secrets som ikke er tilgjengelig i testmiljøer.
        val clusterName = System.getenv(NAIS_CLUSTER_NAME)
        return clusterName == null || !clusterName.contains("prod")
    }

    fun isMockAltProfil(): Boolean {
        val value = System.getenv(SPRING_PROFILES_ACTIVE) ?: ""
        return value.contains("mock-alt")
    }
}
