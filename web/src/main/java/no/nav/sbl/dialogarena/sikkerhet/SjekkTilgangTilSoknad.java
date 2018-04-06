package no.nav.sbl.dialogarena.sikkerhet;

import java.lang.annotation.*;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * Sjekker tilgang til søknad. Skjekker både søknadId og xsrf token.
 */
public @interface SjekkTilgangTilSoknad {
    boolean sjekkXsrf() default true;
    Type type() default Type.Behandling;
    public enum Type {
        Behandling, Vedlegg, Faktum, Metadata, Soknad
    }
}