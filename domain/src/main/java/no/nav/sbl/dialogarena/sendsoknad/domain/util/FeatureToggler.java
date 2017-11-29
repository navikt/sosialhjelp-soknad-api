package no.nav.sbl.dialogarena.sendsoknad.domain.util;

public class FeatureToggler {

    public static boolean erFeatureAktiv(Toggle toggle) {
        return Boolean.valueOf(System.getProperty(toggle.getPropertyNavn(), "false"));
    }

    public enum Toggle {
        RESSURS_ALTERNATIVREPRESENTASJON("soknad.alternativrepresentasjon.ressurs.enabled", "Skrur på en ressurs for å hente ut alternativrepresetasjon på en url for testformål"),
        RESSURS_FULLOPPSUMERING("soknad.fulloppsummering.ressurs.enabled", "Skrur på ressurs for fulloppsummering"),
        ARKIVER_ALTERNATIVREPRESENTASJON_FORELDREPENGER("soknad.feature.foreldrepenger.alternativrepresentasjon.enabled",
                "Skrur på muligheten til å akrivere alternativ representasjon i foreldrepenger");

        private String propertyNavn;
        private String beskrivelse;

        Toggle(String propertyNavn, String beskrivelse) {
            this.propertyNavn = propertyNavn;
            this.beskrivelse = beskrivelse;
        }

        public String getBeskrivelse() {
            return beskrivelse;
        }

        public String getPropertyNavn() {
            return propertyNavn;
        }
    }
}

