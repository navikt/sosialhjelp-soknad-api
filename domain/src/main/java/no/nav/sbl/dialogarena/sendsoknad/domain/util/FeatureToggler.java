package no.nav.sbl.dialogarena.sendsoknad.domain.util;

public class FeatureToggler {

    public static String ALTERNATIV_REPRESENTASJON = "soknad.alternativrepresentasjon.ressurs.enabled";
    public static String FULLOPPSUMMERING = "soknad.fulloppsummering.enabled";
    public static String FORELDREPENGER_ALTERNATIV_REPRESENTASJON = "soknad.feature.foreldrepenger.alternativrepresentasjon.enabled";

    public static boolean erFeatureAktiv(final String propertyNavn) {
        return Boolean.valueOf(System.getProperty(propertyNavn, "false"));
    }
}
