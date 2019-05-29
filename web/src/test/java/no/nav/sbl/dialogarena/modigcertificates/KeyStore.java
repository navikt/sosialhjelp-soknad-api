package no.nav.sbl.dialogarena.modigcertificates;

/*
    Copied from https://github.com/navikt/modig-testcertificates-safe-fork
 */

import java.util.Properties;

public class KeyStore extends PropertySetter {

    public KeyStore(String filePath, String password) {
        super(createKeyStoreProperties(filePath, password));
    }

    private static Properties createKeyStoreProperties(String filePath, String password) {
        Properties props = new Properties();
        props.setProperty("no.nav.modig.security.appcert.keystore", filePath);
        props.setProperty("no.nav.modig.security.appcert.password", password);
        return props;
    }
}