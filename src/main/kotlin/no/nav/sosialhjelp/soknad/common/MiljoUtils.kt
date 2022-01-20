package no.nav.sosialhjelp.soknad.common

object MiljoUtils {

    private const val NAIS_APP_IMAGE = "NAIS_APP_IMAGE"
    private const val NAIS_APP_NAME = "NAIS_APP_NAME"

    val naisAppImage: String
        get() = getenv(NAIS_APP_IMAGE, "version")

    val naisAppName: String
        get() = getenv(NAIS_APP_NAME, "sosialhjelp-soknad-api")

    private fun getenv(env: String, defaultValue: String): String {
        return try {
            System.getenv(env)
        } catch (e: Exception) {
            defaultValue
        }
    }
}