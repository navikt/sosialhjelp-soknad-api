package no.nav.sosialhjelp.soknad.web.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import no.nav.sosialhjelp.soknad.arbeid.ArbeidRessurs;
import no.nav.sosialhjelp.soknad.navenhet.NavEnhetRessurs;
import no.nav.sosialhjelp.soknad.oppsummering.OppsummeringRessurs;
import no.nav.sosialhjelp.soknad.web.mdc.MdcFilter;
import no.nav.sosialhjelp.soknad.web.oidc.OidcResourceFilteringFeature;
import no.nav.sosialhjelp.soknad.web.sikkerhet.CORSFilter;
import no.nav.sosialhjelp.soknad.web.sikkerhet.HeaderFilter;
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
        // JacksonJaxbJsonProvider registreres manuelt for å unngå å dra inn Jacksons egne ExceptionMappers, som
        // returnerer litt for mye informasjon i sine feilmeldinger. Desse ExceptionMappers har @Provider-annotationer
        // og blir automatisk trukket inn hvis du tar tar inn hele Jackson-pakken for JSON.
        packages("no.nav.sosialhjelp.soknad.web.rest");
        register(NavEnhetRessurs.class);
        register(OppsummeringRessurs.class);
        register(ArbeidRessurs.class);

        register(JacksonJaxbJsonProvider.class);
        register(MultiPartFeature.class);

        // Filters
        register(HeaderFilter.class);
        register(CORSFilter.class);
        register(MdcFilter.class);

        register(OidcResourceFilteringFeature.class);

        logger.info("Starter Jersey");
    }
}
