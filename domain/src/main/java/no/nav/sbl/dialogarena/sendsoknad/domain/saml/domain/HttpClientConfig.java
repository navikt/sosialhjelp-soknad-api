package no.nav.sbl.dialogarena.sendsoknad.domain.saml.domain;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.UnsupportedSchemeException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.DefaultSchemePortResolver;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.security.*;

public class HttpClientConfig {

    private URI endpointURL;
    private boolean ssl;
    private KeyStore trustStore;

    /**
     * Creates a HttpClient. Uses default truststore if endpoint uses https.
     *
     * @param endpointURL
     *            the endpointURL
     */
    public HttpClientConfig(URI endpointURL) {
        this.endpointURL = endpointURL;
    }

    /**
     * Creates a HttpClient using the supplied truststore file.
     *
     * @param endpointURL
     *            the endpointURL
     * @param trustStoreFile
     *            path to truststore for the ssl connection, default truststore will be used if null
     * @param trustStorePassword
     *            the truststore password
     */
    public HttpClientConfig(URI endpointURL, File trustStoreFile, String trustStorePassword) {
        this.endpointURL = endpointURL;
        String scheme = endpointURL.getScheme();
        this.ssl = scheme != null && scheme.equalsIgnoreCase("https");
        this.trustStore = trustStoreFile != null && trustStorePassword != null ?
                loadTrustStore(KeyStore.getDefaultType(), trustStoreFile, trustStorePassword, null) : null;
    }

    public String getEndpoint() {
        return endpointURL.toString();
    }

    public HttpClient createHttpClient() throws GeneralSecurityException, UnsupportedSchemeException {
        return useSSL() ? createSecureHttpClient() : HttpClientBuilder.create().build();
    }

    public boolean useSSL() {
        return this.ssl;
    }

    private HttpClient createSecureHttpClient() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, UnsupportedSchemeException {

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[] { new AdditionalKeyStoresTrustManager(trustStore) }, null);

        HttpHost httpHost = new HttpHost("https",443);
        SchemePortResolver schemePortResolver = DefaultSchemePortResolver.INSTANCE;
        schemePortResolver.resolve(httpHost);

        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(ctx);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionSocketFactory)
                .build();

        HttpClientConnectionManager conManager = new BasicHttpClientConnectionManager(registry);


        return HttpClientBuilder.create()
                .setConnectionManager(conManager)
                .setSchemePortResolver(schemePortResolver)
                .build();
    }

    protected KeyStore getTrustStore() {
        return trustStore;
    }

    protected void setTrustStore(KeyStore trustStore) {
        this.trustStore = trustStore;
    }

    /**
     * gets a keystore instance
     *
     * @param type
     * @param path
     * @param password
     * @param provider
     * @return keyStore
     */
    private KeyStore loadTrustStore(String type, File path, String password, Provider provider) {
        FileInputStream stream = null;
        KeyStore keyStore = null;
        try {
            keyStore = useProvider(provider, type) ? KeyStore.getInstance(type, provider) : KeyStore.getInstance(type);
            stream = FileUtils.openInputStream(path);
            keyStore.load(stream, password.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Could not load truststore.", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return keyStore;
    }

    /**
     * checks if provider should be used
     *
     * @param provider
     * @param type
     * @return
     */
    private static boolean useProvider(Provider provider, String type) {
        return provider != null && type.equalsIgnoreCase("PKCS12");
    }

    @Override
    public String toString(){
        return this.getClass() + "[endpoint="+getEndpoint()+"]";
    }
}
