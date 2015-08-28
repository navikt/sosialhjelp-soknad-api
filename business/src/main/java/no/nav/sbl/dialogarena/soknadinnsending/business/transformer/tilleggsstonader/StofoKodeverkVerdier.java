package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

public class StofoKodeverkVerdier {
    public enum FormaalKodeverk {
        oppfolging("OPPF"), jobb("JOBB"), arbeid("TILT");
        public String kodeverksverdi;

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

    public enum ErUtgifterDekketKodeverk{
        ja("JA"), nei("NEI"), delvis("DEL");

        public final String kodeverk;

        ErUtgifterDekketKodeverk(String kodeverk) {
            this.kodeverk = kodeverk;
        }
    }
}
