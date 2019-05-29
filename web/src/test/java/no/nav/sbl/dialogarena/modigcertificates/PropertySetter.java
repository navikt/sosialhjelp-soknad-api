package no.nav.sbl.dialogarena.modigcertificates;

/*
    Copied from https://github.com/navikt/modig-testcertificates-safe-fork
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PropertySetter {

    private static final Logger LOG = LoggerFactory.getLogger(PropertySetter.class);

    private final Properties propertiesToSet;

    public PropertySetter(Properties propertiesToSet) {
        this.propertiesToSet = propertiesToSet;
    }

    public final void setOn(Properties properties) {
        for (String propName : propertiesToSet.stringPropertyNames()) {
            if (properties.containsKey(propName)) {
                LOG.warn("Overwriting {} = {} to {} = {}",
                        propName, properties.getProperty(propName), propName, propertiesToSet.getProperty(propName));
            } else {
                LOG.info("Setting {} = {}", propName, propertiesToSet.getProperty(propName));
            }
            properties.setProperty(propName, propertiesToSet.getProperty(propName));
        }
    }
}