package no.nav.sbl.dialogarena.modigcertificates;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.util.Properties;

public class TrustStore extends PropertySetter {
    public TrustStore(String truststoreFilePath, String truststorePassword) {
        super(createTrustStoreProperties(truststoreFilePath, truststorePassword));
    }

    private static Properties createTrustStoreProperties(String truststoreFilePath, String truststorePassword) {
        Properties props = new Properties();
        props.setProperty("javax.net.ssl.trustStore", truststoreFilePath);
        props.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
        return props;
    }
}
