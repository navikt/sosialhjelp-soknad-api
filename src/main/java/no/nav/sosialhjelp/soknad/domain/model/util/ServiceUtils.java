package no.nav.sosialhjelp.soknad.domain.model.util;

public final class ServiceUtils {

    private ServiceUtils() {
    }

    public static boolean isNonProduction() {
        // Bruk isNonProduction() -sjekk fremfor å sjekke om miljø configurert som prod. På denne måten er default-configurasjon vår alltid prodlik.
        // Slik at ved evt. endringer eller feilkonfigurasjoner, vil ikke prod bli ødelagt. Feks. ved sende ekte søknader til testkommuner som ikke finnes.
        // Prod-konfigurasjon i test vil oppdages raskt og man vil ikke klare å skape problemer for prod da man trenger secrets som ikke er tilgjengelig i testmiljøer.
        String miljo = System.getProperty("environment.name", "");
        return miljo.equals("q0")
                || miljo.equals("q1")
                || miljo.equals("labs-gcp")
                || miljo.equals("dev-gcp")
                || miljo.equals("local")
                || miljo.equals("test");
    }

}
