package no.nav.sosialhjelp.soknad.common.rest

/* Originally from common-java-modules (no.nav.sbl.dialogarena.common.rest) */

import no.nav.sosialhjelp.soknad.common.json.JsonProvider
import no.nav.sosialhjelp.soknad.common.rest.ClientLogFilter.ClientLogFilterConfig
import org.glassfish.jersey.client.ClientConfig
import org.glassfish.jersey.client.ClientProperties
import org.glassfish.jersey.client.JerseyClientBuilder
import javax.net.ssl.SSLContext
import javax.ws.rs.client.Client

object RestUtils {
    const val CSRF_COOKIE_NAVN = "NAV_CSRF_PROTECTION"
    private val DEFAULT_CONFIG = RestConfig()

    private fun createClientConfig(restConfig: RestConfig, metricName: String): ClientConfig {
        val clientLogFilter = ClientLogFilter(
            ClientLogFilterConfig(
                metricName = metricName,
                disableMetrics = restConfig.disableMetrics,
                disableParameterLogging = restConfig.disableParameterLogging
            )
        )
        val clientConfig = ClientConfig()
        clientConfig.register(JsonProvider())
        clientConfig.register(clientLogFilter)
        clientConfig.property(ClientProperties.FOLLOW_REDIRECTS, false)
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, restConfig.connectTimeout)
        clientConfig.property(ClientProperties.READ_TIMEOUT, restConfig.readTimeout)
        clientConfig.connectorProvider(MetricsConnectorProvider(clientConfig.connectorProvider, clientLogFilter))
        return clientConfig
    }

    fun createClient(): Client {
        return createClient(DEFAULT_CONFIG, metricName)
    }

    fun createClient(restConfig: RestConfig): Client {
        return createClient(restConfig, metricName)
    }

    private fun createClient(restConfig: RestConfig, metricName: String): Client {
        return JerseyClientBuilder()
            .sslContext(defaultSSLContext())
            .withConfig(createClientConfig(restConfig, metricName))
            .build()
    }

    private fun defaultSSLContext(): SSLContext {
        return SSLContext.getDefault()
    }

    private val metricName: String
        get() {
            val element = Thread.currentThread().stackTrace[3]
            return String.format("rest.client.%s.%s", element.className, element.methodName)
        }
}
