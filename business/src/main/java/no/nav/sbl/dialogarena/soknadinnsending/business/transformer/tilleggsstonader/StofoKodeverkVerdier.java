package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

public class StofoKodeverkVerdier {
    public static final class SammensattAdresse{
        public final String sammensattAdresse;
        public SammensattAdresse(String adresse, String postnr) {
            sammensattAdresse = String.format("%s, %s", adresse, postnr);
        }
    }
    public enum FormaalKodeverk{
        oppfolging("OPPF"), jobbintervju("JOBB"), tiltraa("TILT");
        public  final String kodeverksverdi;

        FormaalKodeverk(String kodeverksverdi) {
            this.kodeverksverdi = kodeverksverdi;
        }
    }
    public enum SkolenivaaerKodeverk{
        videregaende("VGS"), hoyereutdanning("HGU"), annet("ANN");

        public final String kodeverk;

        SkolenivaaerKodeverk(String kodeverk) {
            this.kodeverk = kodeverk;
        }
    }

    public enum ErUtgifterDekketKodeverk {
        ja("JA"), nei("NEI"), delvis("DEL");

        public final String kodeverk;

        ErUtgifterDekketKodeverk(String kodeverk) {
            this.kodeverk = kodeverk;
        }
    }
    public enum InnsendingsintervallerKodeverk{
        uke("UKE"), maaned("MND");
        public final String kodeverksverdi;

        InnsendingsintervallerKodeverk(String kodeverksverdi) {
            this.kodeverksverdi = kodeverksverdi;
        }

    }
}
