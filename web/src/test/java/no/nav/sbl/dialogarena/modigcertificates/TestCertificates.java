package no.nav.sbl.dialogarena.modigcertificates;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.io.InputStream;

public final class TestCertificates {
    public static void setupKeyAndTrustStore() {
        setupTemporaryKeyStore("no/nav/modig/testcertificates/keystore.jks", "devillokeystore1234");
        setupTemporaryTrustStore("no/nav/modig/testcertificates/truststore.jts", "changeit");
    }

    public static void setupTemporaryKeyStore(String keyStoreResourceName, String password) {
        InputStream keyStore = TestCertificates.class.getClassLoader().getResourceAsStream(keyStoreResourceName);
        setupTemporaryKeyStore(keyStore, password);
    }

    public static void setupTemporaryKeyStore(InputStream keystore, String password) {
        (new KeyStore(FileUtils.putInTempFile(keystore).getAbsolutePath(), password)).setOn(System.getProperties());
    }

    public static void setupTemporaryTrustStore(String trustStoreResourceName, String password) {
        InputStream trustStore = TestCertificates.class.getClassLoader().getResourceAsStream(trustStoreResourceName);
        setupTemporaryTrustStore(trustStore, password);
    }

    public static void setupTemporaryTrustStore(InputStream trustStore, String password) {
        (new TrustStore(FileUtils.putInTempFile(trustStore).getAbsolutePath(), password)).setOn(System.getProperties());
    }

    private TestCertificates() {
    }
}
