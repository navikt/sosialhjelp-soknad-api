package no.nav.sbl.dialogarena.rest;

import no.nav.sbl.dialogarena.rest.actions.SoknadActions;
import no.nav.sbl.dialogarena.rest.ressurser.SoknadRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.FaktaRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.InformasjonRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.InternalRessurs;
import no.nav.sbl.dialogarena.rest.ressurser.VedleggRessurs;
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
        super(InformasjonRessurs.class,
                InternalRessurs.class,
                FaktaRessurs.class,
                VedleggRessurs.class,
                SoknadRessurs.class,
                SoknadActions.class);

        register(MultiPartFeature.class);
        LOG.info("Starter Jersey");
    }
}
