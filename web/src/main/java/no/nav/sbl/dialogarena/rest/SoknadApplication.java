package no.nav.sbl.dialogarena.rest;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jersey 2 config
 */
public class SoknadApplication extends ResourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(SoknadApplication.class);

    public SoknadApplication() {
        packages("no.nav.sbl.dialogarena.rest");
        register(MultiPartFeature.class);

        logger.info("Starter Jersey");
    }
}
