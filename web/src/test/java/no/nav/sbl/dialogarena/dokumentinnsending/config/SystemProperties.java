package no.nav.sbl.dialogarena.dokumentinnsending.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class SystemProperties {

    public static void setFrom(String resource) throws IOException {
        Properties props = new Properties();
        InputStream inputStream = SystemProperties.class.getClassLoader().getResourceAsStream(resource);
        props.load(inputStream);

        for (String entry : props.stringPropertyNames()) {
            System.setProperty(entry, props.getProperty(entry));
        }
    }

    private SystemProperties() { }

}