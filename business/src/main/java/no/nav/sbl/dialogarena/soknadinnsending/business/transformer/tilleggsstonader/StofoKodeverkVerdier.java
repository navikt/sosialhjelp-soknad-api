package no.nav.sbl.dialogarena.soknadinnsending.business.transformer.tilleggsstonader;

import org.apache.commons.collections15.Transformer;

public class StofoKodeverkVerdier {
    public static final class SammensattAdresse {
        public final String sammensattAdresse;

        public SammensattAdresse(String adresse, String postnr) {
            sammensattAdresse = String.format("%s, %s", adresse, postnr);
        }
    }

    public enum FormaalKodeverk {
        oppfolging("OPPF"), jobbintervju("JOBB"), tiltraa("TILT");
        public final String kodeverksverdi;

        FormaalKodeverk(String kodeverksverdi) {
            this.kodeverksverdi = kodeverksverdi;
        }
    }

    public enum SkolenivaaerKodeverk {
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

    public enum InnsendingsintervallerKodeverk {
        uke("UKE"), maned("MND");
        public final String kodeverksverdi;

        InnsendingsintervallerKodeverk(String kodeverksverdi) {
            this.kodeverksverdi = kodeverksverdi;
        }

    }

    public enum TilsynForetasAv {
        privat("Privat"), offentlig("Offentlig"), annet("Annet");
        public final String stofoString;

        TilsynForetasAv(String stofoString) {
            this.stofoString = stofoString;
        }

        public static Transformer<String, String> TRANSFORMER = new Transformer<String, String>() {
            @Override
            public String transform(String kodeverk) {
                try {
                    return TilsynForetasAv.valueOf(kodeverk).stofoString;
                } catch (IllegalArgumentException ignore) {
                    return "";
                }
            }
        };
    }

    public enum TilsynForetasAvKodeverk{
        dagmamma("KOM"), barnehage("OFF"), privat("PRI");
        public final String kodeverksverdi;

        TilsynForetasAvKodeverk(String kodeverksverdi) {
            this.kodeverksverdi = kodeverksverdi;
        }

    }
}
