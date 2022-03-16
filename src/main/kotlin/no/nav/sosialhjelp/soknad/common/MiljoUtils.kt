package no.nav.sosialhjelp.soknad.common

import no.nav.sosialhjelp.kotlin.utils.logger

object MiljoUtils {

    private const val NAIS_APP_NAME = "NAIS_APP_NAME"
    private const val NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME"
    private const val ENVIRONMENT_NAME = "ENVIRONMENT_NAME"

    private const val IS_ALLTID_SEND_TIL_NAV_TESTKOMMUNE = "IS_ALLTID_SEND_TIL_NAV_TESTKOMMUNE"
    private const val IS_ALLTID_HENT_KOMMUNEINFO_FRA_NAV_TESTKOMMUNE = "IS_ALLTID_HENT_KOMMUNEINFO_FRA_NAV_TESTKOMMUNE"
    private const val SPRING_PROFILES_ACTIVE = "SPRING_PROFILES_ACTIVE"
    private const val IN_MEMORY_DATABASE = "IN_MEMORY_DATABASE"

    private val log by logger()

    val naisAppName: String
        get() = getenv(NAIS_APP_NAME, "sosialhjelp-soknad-api")

    val environmentName: String
        get() = System.getenv(ENVIRONMENT_NAME) ?: ""

    private fun getenv(env: String, defaultValue: String): String {
        return System.getenv(env)
            ?: (defaultValue.also { log.warn("Fant ikke env variabel ($env), bruker default verdi: ($defaultValue)") })
    }

    fun isNonProduction(): Boolean {
        // Bruk isNonProduction() -sjekk fremfor å sjekke om miljø configurert som prod. På denne måten er default-configurasjon vår alltid prodlik.
        // Slik at ved evt. endringer eller feilkonfigurasjoner, vil ikke prod bli ødelagt. Feks. ved sende ekte søknader til testkommuner som ikke finnes.
        // Prod-konfigurasjon i test vil oppdages raskt og man vil ikke klare å skape problemer for prod da man trenger secrets som ikke er tilgjengelig i testmiljøer.
        val clusterName = System.getenv(NAIS_CLUSTER_NAME)
        return clusterName == null || !clusterName.contains("prod")
    }

    fun isAlltidSendTilNavTestkommune(): Boolean {
        val value = System.getenv(IS_ALLTID_SEND_TIL_NAV_TESTKOMMUNE) ?: "false"
        return value.toBoolean()
    }

    fun isAlltidHentKommuneInfoFraNavTestkommune(): Boolean {
        val value = System.getenv(IS_ALLTID_HENT_KOMMUNEINFO_FRA_NAV_TESTKOMMUNE) ?: "false"
        return value.toBoolean()
    }

    fun isMockAltProfil(): Boolean {
        val value = System.getenv(SPRING_PROFILES_ACTIVE) ?: ""
        return value.contains("mock-alt")
    }

    fun isRunningWithInMemoryDb(): Boolean {
        val value = System.getenv(IN_MEMORY_DATABASE) ?: "false"
        return value.toBoolean()
    }
}
