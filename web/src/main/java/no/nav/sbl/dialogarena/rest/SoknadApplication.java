package no.nav.sbl.dialogarena.rest;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
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
        packages("no.nav.sbl.dialogarena.rest");
        register(JacksonJaxbJsonProvider.class);
        register(MultiPartFeature.class);

        logger.info("Starter Jersey");
    }
}