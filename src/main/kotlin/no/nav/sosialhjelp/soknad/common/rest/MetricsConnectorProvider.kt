package no.nav.sosialhjelp.soknad.common.rest

/* Originally from common-java-modules (no.nav.sbl.dialogarena.common.rest) */

import org.glassfish.jersey.client.ClientRequest
import org.glassfish.jersey.client.ClientResponse
import org.glassfish.jersey.client.spi.AsyncConnectorCallback
import org.glassfish.jersey.client.spi.Connector
import org.glassfish.jersey.client.spi.ConnectorProvider
import java.util.concurrent.Future
import javax.ws.rs.client.Client
import javax.ws.rs.core.Configuration

class MetricsConnectorProvider(
    private val connectorProvider: ConnectorProvider,
    private val clientLogFilter: ClientLogFilter
) :
    ConnectorProvider {
    override fun getConnector(client: Client, runtimeConfig: Configuration): Connector {
        return MetricsConnector(connectorProvider.getConnector(client, runtimeConfig))
    }

    private inner class MetricsConnector(private val connector: Connector) : Connector {
        override fun apply(request: ClientRequest): ClientResponse {
            return try {
                connector.apply(request)
            } catch (t: Throwable) {
                clientLogFilter.requestFailed(request, t)
                throw t
            }
        }

        override fun apply(request: ClientRequest, callback: AsyncConnectorCallback): Future<*> {
            return connector.apply(request, callback)
        }

        override fun getName(): String {
            return connector.name
        }

        override fun close() {
            connector.close()
        }
    }
}
