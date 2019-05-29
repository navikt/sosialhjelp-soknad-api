package no.nav.sbl.dialogarena.sendsoknad.domain.saml.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Allows you to trust certificates from additional KeyStores in addition to
 * the default KeyStore
 *
 * Based on http://download.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#X509TrustManager
 */
public class AdditionalKeyStoresTrustManager implements X509TrustManager {
    private static final Logger log = LoggerFactory.getLogger(AdditionalKeyStoresTrustManager.class);

    protected List<X509TrustManager> x509TrustManagers = new ArrayList<>();

    public AdditionalKeyStoresTrustManager(KeyStore... additionalkeyStores) {
        final ArrayList<TrustManagerFactory> factories = new ArrayList<>();
        try {
            // The default Trustmanager with default keystore
            final TrustManagerFactory original = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            original.init((KeyStore) null);
            factories.add(original);
            log.debug("Adding X509TrustManager with default truststore.");
            if (additionalkeyStores != null) {
                for (KeyStore keyStore : additionalkeyStores) {
                    if (keyStore != null) {
                        log.debug("Adding new X509TrustManager with additional truststore.");
                        final TrustManagerFactory additionalCerts = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        additionalCerts.init(keyStore);
                        factories.add(additionalCerts);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        /*
         * Iterate over the returned trustmanagers, and hold on to any that are X509TrustManagers
         */
        for (TrustManagerFactory tmf : factories) {
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    x509TrustManagers.add((X509TrustManager) tm);
                }
            }
        }

        if (x509TrustManagers.isEmpty()){
            throw new RuntimeException("Couldn't find any X509TrustManagers.");
        }
    }

    /*
     * Delegate to the default trust manager.
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        final X509TrustManager defaultX509TrustManager = x509TrustManagers.get(0);
        defaultX509TrustManager.checkClientTrusted(chain, authType);
    }

    /*
     * Loop over the trustmanagers until we find one that accepts our server
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        ArrayList<Exception> exceptions = new ArrayList<>(x509TrustManagers.size());
        for (X509TrustManager tm : x509TrustManagers) {
            try {
                tm.checkServerTrusted(chain, authType);
                return;
            } catch (Exception e) {
                exceptions.add(e);
                log.debug("Caught exception in checkServerTrusted() from X509TrustManager (with keystore). Exception: " + e);
                log.debug("Checking next X509TrustManager in list.");
            }
        }
        Exception lastException = !exceptions.isEmpty() ? exceptions.get(exceptions.size() - 1) : null;
        log.debug("Certificate not trusted. Add server certificate to the appropiate keystore.");
        throw new CertificateException("All configured trustmanagers threw exceptions for checkServerTrusted().", lastException);
    }

    public X509Certificate[] getAcceptedIssuers() {
        final ArrayList<X509Certificate> list = new ArrayList<>();
        for (X509TrustManager tm : x509TrustManagers) {
            list.addAll(Arrays.asList(tm.getAcceptedIssuers()));
        }
        return list.toArray(new X509Certificate[list.size()]);
    }
}