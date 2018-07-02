package no.nav.sbl.dialogarena.sendsoknad.domain.util;

public class FeatureToggler {

    public static boolean erFeatureAktiv(Toggle toggle) {
        return Boolean.valueOf(System.getenv(toggle.getPropertyNavn()));
    }

    public enum Toggle {
        RESSURS_ALTERNATIVREPRESENTASJON("SOKNAD_ALTERNATIVREPRESENTASJON_RESSURS_ENABLED", "Skrur på en ressurs for å hente ut alternativrepresetasjon på en url for testformål"),
        RESSURS_FULLOPPSUMERING("SOKNAD_FULLOPPSUMMERING_RESSURS_ENABLED", "Skrur på ressurs for fulloppsummering");

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

