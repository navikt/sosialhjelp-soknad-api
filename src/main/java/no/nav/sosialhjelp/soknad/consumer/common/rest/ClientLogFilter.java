//package no.nav.sosialhjelp.soknad.consumer.common.rest;
//
///* Originally from common-java-modules (no.nav.sbl.dialogarena.common.rest) */
//
//import lombok.Builder;
//import lombok.Value;
//import lombok.With;
//import no.nav.sosialhjelp.metrics.MetricsFactory;
//import no.nav.sosialhjelp.metrics.Timer;
//import no.nav.sosialhjelp.soknad.web.utils.MiljoUtils;
//import org.apache.commons.lang3.exception.ExceptionUtils;
//import org.slf4j.Logger;
//
//import javax.ws.rs.client.ClientRequestContext;
//import javax.ws.rs.client.ClientRequestFilter;
//import javax.ws.rs.client.ClientResponseContext;
//import javax.ws.rs.client.ClientResponseFilter;
//import javax.ws.rs.core.Cookie;
//import javax.ws.rs.core.MultivaluedMap;
//import javax.ws.rs.core.UriBuilder;
//import java.io.IOException;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Optional;
//
//import static java.util.Arrays.stream;
//import static java.util.Optional.ofNullable;
//import static java.util.concurrent.TimeUnit.MILLISECONDS;
//import static java.util.stream.Collectors.joining;
//import static javax.ws.rs.core.HttpHeaders.COOKIE;
//import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.MDC_CALL_ID;
//import static no.nav.sosialhjelp.soknad.consumer.mdc.MDCOperations.getFromMDC;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.HEADER_CONSUMER_ID;
//import static no.nav.sosialhjelp.soknad.domain.model.util.HeaderConstants.NAV_CALL_ID_HEADER_NAMES;
//import static org.slf4j.LoggerFactory.getLogger;
//
//public class ClientLogFilter implements ClientResponseFilter, ClientRequestFilter {
//
//    private static final Logger LOG = getLogger(ClientLogFilter.class);
//    private static final String NAME = ClientLogFilter.class.getName();
//    private static final String CSRF_TOKEN = "csrf-token";
//
//    private final ClientLogFilterConfig filterConfig;
//
//    public ClientLogFilter(final ClientLogFilterConfig filterConfig) {
//        this.filterConfig = filterConfig;
//    }
//
//    @Override
//    public void filter(ClientRequestContext clientRequestContext) throws IOException {
//        LOG.info("{} {}", clientRequestContext.getMethod(), uriForLogging(clientRequestContext));
//
//        MultivaluedMap<String, Object> requestHeaders = clientRequestContext.getHeaders();
//
//        Optional.ofNullable(getFromMDC(MDC_CALL_ID)).ifPresent(callId -> stream(NAV_CALL_ID_HEADER_NAMES).forEach(headerName -> requestHeaders.add(headerName, callId)));
//        requestHeaders.add(HEADER_CONSUMER_ID, MiljoUtils.getNaisAppName());
//        requestHeaders.add(RestUtils.CSRF_COOKIE_NAVN, CSRF_TOKEN);
//        requestHeaders.add(COOKIE, new Cookie(RestUtils.CSRF_COOKIE_NAVN, CSRF_TOKEN));
//
//
//        // jersey-client generates cookies in org.glassfish.jersey.message.internal.CookieProvider according to the
//        // deprecated rfc2109 specification, which prefixes the cookie with its version. This may not be supported by modern servers.
//        // Therefore we serialize cookies on the more modern and simpler rfc6265-format
//        // https://www.ietf.org/rfc/rfc2109.txt
//        // https://tools.ietf.org/html/rfc6265
//        requestHeaders.replace(COOKIE, new ArrayList<>(Arrays.asList(
//                requestHeaders.get(COOKIE)
//                        .stream()
//                        .map(this::toCookieString)
//                        .collect(joining("; "))
//        )));
//
//
//        if (!filterConfig.disableMetrics) {
//            Timer timer = MetricsFactory.createTimer(filterConfig.metricName);
//            timer.start();
//            clientRequestContext.setProperty(NAME, new Data(timer));
//        }
//    }
//
//    private String toCookieString(Object cookie) {
//        if (cookie instanceof String) {
//            return (String) cookie;
//        } else if (cookie instanceof Cookie) {
//            Cookie c = (Cookie) cookie;
//            return c.getName() + "=" + c.getValue();
//        } else {
//            throw new IllegalArgumentException();
//        }
//    }
//
//    @Override
//    public void filter(ClientRequestContext clientRequestContext, ClientResponseContext clientResponseContext) throws IOException {
//        requestComplete(clientRequestContext, clientResponseContext.getStatus(), null);
//    }
//
//    public void requestFailed(ClientRequestContext request, Throwable throwable) {
//        LOG.warn(throwable.getMessage(), throwable);
//        requestComplete(request, 520, throwable);
//    }
//
//    private void requestComplete(ClientRequestContext clientRequestContext, int status, Throwable throwable) {
//        if (!filterConfig.disableMetrics) {
//            Data data = (Data) clientRequestContext.getProperty(NAME);
//            Timer timer = data.timer;
//            URI uri = clientRequestContext.getUri();
//            String host = uri.getHost();
//
//            timer
//                    .stop()
//                    .addFieldToReport("httpStatus", status)
//                    .addFieldToReport("host", host)
//                    .addFieldToReport("path", uri.getPath())
//                    .report();
//
//            MetricsFactory.getMeterRegistry().timer(
//                    "rest_client",
//                    "host",
//                    host,
//                    "status",
//                    Integer.toString(status),
//                    "error",
//                    ofNullable(throwable).map(ExceptionUtils::getRootCause).map(t -> t.getClass().getSimpleName()).orElse("")
//            ).record(System.currentTimeMillis() - data.invocationTimestamp, MILLISECONDS);
//        }
//    }
//
//    private URI uriForLogging(ClientRequestContext clientRequestContext) {
//        URI uri = clientRequestContext.getUri();
//        return filterConfig.disableParameterLogging ? UriBuilder.fromUri(uri).replaceQuery("").build() : uri;
//    }
//
//    @With
//    @Value
//    @Builder
//    public static class ClientLogFilterConfig {
//        public final String metricName;
//        public final boolean disableMetrics;
//        public final boolean disableParameterLogging;
//    }
//
//    private static class Data {
//        private final long invocationTimestamp = System.currentTimeMillis();
//        private final Timer timer;
//
//        private Data(Timer timer) {
//            this.timer = timer;
//        }
//
//    }
//}
