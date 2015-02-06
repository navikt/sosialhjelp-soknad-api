package no.nav.sbl.dialogarena.websoknad.servlet;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jersey 2 config
 */
public class SoknadApplication extends ResourceConfig {

    Logger LOG = LoggerFactory.getLogger(SoknadApplication.class);

    public SoknadApplication() {
        super(InformasjonController.class,
                InternalController.class,
                FaktaController.class,
                VedleggController.class);

        register(MultiPartFeature.class);
        LOG.info("Starter Jersey");
    }
}
