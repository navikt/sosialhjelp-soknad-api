package no.nav.sosialhjelp.soknad.common.rest

data class RestConfig(
    val connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
    val readTimeout: Int = DEFAULT_READ_TIMEOUT,
    val disableMetrics: Boolean = false,
    val disableParameterLogging: Boolean = false,
) {
    companion object {
        private const val DEFAULT_CONNECT_TIMEOUT = 5000
        private const val DEFAULT_READ_TIMEOUT = 15000
    }
}
