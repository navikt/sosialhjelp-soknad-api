package no.nav.sosialhjelp.soknad.consumer.common.rest;

/* Originally from common-java-modules (no.nav.sbl.dialogarena.common.rest) */

import lombok.SneakyThrows;
import no.nav.sosialhjelp.soknad.consumer.common.json.JsonProvider;
import no.nav.sosialhjelp.soknad.consumer.common.rest.client.MetricsConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;

import static org.glassfish.jersey.client.ClientProperties.CONNECT_TIMEOUT;
import static org.glassfish.jersey.client.ClientProperties.FOLLOW_REDIRECTS;
import static org.glassfish.jersey.client.ClientProperties.READ_TIMEOUT;

public final class RestUtils {

    public static final String CSRF_COOKIE_NAVN = "NAV_CSRF_PROTECTION";
    public static final RestConfig DEFAULT_CONFIG = new RestConfig.Builder().build();

    private RestUtils() {
    }

    private static ClientConfig createClientConfig(RestConfig restConfig, String metricName) {
        ClientLogFilter clientLogFilter = new ClientLogFilter(ClientLogFilter.ClientLogFilterConfig.builder()
                .disableMetrics(restConfig.getDisableMetrics())
                .disableParameterLogging(restConfig.getDisableParameterLogging())
                .metricName(metricName)
                .build()
        );

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(new JsonProvider());
        clientConfig.register(clientLogFilter);
        clientConfig.property(FOLLOW_REDIRECTS, false);
        clientConfig.property(CONNECT_TIMEOUT, restConfig.getConnectTimeout());
        clientConfig.property(READ_TIMEOUT, restConfig.getReadTimeout());
        clientConfig.connectorProvider(new MetricsConnectorProvider(clientConfig.getConnectorProvider(), clientLogFilter));
        return clientConfig;
    }

    public static Client createClient() {
        return createClient(DEFAULT_CONFIG, getMetricName());
    }

    public static Client createClient(RestConfig restConfig) {
        return createClient(restConfig, getMetricName());
    }

    private static Client createClient(RestConfig restConfig, String metricName) {
        return new JerseyClientBuilder()
                .sslContext(defaultSSLContext())
                .withConfig(createClientConfig(restConfig, metricName))
                .build();
    }

    @SneakyThrows
    private static SSLContext defaultSSLContext() {
        return SSLContext.getDefault();
    }

    private static String getMetricName() {
        StackTraceElement element = Thread.currentThread().getStackTrace()[3];
        return String.format("rest.client.%s.%s", element.getClassName(), element.getMethodName());
    }
}
