package no.nav.sbl.dialogarena.dokumentinnsending.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class JettyEnvironmentSettings {
    public static void load() {
        Properties props = new Properties();
        try (InputStream inputStream = props.getClass().getResourceAsStream("/jetty-environment.properties")) {
            props.load(inputStream);
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                System.setProperty((String) entry.getKey(), (String) entry.getValue());
            }
        } catch (IOException e) {
            throw new RuntimeException("Kunne ikke laste jetty-environment.properties", e);
        }
    }
}